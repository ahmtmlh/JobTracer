package edu.deu.resumeie.shared;

public class SharedObjects {

    // --------- CONSTANTS -------- //
    public static final String VERSION = "V1.4";


    // --------- OBJECTS -------- //
    public static final ServiceRunParameters serviceParams = new ServiceRunParameters();


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
