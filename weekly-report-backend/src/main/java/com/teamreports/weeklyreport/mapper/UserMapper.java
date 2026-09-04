package com.teamreports.weeklyreport.mapper;

import com.teamreports.weeklyreport.dto.user.UserResponse;
import com.teamreports.weeklyreport.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(),
                user.getRole(), user.isActive(), user.getCreatedAt());
    }
}
