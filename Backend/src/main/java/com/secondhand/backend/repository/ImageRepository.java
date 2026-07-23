package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA repository for {@code Image} entities.
 * <p>
 * This interface performs read and write operations on the database via Spring Data JPA; method implementations are generated at runtime from the method name or the {@code @Query} annotation.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    /**
     * Finds by item id.
     *
     * @param itemId id of the ad (item)
     * @return a {@code List<Image>} with the results; empty if nothing matches
     */
    List<Image> findByItemId(Long itemId);
}