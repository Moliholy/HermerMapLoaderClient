package com.molina.offlinemaps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.molina.loadmaps.R;

/**
 * Servicio que se ejecuta as�ncronamente y que se usa para descargar todos los
 * tiles necesarios en todos los zooms indicados de una zona en concreto. Adem�s
 * activa una notificaci�n al usuario y almacena directamente todos los tiles
 * descargados de forma que puedan ser directamente usados por osmdroid para el
 * manejo de mapas offline
 * 
 * @author Molina
 * 
 */
public class OfflineMapTileDownloader extends IntentService {
	private static final String BASE_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/osmdroid/tiles/Mapnik";
	private static final String DEFAULT_FILE_EXTENSION = ".png.tile";
	private static String BASE_URL = "http://tile.openstreetmap.org/";
	public static final String TILE_DATA = "TILE_DATA";
	public static final String HANDLER_IDENTICATOR = "MESSENGER";
	private static byte[] buffer = new byte[2048];
	private NotificationManager mNotifyManager;
	private Builder mBuilder;
	private static final int ID = 243234;
	private PendingIntent returnIntent;
	private Intent originalIntent;

	/**
	 * Constructor por defecto
	 */
	public OfflineMapTileDownloader() {
		super("OfflineMapTileDownloader");
	}

	/**
	 * M�todo llamado cuando se crea el servicio. Inicializa tanto los
	 * par�metros de la notificaci�n como los directorios requeridos para
	 * almacenar los tiles en la tarjeta SD
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		File file = new File(BASE_PATH);
		if (!file.exists() || !file.isDirectory())
			file.mkdir();

		returnIntent = PendingIntent.getActivity(this, 0, null, 0);
		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(getApplicationContext());
		mBuilder.setContentTitle(
				getResources().getString(R.string.notification_download_title))
				.setContentText(
						getResources().getString(
								R.string.notification_download_text))
				.setSmallIcon(R.drawable.ic_menu_archive)
				.setContentIntent(returnIntent).setAutoCancel(true)
				.setOngoing(true);
	}

	/**
	 * Descarga un tile de la URL pasada como par�metro
	 * 
	 * @param URL
	 *            trozo de la direcci�n web donde se encuentra el tile
	 * @param zoom
	 *            zoom que tiene el tile
	 * @param column
	 *            columna que ocupa el tile para el zoom determinado
	 * @param row
	 *            fila que ocupa el tile para el zoom determinado
	 * @return el fichero descargado
	 */
	protected File downloadTile(String URL, Integer zoom, Integer column,
			Integer row) {
		return downloadTile(URL, zoom, column, row, DEFAULT_FILE_EXTENSION);
	}

	/**
	 * Descarga un tile de la URL pasada como par�metro
	 * 
	 * @param URL
	 *            trozo de la direcci�n web donde se encuentra el tile
	 * @param tile
	 *            objeto que contiene los datos referentes al zoom, fila y
	 *            columna del tile que se necesita descargar
	 * @return el fichero descargado
	 */
	protected File downloadTile(String URL, TileIdentifier tile) {
		return downloadTile(URL, tile.getZoom(), tile.getColumn(),
				tile.getRow());
	}

	/**
	 * Descarga un tile de la URL pasada como par�metro
	 * 
	 * @param URL
	 *            trozo de la direcci�n web donde se encuentra el tile
	 * @param zoom
	 *            zoom que tiene el tile
	 * @param column
	 *            columna que ocupa el tile para el zoom determinado
	 * @param row
	 *            fila que ocupa el tile para el zoom determinado
	 * @param extension
	 *            extensi�n que se le otorgar� al archivo descargado
	 * @return el fichero descargado
	 */
	private File downloadTile(String URL, Integer zoom, Integer column,
			Integer row, String extension) {
		String partialPath = BASE_PATH + "/" + zoom.toString() + "/"
				+ column.toString();
		File directories = new File(partialPath);
		File file = new File(partialPath + "/" + row.toString() + extension);
		if (!directories.exists())
			directories.mkdirs();
		if (file.exists())
			return file;

		InputStream stream = null;
		FileOutputStream fos = null;
		try {
			URL url = new URL(URL);
			stream = url.openStream();
			fos = new FileOutputStream(file);
			int length;
			while ((length = stream.read(buffer)) != -1) {
				fos.write(buffer, 0, length);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			finish(false);
		} catch (IOException e) {
			e.printStackTrace();
			finish(false);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
					finish(false);
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
					finish(false);
				}
			}
		}
		return file;
	}

	/**
	 * Este m�todo hay que sobreescribirlo, ya que es el que se ejecuta en el
	 * servicio. Adem�s contiene el intent en el que se la ha pasado todo la
	 * informaci�n referente a los datos que tiene que descargar. En �l se lleva
	 * a cabo todo el trabajo pesado correspondiente a la descarga de los tiles,
	 * que pueden llegar a ser varios miles, por lo que se plantea como un
	 * servicio de larga duraci�n. Va guardando convenientemente todos los tiles
	 * descargados en el directorio de cache de osmdroid para que puedan ser
	 * directamente usados para el manejo de mapas offline
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		// cogemos todos los datos encapsulados en la clase TileAreaData
		originalIntent = intent;
		TileAreaData tileData = (TileAreaData) intent
				.getParcelableExtra(TILE_DATA);
		if (tileData == null)
			return;

		// comenzamos a iterar
		// 1�) Para cada zoom...
		// android.os.Debug.waitForDebugger();
		ArrayList<TileIdentifier> tileList = new ArrayList<TileIdentifier>();
		for (Integer zoom : tileData.getZooms()) {
			tileList.addAll(tileData.getContainedTiles(zoom));
		}

		int totalSize = tileList.size();
		int downloaded = 0;
		mBuilder.setProgress(totalSize, 0, false);
		Notification notification = mBuilder.build();
		mNotifyManager.notify(ID, notification);

		// para cada tile que haya que descargar en este zoom...
		for (TileIdentifier tile : tileList) {
			// nos descargamos el tile y lo ponemos donde corresponda
			downloadTile(tile);
			downloaded++;
			File file = downloadTile(tile);

			Log.d("DOWNLOADED", "Downloaded: " + file.getAbsolutePath());

			// actualizamos el progress bar de la notificaci�n
			mBuilder.setProgress(totalSize, downloaded, false);
			// mBuilder.setProgress(totalSize, downloaded, false);
			mNotifyManager.notify(ID, mBuilder.build());
		}
		// hemos terminado. Lo notificamos convenientemente y mandamos los
		// resultados a la Activity original
		mBuilder.setAutoCancel(true).setOngoing(false)
				.setDeleteIntent(returnIntent);
		mBuilder.setContentText(getResources().getString(
				R.string.notification_download_finish));
		mNotifyManager.notify(ID, mBuilder.build());
		finish(true);
	}

	/**
	 * Finaliza la ejecuci�n del servicio y manda un mensaje a la actividad que
	 * lo llam� inform�ndole del resultado de la descarga
	 * 
	 * @param intent
	 *            Intent que inici� el servicio y que contiene los datos de
	 *            interacci�n con la Activity original
	 */
	private void finish(boolean success) {
		Bundle extras = originalIntent.getExtras();
		if (extras != null) {
			Messenger messenger = (Messenger) extras.get(HANDLER_IDENTICATOR);
			Message msg = Message.obtain();
			if (success)
				msg.arg1 = Activity.RESULT_OK;
			else
				msg.arg2 = Activity.RESULT_CANCELED;
			try {
				messenger.send(msg);
			} catch (android.os.RemoteException e1) {
				Log.w(getClass().getName(), "Exception sending message", e1);
			}
		}
		stopSelf();
	}

	/**
	 * Descarga un tile del sitio web montado a partir de los datos pasados en
	 * el TileIdentifier
	 * 
	 * @param tile
	 *            datos del tile a descargar
	 * @return el fichero descargado
	 */
	protected File downloadTile(TileIdentifier tile) {
		String URL = BASE_URL + tile.toString() + ".png";
		return downloadTile(URL, tile);
	}
}
