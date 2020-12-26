package edu.deu.resumeie.service.model.dto;

import edu.deu.resumeie.service.model.CV;
import edu.deu.resumeie.shared.SharedObjects;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

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


    ////Transfer

    public CV createCV(){

        //CV cv = new CV(asd);

//        private String name;
//        private String surname;
//
//        private String profession;
//        private int educationStatus;
//        private final List<City> desiredCities;
//        private int experience;
//        private List<String> qualificationList;
        CV cv = new CV("Ali Ata",
                "Bak",
                this.workExperiences.profession,
                SharedObjects.educationStatusValues.get(this.educationInformation.degree),
                this.contactInformation.getCity(),
                2,
                createExperienceList());


        return cv;
    }

    private List<String> createExperienceList(){
        //default list
        List<String> list = this.getWorkExperiences().experiences;

        //add language information
        list.add(getLanguageStatus(this.foreignLanguageInformation));

        //add military information
        if(this.getPersonalInformation().gender.getType() == 1 && !this.getMilitaryServiceStatus().equals(""))
            list.add(getMilitaryServiceStatus());

        //add driving licence information
        list.addAll(driverLicences());

        return list;
    }

    private String getLanguageStatus(ForeignLanguageInformationDTO language){
        String languageInfo = "";
        int level = language.getLevel().getLevelType();

        switch (level) {
            case 0:
                languageInfo = "Başlangıç seviyesinde";
                break;
            case 1:
                languageInfo = "Orta seviye";
                break;
            case 2:
                languageInfo = "İleri seviyede";
                break;
            case 3:
                languageInfo = "Anadil seviyesinde";
                break;
        }

        languageInfo = languageInfo + " " + language.getName() +" bilen.";

        return languageInfo;
    }

    private String getMilitaryServiceStatus(){

        if(this.getMilitaryServiceStatus().equals("yaptım"))
            return "Askerliğimi yaptım.";
        if(this.getMilitaryServiceStatus().equals("yapmadım"))
            return "Askerliğimi yapmadım.";
        if(this.getMilitaryServiceStatus().equals("tecilli"))
            return "Askerliğim tecillenmiştir.";

        return "";
    }

    private List<String> driverLicences(){

        List<String> licences = new ArrayList<>();
        getPersonalInformation().getDriverLicence().forEach(item->licences.add(item + " seviye sürücü belgesine sahibim."));

        return licences;
    }

}
