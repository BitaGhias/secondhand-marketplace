package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>{
    List<Item> findByStatus(String status);
    List<Item> findByUserId(Long userId);
}
