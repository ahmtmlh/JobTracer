package edu.deu.resumeie.service.controller;

import edu.deu.resumeie.service.matcher.Matcher;
import edu.deu.resumeie.service.model.CV;
import edu.deu.resumeie.service.model.Job;
import edu.deu.resumeie.service.model.dto.CVDTO;
import edu.deu.resumeie.service.service.ResumeMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/rest")
public class ResumeMatchingRestController {
	
	@Autowired
	private ResumeMatchingService resumeMatchingService;

	@GetMapping("/positions/{prefix}")
	public ResponseEntity<List<String>> getJobPositions(@PathVariable(name = "prefix") String prefix){
		List<String> positions = resumeMatchingService.getJobPositionStartingWith(prefix);
		return positions.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(positions);
	}

	@GetMapping("/positions")
	public ResponseEntity<List<String>> getJobPositions(){
		List<String> positions = resumeMatchingService.getJobPositions();
		return positions.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(positions);
	}


	@PostMapping("/resumeInfo")
	public ResponseEntity<List<Job>> getNew(@RequestBody @Valid CVDTO cv){

		//  Convert CVDTO to CV
		CV createdCV = cv.createCV();
		Optional<List<Job>> jobList = resumeMatchingService.getMatchedJobs(createdCV, Matcher.MatchingPriority.MEDIUM);
		return jobList.isPresent() ? ResponseEntity.ok(jobList.get()) : ResponseEntity.ok(new ArrayList<Job>());
	}
	
}
