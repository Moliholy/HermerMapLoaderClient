package com.molina.serverconnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.molina.models.Competitor;
import com.molina.models.CompetitorTracking;
import com.molina.models.Event;

/**
 * Esta clase sirve para realizar las conexiones con el servidor. Al trabajar en
 * versiones de Android mayores de la API 10 SE DEBE USAR OBLIGATORIAMENTE FUERA
 * DEL UI THREAD. De otro modo lanzará una excepción, ya que todos los métodos
 * establecen conexiones con el servidor y producen retardos importantes
 * 
 * @author Molina
 * 
 */
public class ServerConnection {

	/**
	 * URL del sitio
	 */
	public static final String SITE_URL = "http://192.168.1.34/";
	public static final String SITE_URL2 = "http://baeza.ujaen.es/~molina/";
	
	/**
	 * URL para llamar a los controladores
	 */
	public static final String BASE_URL = SITE_URL + "ci/index.php/";

	/**
	 * Manda los datos de un nuevo competidor al servidor para insertarlo en la
	 * BD
	 * 
	 * @param name
	 *            nombre del competidor
	 * @param surname
	 *            apellido del competidor
	 * @param email
	 *            email del competidor
	 * @param password
	 *            contraseá del competidor
	 * @param image
	 *            imagen de avatar del competidor
	 * @throws ParseException
	 * @throws IOException
	 */
	public static int registerCompetitor(String name, String surname,
			String email, String password, File image) throws ParseException,
			IOException {
		final HttpClient httpclient = new DefaultHttpClient();
		final HttpPost httppost = new HttpPost(BASE_URL + "competitor/insert");
		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("competitor_name", new StringBody(name));
		reqEntity.addPart("competitor_surname", new StringBody(surname));
		reqEntity.addPart("competitor_email", new StringBody(email));
		reqEntity.addPart("competitor_password", new StringBody(password));
		httppost.setEntity(reqEntity);

		if (image != null)
			reqEntity.addPart("avatar", new FileBody(image, "image"));

		HttpResponse response = httpclient.execute(httppost);
		HttpEntity resEntity = response.getEntity();
		if (resEntity != null) {
			String responseToString = EntityUtils.toString(resEntity);
			Log.i("RESPONSE", responseToString);
			try {
				int id = Integer.parseInt(responseToString);
				return id;
			} catch (NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}

	/**
	 * Obtiene un JSON del servidor
	 * 
	 * @param url
	 *            url de la que extraer el JSON
	 * @return un JSONObject con la información recibida
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws JSONException
	 */
	private static JSONArray getJSONArray(String url)
			throws IllegalStateException, IOException, JSONException {
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

		JSONArray jsonArray = new JSONArray(result);
		return jsonArray;
	}

	/**
	 * Obtiene todos los eventos que ha creado un competidor mediante un parseo
	 * de un JSON
	 * 
	 * @param c
	 *            competidor
	 * @return los eventos del cokmpetidor
	 */
	public static ArrayList<Event> getCreatedEvents(Competitor c) {
		String url = BASE_URL + "event/get_from_competitor/" + c.getId();
		ArrayList<Event> list = new ArrayList<Event>();
		try {
			JSONArray json = getJSONArray(url);
			for (int i = 0; i < json.length(); i++) {
				JSONObject object = json.getJSONObject(i);
				int id = object.getInt("event_id");
				String name = object.getString("event_name");

				// fecha
				Date begin_date = null;
				if (!object.isNull("event_begin_date"))
					begin_date = Date.valueOf(object
							.getString("event_begin_date"));

				// lugar
				String place = null;
				if (!object.isNull("event_place"))
					place = object.getString("event_place");

				String description = null;
				if (!object.isNull("event_description"))
					description = object.getString("event_description");

				Integer privacity = Integer.parseInt(object
						.getString("event_privacity"));

				// creamos el evento
				Event event = new Event(c.getId(), id, null, name, begin_date,
						place, privacity, description);
				list.add(event);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Obtiene un stream de la imagen del competidor
	 * 
	 * @param event_id
	 *            id del competidor
	 * @return el InputStream de la foto del evento
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static InputStream getCompetitorImageStream(int competitor_id)
			throws MalformedURLException, IOException {
		String url = SITE_URL + "/images/competitors/" + competitor_id;
		return new URL(url).openStream();
	}

	/**
	 * Gets a stream of the event's image
	 * 
	 * @param event_id
	 *            event's id
	 * @return the InputStream's photo
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static InputStream getEventImageStream(int event_id)
			throws MalformedURLException, IOException {
		String url = SITE_URL + "/images/events/" + event_id;
		return new URL(url).openStream();
	}

	/**
	 * Obtiene la lista de amigos de un competidor
	 * 
	 * @param competitor
	 *            competidor del cual se tiene que buscar los amigos
	 * @return una lista con todos los amigos del competidor
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static ArrayList<Competitor> getCompetitorFriends(
			Competitor competitor) throws IllegalStateException, IOException,
			JSONException {
		ArrayList<Competitor> array = new ArrayList<Competitor>();
		String urlString = BASE_URL + "competitor/friends/"
				+ competitor.getId();
		JSONArray jsonArray = getJSONArray(urlString);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject object = jsonArray.getJSONObject(i);
			Integer id = object.getInt("competitor_id");
			String name = object.getString("competitor_name");
			String surname = object.getString("competitor_surname");
			String email = object.getString("competitor_email");
			array.add(new Competitor(name, surname, id, email, null));
		}

		return array;

	}

	/**
	 * Registra un evento en la BD
	 * 
	 * @param eventName
	 *            nombre del evento
	 * @param eventPlace
	 *            lugar del evento
	 * @param eventDescription
	 *            descripción del evento
	 * @param eventDate
	 *            fecha del evento
	 * @param isPrivate
	 *            indica si el evento es privado o no
	 * @param password
	 *            contraseña del evento
	 * @param imageFile
	 *            imagen del evento
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static int registerEvent(String eventName, String competitorID,
			String eventPlace, String eventDescription, String eventDate,
			String isPrivate, String password, File imageFile)
			throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(BASE_URL + "event/insert");
		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("event_name", new StringBody(eventName));
		reqEntity.addPart("competitor_id", new StringBody(competitorID));
		reqEntity.addPart("event_place", new StringBody(eventPlace));
		reqEntity
				.addPart("event_description", new StringBody(eventDescription));
		reqEntity.addPart("event_password", new StringBody(password));
		reqEntity.addPart("event_privacity", new StringBody(isPrivate));
		httppost.setEntity(reqEntity);

		if (imageFile != null)
			reqEntity.addPart("event_image", new FileBody(imageFile, "image"));

		HttpResponse response = httpclient.execute(httppost);
		HttpEntity resEntity = response.getEntity();
		if (resEntity != null) {
			String responseToString = EntityUtils.toString(resEntity);
			Log.i("RESPONSE", responseToString);
			try {
				int id = Integer.parseInt(responseToString);
				return id;
			} catch (NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}

	/**
	 * Comprueba si el par email-password es válido
	 * 
	 * @param competitorEmail
	 *            email del competidor
	 * @param competitorPassword
	 *            contraseña del competidor
	 * @return true si el par email-password es válido, y false en caso
	 *         contrario
	 */
	public static Competitor verifyCompetitorCredentials(
			String competitorEmail, String competitorPassword) {
		String url = BASE_URL + "competitor/login/" + competitorEmail + "/"
				+ competitorPassword;
		try {
			JSONArray array = getJSONArray(url);
			if (array.length() > 0) {
				JSONObject object = array.getJSONObject(0);
				int id = object.getInt("competitor_id");
				String name = object.getString("competitor_name");
				String surname = object.getString("competitor_surname");
				String email = object.getString("competitor_email");
				return new Competitor(name, surname, id, email,
						competitorPassword);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Edita un evento mediante un post al servidor con los nuevos datos
	 * 
	 * @param eventID
	 *            ID del evento
	 * @param eventName
	 *            nombre del evento
	 * @param competitorID
	 *            id del competidor
	 * @param eventPlace
	 *            lugar del evento
	 * @param eventDescription
	 *            descripción del evento
	 * @param eventDate
	 *            fecha del evento
	 * @param privacity
	 *            privacidad del evento
	 * @param eventOldPassword
	 *            antigua contraseña del evento
	 * @param eventNewPassword
	 *            nueva contraseña del evento
	 * @param imageFile
	 *            archivo de imagen del evento
	 * @return true si se ha actualizado con éxito, y false en caso contrario
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static boolean editEvent(String eventID, String eventName,
			String competitorID, String eventPlace, String eventDescription,
			String eventDate, String privacity, String eventOldPassword,
			String eventNewPassword, File imageFile)
			throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(BASE_URL + "event/edit");
		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("event_id", new StringBody(eventID));
		reqEntity.addPart("event_name", new StringBody(eventName));
		reqEntity.addPart("competitor_id", new StringBody(competitorID));
		reqEntity.addPart("event_place", new StringBody(eventPlace));
		reqEntity
				.addPart("event_description", new StringBody(eventDescription));
		reqEntity.addPart("event_old_password",
				new StringBody(eventOldPassword));
		reqEntity.addPart("event_privacity", new StringBody(privacity));
		httppost.setEntity(reqEntity);

		if (imageFile != null)
			reqEntity.addPart("event_image", new FileBody(imageFile, "image"));

		HttpResponse response = httpclient.execute(httppost);
		HttpEntity resEntity = response.getEntity();
		if (resEntity != null) {
			String responseToString = EntityUtils.toString(resEntity);
			Log.i("RESPONSE", responseToString);
			boolean responseBoolean = Boolean.getBoolean(responseToString);
			return responseBoolean;
		}
		return false;
	}

	/**
	 * COmprueba si existe el par competidor-evento
	 * 
	 * @param compeditorID
	 * @param eventID
	 * @return true si existe el par competidor-evento, y false en caso
	 *         contrario
	 * @throws IOException
	 */
	public static boolean checkEventParticipation(int competitorID, int eventID)
			throws IOException {
		String URLstring = BASE_URL + "competitor/check_event_participation/"
				+ competitorID + "/" + eventID;
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(URLstring);
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity resEntity = response.getEntity();
		if (resEntity != null) {
			String responseToString = EntityUtils.toString(resEntity);
			Log.i("RESPONSE", responseToString);
			boolean responseBoolean = Boolean.valueOf(responseToString);
			return responseBoolean;
		}
		return false;
	}

	/**
	 * Cambia el estado de participación de un evento
	 * 
	 * @param competitorID
	 * @param eventID
	 * @return true si se ha realizado con éxto la operación
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static Boolean changeEventParticipation(int competitorID,
			int eventID, boolean currentStatus) throws ClientProtocolException,
			IOException {
		String URLstring;
		if (currentStatus)
			URLstring = BASE_URL + "competitor/leave_event/" + competitorID
					+ "/" + eventID;
		else
			URLstring = BASE_URL + "competitor/add_event/" + competitorID + "/"
					+ eventID;
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(URLstring);
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity resEntity = response.getEntity();
		if (resEntity != null) {
			String responseToString = EntityUtils.toString(resEntity);
			Log.i("RESPONSE", responseToString);
			boolean responseBoolean = Boolean.valueOf(responseToString);
			return responseBoolean;
		}
		return false;
	}

	/**
	 * Comprueba si el evento tiene alguna entrada
	 * 
	 * @param eventID
	 *            id del evento
	 * @return true si el evento tiene alguna entrada, false en caso contrario
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Boolean checkEventBegun(int eventID)
			throws ClientProtocolException, IOException {
		String URLstring = BASE_URL + "event/check_event_avaliability/"
				+ eventID;
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(URLstring);
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity resEntity = response.getEntity();
		if (resEntity != null) {
			String responseToString = EntityUtils.toString(resEntity);
			Log.i("RESPONSE", responseToString);
			boolean responseBoolean = Boolean.valueOf(responseToString);
			return responseBoolean;
		}
		return false;
	}

	/**
	 * Envía las coordenadas de un par competidor-evento al servidor según la
	 * hora del sistema
	 * 
	 * @param eventID
	 *            id del evento
	 * @param competitorID
	 *            id del competidor
	 * @param password
	 *            password del competidor
	 * @param coordinates
	 *            coordenadas del competidor
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static boolean sendCoordinates(int eventID, int competitorID,
			String password, LatLng coordinates)
			throws ClientProtocolException, IOException {
		password = password == null ? "" : password;
		String url = BASE_URL + "coordinates/send/" + competitorID + "/"
				+ password + "/" + eventID + "/" + coordinates.latitude + "/"
				+ coordinates.longitude;
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity resEntity = response.getEntity();
		if (resEntity != null) {
			String responseToString = EntityUtils.toString(resEntity);
			Log.i("RESPONSE", responseToString);
			boolean responseBoolean = Boolean.valueOf(responseToString);
			return responseBoolean;
		}
		return false;
	}

	/**
	 * Obtiene los datos de un competidor
	 * 
	 * @param competitorID
	 *            id del competidor
	 * @return los datos del competidor
	 * @throws JSONException
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	public static Competitor getCompetitorData(int competitorID)
			throws IllegalStateException, IOException, JSONException {
		String url = BASE_URL + "competitor/get/" + competitorID;
		JSONArray jsonArray = getJSONArray(url);
		if (jsonArray.length() > 0) {
			JSONObject object = jsonArray.getJSONObject(0);
			Integer id = object.getInt("competitor_id");
			String name = object.getString("competitor_name");
			String surname = object.getString("competitor_surname");
			String email = object.getString("competitor_email");
			return new Competitor(name, surname, id, email, null);
		}
		return null;
	}

	/**
	 * Obtiene todos los datos de los competidores que tienen al menos una marca
	 * publicada en el evento
	 * 
	 * @param eventID
	 *            id del evento
	 * @return una lista con los datos de los competidores
	 * @throws JSONException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public static ArrayList<Competitor> getCompetitorsInEvent(int eventID)
			throws JSONException, IllegalStateException, IOException {
		String url = BASE_URL + "event/get_competitors_in_event/" + eventID;
		JSONArray jsonArray = getJSONArray(url);
		ArrayList<Competitor> competitors = new ArrayList<Competitor>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject object = jsonArray.getJSONObject(i);
			Integer id = object.getInt("competitor_id");
			String name = object.getString("competitor_name");
			String surname = object.getString("competitor_surname");
			String email = object.getString("competitor_email");
			competitors.add(new Competitor(name, surname, id, email, null));
		}
		return competitors;
	}

	/**
	 * Obtiene todos los datos referentes a las marcas de todos los corredores
	 * participantes en un evento
	 * 
	 * @param eventID
	 *            id del evento
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static void getCompetitorsRoutesForEvent(int eventID,
			CompetitorTracking[] competitorData) throws IllegalStateException,
			IOException, JSONException {
		String url = BASE_URL + "coordinates/get_all_marks/" + eventID;
		JSONArray jsonarray = getJSONArray(url);
		if (jsonarray != null && jsonarray.length() > 0
				&& competitorData != null && competitorData.length > 0) {
			// vamos a usar un mapa, es más rápido encontrar a los competidores
			// por su ID, y por el JSON que recibimos nos será más cómodo
			HashMap<Integer, CompetitorTracking> map = new HashMap<Integer, CompetitorTracking>(
					competitorData.length);
			// metemos los datos en el mapa para que el acceso sea más rápido
			for (CompetitorTracking c : competitorData) {
				// ya de paso limpiamos las coordenadas
				c.clearAllCoordinates();
				map.put(c.getCompetitor().getId(), c);
			}
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject o = jsonarray.getJSONObject(i);
				int id = o.getInt("competitor_id");
				CompetitorTracking ct = map.get(id);
				if (ct != null) {
					double latitude = o.getDouble("mark_latitude");
					double longitude = o.getDouble("mark_longitude");
					// añadimos la coordenada
					ct.addCoordinate(new LatLng(latitude, longitude));
				}
			}
		}
	}
}
