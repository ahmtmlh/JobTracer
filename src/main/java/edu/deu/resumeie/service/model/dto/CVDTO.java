package edu.deu.resumeie.service.model.dto;

import edu.deu.resumeie.service.model.CV;
import edu.deu.resumeie.shared.SharedObjects;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class CVDTO {

    @NotNull private ContactInformationDTO contactInformation;
    @NotNull private PersonalInformationDTO personalInformation;
    @NotNull private EducationInformationDTO educationInformation;
    @NotNull private List<ForeignLanguageInformationDTO> foreignLanguageInformation;
    @NotNull private WorkExperienceDTO workExperiences;
    private String matchingPriority;

    public CVDTO() { }

    public CVDTO(ContactInformationDTO contactInformation, PersonalInformationDTO personalInformation, EducationInformationDTO educationInformation, List<ForeignLanguageInformationDTO> foreignLanguageInformation, WorkExperienceDTO workExperiences) {
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

    public List<ForeignLanguageInformationDTO> getForeignLanguageInformation() {
        return foreignLanguageInformation;
    }

    public void setForeignLanguageInformation(List<ForeignLanguageInformationDTO> foreignLanguageInformation) {
        this.foreignLanguageInformation = foreignLanguageInformation;
    }

    public WorkExperienceDTO getWorkExperiences() {
        return workExperiences;
    }

    public void setWorkExperiences(WorkExperienceDTO workExperiences) {
        this.workExperiences = workExperiences;
    }

    public String getMatchingPriority() {
        return matchingPriority;
    }

    public void setMatchingPriority(String matchingPriority) {
        this.matchingPriority = matchingPriority;
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

    public CV createCV(){
        return new CV("Ali Ata",
                "Bak",
                this.workExperiences.getProfession(),
                this.educationInformation.getDegree(),
                this.contactInformation.getCity(),
                this.workExperiences.getTotalExperience(),
                createExperienceList());
    }

    private List<String> createExperienceList(){
        // Default list
        List<String> list = new ArrayList<>(this.getWorkExperiences().getExperiences());

        // Add language information
        if (this.foreignLanguageInformation != null && !this.foreignLanguageInformation.isEmpty())
            this.foreignLanguageInformation.forEach(language -> list.add(getLanguageStatus(language)));

        // Add military information
        if(this.getPersonalInformation().gender.getType() == 1 &&
                this.personalInformation.getMilitaryServiceStatus().equalsIgnoreCase("yaptım"))
            list.add("Askerlik görevimi tamamladım.");

        // Add driving licence information
        if (getPersonalInformation().driverLicence != null)
            list.addAll(driverLicences());

        // Add education information
        String temp = getEducationStatus(this.educationInformation);
        if (!temp.isEmpty())
            list.add(temp);

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

    private String getEducationStatus(EducationInformationDTO educationInformation){
        int status = SharedObjects.educationStatusValues.get(educationInformation.getDegree().toLowerCase());
        if (status > 3){
            // Even numbers means currently studying
            if (status < 8 && status % 2 == 0){
                return String.format("%s %s öğrencisiyim.",
                        educationInformation.getUniversity().getName(), educationInformation.getDepartment().getName());
            }
            else {
                return String.format("%s %s mezunuyum.",
                        educationInformation.getUniversity().getName(), educationInformation.getDepartment().getName());
            }
        }
        return "";
    }

    private List<String> driverLicences(){

        List<String> licences = new ArrayList<>();
        getPersonalInformation().getDriverLicence().forEach(item->licences.add(item + " seviye sürücü belgesine sahibim."));

        return licences;
    }

}
