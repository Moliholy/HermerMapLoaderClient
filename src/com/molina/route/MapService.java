package com.molina.route;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

/**
 * Esta clase llama al servicio de google maps para obtener una ruta entre dos
 * puntos
 * 
 * @author Molina
 * 
 */
public class MapService {
	public static final String MODE_CAR = "driving";
	public static final String MODE_WALKING = "walking";
	public static final String MODE_BICYCLING = "bicycling";
	public static final String MODE_TRANSIT = "transit";

	/**
	 * Transforma un InputStream en una cadena de caracteres extrayendo su
	 * contenido
	 * 
	 * @param in
	 *            InputStream del que se leen los datos
	 * @return el inputstream transformado a cadena de caracteres
	 * @throws IOException
	 *             si ha habido erroes en la lectura
	 */
	public static String inputStreamToString(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	/**
	 * Calcula la ruta entre dos puntos
	 * 
	 * @param startLat
	 *            latitud del punto inicial
	 * @param startLng
	 *            longitud del punto inicial
	 * @param targetLat
	 *            latitud del punto final
	 * @param targetLng
	 *            longitud del punto final
	 * @param mode
	 *            modo por el que se calculará la ruta
	 * @return una lista con los puntos que conforman la ruta en la forma
	 *         latitud/longitud
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static List<LatLng> calculateRoute(Double startLat, Double startLng,
			Double targetLat, Double targetLng, String mode)
			throws ClientProtocolException, IOException, JSONException {
		return calculateRoute(startLat + "," + startLng, targetLat + ","
				+ targetLng, mode);
	}

	/**
	 * Calcula la ruta entre dos puntos dando los parámetros en forma de String
	 * 
	 * @param startLat
	 *            latitud del punto inicial
	 * @param startLng
	 *            longitud del punto inicial
	 * @param targetLat
	 *            latitud del punto final
	 * @param targetLng
	 *            longitud del punto final
	 * @param mode
	 *            modo por el que se calculará la ruta
	 * @return una lista con los puntos que conforman la ruta en la forma
	 *         latitud/longitud
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static List<LatLng> calculateRoute(String startCoords,
			String targetCoords, String mode) throws ClientProtocolException,
			IOException, JSONException {
		// creamos en primer lugar la URL
		String url = "http://maps.googleapis.com/maps/api/directions/json?origin="
				+ startCoords
				+ "&destination="
				+ targetCoords
				+ "&sensor=false" + "&mode=" + mode;

		// hacemos la conexión HTTP con el servicio que ofrece Google
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		InputStream is = null;
		// obtenemos el contenido y lo analizamos
		is = entity.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is,
				"iso-8859-1"), 8);
		StringBuilder sb = new StringBuilder();
		sb.append(reader.readLine() + "\n");
		String line = "0";
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		is.close();
		reader.close();
		String result = sb.toString();
		// ahora empieza lo duro: parsear el JSON para extraer las coordenadas
		JSONObject jsonObject = new JSONObject(result);
		JSONArray routeArray = jsonObject.getJSONArray("routes");
		if (routeArray != null && !routeArray.isNull(0)) {
			JSONObject routes = routeArray.getJSONObject(0);
			JSONObject overviewPolylines = routes
					.getJSONObject("overview_polyline");
			String encodedString = overviewPolylines.getString("points");
			List<LatLng> pointToDraw = decodePoly(encodedString);

			return pointToDraw;
		}
		return null;
	}

	/**
	 * Decodifica la polilínea dada en un formato de Google Maps
	 * 
	 * @param encoded
	 *            lista de puntos codificados que conforman la polilínea
	 * @return la polilínea decodificada en la forma latitud/longitud
	 */
	private static List<LatLng> decodePoly(String encoded) {
		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			double latitude = (lat / 1E5);
			double longitude = (lng) / 1E5;

			LatLng p = new LatLng(latitude, longitude);
			poly.add(p);
		}

		return poly;
	}
}
