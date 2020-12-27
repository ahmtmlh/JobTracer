package edu.deu.resumeie.service.model.dto;

import javax.validation.constraints.NotNull;
import java.util.List;

public class PersonalInformationDTO {

    @NotNull GenderModel gender;
    String militaryServiceStatus;
    List<String> driverLicence;

    public PersonalInformationDTO() {
    }

    public PersonalInformationDTO(GenderModel gender, String militaryServiceStatus, List<String> driverLicence) {
        this.gender = gender;
        this.militaryServiceStatus = militaryServiceStatus;
        this.driverLicence = driverLicence;
    }

    public GenderModel getGender() {
        return gender;
    }

    public void setGender(GenderModel gender) {
        this.gender = gender;
    }

    public String getMilitaryServiceStatus() {
        return militaryServiceStatus;
    }

    public void setMilitaryServiceStatus(String militaryServiceStatus) {
        this.militaryServiceStatus = militaryServiceStatus;
    }

    public List<String> getDriverLicence() {
        return driverLicence;
    }

    public void setDriverLicence(List<String> driverLicence) {
        this.driverLicence = driverLicence;
    }

    @Override
    public String toString() {
        return "PersonalInformationDTO{" +
                "gender=" + gender +
                ", militaryServiceStatus='" + militaryServiceStatus + '\'' +
                ", driverLicence=" + driverLicence +
                '}';
    }
}
