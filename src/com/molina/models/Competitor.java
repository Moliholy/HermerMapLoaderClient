package com.molina.models;

/**
 * Esta clase encapsula el modelo de datos de un Competidor
 * 
 * @author Molina
 * 
 */
public class Competitor {
	private String name;
	private String surname;
	private int id;
	private String email;
	private String password;

	public static final String COMPETITOR_NAME = "competitor_name";
	public static final String COMPETITOR_SURNAME = "competitor_surname";
	public static final String COMPETITOR_ID = "competitor_id";
	public static final String COMPETITOR_EMAIL = "competitor_email";
	public static final String COMPETITOR_PASSWORD = "competitor_password";

	public boolean isValid() {
		return id >= 0;
	}

	public Competitor(String name, String surname, int id, String email,
			String password) {
		super();
		this.name = name;
		this.surname = surname;
		this.id = id;
		this.email = email;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
