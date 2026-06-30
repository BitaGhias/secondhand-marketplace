package com.secondhand.backend.repository;

import com.secondhand.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long>
{
    boolean existsByUsername(String username);
}
