package com.example.ucakbiletotamasyonu.mapper;

import com.example.ucakbiletotamasyonu.dto.UserDto;
import com.example.ucakbiletotamasyonu.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto userToDto(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setCreateTime(user.getCreateTime());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        return dto;
    }
}
