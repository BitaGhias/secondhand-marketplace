package com.secondhand.backend.service;

import com.secondhand.backend.entity.City;
import com.secondhand.backend.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CityService {

    @Autowired
    public CityRepository cityRepository;

    public City addCity(String name) {
        cityRepository.findByName(name).ifPresent(c -> {
            throw new RuntimeException("این شهر از قبل در سیستم ثبت شده است!");
        });
        City city = new City();
        city.name = name;
        return cityRepository.save(city);
    }

    public List<City> getAllCities() {
        return cityRepository.findAll();
    }
}