package com.secondhand.backend.repository;

import com.secondhand.backend.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@code City} entities.
 * <p>
 * This interface performs read and write operations on the database via Spring Data JPA; method implementations are generated at runtime from the method name or the {@code @Query} annotation.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    /**
     * Finds by name.
     *
     * @param name the name
     * @return an {@code Optional<City>} that may be empty
     */
    Optional<City> findByName(String name);
}