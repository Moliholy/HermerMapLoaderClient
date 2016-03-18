package com.molina.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.molina.loadmaps.R;
import com.molina.models.CompetitorTracking;

/**
 * Adapter necesario para mostrar datos de los competidores en el listView
 * 
 * @author Molina
 * 
 */
public class CompetitorArrayAdapter extends ArrayAdapter<CompetitorTracking> {
	private CompetitorTracking[] competitors;
	private Context context;

	public CompetitorArrayAdapter(Context context, CompetitorTracking[] objects) {
		super(context, R.layout.event_viewer, objects);
		this.context = context;
		this.competitors = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CompetitorTracking competitor = competitors[position];
		// inflamos la vista
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.competitor_viewer, parent,
				false);

		// cogemos individualmente los elementos
		TextView name = (TextView) rowView
				.findViewById(R.id.competitor_viewer_name);
		TextView surname = (TextView) rowView
				.findViewById(R.id.competitor_viewer_surname);
		ImageView image = (ImageView) rowView
				.findViewById(R.id.competitor_viewer_image);

		// y los editamos convenientemente
		name.setText(competitor.getCompetitor().getName());
		surname.setText(competitor.getCompetitor().getSurname());

		competitor.setImage(image);
		View backgroundView = rowView
				.findViewById(R.id.competitor_viewer_layout);
		backgroundView.setBackgroundColor(competitor.getColor());
		// nos descargamos la imagen vía internet
		AsyncTaskExecutionHelper.executeParallel(new CompetitorImageDownloader(
				image), competitor.getCompetitor());

		return rowView;
	}
}
