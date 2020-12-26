package edu.deu.resumeie.service.model.dto;

import java.util.List;

public class WorkExperienceDTO {

    String profession;
    List<String> experiences;
    int totalExperience;

    public WorkExperienceDTO() {
    }

    public WorkExperienceDTO(String profession, List<String> experiences, int totalExperience) {
        this.profession = profession;
        this.experiences = experiences;
        this.totalExperience = totalExperience;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public List<String> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<String> experiences) {
        this.experiences = experiences;
    }

    public int getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
    }

    @Override
    public String toString() {
        return "WorkExperienceDTO{" +
                "profession='" + profession + '\'' +
                ", experiences=" + experiences +
                '}';
    }
}
