package com.molina.route;

import java.io.File;
import java.util.List;

import com.molina.parsekml.Placemark;

/**
 * Interfaz usada para aquellos mapas que admiten el dibujado de rutas y su
 * procesamiento desde archivo
 * 
 * @author Molina
 * 
 */
public interface RouteDisplayable {
	void loadRoute(File file);

	void drawRoutePoints(List<Placemark> placemarkList);
}
