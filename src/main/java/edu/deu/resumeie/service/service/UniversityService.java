package edu.deu.resumeie.service.service;

import edu.deu.resumeie.service.dao.UniversityDataRepository;
import edu.deu.resumeie.service.model.dto.DepartmentDTO;
import edu.deu.resumeie.service.model.dto.FacultyDTO;
import edu.deu.resumeie.service.model.dto.UniversityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UniversityService {

    @Autowired
    private UniversityDataRepository universityDataRepository;

    public List<UniversityDTO> getUniversities(){
        return universityDataRepository.getUniversities();
    }

    public List<FacultyDTO> getFacultiesOfUniversity(int universityId){
        return universityDataRepository.getFacultyOfUniversity(universityId);
    }

    public List<DepartmentDTO> getDepartmentOfFacultyAndUniversity(int facultyId, int universityId){
        return universityDataRepository.getDepartmentOfFacultyAndUniversity(facultyId, universityId);
    }

}
