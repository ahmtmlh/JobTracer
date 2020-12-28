package edu.deu.resumeie.service.matcher;

import edu.deu.resumeie.service.model.Job;

import java.util.*;

public class Matcher {

    /**
     * MatchingPriority for the matching process.
     *
     * <ul>
     *     <li><b>NONE</b>: Cluster matching will not be performed</li>
     *     <li><b>LOW</b>: Include Jobs that shares one or more cluster with given CV's cluster set</li>
     *     <li><b>MEDIUM</b>: Include Jobs that has %40 (or more) clusters of given CV's cluster set.</li>
     *     <li><b>HIGH</b>: Include Jobs that has %75 (or more) clusters of given CV's cluster set.</li>
     *     <li><b>HIGHEST</b>: Include only Jobs that has the same set of clusters as CV cluster set.
     *     <u>(This option will drastically decrease total number of matches)</u></li>
     * </ul>
     */
    public enum MatchingPriority {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        HIGHEST
    }

    /**
     * Matches given clustering information to given Jobs. Matching process is done with with unique clusters.
     * This function will return matched jobs with the given MatchingPriority
     * @see MatchingPriority MatchingPriority for priority options
     * @param preMatchedJobs A {@link List List} consists of Job information, which were pre matched by other CV information
     * @param cvClusters Clustering information of the CV
     * @param priority A {@link MatchingPriority MatchingPriority} that
     *                 specifies the matching sensitivity <u>(passing null will default to LOW)</u>
     *
     * @return List of Job Ads that matches to the given clustering, with the given MatchPriority setting
     */
    public static List<Job> match(List<Job> preMatchedJobs, String cvClusters, MatchingPriority priority){

        if(priority == null){
            priority = MatchingPriority.LOW;
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
            } else if(priority != MatchingPriority.HIGHEST) {
                double intersectionSize = getIntersectionSize(jobAdClusterSet, cvClustersSet);
                // At this point, sets are not identical. Intersection size is checked
                // if intersection set is not empty (intersectionSize > 0), add that job to queue
                // with ordinal = cv.size - intersectionSize + 1;
                if (intersectionSize > 0){
                    int ordinal = cvClustersSet.size() - (int)intersectionSize + 1;
                    // This correction is done to populate low-information requests.
                    if (cvClustersSet.size() < 6) intersectionSize += 0.25;
                    if (priority == MatchingPriority.LOW ||
                            (priority == MatchingPriority.MEDIUM && intersectionSize >= (double) cvClustersSet.size() * 2.0 / 5.0) ||
                            (priority == MatchingPriority.HIGH && intersectionSize >= (double) cvClustersSet.size() * 3.0 / 4.0))
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
