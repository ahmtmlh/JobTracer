package edu.deu.resumeie.service.model;

import java.util.List;
import static edu.deu.resumeie.shared.SharedObjects.educationStatusValues;

public class CV {


    private String name;
    private String surname;

    private String profession;
    private int educationStatus;
    private String desiredCities;
    private int experience;
    private List<String> qualificationList;

    public CV(String name, String surname, String profession, int educationStatus, String desiredCities, int experience, List<String> qualificationList) {
        this.name = name;
        this.surname = surname;
        this.profession = profession;
        this.educationStatus = educationStatus;
        this.desiredCities = desiredCities;
        this.experience = experience;
        this.qualificationList = qualificationList;
    }

    public CV(String name, String surname, String profession, String educationStatus, String desiredCities, int experience, List<String> qualificationList) {
        this(name, surname, profession, educationStatusValues.get(educationStatus.toLowerCase()), desiredCities, experience, qualificationList);
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

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public int getEducationStatus() {
        return educationStatus;
    }

    public void setEducationStatus(int educationStatus) {
        this.educationStatus = educationStatus;
    }

    public String getDesiredCities() {
        return desiredCities;
    }

    public void setDesiredCities(String desiredCities) {
        this.desiredCities = desiredCities;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public List<String> getQualificationList() {
        return qualificationList;
    }

    public void setQualificationList(List<String> qualificationList) {
        this.qualificationList = qualificationList;
    }
}
