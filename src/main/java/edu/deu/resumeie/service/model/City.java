package edu.deu.resumeie.service.model;


public class City {
	private String cityName;
	private String zipCode;
	
	
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
