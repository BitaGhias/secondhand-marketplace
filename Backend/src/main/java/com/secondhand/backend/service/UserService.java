package com.secondhand.backend.service;

import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService
{
    @Autowired
    private UserRepository userRepository;

    public void registerUser(User user)
    {
        if(userRepository.existsByUsername(user.getUsername()))
            throw new RuntimeException("نام کاربری تکراری است");

        userRepository.save(user);
    }
}
