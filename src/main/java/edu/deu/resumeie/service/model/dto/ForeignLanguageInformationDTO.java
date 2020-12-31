package edu.deu.resumeie.service.model.dto;

public class ForeignLanguageInformationDTO {

    private int id;
    private String name;
    private LanguageLevelDTO level;

    public ForeignLanguageInformationDTO() { }

    public ForeignLanguageInformationDTO(int id, String name, LanguageLevelDTO level) {
        this.id = id;
        this.name = name;
        this.level = level;
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

    public LanguageLevelDTO getLevel() {
        return level;
    }

    public void setLevel(LanguageLevelDTO level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "ForeignLanguageInformationDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", level=" + level +
                '}';
    }


    public static class LanguageLevelDTO {

        String levelName;
        int levelType;

        public LanguageLevelDTO() {
        }

        public LanguageLevelDTO(String levelName, int levelType) {
            this.levelName = levelName;
            this.levelType = levelType;
        }

        public String getLevelName() {
            return levelName;
        }

        public void setLevelName(String levelName) {
            this.levelName = levelName;
        }

        public int getLevelType() {
            return levelType;
        }

        public void setLevelType(int levelType) {
            this.levelType = levelType;
        }

        @Override
        public String toString() {
            return "LanguageLevelDTO{" +
                    "levelName='" + levelName + '\'' +
                    ", levelType=" + levelType +
                    '}';
        }
    }
}