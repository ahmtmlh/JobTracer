package edu.deu.resumeie.service.controller;

import edu.deu.resumeie.service.model.City;
import edu.deu.resumeie.service.service.ResumeMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest")
public class ResumeMatchingRestController {
	
	@Autowired
	private ResumeMatchingService resumeMatchingService;
	
	//Resume Matching
	
	@RequestMapping(method = RequestMethod.POST,value = "/cities")
	public ResponseEntity<List<City>> getCities(){
		List<City> cities = resumeMatchingService.getCities();
		return ResponseEntity.ok(cities);
	}
	
	@RequestMapping(method = RequestMethod.POST,path = "/unilist")
	public Map<String, List<String>> getUniList() {
	    
	    return resumeMatchingService.getUniFacDep();
	}
	
	@RequestMapping(method = RequestMethod.POST,path = "/languages")
	public ResponseEntity<List<String>> getLanguages() {
	    
		List<String> languages = resumeMatchingService.getLanguages();
	    
		return ResponseEntity.ok(languages);
	}
	
	@RequestMapping(method = RequestMethod.POST,path = "/driverlicencetypes")
	public ResponseEntity<List<String>> getDriverLicenceTypes() {
	    
		List<String> types = resumeMatchingService.getDriverLicenceTypes();
	    
		return ResponseEntity.ok(types);
	}

	
	@RequestMapping(method = RequestMethod.POST,value = "/resumeInfo")
	public ResponseEntity<List<City>> getNew(@RequestBody City city){
		
		System.out.println("check");
		
		//Burada ozgecmis nesnesi oluşturulacak, city yerine ozgecmis parametre olarak gelecek.
		return ResponseEntity.ok(resumeMatchingService.getCities());	
	}
	
	
	

	
}
