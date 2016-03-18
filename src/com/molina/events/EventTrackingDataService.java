package com.molina.events;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.google.android.gms.maps.model.LatLng;
import com.molina.loadmaps.EventTrackingActivity;
import com.molina.loadmaps.R;
import com.molina.models.Competitor;
import com.molina.models.Event;
import com.molina.preferences.Preferences;
import com.molina.serverconnection.ServerConnection;

public class EventTrackingDataService extends IntentService {

	private NotificationManager mNotifyManager;
	private Builder mBuilder;
	private PendingIntent returnIntent;

	// cada minuto se mandará información
	private static final long DEFAULT_WAITING_TIME = 60000L;
	private static final int ID = 24234;

	/**
	 * Constructor por defecto
	 */
	public EventTrackingDataService() {
		super("EventTrackingDataService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Intent i = new Intent(this, EventTrackingActivity.class);
		returnIntent = PendingIntent.getActivity(this, 0, i,
				Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(getApplicationContext());
		mBuilder.setContentTitle(
				getResources().getString(R.string.notification_track_title))
				.setContentText(
						getResources().getString(
								R.string.notification_track_text))
				.setSmallIcon(R.drawable.ic_menu_upload)
				.setContentIntent(returnIntent).setAutoCancel(false)
				.setOngoing(true);
	}

	/**
	 * Obtiene la localización actual basándose en los datos del proveedor más
	 * adecuado
	 * 
	 * @return la localización actual en la forma latitud-longitud
	 */
	private LatLng getCurrentLocation() {
		// conseguimos la localización
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location lastLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		LatLng latlng = null;
		if (lastLocation == null) {
			lastLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (lastLocation != null) {
			latlng = new LatLng(lastLocation.getLatitude(),
					lastLocation.getLongitude());
		}
		return latlng;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mBuilder.setAutoCancel(true).setOngoing(false)
				.setDeleteIntent(returnIntent);
		mBuilder.setContentText(getResources().getString(
				R.string.notification_sending_data_finished));
		mNotifyManager.notify(ID, mBuilder.build());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int eventID = intent.getIntExtra(Event.ID, -1);
		mNotifyManager.notify(ID, mBuilder.build());
		Competitor competitor = Preferences.getDefaultCompetitor(this);
		while (true) {
			synchronized (this) {
				LatLng coordinates = getCurrentLocation();
				try {
					ServerConnection.sendCoordinates(eventID,
							competitor.getId(), competitor.getPassword(),
							coordinates);
					wait(DEFAULT_WAITING_TIME);
				} catch (ClientProtocolException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}