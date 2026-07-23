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

/**
 * Business-logic service for "city" operations.
 * <p>
 * This class implements the core business logic and sits between the controller layer and the repository layer. Validation and access control are enforced here and a proper exception is thrown when a rule is violated.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Service
public class CityService {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private UserService userService;

    /**
     * Converts to response.
     *
     * @param city the city object
     * @return the resulting {@code CityResponse} instance
     */
    private CityResponse convertToResponse(City city) {
        return new CityResponse(city.getId(), city.getName());
    }

    // اضافه کردن شهر فقط توسط ادمین
    /**
     * Adds a new city; only admins are allowed and duplicate names are rejected with HTTP 400.
     *
     * @param adminId the "admin id" value of type {@code Long}
     * @param request request body received from the client
     * @return the resulting {@code CityResponse} instance
     */
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

    /**
     * Gets all cities.
     *
     * @return a {@code List<CityResponse>} with the results; empty if nothing matches
     */
    public List<CityResponse> getAllCities() {
        return cityRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}