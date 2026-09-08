package com.teamreports.weeklyreport.service;

import com.teamreports.weeklyreport.dto.user.CreateUserRequest;
import com.teamreports.weeklyreport.dto.user.UpdateUserRequest;
import com.teamreports.weeklyreport.dto.user.UserResponse;
import com.teamreports.weeklyreport.entity.User;
import com.teamreports.weeklyreport.exception.DuplicateResourceException;
import com.teamreports.weeklyreport.exception.ResourceNotFoundException;
import com.teamreports.weeklyreport.mapper.UserMapper;
import com.teamreports.weeklyreport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Admin/manager-only user management: invite team members, assign roles, activate/deactivate. */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userMapper.toResponse(findUserOrThrow(id));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.temporaryPassword()))
                .role(request.role())
                .active(true)
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);
        user.setRole(request.role());
        user.setActive(request.active());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = findUserOrThrow(id);
        user.setActive(false);
        userRepository.save(user);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
