package com.molina.mainuser;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.molina.events.EventVisualizationActivity;
import com.molina.formularies.AddEvent;
import com.molina.formularies.Login;
import com.molina.formularies.RegisterCompetitor;
import com.molina.loadmaps.R;
import com.molina.models.Competitor;
import com.molina.models.Event;
import com.molina.preferences.Preferences;
import com.molina.serverconnection.ServerConnection;
import com.molina.utils.AsyncTaskExecutionHelper;
import com.molina.utils.EventImageDownloader;
import com.molina.viewpager.ViewPagerActivity;

/**
 * Este Fragment muestra los eventos creados por el usuario, o bien nos lleva a
 * la pantalla para logearse o crear un nuevo usuario
 * 
 * @author Molina
 * 
 */
public class MyEvents extends Fragment {

	private ArrayList<Event> events;
	private ListView listView;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// comprobamos si deberíamos estar aquí o en la de registro de nuevo
		// competidor
		Competitor competitor = Preferences.getDefaultCompetitor(getActivity());
		if (competitor.isValid()) {
			listView = (ListView) getActivity().findViewById(
					R.id.event_listview);
			AsyncTaskExecutionHelper.executeParallel(new DownloadEventData(),
					competitor);
			Button addNewEvent = (Button) getActivity().findViewById(
					R.id.button_add_new_event);
			addNewEvent.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					addEvent();
				}
			});
			// listener para la lista
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Event event = events.get(position);
					Intent i = new Intent(getActivity(),
							EventVisualizationActivity.class);
					i.putExtra(Event.ID, event.getId());
					i.putExtra(Event.NAME, event.getName());
					i.putExtra(Event.DESCRIPTION, event.getDescription());
					i.putExtra(Event.PLACE, event.getPlace());
					i.putExtra(Event.PRIVACITY, event.getPrivacity());
					i.putExtra(Event.DATE, event.getBegin_date());
					i.putExtra("editing", true);

					startActivity(i);
				}
			});
		} else {
			Activity act = getActivity();
			Button register = (Button) act
					.findViewById(R.id.button_new_competitor);
			Button login = (Button) act
					.findViewById(R.id.button_login_competitor);

			register.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivity(new Intent(getActivity(),
							RegisterCompetitor.class));
				}
			});

			login.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivityForResult(new Intent(getActivity(),
							Login.class), ViewPagerActivity.REQUEST_CODE);
				}
			});
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Competitor competitor = Preferences.getDefaultCompetitor(getActivity());
		if (competitor.isValid()) {
			return inflater.inflate(R.layout.fragment_my_events, null);
		} else {
			return inflater.inflate(R.layout.fragment_register_or_login, null);
		}
	}

	/**
	 * Carga la Activity tipo formulario para añadir un evento nuevo
	 * 
	 * @param v
	 */
	private void addEvent() {
		Intent intent = new Intent(getActivity(), AddEvent.class);
		startActivityForResult(intent, ViewPagerActivity.REQUEST_CODE);
	}

	/**
	 * Llena la lista de eventos mediante los datos descargados
	 */
	private void fillList() {
		if (events != null && events.size() > 0) {
			Event[] eventList = new Event[events.size()];
			listView.setAdapter(new EventArrayAdapter(getActivity(), events
					.toArray(eventList)));
		}
	}

	/**
	 * Clase interna que controla las vistas que se muestran dentro del ListView
	 * con todos los eventos del usuario
	 * 
	 * @author Molina
	 * 
	 */
	public class EventArrayAdapter extends ArrayAdapter<Event> {
		private Event[] events;
		private Context context;

		public EventArrayAdapter(Context context, Event[] objects) {
			super(context, R.layout.event_viewer, objects);
			this.context = context;
			this.events = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Event event = events[position];
			// inflamos la vista
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.event_viewer, parent,
					false);

			// cogemos individualmente los elementos
			TextView name = (TextView) rowView
					.findViewById(R.id.event_viewer_name);
			TextView place = (TextView) rowView
					.findViewById(R.id.event_viewer_place);
			TextView description = (TextView) rowView
					.findViewById(R.id.event_viewer_description);
			ImageView image = (ImageView) rowView
					.findViewById(R.id.event_viewer_image);

			// y los editamos convenientemente
			name.setText(event.getName());
			place.setText(event.getPlace());
			description.setText(event.getDescription());

			// nos descargamos la imagen vía internet
			AsyncTaskExecutionHelper.executeParallel(new EventImageDownloader(
					image), event);

			return rowView;
		}
	}

	/**
	 * Descarga los datos de los eventos que ha creado un usuario en concreto
	 * 
	 * @author Molina
	 * 
	 */
	private class DownloadEventData extends AsyncTask<Competitor, Void, Void> {

		@Override
		protected Void doInBackground(Competitor... competitors) {
			events = ServerConnection.getCreatedEvents(competitors[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			fillList();
		}

	}

}
