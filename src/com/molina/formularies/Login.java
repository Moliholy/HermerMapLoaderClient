package com.molina.formularies;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.molina.loadmaps.R;
import com.molina.models.Competitor;
import com.molina.preferences.Preferences;
import com.molina.serverconnection.ServerConnection;

public class Login extends SherlockActivity {
	private EditText email;
	private EditText password;
	private LinearLayout progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		email = (EditText) findViewById(R.id.login_competitor_email);
		password = (EditText) findViewById(R.id.login_competitor_password);
		progressBar = (LinearLayout) findViewById(R.id.progressbar);
	}

	private boolean isValid() {
		return !email.getText().toString().isEmpty()
				&& !password.getText().toString().isEmpty();
	}

	public void sendCredentials(View v) {
		if (isValid()) {
			new CheckCredentials().execute();
		}
	}

	/**
	 * Esta clase conecta con el servidor para verificar si el par
	 * email-password es correcto
	 * 
	 * @author Molina
	 * 
	 */
	private class CheckCredentials extends AsyncTask<Void, Void, Competitor> {

		@Override
		protected void onPreExecute() {
			// ponemos el ActionBar
			setProgressBarIndeterminateVisibility(true);
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(Competitor competitor) {
			// quitamos el ActionBar
			setProgressBarIndeterminateVisibility(false);
			progressBar.setVisibility(View.VISIBLE);

			// comprobamos si todo ha ido bien
			if (competitor != null) {
				Preferences.setDefaultCompetitor(Login.this, competitor);
				setResult(RESULT_OK);
				finish();
			} else {
				// mostramos un Toast indicando que ha habido problemas
				Toast.makeText(Login.this,
						R.string.messages_error_email_password_pair,
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected Competitor doInBackground(Void... arg0) {
			String competitorEmail = email.getText().toString();
			String competitorPassword = password.getText().toString();
			Competitor competitor = ServerConnection
					.verifyCompetitorCredentials(competitorEmail,
							competitorPassword);

			return competitor;
		}

	}

}
