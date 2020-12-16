package edu.deu.resumeie.service.service;

import edu.deu.resumeie.service.dao.JobDataRepository;
import edu.deu.resumeie.service.model.City;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResumeMatchingService {

	@Autowired
	private JobDataRepository repository;

	// Resume matching
	public List<City> getCities() {
		return repository.getCities();
	}

	public List<String> getLanguages() {
		return repository.getLanguages();
	}

	public List<String> getDriverLicenceTypes() {
		return repository.getDriverLicenceTypes();

	}
	
	

}
