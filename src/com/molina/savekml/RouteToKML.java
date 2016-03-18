package com.molina.savekml;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.os.Environment;

import com.google.android.gms.maps.model.LatLng;

/**
 * Esta clase contiene métodos estáticos para salvar una ruta seleccionada por
 * el usuario en un archivo kml
 * 
 * @author Molina
 * 
 */
public class RouteToKML {
	private static final String PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/Routes";
	public static final String KML_EXTENSION = ".kml";

	/**
	 * Salva una ruta en un archivo
	 * 
	 * @param points
	 *            puntos que conforman la ruta
	 * @param filename
	 *            nombre del fichero donde se va a almacenar la ruta en formato
	 *            kml
	 */
	public static void saveRouteInKML(List<LatLng> points, String filename) {
		try {
			File mainDirectory = new File(PATH);
			if (!mainDirectory.exists() || !mainDirectory.isDirectory())
				mainDirectory.mkdir();

			// creamos el documento
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// kml
			Element kml = doc.createElement("kml");
			Attr attr = doc.createAttribute("xmlns");
			attr.setValue("http://earth.google.com/kml/2.2");
			kml.setAttributeNode(attr);
			doc.appendChild(kml);

			// Document
			Element document = doc.createElement("Document");
			kml.appendChild(document);

			// name
			Element name = doc.createElement("name");
			name.appendChild(doc.createTextNode(filename));
			document.appendChild(name);

			// visibility
			Element visivility = doc.createElement("visibility");
			visivility.appendChild(doc.createTextNode("1"));
			document.appendChild(visivility);

			// open
			Element open = doc.createElement("open");
			open.appendChild(doc.createTextNode("1"));
			document.appendChild(open);

			// Folder
			Element folder = doc.createElement("Folder");
			document.appendChild(folder);

			// Folder.name
			Element folderName = doc.createElement("name");
			folderName.appendChild(doc.createTextNode("Tracks"));
			folder.appendChild(folderName);

			// Folder.visibility
			Element folderVisibility = doc.createElement("visibility");
			folderVisibility.appendChild(doc.createTextNode("1"));
			folder.appendChild(folderVisibility);

			// Folder.open
			Element folderOpen = doc.createElement("open");
			folderOpen.appendChild(doc.createTextNode("1"));
			folder.appendChild(folderOpen);

			// Folder.Placemark
			Element placemark = doc.createElement("Placemark");
			folder.appendChild(placemark);

			// Folder.Placemark.MultiGeometry
			Element multiGeometry = doc.createElement("MultiGeometry");
			placemark.appendChild(multiGeometry);

			// Folder.Placemark.Multigeometry.LineString
			Element lineString = doc.createElement("LineString");
			multiGeometry.appendChild(lineString);

			// Folder.Placemark.Multigeometry.LineString.tessellate
			Element tesselate = doc.createElement("tessellate");
			tesselate.appendChild(doc.createTextNode("1"));
			lineString.appendChild(tesselate);

			// Folder.Placemark.Multigeometry.LineString.altitudeMode
			Element altitudeMode = doc.createElement("altitudeMode");
			altitudeMode.appendChild(doc.createTextNode("clampToGround"));
			lineString.appendChild(altitudeMode);

			// Folder.Placemark.Multigeometry.LineString.coordinates
			Element coordinates = doc.createElement("coordinates");
			// aquí empieza lo bueno. Parseamos las coordenadas de la lista
			// convenientemente para meterlas en el fichero kml
			StringBuilder coord = new StringBuilder();

			for (LatLng latlng : points) {
				coord.append(Double.toString(latlng.longitude) + ",").append(
						Double.toString(latlng.latitude) + ",0 ");
			}

			coordinates.appendChild(doc.createTextNode(coord.toString()));
			lineString.appendChild(coordinates);

			// escribimos definitivamente el xml
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(PATH + "/"
					+ filename));

			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
}
