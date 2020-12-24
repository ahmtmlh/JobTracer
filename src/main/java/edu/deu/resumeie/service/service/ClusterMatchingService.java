package edu.deu.resumeie.service.service;

import edu.deu.resumeie.service.dao.JobDataRepository;
import edu.deu.resumeie.service.json.ClusterServiceMessage;
import edu.deu.resumeie.service.json.JsonMessage;
import edu.deu.resumeie.service.matcher.Matcher;
import edu.deu.resumeie.service.model.CV;
import edu.deu.resumeie.service.model.Job;
import edu.deu.resumeie.service.service.socket.Client;
import edu.deu.resumeie.shared.SharedObjects;
import edu.deu.resumeie.training.nlp.informationextraction.InformationExtractor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ClusterMatchingService {

    private final InformationExtractor ie;

    //@Autowired
    private final JobDataRepository jobDataRepository;

    public ClusterMatchingService(){
        ie = new InformationExtractor(3);
        jobDataRepository = new JobDataRepository();
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
            Client clusterServiceClient = Client.create("127.0.0.1", 65432);
            if (clusterServiceClient.startConnection()){
                System.err.printf("Service ERR. Detailed Message: %s%n", clusterServiceClient.getErrorCause());
                return;
            }
            String receivedMessageStr = clusterServiceClient.sendAndReceive(messageToSend.toString());
            clusterServiceClient.stopConnection();
            // Parse received message
            Optional<ClusterServiceMessage> receivedMessage = ClusterServiceMessage.parse(receivedMessageStr);
            receivedMessage.ifPresent(message -> clustersReference.set(message.getArrayAsString("clusters")));

            // At this point, preMatchingThread is most likely to be done
            // Retrieve both information from AtomicReferences and begin matching process.
        });

        List<Job> finalList = null;
        try{
            // Start threads
            preMatcherThread.start();
            clusterServiceThread.start();
            // Wait for them to finish
            preMatcherThread.join();
            clusterServiceThread.join();
            // Retrieve information
            List<Job> preMatchList = preMatchedList.get();
            String clusterString = clustersReference.get();

            // Check for values
            if (preMatchList == null || preMatchList.isEmpty()){
                throw new IllegalArgumentException("Get PreMatchList Error. Check Logs");
            }
            if (clusterString == null || clusterString.isEmpty()){
                throw new IllegalArgumentException("Get Clustering Information Error. Check Logs");
            }

            // Begin matching
            finalList = Matcher.match(preMatchList, clusterString, priority);

        } catch (InterruptedException | IllegalArgumentException e){
            e.printStackTrace();
            // Return ERR to front-end, finalList will be null
        }

        return Optional.ofNullable(finalList);
    }
}
