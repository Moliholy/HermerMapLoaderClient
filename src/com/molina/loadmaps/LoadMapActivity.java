package com.molina.loadmaps;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.xml.sax.SAXException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.molina.offlinemaps.OfflineMapTileDownloader;
import com.molina.offlinemaps.TileAreaData;
import com.molina.parsekml.FileChooserCallable;
import com.molina.parsekml.NavigationDataSet;
import com.molina.parsekml.NavigationSaxHandler;
import com.molina.parsekml.Placemark;
import com.molina.route.MapService;
import com.molina.route.RouteDisplayable;
import com.molina.savekml.ChooseFileName;

/**
 * Esta actividad representa la funcionalidad del manejo de mapas online
 * 
 * @author Molina
 * 
 */
public class LoadMapActivity extends SherlockFragmentActivity implements
		RouteDisplayable, FileChooserCallable {
	private GoogleMap map;
	private LinkedList<LatLng> pointList;
	private List<LatLng> currentRoute;
	private boolean locationEnabled;

	/**
	 * Este Handler recibe la notificación de fin de descarga si la actividad
	 * sigue activa. Mostrará un texto en pantalla informado del resultado de la
	 * descarga
	 */
	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message message) {
			if (message.arg1 == RESULT_OK) {
				Toast.makeText(
						LoadMapActivity.this,
						getResources().getString(
								R.string.message_download_successful),
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(
						LoadMapActivity.this,
						getResources().getString(
								R.string.message_download_failed),
						Toast.LENGTH_LONG).show();
			}
		}
	};

	/**
	 * Método llamado al crear la Activity. Inicializa los parámetros de la
	 * misma.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pointList = new LinkedList<LatLng>();
		locationEnabled = false;
		setContentView(R.layout.activity_loadmap);
		SupportMapFragment fragmentMap = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		map = fragmentMap.getMap();
		map.setMyLocationEnabled(locationEnabled);

		// para la barra superior
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	/**
	 * Llama a la Activity para elegir un archivo. Esto se usa para escoger un
	 * archivo KML
	 */
	public void loadFileChooser() {
		Intent intent = new Intent(this, FileChooserActivity.class);
		try {
			startActivityForResult(intent, REQUEST_CODE);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Se llama al volver de una vista posterior. Se usa para procesar el
	 * archivo seleccionado por el usuario y cargar el archivo KML
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE:
			// Si se ha seleccionado un archivo
			if (resultCode == RESULT_OK) {
				if (data != null) {
					// Obtenemos el URI del archivo
					final Uri uri = data.getData();
					try {
						// Creamos el objeto File a partir del URI proporcionado
						final File file = FileUtils.getFile(uri);
						String textToShow;
						if (file.getAbsoluteFile().toString().endsWith(".kml")) {
							textToShow = getResources().getString(
									R.string.kml_loaded_successfully);
							loadRoute(file);
						} else {
							textToShow = getResources().getString(
									R.string.kml_loaded_unsuccessfully);
						}
						Toast.makeText(this, textToShow + " " + file.getName(),
								Toast.LENGTH_LONG).show();
					} catch (Exception e) {
						Log.e("FileSelectorTestActivity", "File select error",
								e);
					}
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Cargar una ruta especificada en un archivo kml y la muestra en el mapa
	 */
	public void loadRoute(File file) {
		// Instanciamos el parser factory
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			// obtenemos el parser
			SAXParser saxParser = factory.newSAXParser();
			// ahora el handler que vamos a utilizar para parsear los datos
			NavigationSaxHandler handler = new NavigationSaxHandler();
			// los parseamos con el fichero indicado
			saxParser.parse(file, handler);
			// obtenemos los datos para pintarlos en el GoogleMap
			NavigationDataSet data = handler.getParsedData();
			List<Placemark> points = data.getPlacemarks();
			drawRoutePoints(points);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	 * Se llama a este método cuando se va a abrir el menú
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu_online, menu);
		return true;
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
		} else if (itemId == R.id.menu_load_route) {
			loadFileChooser();
		} else if (itemId == R.id.menu_select_route_points) {
			Toast.makeText(this, R.string.message_store_area, Toast.LENGTH_LONG)
			.show();
			chooseRoutePoints();
		} else if (itemId == R.id.menu_enable_location) {
			locationEnabled = !locationEnabled;
			map.setMyLocationEnabled(locationEnabled);
		} else if (itemId == R.id.menu_select_zone_for_storage) {
			Toast.makeText(this, R.string.message_store_area, Toast.LENGTH_LONG)
					.show();
			chooseRectangleToDownload();
		} else if (itemId == R.id.menu_save_current_route) {
			saveCurrentRoute();
		} else if (itemId == android.R.id.home) {
			finish();
		} else if (itemId == R.id.menu_clear) {
			map.clear();
			pointList.clear();
			currentRoute.clear();
		}
		return false;
	}

	/**
	 * Permite al usuario la selección del retángulo cuyos tiles van a ser
	 * descargados
	 */
	private void chooseRectangleToDownload() {
		pointList.clear();
		map.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng point) {
				pointList.add(point);
				map.addMarker(new MarkerOptions().position(point));
				if (pointList.size() == 2) {
					drawRectangle();
					// quitamos el listener lo primero
					map.setOnMapLongClickListener(null);
					Intent intent = new Intent(LoadMapActivity.this,
							OfflineMapTileDownloader.class);
					// creamos un nuevo mensaje
					Messenger messenger = new Messenger(handler);
					intent.putExtra(
							OfflineMapTileDownloader.HANDLER_IDENTICATOR,
							messenger);
					TileAreaData area = new TileAreaData(pointList.getFirst(),
							pointList.getLast());
					for (int i = 0; i <= 16; i++)
						area.addZoom(i);
					intent.putExtra(OfflineMapTileDownloader.TILE_DATA, area);
					startService(intent);
				}
			}
		});
	}

	/**
	 * Dibuja el rectángulo seleccionado en chooseRectangleToLoad()
	 */
	protected void drawRectangle() {
		LatLng point1 = pointList.getFirst();
		LatLng point3 = pointList.getLast();
		LatLng point2 = new LatLng(point3.latitude, point1.longitude);
		LatLng point4 = new LatLng(point1.latitude, point3.longitude);
		PolygonOptions poly = new PolygonOptions().add(point1, point2, point3,
				point4);
		poly.strokeWidth(3);
		poly.strokeColor(getResources().getColor(
				R.color.selection_rectangle_stroke));
		poly.fillColor(getResources().getColor(R.color.selection_rectangle));
		map.addPolygon(poly);
	}

	/**
	 * Permite al usuario elegir los dos puntos para iniciar el cálculo de la
	 * ruta entre los mismos
	 */
	private void chooseRoutePoints() {
		pointList.clear();
		map.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng point) {
				pointList.add(point);
				map.addMarker(new MarkerOptions().position(point));
				if (pointList.size() == 2) {
					// quitamos el listener lo primero
					map.setOnMapLongClickListener(null);
					showRouteModeSelectionDialog();
					// hacemos seguidamente el cálculo de la ruta llamando
					// al servicio de google
				}
			}
		});
	}

	/**
	 * Muestra una ventana para escoger el tipo de ruta que queremos calcular y
	 * una vez seleccionado se procede al cálculo de la ruta mediante el
	 * servicio de Google Maps
	 */
	private void showRouteModeSelectionDialog() {
		OnClickListener onClickListener = new OnClickListener() {
			String choosed;

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					choosed = MapService.MODE_WALKING;
					break;
				case 1:
					choosed = MapService.MODE_CAR;
					break;
				case 2:
					choosed = MapService.MODE_BICYCLING;
					break;
				case 3:
					choosed = MapService.MODE_TRANSIT;
					break;
				default:
					choosed = MapService.MODE_WALKING;
					break;
				}
				try {
					calcutateRouteWithGivenPoints(choosed);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		};
		new AlertDialog.Builder(this).setTitle(R.string.choose_route_type)
				.setItems(R.array.route_type, onClickListener).show();
	}

	/**
	 * Calcula la ruta entre los dos puntos almacenados por el usuario
	 * 
	 * @param mode
	 *            modo sobre el que se calculará la ruta
	 * @return una lista con todos los puntos que conforman la ruta obtenida
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	private List<LatLng> calcutateRouteWithGivenPoints(String mode)
			throws ClientProtocolException, IOException, JSONException {
		if (pointList.size() != 2)
			return null;
		LatLng begin = pointList.getFirst();
		LatLng end = pointList.getLast();
		List<LatLng> points = MapService.calculateRoute(begin.latitude,
				begin.longitude, end.latitude, end.longitude, mode);

		if (points != null && !points.isEmpty()) {
			currentRoute = points;
			drawRouteWithPoints(points);
		} else {
			Toast.makeText(this, R.string.route_empty, Toast.LENGTH_LONG)
					.show();
		}
		return points;
	}

	/**
	 * Dibuja la ruta seleccionada por el usuario y posteriormente calculada
	 * llamando al sercicio de Google
	 * 
	 * @param points
	 *            Puntos que conforman la ruta
	 */
	private void drawRouteWithPoints(List<LatLng> points) {
		PolylineOptions route = new PolylineOptions();
		route.width(5);
		route.color(Color.RED);
		for (LatLng latlng : points) {
			if (latlng != null) {
				route.add(latlng);
			}
		}
		map.addPolyline(route);
	}

	/**
	 * Dibuja los puntos de la ruta cargada de un fichero kml y los une mediante
	 * una polinea
	 * 
	 * @param placemarkList
	 *            lista de Placemarks que se han encontrado en el fichero kml
	 */
	public void drawRoutePoints(List<Placemark> placemarkList) {
		// primero dibujamos la polilinea
		PolylineOptions route = new PolylineOptions();
		route.width(5);
		route.color(Color.RED);
		for (Placemark placemark : placemarkList) {
			for (LatLng latlng : placemark.getGeoCoordinates()) {
				if (latlng != null) {
					route.add(latlng);
				}
			}
		}
		map.addPolyline(route);

		// y ahora ponemos los markers al comienzo y fin de la ruta
		// ponemos un indicador en el primer punto
		LatLng firstPoint = route.getPoints().get(0);
		LatLng lastPoint = route.getPoints().get(route.getPoints().size() - 1);
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(firstPoint, 15.0f));
		map.addMarker(new MarkerOptions().position(firstPoint).title(
				getResources().getString(R.string.begin_route_message)));
		map.addMarker(new MarkerOptions().position(lastPoint).title(
				getResources().getString(R.string.end_route_message)));
	}

	/**
	 * Guarda en un fichero kml la ruta almacenada
	 */
	private void saveCurrentRoute() {
		if (currentRoute != null && !currentRoute.isEmpty()) {
			new ChooseFileName(this, currentRoute).show();
		}
	}

}