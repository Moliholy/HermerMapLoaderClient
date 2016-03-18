package com.molina.models;

import java.util.LinkedList;
import java.util.Random;

import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

/**
 * Esta clase de modelo se utiliza para mantener todos los elementos necesarios
 * para el seguimiento de un competidor en el mapa del evento
 * 
 * @author Molina
 * 
 */
public class CompetitorTracking {
	private Competitor competitor;
	private ImageView image;
	private int color;
	private LinkedList<LatLng> positions;

	public LinkedList<LatLng> getPositions() {
		return positions;
	}

	public CompetitorTracking() {
		super();
		this.color = randomColor();
		positions = new LinkedList<LatLng>();
	}

	private int randomColor() {
		Random random = new Random();
		return random.nextInt() | 0xff000000;
	}

	public CompetitorTracking(Competitor competitor) {
		super();
		this.competitor = competitor;
		this.color = randomColor();
		positions = new LinkedList<LatLng>();
	}

	public CompetitorTracking(Competitor competitor, ImageView image) {
		super();
		this.competitor = competitor;
		this.image = image;
		this.color = randomColor();
		positions = new LinkedList<LatLng>();
	}

	public CompetitorTracking(Competitor competitor, ImageView image, int color) {
		super();
		this.competitor = competitor;
		this.image = image;
		this.color = color;
	}

	public Competitor getCompetitor() {
		return competitor;
	}

	public void setCompetitor(Competitor competitor) {
		this.competitor = competitor;
	}

	public ImageView getImage() {
		return image;
	}

	public void setImage(ImageView image) {
		this.image = image;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public LatLng getLastPosition() {
		if (positions.size() > 0) {
			return positions.getLast();
		}
		return null;
	}
	
	public void clearAllCoordinates(){
		if(positions!=null)
			positions.clear();
	}
	
	public void addCoordinate(LatLng coordinate){
		positions.add(coordinate);
	}
}
