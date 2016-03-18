package com.molina.formularies;

import java.io.File;
import java.io.IOException;

import org.apache.http.ParseException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.ipaulpro.afilechooser.FileChooserActivity;
import com.molina.loadmaps.R;
import com.molina.models.Competitor;
import com.molina.parsekml.FileChooserCallable;
import com.molina.preferences.Preferences;
import com.molina.serverconnection.ServerConnection;

/**
 * Esta clase carga el formulario de registro y manda los datos al servidor
 * 
 * @author Molina
 * 
 */
public class RegisterCompetitor extends SherlockActivity implements FileChooserCallable {

	private EditText name;
	private EditText surname;
	private EditText email;
	private EditText password;
	private ImageView image;
	private File imageFile;

	/**
	 * LLamado cuando se inicia la Activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register_competitor);
		name = (EditText) findViewById(R.id.register_competitor_name_edittext);
		surname = (EditText) findViewById(R.id.register_competitor_surname_edittext);
		email = (EditText) findViewById(R.id.register_competitor_email_edittext);
		image = (ImageView) findViewById(R.id.register_competitor_avatar);
		password = (EditText) findViewById(R.id.register_competitor_password_edittext);
		imageFile = null;
	}

	/**
	 * Comprueba si los datos introducidos son correctos
	 * 
	 * @return true si los datos son correctos, o false en caso contrario
	 */
	private boolean checkInput() {
		return !name.getText().toString().isEmpty()
				&& !surname.getText().toString().isEmpty()
				&& !email.getText().toString().isEmpty()
				&& !password.getText().toString().isEmpty()
				&& password.getText().toString().length() >= 4;
	}

	/**
	 * Registra al competidor en la BD
	 */
	public void register() {
		if (checkInput()) {
			new RegisterCommpetitorServerConnection().execute();
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
					image.setImageURI(uri);
					imageFile = new File(uri.getPath());
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Método llamado cuando se pulsa sobre el inico del avatar para cargar una
	 * foto
	 * 
	 * @param v
	 */
	public void loadImage(View v) {
		loadFileChooser();
	}

	/**
	 * Método que se llama al pulsar el botón de crear un nuevo competidor
	 * 
	 * @param v
	 */
	public void registerCompetitor(View v) {
		register();
	}

	/**
	 * Carga la imagen de avatar para el nuevo competidor
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
	 * Esta clase establece la conexión con el servidor para registrar un nuevo
	 * competidor
	 * 
	 * @author Molina
	 * 
	 */
	private class RegisterCommpetitorServerConnection extends
			AsyncTask<Void, Void, Competitor> {

		@Override
		protected void onPostExecute(Competitor result) {
			if (result != null && result.isValid()) {
				// nos hemos registrado, todo va bien de momento
				// ahora tocamos las opciones y cargamos la vista de usuario
				Preferences.setDefaultCompetitor(RegisterCompetitor.this,
						result);
				Toast.makeText(RegisterCompetitor.this,
						R.string.register_competitor_success, Toast.LENGTH_LONG)
						.show();
				finishActivity(Activity.RESULT_OK);
			} else {
				Toast.makeText(RegisterCompetitor.this,
						R.string.register_competitor_fail, Toast.LENGTH_LONG)
						.show();
			}
		}

		@Override
		protected Competitor doInBackground(Void... params) {
			String competitor_name = name.getText().toString();
			String competitor_surname = surname.getText().toString();
			String competitor_email = email.getText().toString();
			String competitor_password = password.getText().toString();
			try {
				int id = ServerConnection.registerCompetitor(competitor_name,
						competitor_surname, competitor_email,
						competitor_password, imageFile);
				Competitor myself = new Competitor(competitor_name,
						competitor_surname, id, competitor_email,
						competitor_password);
				return myself;
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

	}
}
