package edu.deu.resumeie.service.service;

import edu.deu.resumeie.service.dao.JobDataRepository;
import edu.deu.resumeie.service.json.ClusterServiceMessage;
import edu.deu.resumeie.service.json.JsonMessage;
import edu.deu.resumeie.service.matcher.Matcher;
import edu.deu.resumeie.service.model.CV;
import edu.deu.resumeie.service.model.Job;
import edu.deu.resumeie.service.nlp.JobNameAnalyzer;
import edu.deu.resumeie.service.service.socket.Client;
import edu.deu.resumeie.shared.SharedObjects;
import edu.deu.resumeie.training.nlp.informationextraction.InformationExtractor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ClusterMatchingService {

    private static final Logger logger = LogManager.getLogger(ClusterMatchingService.class);

    private final InformationExtractor ie;

    //@Autowired
    private final JobDataRepository jobDataRepository;
    private final JobNameAnalyzer jobNameAnalyzer;
    private Client clusterServiceClient;

    public ClusterMatchingService(){
        ie = new InformationExtractor(3);
        jobDataRepository = new JobDataRepository();
        jobNameAnalyzer = new JobNameAnalyzer();
        startServiceConnection();
    }

    private void startServiceConnection(){
        clusterServiceClient = Client.create("localhost", 65432);
        clusterServiceClient.startConnection();
    }


    /**
     * MainProcess of the back-end service. Receives a CV from front-end service and
     * matches that CV to available jobs in the database to find the optimal job ad for
     * that cv.
     *
     * CV is received from front-end service.
     * @param cv A CV object that represents person's information as well as abilities.
     * @param priority Matching priority for the matching process
     */
    public Optional<List<Job>> matchingProcess(CV cv, Matcher.MatchingPriority priority){

        // Make this change to CV to make sure that there is a job %100
        cv.setProfession(jobNameAnalyzer.getBestMatch(cv.getProfession()));
        logger.info("Current profession: " + cv.getProfession());

        AtomicReference<String> clustersReference = new AtomicReference<>();
        AtomicReference<List<Job>> preMatchedList = new AtomicReference<>();

        Thread preMatcherThread = new Thread(() -> {
            List<String> cities = new ArrayList<>();
            cv.getDesiredCities().forEach(city -> cities.add(city.getCityName()));
            preMatchedList.set(jobDataRepository.getPreMatchedJobAds(
                    cv.getExperience(), cv.getEducationStatus(), cv.getProfession(), cities, SharedObjects.serviceParams.getTextFields));
        });

        Thread clusterServiceThread = new Thread(() -> {
            // Information Extraction
            ie.clear();
            ie.extractFromStringList(cv.getQualificationList());
            List<String> ieResult = ie.getLemmatizedResults();
            // Prepare JSON Message to be send to clustering service
            JsonMessage messageToSend = new ClusterServiceMessage();
            messageToSend.addArray("jobInfo");
            // Force the service to use TF-IDF vectorizer
            messageToSend.addItem("vectorizer", SharedObjects.serviceParams.vectorizer);
            ieResult.forEach(item -> messageToSend.addItemToArray("jobInfo", item));
            // Connect to python clustering service
            String receivedMessageStr = "";
            try{
                if (clusterServiceClient.hasError())
                    throw new IOException(String.format("Clustering service error: %s", clusterServiceClient.getErrorCause()));

                receivedMessageStr = clusterServiceClient.sendAndReceive(messageToSend.toString());
                // Parse received message
                Optional<ClusterServiceMessage> receivedMessage = ClusterServiceMessage.parse(receivedMessageStr);
                receivedMessage.ifPresent(message -> clustersReference.set(message.getArrayAsString("clusters")));
            } catch(IOException e) {
                logger.error(e.getLocalizedMessage(), e);
                logger.warn("Client service communication error. Restarting service...");
                // Reset connection
                clusterServiceClient.stopConnection();
                startServiceConnection();
            }
        });

        // Retrieve both information from AtomicReferences and begin matching process.
        List<Job> finalList = null;
        try{
            // Start threads
            preMatcherThread.start();
            if (priority != Matcher.MatchingPriority.NONE)
                clusterServiceThread.start();
            // Wait for them to finish
            preMatcherThread.join();
            if (priority != Matcher.MatchingPriority.NONE)
                clusterServiceThread.join();
            // Retrieve information
            List<Job> preMatchList = preMatchedList.get();
            String clusterString = clustersReference.get();

            // Check for values
            if (preMatchList == null || preMatchList.isEmpty()){
                throw new IllegalArgumentException("Get PreMatchList Error. Check Logs");
            }
            if (priority != Matcher.MatchingPriority.NONE && (clusterString == null || clusterString.isEmpty())){
                throw new IllegalArgumentException("Get Clustering Information Error. Check Logs");
            }

            // Begin matching
            finalList = Matcher.match(preMatchList, clusterString, priority);

        } catch (InterruptedException e){
            logger.error(e.getLocalizedMessage(), e);
        } catch (IllegalArgumentException e){
            logger.fatal(e.getLocalizedMessage());
        }

        return Optional.ofNullable(finalList);
    }
}
