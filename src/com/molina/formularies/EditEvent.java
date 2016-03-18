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
import com.molina.models.Event;
import com.molina.parsekml.FileChooserCallable;
import com.molina.preferences.Preferences;
import com.molina.serverconnection.ServerConnection;
import com.molina.utils.AsyncTaskExecutionHelper;
import com.molina.utils.EventImageDownloader;

/**
 * Esta clase sirve de formulario para modificar un evento
 * 
 * @author Molina
 * 
 */
public class EditEvent extends SherlockActivity implements FileChooserCallable {

	private EditText name;
	private EditText place;
	private EditText description;
	private EditText date;
	private EditText oldPassword;
	private EditText newPassword;
	private RadioGroup privacity;
	private ImageView image;
	private File imageFile;
	private LinearLayout progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_event);
		name = (EditText) findViewById(R.id.new_event_name);
		place = (EditText) findViewById(R.id.new_event_place);
		description = (EditText) findViewById(R.id.new_event_description);
		date = (EditText) findViewById(R.id.new_event_begin_date);
		privacity = (RadioGroup) findViewById(R.id.radio_group_event_privacity);
		oldPassword = (EditText) findViewById(R.id.edit_event_old_password);
		newPassword = (EditText) findViewById(R.id.edit_event_new_password);

		// cargamos los textos por defecto
		Intent i = getIntent();
		name.setText(i.getStringExtra(Event.NAME));
		place.setText(i.getStringExtra(Event.PLACE));
		description.setText(i.getStringExtra(Event.DESCRIPTION));
		date.setText(i.getStringExtra(Event.DATE));
		// nos da directamente el id del radiobutton (R.id.___)
		privacity.check(i.getIntExtra(Event.PRIVACITY, 0));
		oldPassword.setText(i.getStringExtra(Event.PASSWORD));

		imageFile = null;
		Event event = new Event();
		event.setId(i.getIntExtra(Event.ID, -1));
		AsyncTaskExecutionHelper.executeParallel(
				new EventImageDownloader(image), event);

		progressBar = (LinearLayout) findViewById(R.id.progressbar);
		image = (ImageView) findViewById(R.id.new_event_image);
		image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loadFileChooser();
			}
		});

		Button button = (Button) findViewById(R.id.edit_event_button_edit);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				editEvent();
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
	private void editEvent() {
		if (validFormulary()) {
			new EditEventServerConection().execute();
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
	 * Esta clase conecta con el servidor para modificar un evento perteneciente
	 * al usuario actual
	 * 
	 * @author Molina
	 * 
	 */
	private class EditEventServerConection extends
			AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressBar.setVisibility(View.GONE);
			if (result) {
				// ha ido bien la cosa
				Toast.makeText(EditEvent.this, R.string.add_event_success,
						Toast.LENGTH_LONG).show();
				setResult(RESULT_OK);
				finish();
			} else {
				Toast.makeText(EditEvent.this, R.string.add_event_fail,
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			String eventName = name.getText().toString();
			String eventPlace = place.getText().toString();
			String eventDescription = description.getText().toString();
			String eventDate = name.getText().toString();
			String eventOldPassword = oldPassword.getText().toString();
			String eventNewPassword = newPassword.getText().toString();
			Integer eventID = getIntent().getIntExtra("ID", -1);
			Integer competitorID = Preferences.getDefaultCompetitor(
					EditEvent.this).getId();
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
				boolean success = ServerConnection.editEvent(
						eventID.toString(), eventName, competitorID.toString(),
						eventPlace, eventDescription, eventDate,
						eventPrivacity.toString(), eventOldPassword,
						eventNewPassword, imageFile);
				return success;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
	}
}
