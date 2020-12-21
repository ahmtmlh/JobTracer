package edu.deu.resumeie.service.service;

import edu.deu.resumeie.service.dao.CityDataRepository;
import edu.deu.resumeie.service.model.City;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CitiesService {


    @Autowired
    private CityDataRepository cityDataRepository;


    public List<City> getCities() {
        return cityDataRepository.getCities();
    }

    public List<City> getCities(String prefix){
        return cityDataRepository.getCitiesStartingWith(prefix);
    }


}
