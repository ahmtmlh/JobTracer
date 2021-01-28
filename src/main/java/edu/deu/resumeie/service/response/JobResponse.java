package edu.deu.resumeie.service.response;

import edu.deu.resumeie.service.model.Job;

import java.util.ArrayList;
import java.util.List;

public class JobResponse {

    public static final List<Job> EMPTY_LIST = new ArrayList<>();

    private Integer listId;
    private Integer totalPages;
    private List<Job> jobs;

    public JobResponse() { }

    public JobResponse(Integer listId, List<Job> jobs) {
        this.listId = listId;
        this.jobs = jobs;
    }

    public JobResponse(Integer listId, Integer totalPages, List<Job> jobs) {
        this(listId, jobs);
        this.totalPages = totalPages;
    }

    public static JobResponse emptyResponse(int listId){
        return new JobResponse(listId, EMPTY_LIST);
    }

    public Integer getListId() {
        return listId;
    }

    public void setListId(Integer listId) {
        this.listId = listId;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
