package com.molina.events;

import java.io.IOException;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.molina.formularies.EditEvent;
import com.molina.loadmaps.EventTrackingActivity;
import com.molina.loadmaps.R;
import com.molina.models.Event;
import com.molina.preferences.Preferences;
import com.molina.serverconnection.ServerConnection;
import com.molina.utils.AsyncTaskExecutionHelper;
import com.molina.utils.EventImageDownloader;

public class EventVisualizationActivity extends SherlockActivity {

	private TextView name;
	private TextView place;
	private TextView description;
	private TextView date;
	private RadioGroup privacity;
	private ImageView image;
	private Boolean eventStatus = false;
	private Button joinEvent;
	private Button beginTracking;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_visualization);
		name = (TextView) findViewById(R.id.new_event_name);
		place = (TextView) findViewById(R.id.new_event_place);
		description = (TextView) findViewById(R.id.new_event_description);
		date = (TextView) findViewById(R.id.new_event_begin_date);
		privacity = (RadioGroup) findViewById(R.id.radio_group_event_privacity);
		joinEvent = (Button) findViewById(R.id.event_visualization_button_join);
		beginTracking = (Button) findViewById(R.id.event_visualization_button_begin_tracking);

		// cargamos los textos por defecto
		Intent i = getIntent();
		name.setText(i.getStringExtra(Event.NAME));
		place.setText(i.getStringExtra(Event.PLACE));
		description.setText(i.getStringExtra(Event.DESCRIPTION));
		date.setText(i.getStringExtra(Event.DATE));
		// nos da directamente el id del radiobutton (R.id.___)
		int privacityInt = i.getIntExtra(Event.PRIVACITY,
				R.id.radio_button_event_privacity_private);
		privacity.check(getRadioButtonWithPrivacity(privacityInt));
		image = (ImageView) findViewById(R.id.new_event_image);
		Event event = new Event();
		event.setId(i.getIntExtra(Event.ID, -1));
		AsyncTaskExecutionHelper.executeParallel(
				new EventImageDownloader(image), event);

		boolean edit = i.getBooleanExtra("editing", false);
		manageButtons(edit);
	}

	/**
	 * Obtiene el ID del botón según la privacidad del evento
	 * 
	 * @param privacity
	 * @return la id del botón que corresponda
	 */
	private int getRadioButtonWithPrivacity(int privacity) {
		switch (privacity) {
		case 0:
			return R.id.radio_button_event_privacity_private;
		case 1:
			return R.id.radio_button_event_privacity_friends_only;
		case 2:
			return R.id.radio_button_event_privacity_public;
		default:
			return R.id.radio_button_event_privacity_private;
		}
	}

	/**
	 * Establece el texto de los botones a mostrar en esta vista
	 * 
	 * @param edit
	 */
	private void manageButtons(boolean edit) {
		if (edit) {
			Button editButton = (Button) findViewById(R.id.event_visualization_button_edit);
			editButton.setEnabled(edit);
		}
		int id = getIntent().getIntExtra(Event.ID, -1);
		AsyncTaskExecutionHelper.executeParallel(new CompetitorEventStatus(),
				id);
	}

	/**
	 * Cambia el estado del evento en relación al jugador
	 * 
	 * @param v
	 */
	public void changeEventStatus(View v) {
		int id = getIntent().getIntExtra(Event.ID, -1);
		AsyncTaskExecutionHelper.executeParallel(new ChangeEventStatus(), id);
	}

	/**
	 * Carga la activity de Edición de evento
	 * 
	 * @param v
	 */
	public void loadEdition(View v) {
		Intent i = new Intent(this, EditEvent.class);
		startActivity(i);
	}

	/**
	 * Comienza el seguimiento del evento
	 * 
	 * @param v
	 */
	public void beginTracking(View v) {
		if (isMyServiceRunning()) {
			Toast.makeText(this, R.string.message_service_already_running,
					Toast.LENGTH_LONG).show();
			return;
		}
		int id = getIntent().getIntExtra(Event.ID, -1);

		Intent i = new Intent(this, EventTrackingDataService.class);
		i.putExtra(Event.ID, id);
		startService(i);
	}

	/**
	 * Comprueba si el servicio de envío de coordenadas está funcionando
	 * 
	 * @return
	 */
	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("EventTrackingDataService".equals(service.service
					.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Empieza a mandar las coordenadas al servidor para el evento en concreto
	 * 
	 * @param v
	 */
	public void loadEventTrackingVisualization(View v) {
		int id = getIntent().getIntExtra(Event.ID, -1);
		AsyncTaskExecutionHelper.executeParallel(
				new CheckEventTrakingAvaliability(), id);
	}

	/**
	 * Esta clase interna comprueba si un evento tiene alguna entrada antes de
	 * entrar en la activity de visualización del mismo
	 * 
	 * @author Molina
	 * 
	 */
	private class CheckEventTrakingAvaliability extends
			AsyncTask<Integer, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... params) {
			int eventID = params[0];
			try {
				return ServerConnection.checkEventBegun(eventID);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Intent i = new Intent(EventVisualizationActivity.this,
						EventTrackingActivity.class);
				int id = getIntent().getIntExtra(Event.ID, -1);
				i.putExtra(Event.ID, id);
				startActivity(i);
			} else {
				Toast.makeText(EventVisualizationActivity.this,
						R.string.event_visualization_message_unavaliable_event,
						Toast.LENGTH_LONG).show();
			}
		}

	}

	/**
	 * Esta clase interna llama al servidor para cambiar el estado del evento
	 * respecto al competidor
	 * 
	 * @author Molina
	 * 
	 */
	private class ChangeEventStatus extends AsyncTask<Integer, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... params) {
			int eventID = params[0];
			int competitorID = Preferences.getDefaultCompetitor(
					EventVisualizationActivity.this).getId();
			try {
				return ServerConnection.changeEventParticipation(competitorID,
						eventID, eventStatus);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			eventStatus = !eventStatus;
			beginTracking.setEnabled(eventStatus);
			if (eventStatus) {
				// está en el evento, así que ponemos el titulo para que se
				// salga
				joinEvent.setText(R.string.event_visualization_button_leave);
			} else {
				// sino, pues lo contrario
				joinEvent.setText(R.string.event_visualization_button_join);
			}
		}
	}

	/**
	 * Esta clase obtiene el estado de un competidor respecto a un evento
	 * 
	 * @author Molina
	 * 
	 */
	private class CompetitorEventStatus extends
			AsyncTask<Integer, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... params) {
			int eventID = params[0];
			int competitorID = Preferences.getDefaultCompetitor(
					EventVisualizationActivity.this).getId();
			try {
				return ServerConnection.checkEventParticipation(competitorID,
						eventID);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			eventStatus = result;
			beginTracking.setEnabled(eventStatus);
			if (eventStatus) {
				// está en el evento, así que ponemos el titulo para que se
				// salga
				joinEvent.setText(R.string.event_visualization_button_leave);
			} else {
				// sino, pues lo contrario, para que se una
				joinEvent.setText(R.string.event_visualization_button_join);
			}
		}
	}
}
