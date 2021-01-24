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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/rest")
public class ResumeMatchingRestController {

    private static final int PAGE_SIZE = 10;

    @Autowired
    private ResumeMatchingService resumeMatchingService;

    private final Map<Integer, MatchingResult> results;
    private final Dispatcher dispatcher;

    public ResumeMatchingRestController() {
        results = new HashMap<>();
        dispatcher = new Dispatcher(results);
    }

    @GetMapping("/positions/{prefix}")
    public ResponseEntity<List<String>> getJobPositions(@PathVariable(name = "prefix") String prefix) {
        List<String> positions = resumeMatchingService.getJobPositionStartingWith(prefix);
        return positions.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(positions);
    }

    @GetMapping("/positions")
    public ResponseEntity<List<String>> getJobPositions() {
        List<String> positions = resumeMatchingService.getJobPositions();
        return positions.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(positions);
    }

    @PostMapping("/resumeInfo")
    public ResponseEntity<JobResponse> getJobs(@RequestBody @Valid CVDTO cv) {

        //  Convert CVDTO to CV
        CV createdCV = cv.createCV();
        Matcher.MatchingPriority priority = cv.getMatchingPriority() == null ?
                Matcher.MatchingPriority.MEDIUM : Matcher.MatchingPriority.valueOf(cv.getMatchingPriority().toUpperCase());

        Optional<List<Job>> jobList = resumeMatchingService.getMatchedJobs(createdCV, priority);

        if (jobList.isPresent() && !jobList.get().isEmpty()) {
			int listId = dispatcher.getId();
			int totalPages = (int) Math.ceil((double) jobList.get().size() / PAGE_SIZE);
			results.put(listId, new MatchingResult(jobList.get(), totalPages, priority, createdCV));
			List<Job> page = getPage(0, jobList.get());

			return ResponseEntity.ok(new JobResponse(listId, totalPages, page));
        } else {
            return ResponseEntity.ok(JobResponse.EMPTY_RESPONSE);
        }
    }

    @PostMapping("/resumeInfoPage")
    public ResponseEntity<JobResponse> getJobs(@RequestBody @Valid PageRequest page) {
        if (results.containsKey(page.listId)) {
			dispatcher.resetTimer(page.listId);
            MatchingResult res = results.get(page.listId);
            Matcher.MatchingPriority newPriority = page.matchingPriority == null ? null : Matcher.MatchingPriority.valueOf(page.matchingPriority.toUpperCase());
            if (newPriority != null && newPriority != res.priority) {
                // Match Again
                Optional<List<Job>> jobList = resumeMatchingService.getMatchedJobs(res.cv, newPriority);
                jobList.ifPresent(jobs -> {
					res.jobList = jobs;
					page.setPage(0);
					res.totalPages = (int) Math.ceil((double) jobList.get().size() / PAGE_SIZE);
					res.priority = newPriority;
				});
            }

            List<Job> pageList = getPage(page.getPage(), res.jobList);
            if (pageList != null) {
                return ResponseEntity.ok(new JobResponse(page.listId, res.totalPages, pageList));
            }
        }
        return ResponseEntity.ok(JobResponse.EMPTY_RESPONSE);
    }


    private List<Job> getPage(int page, List<Job> all) {
        int start = page * PAGE_SIZE;
        if (start > all.size()) {
            return null;
        }
        int end = start + PAGE_SIZE;
        end = Math.min(end, all.size());
        return all.subList(start, end);
    }

    private static class PageRequest {
        @NotNull
        private Integer listId;
        @NotNull
        private Integer page;
        private String matchingPriority;

        public PageRequest() {
        }

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

        public String getMatchingPriority() {
            return matchingPriority;
        }

        public void setMatchingPriority(String matchingPriority) {
            this.matchingPriority = matchingPriority;
        }
    }

    private static class MatchingResult {
        private List<Job> jobList;
		private Matcher.MatchingPriority priority;
		private CV cv;
		private int totalPages;

        public MatchingResult(List<Job> jobList, int totalPages, Matcher.MatchingPriority priority, CV cv) {
            this.jobList = jobList;
            this.priority = priority;
            this.cv = cv;
            this.totalPages = totalPages;
        }
    }
}
