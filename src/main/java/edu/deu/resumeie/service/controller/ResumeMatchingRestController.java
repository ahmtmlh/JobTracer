package edu.deu.resumeie.service.controller;

import edu.deu.resumeie.service.model.City;
import edu.deu.resumeie.service.service.ResumeMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class ResumeMatchingRestController {
	
	@Autowired
	private ResumeMatchingService resumeMatchingService;
	//Resume Matching
	
	@GetMapping("/cities")
	public ResponseEntity<List<City>> getCities(){
		List<City> cities = resumeMatchingService.getCities();
		return ResponseEntity.ok(cities);
	}

	@GetMapping("/cities/{prefix}")
	public ResponseEntity<List<City>> getCities(@PathVariable String prefix){
		List<City> cities = resumeMatchingService.getCities(prefix);
		return cities.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(cities);
	}

	@GetMapping("/languages")
	public ResponseEntity<List<String>> getLanguages() {
		List<String> languages = resumeMatchingService.getLanguages();
		return ResponseEntity.ok(languages);
	}
	
	@GetMapping("/driverlicencetypes")
	public ResponseEntity<List<String>> getDriverLicenceTypes() {
	    
		List<String> types = resumeMatchingService.getDriverLicenceTypes();
	    
		return ResponseEntity.ok(types);
	}

	
	@PostMapping("/resumeInfo")
	public ResponseEntity<List<City>> getNew(@RequestBody City city){
		
		System.out.println("check");
		
		//Burada ozgecmis nesnesi oluşturulacak, city yerine ozgecmis parametre olarak gelecek.
		return ResponseEntity.ok(resumeMatchingService.getCities());	
	}
	
}
