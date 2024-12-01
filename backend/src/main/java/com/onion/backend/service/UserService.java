package com.onion.backend.service;

import java.util.List;

import com.onion.backend.dto.SignUpUserDto;
import com.onion.backend.entity.User;
import com.onion.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    public User createUser(SignUpUserDto signUpUserDto) {
        User newUser = new User();
        newUser.setUsername(signUpUserDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(signUpUserDto.getPassword()));
        newUser.setEmail(signUpUserDto.getEmail());
        return userRepository.save(newUser);
    }


    public void deleteUser(Long userId) {

        userRepository.deleteById(userId);

    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
