package edu.deu.resumeie.service.controller;

import edu.deu.resumeie.service.dispatcher.Dispatcher;
import edu.deu.resumeie.service.matcher.Matcher;
import edu.deu.resumeie.service.model.CV;
import edu.deu.resumeie.service.model.Job;
import edu.deu.resumeie.service.model.dto.CVDTO;
import edu.deu.resumeie.service.response.JobResponse;
import edu.deu.resumeie.service.service.ResumeMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;


@RestController
@RequestMapping("/rest")
public class ResumeMatchingRestController {

	private static final int PAGE_SIZE = 10;

	@Autowired
	private ResumeMatchingService resumeMatchingService;

	private final Map<Integer, List<Job>> results;
	private final Dispatcher dispatcher;

	public ResumeMatchingRestController(){
		results = new HashMap<>();
		dispatcher = new Dispatcher(results);
	}

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
	public ResponseEntity<JobResponse> getJobs(@RequestBody @Valid CVDTO cv){

		//  Convert CVDTO to CV
		CV createdCV = cv.createCV();
		Optional<List<Job>> jobList = resumeMatchingService.getMatchedJobs(createdCV,
				cv.getMatchingPriority() == null ? Matcher.MatchingPriority.MEDIUM : Matcher.MatchingPriority.valueOf(cv.getMatchingPriority().toUpperCase()));

		if (jobList.isPresent() && !jobList.get().isEmpty()){
			int listId = dispatcher.getId();
			results.put(listId, jobList.get());
			List<Job> page = getPage(0, jobList.get());
			int totalPages = (int) Math.ceil((double)jobList.get().size() / PAGE_SIZE);
			return ResponseEntity.ok(new JobResponse(listId,totalPages, page));
		} else {
			return ResponseEntity.ok(JobResponse.EMPTY_RESPONSE);
		}
	}

	@PostMapping("/resumeInfoPage")
	public ResponseEntity<JobResponse> getJobs(@RequestBody @Valid PageRequest page){
		if (results.containsKey(page.listId)){
			List<Job> pageList = getPage(page.getPage(), results.get(page.listId));
			if (pageList != null){
				dispatcher.resetTimer(page.listId);
				return ResponseEntity.ok(new JobResponse(page.listId, getPage(page.getPage(), results.get(page.listId))));
			}
		}
		return ResponseEntity.ok(JobResponse.EMPTY_RESPONSE);
	}


	private List<Job> getPage(int page, List<Job> all){
		int start = page * PAGE_SIZE;
		if (start > all.size()){
			return null;
		}
		int end = start + PAGE_SIZE;
		end = Math.min(end, all.size());
		return all.subList(start, end);
	}

	private static class PageRequest{
		@NotNull private Integer listId;
		@NotNull private Integer page;

		public PageRequest() { }

		public Integer getListId() {
			return listId;
		}

		public void setListId(Integer listId) {
			this.listId = listId;
		}

		public Integer getPage() {
			return page;
		}

		public void setPage(Integer page) {
			this.page = page;
		}
	}
}
