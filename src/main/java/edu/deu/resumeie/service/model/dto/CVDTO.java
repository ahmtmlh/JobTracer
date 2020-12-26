package edu.deu.resumeie.service.model.dto;

import javax.validation.constraints.NotNull;

public class CVDTO {
    @NotNull ContactInformationDTO contactInformation;
    @NotNull PersonalInformationDTO personalInformation;
    @NotNull EducationInformationDTO educationInformation;
    @NotNull ForeignLanguageInformationDTO foreignLanguageInformation;
    @NotNull WorkExperienceDTO workExperiences;

    public CVDTO() {
    }

    public CVDTO(ContactInformationDTO contactInformation, PersonalInformationDTO personalInformation, EducationInformationDTO educationInformation, ForeignLanguageInformationDTO foreignLanguageInformation, WorkExperienceDTO workExperiences) {
        this.contactInformation = contactInformation;
        this.personalInformation = personalInformation;
        this.educationInformation = educationInformation;
        this.foreignLanguageInformation = foreignLanguageInformation;
        this.workExperiences = workExperiences;
    }

    public ContactInformationDTO getContactInformation() {
        return contactInformation;
    }

    public void setContactInformation(ContactInformationDTO contactInformation) {
        this.contactInformation = contactInformation;
    }

    public PersonalInformationDTO getPersonalInformation() {
        return personalInformation;
    }

    public void setPersonalInformation(PersonalInformationDTO personalInformation) {
        this.personalInformation = personalInformation;
    }

    public EducationInformationDTO getEducationInformation() {
        return educationInformation;
    }

    public void setEducationInformation(EducationInformationDTO educationInformation) {
        this.educationInformation = educationInformation;
    }

    public ForeignLanguageInformationDTO getForeignLanguageInformation() {
        return foreignLanguageInformation;
    }

    public void setForeignLanguageInformation(ForeignLanguageInformationDTO foreignLanguageInformation) {
        this.foreignLanguageInformation = foreignLanguageInformation;
    }

    public WorkExperienceDTO getWorkExperiences() {
        return workExperiences;
    }

    public void setWorkExperiences(WorkExperienceDTO workExperiences) {
        this.workExperiences = workExperiences;
    }

    @Override
    public String toString() {
        return "CVDTO{" +
                "contactInformation=" + contactInformation.toString() +
                ", personalInformation=" + personalInformation.toString() +
                ", educationInformation=" + educationInformation.toString() +
                ", foreignLanguageInformation=" + foreignLanguageInformation.toString() +
                ", workExperiences=" + workExperiences.toString() +
                '}';
    }
}
