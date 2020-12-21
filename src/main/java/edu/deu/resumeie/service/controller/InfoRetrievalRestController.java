package edu.deu.resumeie.service.controller;

import edu.deu.resumeie.service.model.City;
import edu.deu.resumeie.service.model.dto.DepartmentDTO;
import edu.deu.resumeie.service.model.dto.FacultyDTO;
import edu.deu.resumeie.service.model.dto.UniversityDTO;
import edu.deu.resumeie.service.service.CitiesService;
import edu.deu.resumeie.service.service.LanguageAndLicenseService;
import edu.deu.resumeie.service.service.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class InfoRetrievalRestController {

    @Autowired
    private UniversityService universityService;
    @Autowired
    private LanguageAndLicenseService languageAndLicenseService;
    @Autowired
    private CitiesService citiesService;

    @GetMapping("/universities")
    public ResponseEntity<List<UniversityDTO>> getUniversities(){
        List<UniversityDTO> list = universityService.getUniversities();
        return list.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(list);
    }

    @GetMapping("/faculties")
    public ResponseEntity<List<FacultyDTO>> getFaculties(@RequestParam int universityId){
        List<FacultyDTO> list = universityService.getFacultiesOfUniversity(universityId);
        return list.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(list);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> getDepartments(@RequestParam int universityId, @RequestParam int facultyId){
        List<DepartmentDTO> list = universityService.getDepartmentOfFacultyAndUniversity(facultyId, universityId);
        return list.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(list);
    }

    @GetMapping("/languages/{prefix}")
    public ResponseEntity<List<String>> getLanguages(@PathVariable String prefix) {
        List<String> languages = languageAndLicenseService.getLanguages(prefix);
        return languages.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(languages);
    }

    @GetMapping("/languages")
    public ResponseEntity<List<String>> getLanguages() {
        List<String> languages = languageAndLicenseService.getLanguages();
        return ResponseEntity.ok(languages);
    }

    @GetMapping("/driverlicencetypes")
    public ResponseEntity<List<String>> getDriverLicenceTypes() {
        List<String> types = languageAndLicenseService.getDriverLicenceTypes();
        return ResponseEntity.ok(types);
    }

    @GetMapping("/cities")
    public ResponseEntity<List<City>> getCities(){
        List<City> cities = citiesService.getCities();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/cities/{prefix}")
    public ResponseEntity<List<City>> getCities(@PathVariable String prefix){
        List<City> cities = citiesService.getCities(prefix);
        return cities.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(cities);
    }

}
