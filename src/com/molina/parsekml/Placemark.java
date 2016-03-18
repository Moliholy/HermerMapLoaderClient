package com.molina.parsekml;

import java.util.LinkedList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

/**
 * Esta clase encapsula los datos obtenido al parsear un xml con los datos
 * referentes a una localización, una ruta, y sus elementos asociados que los
 * describen
 * 
 * @author Molina
 * 
 */
public class Placemark {
	private String title;
	private String description;
	private String coordinates;
	private String address;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Obtiene las coordenadas de la ruta en forma latitud/longitud
	 * 
	 * @return coordenadas almacenadas en este Placemark
	 * @throws NumberFormatException
	 *             si las coordenadas no están correctamente escritas en el
	 *             fichero del que se han tomado los datos
	 */
	public List<LatLng> getGeoCoordinates() throws NumberFormatException {
		if (coordinates == null || coordinates.isEmpty())
			return null;
		List<LatLng> toReturn = new LinkedList<LatLng>();
		// obtenemos los "packs" de coordenadas
		String[] coord = coordinates.split(" ");
		for (String numbers : coord) {
			// las separamos una a una
			if (numbers.length() >= 2) {
				String[] number = numbers.split(",");
				double longitude = Double.parseDouble(number[0]);
				double latitude = Double.parseDouble(number[1]);
				// nos olvidamos de la coordenada Z
				LatLng latlong = new LatLng(latitude, longitude);
				toReturn.add(latlong);
			}
		}
		return toReturn;
	}
}
