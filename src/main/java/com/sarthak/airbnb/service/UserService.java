package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.ProfileUpdateRequestDto;
import com.sarthak.airbnb.dto.UserDto;
import com.sarthak.airbnb.entity.User;

public interface UserService {
    User getUserById(Long userId);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
