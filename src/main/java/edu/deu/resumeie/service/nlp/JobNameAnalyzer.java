package edu.deu.resumeie.service.nlp;

import edu.deu.resumeie.training.nlp.morphology.lemmatization.Lemmatizer;
import edu.deu.resumeie.training.nlp.morphology.lemmatization.TurkishLemmatizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

public class JobNameAnalyzer {

    private static final String JOB_INFO_FILE = "./data/allJobs.txt";


    private final Lemmatizer lemmatizer;
    private final Set<String> jobs;
    private boolean error;

    public JobNameAnalyzer() {
        error = false;
        lemmatizer = new TurkishLemmatizer();
        jobs = new HashSet<>();
        init();
    }

    private void init() {
        try (BufferedReader br = new BufferedReader(new FileReader(JOB_INFO_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase();
                jobs.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            error = true;
        }
    }


    private String lemmatize(String str){
        lemmatizer.flush();
        lemmatizer.lemmatizeSentence(str, true);
        return lemmatizer.getLemmatizedSentence();
    }

    private double jaccardIndex(String s1, String s2) {
        s1 = lemmatize(s1);
        s2 = lemmatize(s2);

        Set<String> set1 = new HashSet<>(Arrays.asList(s1.split(" ").clone()));
        Set<String> set2 = new HashSet<>(Arrays.asList(s2.split(" ").clone()));

        int union;
        int intersection = 0;

        for (String word1 : set1) {
            for (String word2 : set2) {
                if (word1.equals(word2)) intersection++;
            }
        }
        union = set1.size() + set2.size() - intersection;

        return (double)intersection / (double)union;
    }

    /**
     * Return the best matching jobInfo from the list.
     * If jobInfo is in the list, no calculation is done. If jobInfo is absent in the list,
     * best matching job is determined by Jaccard Index scores.
     * <p>
     * If there is an error on reading job info file, given value is returned without any calculation.
     *
     * @param jobInfo Job Info to be matched against the list
     * @return Best matching JobInfo from the dataset.
     */
    public String getBestMatch(String jobInfo) {
        if (error){
            System.err.println("JobNameAnalyzer has error");
            return jobInfo;
        }

        String temp = jobInfo.toLowerCase().trim();
        if (jobs.contains(temp)){
            return jobInfo;
        }

        double score = -1.0;
        String candidate = null;

        for(String job : jobs){
            double tempScore = jaccardIndex(job, temp);
            if (tempScore > score){
                score = tempScore;
                candidate = job;
            }
        }
        return candidate;
    }


}
