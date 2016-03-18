package com.molina.savekml;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.molina.loadmaps.R;

public class ChooseFileName extends Dialog {
	private Button confirmButton;
	private EditText filenameEditText;
	private List<LatLng> points;

	public ChooseFileName(Context context, List<LatLng> points) {
		super(context);
		this.points = points;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_kml_filename);
		setTitle(R.string.title_kml_filename);
		confirmButton = (Button) findViewById(R.id.button_confirm_kml_filename);
		filenameEditText = (EditText) findViewById(R.id.edit_text_kml_filename);
		filenameEditText.requestFocus();
		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				String filename = filenameEditText.getText().toString();
				if (filename != null && !filename.isEmpty()) {
					if (!filename.endsWith(RouteToKML.KML_EXTENSION))
						filename += RouteToKML.KML_EXTENSION;
					RouteToKML.saveRouteInKML(points, filename);
					dismiss();
				}
			}
		});
	}

}
