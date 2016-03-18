package com.molina.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.molina.loadmaps.R;
import com.molina.models.Competitor;

@SuppressWarnings("deprecation")
public class Preferences extends SherlockPreferenceActivity {
	private static final String KEY = PreferenceActivity.class.getName();
	
	public static void setDefaultCompetitor(Context context,Competitor c){
		SharedPreferences prefs = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(Competitor.COMPETITOR_NAME, c.getName());
		editor.putString(Competitor.COMPETITOR_SURNAME, c.getSurname());
		editor.putString(Competitor.COMPETITOR_PASSWORD, c.getPassword());
		editor.putString(Competitor.COMPETITOR_EMAIL, c.getEmail());
		editor.putInt(Competitor.COMPETITOR_ID, c.getId());
		editor.commit();
	}
	
	public static Competitor getDefaultCompetitor(Context context){
		SharedPreferences prefs = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		String name =  prefs.getString(Competitor.COMPETITOR_NAME, null);
		String surname =  prefs.getString(Competitor.COMPETITOR_SURNAME, null);
		String email =  prefs.getString(Competitor.COMPETITOR_EMAIL, null);
		String password =  prefs.getString(Competitor.COMPETITOR_PASSWORD, null);
		int id = prefs.getInt(Competitor.COMPETITOR_ID, -1);
		return new Competitor(name, surname, id, email, password);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
