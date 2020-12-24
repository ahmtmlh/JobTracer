package edu.deu.resumeie.service.service;

import edu.deu.resumeie.service.dao.JobDataRepository;
import edu.deu.resumeie.service.matcher.Matcher;
import edu.deu.resumeie.service.model.CV;
import edu.deu.resumeie.service.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ResumeMatchingService {

	@Autowired
	private ClusterMatchingService clusterMatchingService;
	@Autowired
	private JobDataRepository jobDataRepository;


	public Optional<List<Job>> getMatchedJobs(CV cv, Matcher.MatchingPriority priority){
		return clusterMatchingService.matchingProcess(cv, priority);
	}

	public List<String> getJobPositionStartingWith(String prefix){
		return jobDataRepository.getJobPositionStartingWith(prefix);
	}

	public List<String> getJobPositions(){
		return jobDataRepository.getJobPositions();
	}

}
