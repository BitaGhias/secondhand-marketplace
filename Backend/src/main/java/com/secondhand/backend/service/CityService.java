package com.secondhand.backend.service;

import com.secondhand.backend.dto.CityRequest;
import com.secondhand.backend.dto.CityResponse;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CityService {

    @Autowired
    private CityRepository cityRepository;

    private CityResponse convertToResponse(City city) {
        return new CityResponse(city.getId(), city.getName());
    }

    public CityResponse addCity(CityRequest request) {
        cityRepository.findByName(request.getName()).ifPresent(c -> {
            throw new BadRequestException("این شهر از قبل در سیستم ثبت شده است!");
        });

        City city = new City();
        city.setName(request.getName());

        City savedCity = cityRepository.save(city);
        return convertToResponse(savedCity);
    }

    public List<CityResponse> getAllCities() {
        List<City> cities = cityRepository.findAll();
        List<CityResponse> responses = new ArrayList<>();
        for (City city : cities) {
            responses.add(convertToResponse(city));
        }
        return responses;
    }
}