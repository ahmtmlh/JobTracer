package edu.deu.resumeie.service.matcher;

import edu.deu.resumeie.service.model.Job;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Matcher {

    /**
     * MatchingPriority for the matching process.
     *
     * <ul>
     *     <li><b>NONE</b>: Cluster matching will not be performed</li>
     *     <li><b>LOW</b>: Include Jobs that shares one or more cluster with given CV's cluster set</li>
     *     <li><b>MEDIUM</b>: Include Jobs that has %50 (or more) clusters of given CV's cluster set.</li>
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

    private static final Logger logger = LogManager.getLogger(Matcher.class);

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
    public List<Job> match(List<Job> preMatchedJobs, String cvClusters, MatchingPriority priority){

        if(priority == null){
            priority = MatchingPriority.LOW;
        }
        // If priority is NONE, do not perform any matching
        if (priority == MatchingPriority.NONE){
            return preMatchedJobs;
        }
        List<Job> returnList = new ArrayList<>();
        PriorityQueue<PriorityQueueItem> pq = new PriorityQueue<>();
        List<Integer> cvClustersList = createClusterList(cvClusters);
        Set<Integer> uniqueCvClusterList = uniqueClusters(cvClustersList);
        // If specified qualifications are less than 2, and priority is MEDIUM or higher, don't try to match anything.
        // This problem is due to clustering error, clusters must be changed.
        if (priority.ordinal() > MatchingPriority.LOW.ordinal() && uniqueCvClusterList.size() < 2){
            return returnList;
        }
        for(Job job : preMatchedJobs){
            List<Integer> jobAdClusterList = createClusterList(job.getClusters());
            Set<Integer> uniqueJobAdClusterList = uniqueClusters(jobAdClusterList);
            // This is done to eliminate outliers
            if (uniqueJobAdClusterList.size() <= 2)
                continue;
            // If sets are identical, that job has the biggest priority
            if (uniqueJobAdClusterList.equals(uniqueCvClusterList)){
                pq.add(new PriorityQueueItem(job, 0));
            } else if(priority != MatchingPriority.HIGHEST) {
                double intersectionSize = getIntersectionSize(new ArrayList<>(jobAdClusterList), cvClustersList);
                // At this point, sets are not identical. Intersection size is checked
                // if intersection set is not empty (intersectionSize > 0), add that job to queue
                // with ordinal = cv.size - intersectionSize + 1;
                if (intersectionSize > 0){
                    int ordinal = cvClustersList.size() - (int)intersectionSize + 1;
                    // This correction is done to populate low-information requests.
                    if (cvClustersList.size() < 6) intersectionSize += 0.25;
                    if (priority == MatchingPriority.LOW ||
                            (priority == MatchingPriority.MEDIUM && intersectionSize >= cvClustersList.size() * 2.0 / 5.0) ||
                            (priority == MatchingPriority.HIGH && intersectionSize >= (double) cvClustersList.size() * 3.0 / 4.0))
                        pq.add(new PriorityQueueItem(job, ordinal));
                }
            }
        }


        pq.forEach(item -> returnList.add(item.getJob()));
        return returnList;
    }

    private List<Integer> createClusterList(String clusters){
        String[] clusterArr = clusters.split(",");
        List<Integer> list = new ArrayList<>();
        for(String cluster : clusterArr){
            try{
                Integer clusterAsInt = Integer.parseInt(cluster.trim());
                list.add(clusterAsInt);
            } catch (NumberFormatException ignore){ }
        }
        return list;
    }

    private Set<Integer> uniqueClusters(List<Integer> clusterList){
        return new HashSet<>(clusterList);
    }

    private int getIntersectionSize(Collection<Integer> c1, Collection<Integer> c2){
        c1.retainAll(c2);
        return c1.size();
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
