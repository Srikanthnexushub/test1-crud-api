package org.example.service;

import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.entity.UserEntity;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse deleteUser(Long id) {
        Optional<UserEntity> existingUser = userRepository.findById(id);

        if (existingUser.isEmpty()) {
            return new LoginResponse(false, "User not found");
        }

        userRepository.deleteById(id);

        return new LoginResponse(true, "User deleted successfully");
    }

    public LoginResponse updateUser(Long id, LoginRequest request) {
        Optional<UserEntity> existingUser = userRepository.findById(id);

        if (existingUser.isEmpty()) {
            return new LoginResponse(false, "User not found");
        }

        UserEntity user = existingUser.get();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        userRepository.save(user);

        return new LoginResponse(true, "User updated successfully");
    }

    public UserEntity getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public LoginResponse register(LoginRequest request) {
        Optional<UserEntity> existingUser = userRepository.findByEmail(request.getEmail());

        if (existingUser.isPresent()) {
            return new LoginResponse(false, "Email already exists");
        }

        UserEntity user = new UserEntity(request.getEmail(), request.getPassword());
        userRepository.save(user);

        return new LoginResponse(true, "Registration successful");
    }

    public LoginResponse login(LoginRequest request) {
        Optional<UserEntity> user = userRepository.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            return new LoginResponse(false, "User not found");
        }

        if (!user.get().getPassword().equals(request.getPassword())) {
            return new LoginResponse(false, "Invalid password");
        }

        return new LoginResponse(true, "Login successful");
    }
}