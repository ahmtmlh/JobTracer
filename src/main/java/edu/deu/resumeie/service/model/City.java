package edu.deu.resumeie.service.model;


public class City {

	public static final City ALL_CITIES = new City("*", "0");

	private String cityName;
	private String zipCode;

	public City() {
	}

	public City(String cityName, String zipCode) {
		this.cityName = cityName;
		this.zipCode = zipCode;
	}
	
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	
	

}
