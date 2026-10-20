package io.github.icarocavalari.authapi.services;

import io.github.icarocavalari.authapi.dtos.UserDto;
import io.github.icarocavalari.authapi.entities.RoleEnum;
import io.github.icarocavalari.authapi.entities.User;
import io.github.icarocavalari.authapi.exceptions.UserNotFoundException;
import io.github.icarocavalari.authapi.repositories.RoleRepository;
import io.github.icarocavalari.authapi.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<UserDto> allUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getCreatedAt()))
                .toList();
    }

    @Transactional
    public UserDto createAdmin(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException("User was not found"));
        user.addRole(roleRepository.findByName(RoleEnum.ADMIN).orElseThrow());
        userRepository.save(user);

        return new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getCreatedAt());
    }
}
