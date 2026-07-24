package com.secondhand.backend.service;

import com.secondhand.backend.dto.city.CityRequest;
import com.secondhand.backend.dto.city.CityResponse;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CityService}: admin-only city creation,
 * duplicate-name rejection and city listing.
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CityService cityService;

    /** A valid admin should be able to add a brand-new city. */
    @Test
    void addCity_asAdmin_savesAndReturnsCity() {
        when(userService.isAdmin(1L)).thenReturn(true);
        when(cityRepository.findByName("همدان")).thenReturn(Optional.empty());
        City saved = new City();
        saved.setId(10L);
        saved.setName("همدان");
        when(cityRepository.save(any(City.class))).thenReturn(saved);

        CityResponse response = cityService.addCity(1L, new CityRequest("همدان"));

        assertEquals(10L, response.getId());
        assertEquals("همدان", response.getName());
        verify(cityRepository).save(any(City.class));
    }

    /** A non-admin caller must be rejected with 403 and nothing must be saved. */
    @Test
    void addCity_withoutAdminRole_throwsForbidden() {
        when(userService.isAdmin(2L)).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> cityService.addCity(2L, new CityRequest("همدان")));

        verify(cityRepository, never()).save(any(City.class));
    }

    /** Adding a city whose name already exists must fail with 400. */
    @Test
    void addCity_duplicateName_throwsBadRequest() {
        when(userService.isAdmin(1L)).thenReturn(true);
        City existing = new City();
        existing.setId(3L);
        existing.setName("تهران");
        when(cityRepository.findByName("تهران")).thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class,
                () -> cityService.addCity(1L, new CityRequest("تهران")));

        verify(cityRepository, never()).save(any(City.class));
    }

    /** getAllCities must map every entity to a response DTO. */
    @Test
    void getAllCities_mapsEntitiesToResponses() {
        City a = new City();
        a.setId(1L);
        a.setName("تهران");
        City b = new City();
        b.setId(2L);
        b.setName("شیراز");
        when(cityRepository.findAll()).thenReturn(List.of(a, b));

        List<CityResponse> result = cityService.getAllCities();

        assertEquals(2, result.size());
        assertEquals("تهران", result.get(0).getName());
        assertEquals("شیراز", result.get(1).getName());
    }
}
