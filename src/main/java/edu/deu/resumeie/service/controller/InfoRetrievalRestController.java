package edu.deu.resumeie.service.controller;

import edu.deu.resumeie.service.dao.InfoDataRepository;
import edu.deu.resumeie.service.model.pojo.Department;
import edu.deu.resumeie.service.model.pojo.Faculty;
import edu.deu.resumeie.service.model.pojo.University;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class InfoRetrievalRestController {

    @Autowired
    private InfoDataRepository repository;

    @GetMapping("/universities")
    public ResponseEntity<List<University>> getUniversities(){
        List<University> list = repository.getUniversities();
        return list.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(list);
    }

    @GetMapping("/faculties")
    public ResponseEntity<List<Faculty>> getFaculties(@RequestParam int universityId){
        List<Faculty> list = repository.getFacultyOfUniversity(universityId);
        return list.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(list);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments(@RequestParam int universityId,@RequestParam int facultyId){
        List<Department> list = repository.getDepartmentOfFacultyAndUniversity(facultyId, universityId);
        return list.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(list);
    }
}
