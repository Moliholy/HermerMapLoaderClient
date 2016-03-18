package com.molina.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.molina.models.Event;
import com.molina.serverconnection.ServerConnection;

/**
 * Esta clase se encarga de la descarga en segundo plano de la imagen del
 * evento
 * 
 * @author Molina
 * 
 */
public class EventImageDownloader extends AsyncTask<Event, Void, Bitmap> {
	private ImageView image;

	public EventImageDownloader(ImageView image) {
		this.image = image;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if (result != null)
			image.setImageBitmap(result);
	}

	@Override
	protected Bitmap doInBackground(Event... events) {
		try {
			InputStream is = ServerConnection.getEventImageStream(events[0]
					.getId());
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			return bitmap;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
