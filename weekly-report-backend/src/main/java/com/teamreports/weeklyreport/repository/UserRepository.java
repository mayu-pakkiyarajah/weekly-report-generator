package com.teamreports.weeklyreport.repository;

import com.teamreports.weeklyreport.entity.User;
import com.teamreports.weeklyreport.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Page<User> findByRole(Role role, Pageable pageable);
    long countByRoleAndActiveTrue(Role role);
    List<User> findByRoleAndActiveTrue(Role role);
}
