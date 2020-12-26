package edu.deu.resumeie.service.model.dto;

import java.util.List;

public class WorkExperienceDTO {

    String profession;
    List<String> experiences;

    public WorkExperienceDTO() {
    }

    public WorkExperienceDTO(String profession, List<String> experiences) {
        this.profession = profession;
        this.experiences = experiences;
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

    @Override
    public String toString() {
        return "WorkExperienceDTO{" +
                "profession='" + profession + '\'' +
                ", experiences=" + experiences +
                '}';
    }
}
