package edu.deu.resumeie.service.model.dto;

public class DepartmentDTO {

    private int id;
    private int facId;
    private String name;

    public DepartmentDTO() {
    }

    public DepartmentDTO(int id, int facId, String name) {
        this.id = id;
        this.facId = facId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFacId() {
        return facId;
    }

    public void setFacId(int facId) {
        this.facId = facId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
