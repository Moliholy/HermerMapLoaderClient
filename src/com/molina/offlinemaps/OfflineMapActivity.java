package com.molina.offlinemaps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.xml.sax.SAXException;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.google.android.gms.maps.model.LatLng;
import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.molina.loadmaps.R;
import com.molina.parsekml.FileChooserCallable;
import com.molina.parsekml.NavigationDataSet;
import com.molina.parsekml.NavigationSaxHandler;
import com.molina.parsekml.Placemark;
import com.molina.route.RouteDisplayable;

/**
 * Esta clase encapsula la funcionalidad requerida para el manejo de mapas
 * offline. Además permite al usuario interactuar con los mapas en modo online,
 * si así lo desea.
 * 
 * @author Molina
 * 
 */
public class OfflineMapActivity extends SherlockActivity implements
		RouteDisplayable, FileChooserCallable {
	private MapView myOpenMapView;
	private MapController myMapController;
	private ArrayList<OverlayItem> overlayItemArray;
	private LocationManager locationManager;

	private ArrayList<GeoPoint> currentRoute;

	/**
	 * Se llama a este método al iniciar la Activity. Inicializa los parámetros
	 * de la misma, entre ellos la localización mediante GPS o red
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_offline_map);
		currentRoute = new ArrayList<GeoPoint>();

		File omsdroidDirectory = new File(Environment
				.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid");
		if (!omsdroidDirectory.exists() || !omsdroidDirectory.isDirectory()) {
			omsdroidDirectory.mkdir();
		}

		myOpenMapView = (MapView) findViewById(R.id.openmapview);
		myOpenMapView.setBuiltInZoomControls(true);
		myOpenMapView.setMultiTouchControls(true);
		myMapController = myOpenMapView.getController();
		myMapController.setZoom(4);
		// Creamos el Overlay
		overlayItemArray = new ArrayList<OverlayItem>();

		DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(
				this);
		MyItemizedIconOverlay myItemizedIconOverlay = new MyItemizedIconOverlay(
				overlayItemArray, null, defaultResourceProxyImpl);
		myOpenMapView.getOverlays().add(myItemizedIconOverlay);

		// conseguimos la localización
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location lastLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastLocation != null) {
			updateLoc(lastLocation);
		} else {
			lastLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (lastLocation != null) {
				updateLoc(lastLocation);
			}
		}

		// Añadimos la barra de zoom
		ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
		myOpenMapView.getOverlays().add(myScaleBarOverlay);

		// añadimos el ActionBarSherlock
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	/**
	 * Se llama al restaurar el control el usuario. Restaura el dato de la
	 * posición GPS
	 */
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, myLocationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
	}

	/**
	 * Se llama dejar de interactuar con la aplicación. Guarda los datos GPS
	 */
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(myLocationListener);
	}

	/**
	 * Se llama a este método cuando se va a abrir el menú
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu_offline, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId,
			com.actionbarsherlock.view.MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.menu_offline_load_kml_route) {
			loadFileChooser();
		}else if (itemId == android.R.id.home){
			finish();
		}
		return false;
	}

	/**
	 * Se llama a este método al volver del FileChooser para procesar el archivo
	 * elegido y, si procede, cargar el archivo kml con una ruta
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
						Toast.makeText(
								this,
								getResources().getString(
										R.string.kml_loaded_successfully)
										+ " " + file.getName(),
								Toast.LENGTH_LONG).show();
						loadRoute(file);
					} catch (Exception e) {
						Log.e("Select file", "File select error", e);
					}
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Carga una ruta almacenada en el fichero que se pasa como parámetro
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
			// otenemos los datos para pintarlos en el GoogleMap
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
	 * Dibuja las rutas incluidas en los Placemarks que se pasan como parámetro
	 */
	public void drawRoutePoints(List<Placemark> placemarkList) {
		currentRoute.clear();
		for (Placemark placemark : placemarkList) {
			for (LatLng latlng : placemark.getGeoCoordinates()) {
				if (latlng != null) {
					GeoPoint toAdd = new GeoPoint(latlng.latitude,
							latlng.longitude);
					currentRoute.add(toAdd);
				}
			}
		}

		Road road = new Road(currentRoute);
		PathOverlay roadOverlay = RoadManager.buildRoadOverlay(road,
				myOpenMapView.getContext());
		myOpenMapView.getOverlays().add(roadOverlay);
		myOpenMapView.invalidate();
	}

	/**
	 * Método llamado cuando se ha elegido una de las opciones del menú
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);
		int itemId = item.getItemId();
		if (itemId == R.id.menu_offline_load_kml_route) {
			loadFileChooser();
		}
		return false;
	}

	/**
	 * Carga la actividad de elección de archivo
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
	 * Actualiza la geolocalización
	 * 
	 * @param loc
	 *            Parámetro que almacena la localización actual
	 */
	private void updateLoc(Location loc) {
		/*
		 * GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(),
		 * loc.getLongitude()); // myMapController.setCenter(locGeoPoint);
		 * myLocation = locGeoPoint;
		 */

		setOverlayLoc(loc);

		myOpenMapView.invalidate();
	}

	/**
	 * Actualiza el icono que indica la posición actual
	 * 
	 * @param overlayloc
	 */
	private void setOverlayLoc(Location overlayloc) {
		GeoPoint overlocGeoPoint = new GeoPoint(overlayloc);
		overlayItemArray.clear();

		OverlayItem newMyLocationItem = new OverlayItem("My Location",
				"My Location", overlocGeoPoint);
		overlayItemArray.add(newMyLocationItem);
	}

	/**
	 * Listener que nos informa de los cambios de localización cuando se
	 * producen
	 */
	private LocationListener myLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			updateLoc(location);
		}

		@Override
		public void onProviderDisabled(String arg0) {

		}

		@Override
		public void onProviderEnabled(String arg0) {

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

		}

	};

	/**
	 * Esta clase extiende de ItemizedIconOverlay y sirve para pintar en el mapa
	 * el icono de la posición dada una localización determinada
	 * 
	 * @author Molina
	 * 
	 */
	private class MyItemizedIconOverlay extends
			ItemizedIconOverlay<OverlayItem> {

		public MyItemizedIconOverlay(
				List<OverlayItem> pList,
				org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<OverlayItem> pOnItemGestureListener,
				ResourceProxy pResourceProxy) {
			super(pList, pOnItemGestureListener, pResourceProxy);
		}

		/**
		 * Método usado para dibujar el icono en el mapa
		 */
		@Override
		public void draw(Canvas canvas, MapView mapview, boolean arg2) {
			super.draw(canvas, mapview, arg2);

			if (overlayItemArray.size() > 0) {
				// dado que va a tener únicamente un elemento se puede acceder
				// directamente a él
				GeoPoint in = overlayItemArray.get(0).getPoint();

				Point out = new Point();
				mapview.getProjection().toPixels(in, out);

				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_maps_indicator_current_position);
				canvas.drawBitmap(bm, out.x - bm.getWidth() / 2,
						out.y - bm.getHeight() / 2, null);
			}
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event, MapView mapView) {
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent arg0, MapView arg1) {
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e, MapView mapView) {
			return true;
		}

		@Override
		public boolean onLongPress(MotionEvent event, MapView mapView) {
			return true;
		}

	}
}
