package edu.deu.resumeie.service.model.dto;

import edu.deu.resumeie.service.model.City;

import java.util.List;

public class ContactInformationDTO {

    String emailAddress;
    String dateOfBirth;
    List<City> city;

    public ContactInformationDTO() {
    }

    public ContactInformationDTO(String emailAddress, String dateOfBirth, List<City> city) {
        this.emailAddress = emailAddress;
        this.dateOfBirth = dateOfBirth;
        this.city = city;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<City> getCity() {
        return city;
    }

    public void setCity(List<City> city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "ContactInformationDTO{" +
                "emailAddress='" + emailAddress + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", city=" + city +
                '}';
    }
}
