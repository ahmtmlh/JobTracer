package edu.deu.resumeie.service.model.dto;

public class FacultyDTO {

    private int id;
    private int uniId;
    private String name;

    public FacultyDTO() {
    }

    public FacultyDTO(int id, int uniId, String name) {
        this.id = id;
        this.uniId = uniId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUniId() {
        return uniId;
    }

    public void setUniId(int uniId) {
        this.uniId = uniId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
