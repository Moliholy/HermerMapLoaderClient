package com.molina.offlinemaps;

/**
 * Esta clase encapsula el modelo de "Tile" o trozo de mapa
 * 
 * @author Molina
 * 
 */
public class TileIdentifier {
	private int zoom;
	private int row;
	private int column;

	/**
	 * Obtiene el zoom del tile
	 * 
	 * @return zoom del tile
	 */
	public int getZoom() {
		return zoom;
	}

	/**
	 * Establece el zoom para este tile
	 * 
	 * @param zoom
	 *            zoom para este tile
	 */
	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	/**
	 * Obtiene la fila de este tile
	 * 
	 * @return fila de este tile
	 */
	public int getRow() {
		return row;
	}

	/**
	 * Establece la fila de este tile
	 * 
	 * @param row
	 *            la nueva fila
	 */
	public void setRow(int row) {
		this.row = row;
	}

	/**
	 * Obtiene la columna de este tile
	 * 
	 * @return la columna de este tile
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * Establece la columna de este tile
	 * 
	 * @param column
	 *            la nueva columna
	 */
	public void setColumn(int column) {
		this.column = column;
	}

	/**
	 * Constructor de la clase
	 * 
	 * @param zoom
	 *            zoom que va a tener el tile
	 * @param row
	 *            fila del tile
	 * @param column
	 *            columna del tile
	 */
	public TileIdentifier(int zoom, int row, int column) {
		super();
		this.zoom = zoom;
		this.row = row;
		this.column = column;
	}

	/**
	 * Obtiene una representación en la forma "zoom/fila/columna"
	 */
	@Override
	public String toString() {
		return Integer.toString(zoom) + "/" + Integer.toString(column) + "/"
				+ row;
	}

	/**
	 * Establece si dos TileIdentifier son iguales
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof TileIdentifier) {
			TileIdentifier other = (TileIdentifier) o;
			return this.zoom == other.zoom && this.column == other.column
					&& this.row == other.row;
		}
		return super.equals(o);
	}
}
