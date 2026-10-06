package io.github.icarocavalari.authapi.services;

import io.github.icarocavalari.authapi.dtos.UserDto;
import io.github.icarocavalari.authapi.entities.User;
import io.github.icarocavalari.authapi.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> allUsers() {
        List<UserDto> users = new ArrayList<>();

        for (User user: userRepository.findAll()) {
            users.add(new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getCreatedAt()));
        }

        return users;
    }

    public UserDto getUser(Object principal) {
        User user = (User) principal;

        return new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getCreatedAt());
    }
}
