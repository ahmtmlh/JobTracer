package edu.deu.resumeie.service.matcher;

import edu.deu.resumeie.service.model.Job;

import java.util.*;

public class Matcher {

    /**
     * MatchingPriority for the matching process.
     *
     * <ul>
     *     <li>NONE: Cluster matching will not be performed</li>
     *     <li>ALL : Include ALL Jobs that has one ore more common cluster with given clustering information</li>
     *     <li>HALF: Include Jobs that has at least HALF of the given clustering information</li>
     *     <li>FULL: Include Jobs that has the same clusters with given clustering information</li>
     *     <li>SPECIFIC: Include only Jobs that matches the exact clustering information
     *     <u>(This option will drastically decrease total number of matches)</u></li>
     * </ul>
     */
    public enum MatchingPriority {
        NONE,
        ALL,
        HALF,
        FULL,
        SPECIFIC
    }

    /**
     * Matches given clustering information to given Jobs. Matching process is done with with unique clusters.
     * This function will return matched jobs with the given MatchingPriority
     * @see MatchingPriority MatchingPriority for priority options
     * @param preMatchedJobs A {@link List List} consists of Job information, which were pre matched by other CV information
     * @param cvClusters Clustering information of the CV
     * @param priority A {@link MatchingPriority MatchingPriority} that
     *                 specifies the matching sensitivity <u>(passing null will default to ALL)</u>
     *
     * @return List of Job Ads that matches to the given clustering, with the given MatchPriority setting
     */
    public static List<Job> match(List<Job> preMatchedJobs, String cvClusters, MatchingPriority priority){

        if(priority == null){
            priority = MatchingPriority.ALL;
        }
        // If priority is NONE, do not perform any matching
        if (priority == MatchingPriority.NONE){
            return preMatchedJobs;
        }

        PriorityQueue<PriorityQueueItem> pq = new PriorityQueue<>();
        Set<Integer> cvClustersSet = createClusterSet(cvClusters);

        for(Job job : preMatchedJobs){
            Set<Integer> jobAdClusterSet = createClusterSet(job.getClusters());
            // If sets are identical, that job has the biggest priority
            if (jobAdClusterSet.equals(cvClustersSet)){
                pq.add(new PriorityQueueItem(job, 0));
            } else if(priority != MatchingPriority.SPECIFIC) {
                // jobAdClusterSet will be corrupted after this point. It is also
                // intersection set of the two.
                int intersectionSize = getIntersectionSize(jobAdClusterSet, cvClustersSet);
                // At this point, sets are not identical. Intersection size is checked
                // if intersection set is not empty (intersectionSize > 0), add that job to queue
                // with ordinal = cv.size - intersectionSize + 1;
                if (intersectionSize > 0){
                    int ordinal = cvClustersSet.size() - intersectionSize + 1;
                    if (priority == MatchingPriority.ALL ||
                            (priority == MatchingPriority.HALF && intersectionSize >= jobAdClusterSet.size() / 2) ||
                            (priority == MatchingPriority.FULL && intersectionSize == jobAdClusterSet.size()))
                        pq.add(new PriorityQueueItem(job, ordinal));
                }
            }
        }

        List<Job> returnList = new ArrayList<>();
        pq.forEach(item -> returnList.add(item.getJob()));
        return returnList;
    }


    private static Set<Integer> createClusterSet(String clusters){
        String[] clusterArr = clusters.split(",");
        Set<Integer> set = new HashSet<>();
        for(String cluster : clusterArr){
            try{
                Integer clusterAsInt = Integer.parseInt(cluster.trim());
                set.add(clusterAsInt);
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return set;
    }

    // WARNING: THIS FUNCTIONS CHANGES CONTENTS OF SET1
    // ALWAYS GIVE THE TEMP ELEMENT AS THE FIRST PARAMETER
    private static int getIntersectionSize(Set<Integer> set1, Set<Integer> set2){
        Set<Integer> temp = new HashSet<>(set1);
        temp.retainAll(set2);
        return temp.size();
    }


    private static class PriorityQueueItem implements Comparable<PriorityQueueItem>{

        private final Job job;
        private final Integer ordinal;

        public PriorityQueueItem(Job job, int ordinal){
            this.job = job;
            this.ordinal = ordinal;
        }

        @Override
        public int hashCode() {
            return job.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof PriorityQueueItem){
                if (this.ordinal.equals(((PriorityQueueItem)obj).ordinal)){
                    return this.job.equals(((PriorityQueueItem)obj).job);
                }
            }
            return false;
        }

        @Override
        public int compareTo(PriorityQueueItem item) {
            return this.ordinal.compareTo(item.ordinal);
        }

        public Job getJob() {
            return job;
        }
    }

}
