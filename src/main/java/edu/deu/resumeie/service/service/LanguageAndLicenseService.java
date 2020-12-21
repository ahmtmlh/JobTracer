package edu.deu.resumeie.service.service;

import edu.deu.resumeie.service.dao.LanguageAndLicenseDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LanguageAndLicenseService {

    @Autowired
    private LanguageAndLicenseDataRepository repository;

    public List<String> getLanguages(String prefix) {
        return repository.getLanguages(prefix);
    }

    public List<String> getLanguages() {
        return repository.getLanguages();
    }

    public List<String> getDriverLicenceTypes() {
        return repository.getDriverLicenceTypes();
    }

}
