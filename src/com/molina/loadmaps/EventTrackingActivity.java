package com.molina.loadmaps;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.molina.events.EventTrackingDataService;
import com.molina.models.Competitor;
import com.molina.models.CompetitorTracking;
import com.molina.models.Event;
import com.molina.serverconnection.ServerConnection;
import com.molina.utils.AsyncTaskExecutionHelper;
import com.molina.utils.CompetitorArrayAdapter;

/**
 * Esta clase muestra las rutas seguidas por cada uno de los competidores que
 * forman parte del evento
 * 
 * @author Molina
 * 
 */
public class EventTrackingActivity extends SherlockFragmentActivity {

	public static final long DEFAULT_REFRESHING_TIME = 15000;
	private boolean locationEnabled;
	private SlidingMenu slidingMenu;
	private GoogleMap map;
	private ListView competitorListView;
	private CompetitorTracking[] competitors;
	private int eventID;

	/**
	 * Esta Handler recibe los mensajes para refrscar el contenido del mapa
	 */
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			AsyncTaskExecutionHelper.executeParallel(
					new RefreshCompetitorCoordinates(), new Void[0]);
			Message message = new Message();
			message.setTarget(handler);
			sendMessageAtTime(message, SystemClock.uptimeMillis()
					+ DEFAULT_REFRESHING_TIME);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loadmap);
		locationEnabled = false;
		eventID = getIntent().getIntExtra(Event.ID, -1);

		// para el mapa de google maps
		SupportMapFragment fragmentMap = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		map = fragmentMap.getMap();
		map.setMyLocationEnabled(locationEnabled);

		// para la barra superior
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		// para el sliding menu
		setSlidingMenu();
		competitorListView = (ListView) findViewById(R.id.slidingmenu_competitor_list);
		setListListener();

		// para los datos que hay que poner en la listView
		Integer eventID = getIntent().getIntExtra(Event.ID, -1);
		AsyncTaskExecutionHelper.executeParallel(new DownloadCompetitorData(),
				eventID);

		// mandamos el mensaje que inicia la cola de refresco
		Message message = new Message();
		message.setTarget(handler);
		message.sendToTarget();
	}

	/**
	 * Vuelve a pintar todas las rutas de todos los competidores
	 */
	private void refreshRoutes() {
		map.clear();
		for (CompetitorTracking ct : competitors) {
			if (ct.getPositions().size() > 0) {
				PolylineOptions route = new PolylineOptions();
				route.width(5);
				route.color(ct.getColor());
				for (LatLng latlng : ct.getPositions()) {
					if (latlng != null) {
						route.add(latlng);
					}
				}
				map.addPolyline(route);
				String text = ct.getCompetitor().getName() + "\n"
						+ ct.getCompetitor().getSurname();
				map.addMarker(new MarkerOptions()
						.position(ct.getLastPosition()).title(text));
			}
		}
	}

	/**
	 * LLena la lista de competidores con sus datos
	 */
	private void fillCompetitorList() {
		competitorListView.setAdapter(new CompetitorArrayAdapter(this,
				competitors));
	}

	/**
	 * Se llama a este método cuando se va a abrir el menú
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.event_tracking, menu);
		return true;
	}

	/**
	 * Cambia el tipo de mapa entre los disponibles por la API de google maps
	 */
	private void changeMapType() {
		new AlertDialog.Builder(this).setTitle(R.string.menu_choose_map_type)
				.setItems(R.array.map_type, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						int mapType;
						switch (which) {
						case 0:
							mapType = GoogleMap.MAP_TYPE_NORMAL;
							break;
						case 1:
							mapType = GoogleMap.MAP_TYPE_HYBRID;
							break;
						case 2:
							mapType = GoogleMap.MAP_TYPE_SATELLITE;
							break;
						case 3:
							mapType = GoogleMap.MAP_TYPE_TERRAIN;
							break;
						case 4:
							mapType = GoogleMap.MAP_TYPE_NONE;
							break;
						default:
							mapType = GoogleMap.MAP_TYPE_NORMAL;
							break;
						}
						map.setMapType(mapType);
					}
				}).show();
	}

	/**
	 * Se llama a este método cuando el usuario ha seleccionado una opción del
	 * menú
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.menu_choose_map_type) {
			changeMapType();
		} else if (itemId == R.id.menu_enable_location) {
			locationEnabled = !locationEnabled;
			map.setMyLocationEnabled(locationEnabled);
		} else if (itemId == android.R.id.home) {
			finish();
		} else if (itemId == R.id.menu_visualize_competitors) {
			slidingMenu.toggle();
		} else if (itemId == R.id.menu_stop_sending_data) {
			boolean stopped = stopService(new Intent(this,
					EventTrackingDataService.class));
			if (stopped) {
				Toast.makeText(this, R.string.notification_service_stopped,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, R.string.notification_service_not_stopped,
						Toast.LENGTH_LONG).show();
			}
		}
		return false;
	}

	private void setListListener() {
		competitorListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CompetitorTracking ct = competitors[position];
				LatLng lastPosition = ct.getLastPosition();
				if (lastPosition != null) {
					map.animateCamera(CameraUpdateFactory.newLatLngZoom(
							lastPosition, 15.0f));
				}
				slidingMenu.toggle();

			}
		});
	}

	/**
	 * Enlaza el SlidingMenu con la Activity
	 */
	private void setSlidingMenu() {
		slidingMenu = new SlidingMenu(this);
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setBehindOffset(50);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		slidingMenu.setMenu(R.layout.slidingmenu_main);
	}

	/**
	 * Esta clase se descarga del servidor todos los datos referentes al evento,
	 * incluyendo su foto
	 * 
	 * @author Molina
	 * 
	 */
	private class DownloadCompetitorData extends
			AsyncTask<Integer, Void, CompetitorTracking[]> {

		@Override
		protected CompetitorTracking[] doInBackground(Integer... params) {
			int eventID = params[0];
			try {
				// hay que ver primero que participantes hay
				ArrayList<Competitor> competitors = ServerConnection
						.getCompetitorsInEvent(eventID);
				CompetitorTracking[] competitorTrackingArray = new CompetitorTracking[competitors
						.size()];
				int i = 0;
				for (Competitor c : competitors)
					competitorTrackingArray[i++] = new CompetitorTracking(c);
				return competitorTrackingArray;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(CompetitorTracking[] result) {
			if (result != null && result.length > 0) {
				competitors = result;
				fillCompetitorList();
			}
		}
	}

	/**
	 * Esta clase obtiene los datos de las rutas trazadas por los competidores
	 * en un evento determinado
	 * 
	 * @author Molina
	 * 
	 */
	private class RefreshCompetitorCoordinates extends
			AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				ServerConnection.getCompetitorsRoutesForEvent(eventID,
						competitors);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// una vez tenemos los datos de las rutas refrescados tenemos que
			// actualizar el mapa
			refreshRoutes();
		}
	}

}
