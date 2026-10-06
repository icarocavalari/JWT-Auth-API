package io.github.icarocavalari.authapi.services;

import io.github.icarocavalari.authapi.dtos.LoginUserDto;
import io.github.icarocavalari.authapi.dtos.RegisterUserDto;
import io.github.icarocavalari.authapi.entities.User;
import io.github.icarocavalari.authapi.exceptions.EmailAlreadyExistsException;
import io.github.icarocavalari.authapi.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepo, PasswordEncoder encoder, AuthenticationManager authManager) {
        this.userRepository = userRepo;
        this.passwordEncoder = encoder;
        this.authenticationManager = authManager;
    }

    public User signup(RegisterUserDto input) {
        if (userRepository.findByEmail(input.email()).isPresent()) {
            throw new EmailAlreadyExistsException("Email Already exists");
        }

        User user = new User();
        user.setFullName(input.fullName());
        user.setEmail(input.email());
        user.setPassword(passwordEncoder.encode(input.password()));

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto user) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                user.email(), user.password()));

        return userRepository.findByEmail(user.email()).orElseThrow(() ->
                new BadCredentialsException("Wrong username/password"));
    }
}
