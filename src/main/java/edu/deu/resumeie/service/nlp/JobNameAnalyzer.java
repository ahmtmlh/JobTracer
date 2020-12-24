package edu.deu.resumeie.service.nlp;

import edu.deu.resumeie.service.dao.JobDataRepository;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;


public class JobNameAnalyzer {

    /*
     * Key of this map will point to trimmed and lower case versions of professions.
     * Value of this map will point to original name of professions.
     */
    private final Map<String, String> jobs;

    public JobNameAnalyzer() {
        jobs = new HashMap<>();
        init();
    }

    private void init() {
        List<String> jobsTemp = new JobDataRepository().getJobPositions();
        jobsTemp.forEach(job -> jobs.put(job.trim().toLowerCase(), job));
    }

    private float compare(String s1, String s2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(s1.split(" ").clone()));
        Set<String> set2 = new HashSet<>(Arrays.asList(s2.split(" ").clone()));

        int union = set1.size() + set2.size();

        set1.retainAll(set2);
        int intersection = set1.size();

        union = union - intersection;

        return (float)intersection / (float)union;
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

        String temp = jobInfo.toLowerCase().trim();
        if (jobs.containsKey(temp)){
            return jobs.get(temp);
        }

        float score = -1.0f;
        String candidate = null;

        for(String job : jobs.keySet()){
            float tempScore = compare(job, temp);
            if (tempScore > score){
                score = tempScore;
                candidate = job;
            } else if (tempScore == score){
                String[] jobTokens = job.split(" ");
                String[] cvTokens = temp.split(" ");
                if (jobTokens[0].equals(cvTokens[0]))
                    candidate = job;
            }
        }
        // Return the name gathered from DB, rather than lower-case and trimmed version.
        return jobs.get(candidate);
    }


}
