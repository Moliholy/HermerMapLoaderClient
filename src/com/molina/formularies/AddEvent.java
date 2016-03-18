package com.molina.formularies;

import java.io.File;
import java.io.IOException;
import java.sql.Date;

import org.apache.http.client.ClientProtocolException;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.ipaulpro.afilechooser.FileChooserActivity;
import com.molina.loadmaps.R;
import com.molina.parsekml.FileChooserCallable;
import com.molina.preferences.Preferences;
import com.molina.serverconnection.ServerConnection;

public class AddEvent extends SherlockActivity implements FileChooserCallable {
	private EditText name;
	private EditText place;
	private EditText description;
	private EditText date;
	private EditText password;
	private RadioGroup privacity;
	private ImageView image;
	private File imageFile;
	private LinearLayout progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_event);
		name = (EditText) findViewById(R.id.new_event_name);
		place = (EditText) findViewById(R.id.new_event_place);
		description = (EditText) findViewById(R.id.new_event_description);
		date = (EditText) findViewById(R.id.new_event_begin_date);
		privacity = (RadioGroup) findViewById(R.id.radio_group_event_privacity);
		password = (EditText) findViewById(R.id.new_event_password);

		progressBar = (LinearLayout) findViewById(R.id.progressbar);
		image = (ImageView) findViewById(R.id.new_event_image);
		image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loadFileChooser();
			}
		});

		Button button = (Button) findViewById(R.id.new_event_button_add);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				addEvent();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE:
			// Si se ha seleccionado un archivo
			if (resultCode == RESULT_OK) {
				if (data != null) {
					// Obtenemos el URI del archivo
					final Uri uri = data.getData();
					image.setImageURI(uri);
					imageFile = new File(uri.getPath());
				}
			} else {
				imageFile = null;
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Comprueba si el formulario es válido
	 * 
	 * @return
	 */
	private boolean validFormulary() {
		if (name.getText().toString().isEmpty())
			return false;
		if (!date.getText().toString().isEmpty()) {
			try {
				Date.valueOf(date.getText().toString());
			} catch (IllegalArgumentException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Añade el evento a la BD
	 */
	private void addEvent() {
		if (validFormulary()) {
			new AddEventServerConection().execute();
		}
	}

	/**
	 * Carga la Activity del FileChooser
	 */
	@Override
	public void loadFileChooser() {
		Intent intent = new Intent(this, FileChooserActivity.class);
		try {
			startActivityForResult(intent, REQUEST_CODE);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Esta clase conecta con el servidor para añadir un nuevo evento
	 * perteneciente al usuario actual
	 * 
	 * @author Molina
	 * 
	 */
	private class AddEventServerConection extends
			AsyncTask<Void, Void, Integer> {
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(Integer result) {
			progressBar.setVisibility(View.GONE);
			if (result > 0) {
				// ha ido bien la cosa
				Toast.makeText(AddEvent.this, R.string.add_event_success,
						Toast.LENGTH_LONG).show();
				setResult(RESULT_OK);
				finish();
			} else {
				Toast.makeText(AddEvent.this, R.string.add_event_fail,
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected Integer doInBackground(Void... arg0) {
			String eventName = name.getText().toString();
			String eventPlace = place.getText().toString();
			String eventDescription = description.getText().toString();
			String eventDate = name.getText().toString();
			String eventPassword = password.getText().toString();
			Integer competitorID = Preferences.getDefaultCompetitor(
					AddEvent.this).getId();
			Integer eventPrivacity;

			switch (privacity.getCheckedRadioButtonId()) {
			case R.id.radio_button_event_privacity_private:
				eventPrivacity = 0;
				break;
			case R.id.radio_button_event_privacity_friends_only:
				eventPrivacity = 1;
				break;
			case R.id.radio_button_event_privacity_public:
				eventPrivacity = 2;
				break;
			default:
				eventPrivacity = 0;
				break;
			}

			try {
				int eventID = ServerConnection.registerEvent(eventName,
						competitorID.toString(), eventPlace, eventDescription,
						eventDate, eventPrivacity.toString(), eventPassword,
						imageFile);
				if (eventID > 0) {
					return eventID;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return -1;
		}

	}
}
