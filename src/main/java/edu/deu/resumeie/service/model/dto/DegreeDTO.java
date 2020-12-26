package edu.deu.resumeie.service.model.dto;

public class DegreeDTO {

    int id;
    String name;

    public DegreeDTO() {
    }

    public DegreeDTO(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
