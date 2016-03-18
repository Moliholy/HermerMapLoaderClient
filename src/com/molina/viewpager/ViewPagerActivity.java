package com.molina.viewpager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONException;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.molina.loadmaps.R;
import com.molina.mainuser.FindEvent;
import com.molina.mainuser.MainPage;
import com.molina.mainuser.MyEvents;
import com.molina.models.Competitor;
import com.molina.models.CompetitorTracking;
import com.molina.preferences.Preferences;
import com.molina.serverconnection.ServerConnection;
import com.molina.utils.AsyncTaskExecutionHelper;
import com.molina.utils.CompetitorArrayAdapter;
import com.viewpagerindicator.TitlePageIndicator;

public class ViewPagerActivity extends SherlockFragmentActivity {
	private String[] titles;
	public static final int REQUEST_CODE = 34;
	private SlidingMenu slidingMenu;
	private ListView competitorList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_pager);

		// cargamos los títulos
		Resources res = getResources();
		titles = new String[3];
		titles[0] = res.getString(R.string.fragment_title_main_page);
		titles[1] = res.getString(R.string.fragment_title_my_events);
		titles[2] = res.getString(R.string.fragment_title_find_event);
//tururusadfsadf

		// enlazamos el FragmentPagerAdapter
		setFragmentPagerAdapter();

		// y el slidingmenu
		setSlidingMenu();
		
		//establecemos la lista del slidingmenu
		competitorList = (ListView) findViewById(R.id.slidingmenu_competitor_list);

		AsyncTaskExecutionHelper.executeParallel(new DownloadCompetitorData(),
				new Void[0]);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (featureId == android.R.id.home)
			slidingMenu.toggle(true);
		return true;
	}

	/**
	 * Enlaza el SlidingMenu con la Activity
	 */
	private void setSlidingMenu() {
		slidingMenu = new SlidingMenu(this);
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setBehindOffset(50);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		slidingMenu.setMenu(R.layout.slidingmenu_main);
	}

	/**
	 * Establece el PagerAdapter
	 */
	private void setFragmentPagerAdapter() {
		FragmentPagerAdapter adapter = new FragmentAdapterHelper(
				getSupportFragmentManager());
		ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
		pager.setAdapter(adapter);

		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(pager);
	}

	/**
	 * Se llama a este método tras volver de la introducción de un nuevo usuario
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK)
			setFragmentPagerAdapter();
	}

	private class FragmentAdapterHelper extends FragmentPagerAdapter {

		public FragmentAdapterHelper(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int item) {
			Fragment f = null;
			item = item % titles.length;
			switch (item) {
			case 0:
				f = new MainPage();
				break;
			case 1:
				f = new MyEvents();
				break;
			case 2:
				f = new FindEvent();
				break;
			}
			return f;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position % titles.length];
		}

		@Override
		public int getCount() {
			return titles.length;
		}
	}

	/**
	 * Esta clase se descarga del servidor todos los datos referentes a los
	 * amigos del usuario, incluyendo su foto
	 * 
	 * @author Molina
	 * 
	 */
	private class DownloadCompetitorData extends
			AsyncTask<Void, Void, CompetitorTracking[]> {

		@Override
		protected CompetitorTracking[] doInBackground(Void... params) {
			Competitor competitor = Preferences
					.getDefaultCompetitor(ViewPagerActivity.this);
			try {
				// hay que ver primero que participantes hay
				ArrayList<Competitor> competitors = ServerConnection
						.getCompetitorFriends(competitor);
				CompetitorTracking[] competitorTrackingArray = new CompetitorTracking[competitors
						.size()];
				int i = 0;
				for (Competitor c : competitors)
					competitorTrackingArray[i++] = new CompetitorTracking(c);
				return competitorTrackingArray;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(CompetitorTracking[] result) {
			if (result != null && result.length > 0) {
				competitorList.setAdapter(new CompetitorArrayAdapter(
						ViewPagerActivity.this, result));
			}
		}
	}
}
