package edu.deu.resumeie.service.controller;

import edu.deu.resumeie.service.model.dto.DepartmentDTO;
import edu.deu.resumeie.service.model.dto.FacultyDTO;
import edu.deu.resumeie.service.model.dto.UniversityDTO;
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
}
