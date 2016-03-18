package com.molina.offlinemaps;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Esta clase encapsula la información referente a un conjunto de tiles
 * consecutivos, independientemente el zoom.
 * 
 * @author Molina
 * 
 */
public class TileAreaData implements Parcelable {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 962002737710677757L;

	private ArrayList<Integer> zooms;
	private LatLng fromLatLng;
	private LatLng toLatLng;
	public static int MAX_ZOOM = 16;
	public static int MIN_ZOOM = 0;

	/**
	 * Constructor de la clase. Establece el area a procesar mediante dos puntos
	 * en el mapa
	 * 
	 * @param fromLatLng
	 *            punto origen
	 * @param toLatLng
	 *            punto final
	 */
	public TileAreaData(LatLng fromLatLng, LatLng toLatLng) {
		super();
		this.fromLatLng = fromLatLng;
		this.toLatLng = toLatLng;
		zooms = new ArrayList<Integer>();
		for (int i = 0; i < zooms.size(); i++) {
			int zoom = zooms.get(i);
			if (zoom > MAX_ZOOM || zoom < MIN_ZOOM)
				zooms.remove(i);
		}
	}

	/**
	 * Constructor necesario para aplicar la interfaz Parcelable
	 * 
	 * @param in
	 *            Parcel desde el que se obtienen los datos
	 */
	public TileAreaData(Parcel in) {
		zooms = new ArrayList<Integer>();
		int size = in.readInt();
		for (int i = 0; i < size; i++)
			zooms.add(in.readInt());
		double lat = in.readDouble();
		double lng = in.readDouble();
		fromLatLng = new LatLng(lat, lng);
		lat = in.readDouble();
		lng = in.readDouble();
		toLatLng = new LatLng(lat, lng);
	}

	/**
	 * Constructor de la clase. Establece el area a procesar mediante dos puntos
	 * en el mapa
	 * 
	 * @param zooms
	 *            conjunto de zooms a procesar
	 * @param fromLatLng
	 *            punto origen
	 * @param toLatLng
	 *            punto final
	 */
	public TileAreaData(ArrayList<Integer> zooms, LatLng fromLatLng,
			LatLng toLatLng) {
		super();
		if (zooms != null)
			this.zooms = zooms;
		else
			this.zooms = new ArrayList<Integer>();
		this.fromLatLng = fromLatLng;
		this.toLatLng = toLatLng;
	}

	/**
	 * Obtiene el tile al que corresponden las coordenadas datas para el zooms
	 * dado
	 * 
	 * @param lat
	 *            latitud del punto
	 * @param lon
	 *            longitud del punto
	 * @param zoom
	 *            zoom a procesar
	 * @return el tile correspondiente a las coordenadas latitud/longitud
	 *         aportadas
	 */
	public static TileIdentifier getTileIdentifier(double lat, double lon,
			int zoom) {
		int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
		int ytile = (int) Math
				.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1
						/ Math.cos(Math.toRadians(lat)))
						/ Math.PI)
						/ 2 * (1 << zoom));
		return new TileIdentifier(zoom, ytile, xtile);
	}

	/**
	 * Obtiene el tile que engloba al punto original
	 * 
	 * @param zoom
	 *            zoom sobre el que se va a trabajar
	 * @return el tile origen que corresponde a los valores de latitud, longitud
	 *         y zoom aportados
	 */
	public TileIdentifier getOrigin(Integer zoom) {
		if (zooms.contains(zoom))
			return getTileIdentifier(fromLatLng.latitude, fromLatLng.longitude,
					zoom);
		return null;
	}

	/**
	 * Obtiene el tile que engloba al punto final
	 * 
	 * @param zoom
	 *            zoom sobre el que se va a trabajar
	 * @return el tile final que corresponde a los valores de latitud, longitud
	 *         y zoom aportados
	 */
	public TileIdentifier getEnd(Integer zoom) {
		if (zooms.contains(zoom))
			return getTileIdentifier(toLatLng.latitude, toLatLng.longitude,
					zoom);
		return null;
	}

	/**
	 * Obtiene todos los tiles englobados en el área formada por dos tiles
	 * esquina.
	 * 
	 * @param origin
	 *            tile orgien
	 * @param end
	 *            tilen final
	 * @return todos los tiles que se encuentran en el área formadas por los dos
	 *         tiles procesados, o null si tienen distinto zoom
	 */
	public static ArrayList<TileIdentifier> getContainedTiles(
			TileIdentifier origin, TileIdentifier end) {
		if (origin == null || end == null || origin.getZoom() != end.getZoom())
			return null;
		ArrayList<TileIdentifier> list = new ArrayList<TileIdentifier>();
		int zoom = origin.getZoom();
		int minRow = Math.min(origin.getRow(), end.getRow());
		int maxRow = Math.max(origin.getRow(), end.getRow());
		int minColumn = Math.min(origin.getColumn(), end.getColumn());
		int maxColumn = Math.max(origin.getColumn(), end.getColumn());

		for (int column = minColumn; column <= maxColumn; column++)
			for (int row = minRow; row <= maxRow; row++) {
				TileIdentifier toAdd = new TileIdentifier(zoom, row, column);
				list.add(toAdd);
			}
		return list;
	}

	/**
	 * Añade un zoom a la lista de zooms a procesar
	 * 
	 * @param zoom
	 *            zoom que se tienen que añadir
	 * @return true si se ha añadido exitosamente, y false en caso contrario
	 */
	public boolean addZoom(Integer zoom) {
		if (!zooms.contains(zoom) && zoom >= MIN_ZOOM && zoom <= MAX_ZOOM) {
			zooms.add(zoom);
			return true;
		}
		return false;
	}

	/**
	 * Obtiene todos los zooms que se están manejando
	 * 
	 * @return los zooms que contiene este TileAreaData
	 */
	public ArrayList<Integer> getZooms() {
		return zooms;
	}

	/**
	 * Obtiene el punto origen
	 * 
	 * @return punto origen
	 */
	public LatLng getFromLatLng() {
		return fromLatLng;
	}

	/**
	 * Obtiene el punto final
	 * 
	 * @return punto final
	 */
	public LatLng getToLatLng() {
		return toLatLng;
	}

	/**
	 * Establece el punto origen
	 * 
	 * @param fromLatLng
	 *            nuevo punto origen
	 */
	public void setFromLatLng(LatLng fromLatLng) {
		this.fromLatLng = fromLatLng;
	}

	/**
	 * Establece el punto final
	 * 
	 * @param toLatLng
	 *            nuevo punto final
	 */
	public void setToLatLng(LatLng toLatLng) {
		this.toLatLng = toLatLng;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(zooms.size());
		for (int i : zooms)
			out.writeInt(i);
		out.writeDouble(fromLatLng.latitude);
		out.writeDouble(fromLatLng.longitude);
		out.writeDouble(toLatLng.latitude);
		out.writeDouble(toLatLng.longitude);
	}

	public static final Parcelable.Creator<TileAreaData> CREATOR = new Creator<TileAreaData>() {

		@Override
		public TileAreaData createFromParcel(Parcel source) {
			return new TileAreaData(source);
		}

		@Override
		public TileAreaData[] newArray(int size) {
			return new TileAreaData[size];
		}

	};

	/**
	 * Obtiene todos los tiles englobados en el TileAreaData para un determinado
	 * zoom
	 * 
	 * @param zoom
	 *            zoom a procesar para determinar los tiles
	 * @return lista con los tiles englobados entre los dos puntos originales y
	 *         el zoom dado
	 */
	public ArrayList<TileIdentifier> getContainedTiles(Integer zoom) {
		return TileAreaData.getContainedTiles(getOrigin(zoom), getEnd(zoom));
	}

}
