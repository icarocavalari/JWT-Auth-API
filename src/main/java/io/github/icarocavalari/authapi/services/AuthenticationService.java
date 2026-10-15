package io.github.icarocavalari.authapi.services;

import io.github.icarocavalari.authapi.dtos.LoginUserDto;
import io.github.icarocavalari.authapi.dtos.RegisterUserDto;
import io.github.icarocavalari.authapi.dtos.UserDto;
import io.github.icarocavalari.authapi.entities.Role;
import io.github.icarocavalari.authapi.entities.RoleEnum;
import io.github.icarocavalari.authapi.entities.User;
import io.github.icarocavalari.authapi.exceptions.EmailAlreadyExistsException;
import io.github.icarocavalari.authapi.repositories.RoleRepository;
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
    private final RoleRepository roleRepo;

    public AuthenticationService(UserRepository userRepo, PasswordEncoder encoder, AuthenticationManager authManager,
                                 RoleRepository roleRepo) {
        this.userRepository = userRepo;
        this.passwordEncoder = encoder;
        this.authenticationManager = authManager;
        this.roleRepo = roleRepo;
    }

    public UserDto signup(RegisterUserDto input) {
       // Checando se o email ja existe no banco de dados
        if (userRepository.findByEmail(input.email()).isPresent()) {
            throw new EmailAlreadyExistsException("Email Already exists");
        }

        // Preparando a role user
        Role roleUser = roleRepo.findByName(RoleEnum.USER).orElseThrow();

        // Criando o usuário com as credenciais do request body e criptografando a senha
        User user = new User(input.fullName(), input.email(), passwordEncoder.encode(input.password()));
        // Adicionando a role de User no usuário novo
        user.addRole(roleUser);

        // Salvando o objeto no banco de dados
        userRepository.save(user);
        // Retornando o novo usuário criado
        return new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getCreatedAt());
    }

    public User authenticate(LoginUserDto user) {
        // Checando se as credenciais do usuário batem com o banco de dados
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                user.email(), user.password()));

        return userRepository.findByEmail(user.email()).orElseThrow(() ->
                new BadCredentialsException("Wrong username/password"));
    }
}
