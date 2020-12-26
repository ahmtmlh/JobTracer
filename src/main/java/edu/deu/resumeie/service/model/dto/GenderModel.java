package edu.deu.resumeie.service.model.dto;

public class GenderModel {

    String name;
    int type;

    public GenderModel() {
    }

    public GenderModel(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
