package com.cervantesvirtual.rdf.rda2res.model;

public class Person {

	private String personId;
	private String domain;
	
	public static String entity = "person";
	
	public Person(String personId, String domain) {
		super();
		this.personId = personId;
		this.domain = domain;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getPersonUri() {
		return domain + entity + "/" + personId;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
