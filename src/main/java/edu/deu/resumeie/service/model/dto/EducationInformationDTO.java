package edu.deu.resumeie.service.model.dto;

public class EducationInformationDTO {

    String degree;
    String graduationYear;
    UniversityDTO university;
    FacultyDTO faculty;
    DepartmentDTO department;

    public EducationInformationDTO() {
    }

    public EducationInformationDTO(String degree, String graduationYear, UniversityDTO university, FacultyDTO faculty, DepartmentDTO department) {
        this.degree = degree;
        this.graduationYear = graduationYear;
        this.university = university;
        this.faculty = faculty;
        this.department = department;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(String graduationYear) {
        this.graduationYear = graduationYear;
    }

    public UniversityDTO getUniversity() {
        return university;
    }

    public void setUniversity(UniversityDTO university) {
        this.university = university;
    }

    public FacultyDTO getFaculty() {
        return faculty;
    }

    public void setFaculty(FacultyDTO faculty) {
        this.faculty = faculty;
    }

    public DepartmentDTO getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentDTO department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "EducationInformationDTO{" +
                "degree=" + degree +
                ", graduationYear='" + graduationYear + '\'' +
                ", university=" + university +
                ", faculty=" + faculty +
                ", department=" + department +
                '}';
    }
}
