package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.ProfileUpdateRequestDto;
import com.sarthak.airbnb.dto.UserDto;
import com.sarthak.airbnb.entity.User;
import com.sarthak.airbnb.exceptions.ResourceNotFoundException;
import com.sarthak.airbnb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.sarthak.airbnb.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    @Override
    public User getUserById(Long userId) {
        return  userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User not found with id :"+userId));
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto) {
        User user = getCurrentUser();
        if(profileUpdateRequestDto.getDateOfBirth()!=null)user.setDateOfBirth(profileUpdateRequestDto.getDateOfBirth());
        if(profileUpdateRequestDto.getGender()!= null)user.setGender(profileUpdateRequestDto.getGender());
        if(profileUpdateRequestDto.getName()!=null) user.setName(profileUpdateRequestDto.getName());

        userRepository.save(user);
    }

    @Override
    public UserDto getMyProfile() {
        User user = getCurrentUser();
        log.info("Getting the profile profile for user with id : {}",user.getId());
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElse(null);
    }
}
