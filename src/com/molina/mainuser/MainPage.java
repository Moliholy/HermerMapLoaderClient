package com.molina.mainuser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.molina.loadmaps.LoadMapActivity;
import com.molina.loadmaps.R;
import com.molina.offlinemaps.OfflineMapActivity;

/**
 * Este Fragment muestra las opciones de cargar mapas online y offline
 * 
 * @author Molina
 * 
 */
public class MainPage extends Fragment {
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Activity act = getActivity();
		View onlinemap = act.findViewById(R.id.image_online);
		View offlinemap = act.findViewById(R.id.image_offline);
		
		//establecemos los listeners
		onlinemap.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadOnlineMap(v);
			}
		});
		
		offlinemap.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadOfflineMap(v);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main_page, null);
	}

	/**
	 * Carga la Activity de mapas online
	 * 
	 * @param v
	 */
	public void loadOnlineMap(View v) {
		Intent intent = new Intent(getActivity(), LoadMapActivity.class);
		startActivity(intent);
	}

	/**
	 * Carga la Activity de mapas offline
	 * 
	 * @param v
	 */
	public void loadOfflineMap(View v) {
		Intent intent = new Intent(getActivity(), OfflineMapActivity.class);
		startActivity(intent);
	}
}
