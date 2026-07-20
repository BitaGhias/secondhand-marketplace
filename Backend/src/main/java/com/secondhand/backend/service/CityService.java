package com.secondhand.backend.service;

import com.secondhand.backend.dto.city.CityRequest;
import com.secondhand.backend.dto.city.CityResponse;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CityService {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private UserService userService;

    private CityResponse convertToResponse(City city) {
        return new CityResponse(city.getId(), city.getName());
    }

    // اضافه کردن شهر فقط توسط ادمین
    public CityResponse addCity(Long adminId, CityRequest request) {
        // استفاده از متد جدید در UserService برای بررسی نقش ادمین
        if (!userService.isAdmin(adminId)) {
            throw new ForbiddenException("شما دسترسی ادمین ندارید!");
        }

        cityRepository.findByName(request.getName()).ifPresent(c -> {
            throw new BadRequestException("این شهر از قبل در سیستم ثبت شده است!");
        });

        City city = new City();
        city.setName(request.getName());

        City savedCity = cityRepository.save(city);
        return convertToResponse(savedCity);
    }

    public List<CityResponse> getAllCities() {
        return cityRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}