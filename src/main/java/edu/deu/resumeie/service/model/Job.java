package edu.deu.resumeie.service.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Job implements Comparable<Job>{

    private String id;
    private int experience;
    private int maxExperience;
    private String position;
    private List<String> cities;
    private int educationStatus;
    private String clusters;
    private String text;
    private String htmlText;

    public Job(String id, int experience, int maxExperience,String position, String cities, int educationStatus, String clusters, String text, String htmlText){
        this(id, experience, maxExperience, position, cities, educationStatus, clusters);
        this.text = text;
        this.htmlText = htmlText;
    }

    public Job(String id, int experience, int maxExperience,String position, String cities, int educationStatus, String clusters){
        this.id = id;
        this.experience = experience;
        this.maxExperience = maxExperience;
        this.position = position;
        this.cities = new ArrayList<>();
        this.cities.addAll(Arrays.asList(cities.split(",")));
        this.educationStatus = educationStatus;
        this.clusters = clusters;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getMaxExperience() {
        return maxExperience;
    }

    public void setMaxExperience(int maxExperience) {
        this.maxExperience = maxExperience;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<String> getCities() {
        return cities;
    }

    public void addCity(String city){
        this.cities.add(city);
    }

    public void addCities(List<String> cities){
        this.cities.addAll(cities);
    }

    public int getEducationStatus() {
        return educationStatus;
    }

    public void setEducationStatus(int educationStatus) {
        this.educationStatus = educationStatus;
    }

    public String getClusters() {
        return clusters;
    }

    public void setClusters(String clusters) {
        this.clusters = clusters;
    }

    public String getText(){
        return text;
    }

    public void setText(String text){
        this.text = text;
    }

    public String getHtmlText() {
        return htmlText;
    }

    public void setHtmlText(String htmlText) {
        this.htmlText = htmlText;
    }

    @Override
    public String toString() {

        String ret = "Job ID      : " + this.id +
                " Job Exp     : " + this.experience +
                " Job Pos     : " + this.position +
                " Job City    : " + this.cities +
                " Job Clusters: " + this.clusters;

        if (this.text != null && !this.text.isEmpty()){
            ret += "\nJob Text   : " + this.text;
        }

        return ret;
    }

    @Override
    public int compareTo(Job o) {
        return this.getClusters().compareTo(o.getClusters());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof Job)
            return this.id.equals(((Job)obj).id);
        return false;
    }
}
