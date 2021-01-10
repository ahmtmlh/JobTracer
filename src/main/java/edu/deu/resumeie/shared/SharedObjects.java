package edu.deu.resumeie.shared;

import java.util.HashMap;
import java.util.Map;

public class SharedObjects {

    // --------- CONSTANTS -------- //
    public static final String VERSION = "V1.4.18";
    public static final String DB_CONN_STR = "jdbc:sqlite:./data/data.db";
    //public static final String DB_CONN_STR = "jdbc:mysql://localhost:3306/jobtest?user=root&userUnicode=true&useLegacyDatetimeCode=false&serverTimezone=Turkey&characterEncoding=UTF-8";

    // --------- OBJECTS -------- //
    public static final ServiceRunParameters serviceParams = new ServiceRunParameters();

    public static final Map<String, Integer> educationStatusValues = new HashMap<>();

    static{
        educationStatusValues.put("ilköğretim mezunu", 1);
        educationStatusValues.put("lise öğrencisi", 2);
        educationStatusValues.put("lise mezunu", 3);
        educationStatusValues.put("meslek yüksekokulu öğrencisi", 4);
        educationStatusValues.put("meslek yüksekokulu mezunu", 5);
        educationStatusValues.put("üniversite öğrencisi", 6);
        educationStatusValues.put("üniversite mezunu", 7);
        educationStatusValues.put("master öğrencisi", 8);
        educationStatusValues.put("master mezunu", 9);
        educationStatusValues.put("doktora öğrencisi", 10);
        educationStatusValues.put("doktora mezunu", 11);
    }

    // --------- CLASSES -------- //

    public static class ServiceRunParameters {
        public String vectorizer;
        public boolean getTextFields;

        public ServiceRunParameters(){
            // Default values
            vectorizer = "tfidf";
            getTextFields = true;
        }
    }
}
