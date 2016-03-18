package com.molina.models;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Esta clase encapsula el modelo de datos de un Evento
 * 
 * @author Molina
 * 
 */
public class Event {
	public static final String ID = "event_id";
	public static final String COMPETITOR_ID = "competitor_id";
	public static final String PASSWORD = "event_password";
	public static final String NAME = "event_name";
	public static final String PLACE = "event_place";
	public static final String DATE = "event_begin_date";
	public static final String PRIVACITY = "event_privacity";
	public static final String DESCRIPTION = "event_description";

	private int competitor_id;
	private int id;
	private String password;
	private String name;
	private Date begin_date;
	private String place;
	private Integer privacity;
	private String description;

	public Event(int competitor_id, int id, String password, String name,
			Date begin_date, String place, Integer privacity, String description) {
		this.competitor_id = competitor_id;
		this.id = id;
		this.password = password;
		this.name = name;
		this.begin_date = begin_date;
		this.place = place;
		this.privacity = privacity;
		this.description = description;
	}

	public Event() {
		id = -1;
	}

	public int getCompetitor_id() {
		return competitor_id;
	}

	public void setCompetitor_id(int competitor_id) {
		this.competitor_id = competitor_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getBegin_date() {
		return begin_date;
	}

	public void setBegin_date(Timestamp begin_date) {
		this.begin_date = begin_date;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	/**
	 * 0: totalmente privado, se requiere invitación. No aparecerá en ningún
	 * sitio público 1: aparecerá sólo para los seguidores 2: público,
	 * cualquiera podrá verlo
	 * 
	 * @return la privacidad del evento
	 */
	public Integer getPrivacity() {
		return privacity;
	}

	public void setIsPrivate(Integer privacity) {
		this.privacity = privacity;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
