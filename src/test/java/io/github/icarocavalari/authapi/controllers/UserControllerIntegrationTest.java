package io.github.icarocavalari.authapi.controllers;

import io.github.icarocavalari.authapi.TestcontainersConfiguration;
import io.github.icarocavalari.authapi.dtos.LoginUserDto;
import io.github.icarocavalari.authapi.dtos.RegisterUserDto;
import io.github.icarocavalari.authapi.dtos.UserDto;
import io.github.icarocavalari.authapi.entities.Role;
import io.github.icarocavalari.authapi.entities.RoleEnum;
import io.github.icarocavalari.authapi.entities.User;
import io.github.icarocavalari.authapi.repositories.RoleRepository;
import io.github.icarocavalari.authapi.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.NoSuchElementException;
import java.util.Optional;

import static io.github.icarocavalari.authapi.entities.RoleEnum.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class UserControllerIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository userRepo;
    @Autowired
    RoleRepository roleRepo;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void shouldReturnUnauthorizedForAnonymousRequest() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedForInvalidToken() throws Exception {
        mockMvc.perform(get("/users")
                        .headers(header -> header.setBearerAuth("32323232")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void shouldSignupUserAndHidePasswordFromResponse() throws Exception {
        RegisterUserDto input = new RegisterUserDto(
                "joaodasilva@gmail.com", "Joao Da Silva", "joao123");

        mockMvc.perform(post("/auth/signup")
                .content(mapper.writeValueAsString(input))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("joaodasilva@gmail.com"))
                .andExpect(jsonPath("$.fullName").value("Joao Da Silva"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.id").exists());

    }

    @Test
    @Transactional
    void shouldAccessOwnProfileAndReturnCorrectData() throws Exception {
        RegisterUserDto input = new RegisterUserDto(
                "joaodasilva@gmail.com", "Joao Da Silva", "joao123");

        User savedUser = new User(input.fullName(), input.email(), passwordEncoder.encode(input.password()));
        savedUser.addRole(roleRepo.findByName(RoleEnum.USER).orElseThrow());
        savedUser = userRepo.save(savedUser);

        LoginUserDto loginUserDto = new LoginUserDto(savedUser.getUsername(), input.password());

        MockHttpServletResponse response = mockMvc.perform(post("/auth/login")
                        .content(mapper.writeValueAsString(loginUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        String body = response.getContentAsString();
        String token = mapper.readTree(body).get("token").asString();

        mockMvc.perform(get("/users/me")
                        .headers(header -> header.setBearerAuth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.username").value(savedUser.getUsername()))
                .andExpect(jsonPath("$.fullName").value(savedUser.getFullName()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @Transactional
    void shouldNotRegisterUserWithEmailAlreadyUsed() throws Exception {
        RegisterUserDto input = new RegisterUserDto("enzogomes@gmail.com", "Enzo Gomes", "enzo123");

        userRepo.save(new User(input.fullName(), input.email(), passwordEncoder.encode(input.password())));

        RegisterUserDto sameEmail = new RegisterUserDto("enzogomes@gmail.com", "Gomes Enzo", "123enzo");

        mockMvc.perform(post("/auth/signup")
                        .content(mapper.writeValueAsString(sameEmail))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void shouldNotAccessAllUsersWithoutRolePermission() throws Exception {
        String token = registerLogInAndAuthenticateUser(RoleEnum.USER);

        mockMvc.perform(get("/users")
                .headers(header -> header.setBearerAuth(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void shouldReturnAllUsersWithAdminRole() throws Exception {
        String token = registerLogInAndAuthenticateUser(RoleEnum.ADMIN);

        mockMvc.perform(get("/users")
                .headers(header -> header.setBearerAuth(token)))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void shouldNotAllowUserCreateAdmin() throws Exception {
        String token = registerLogInAndAuthenticateUser(RoleEnum.USER);

        User savedUser = createUser();

        mockMvc.perform(put("/users/{id}/admin", savedUser.getId())
                        .headers(header -> header.setBearerAuth(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void shouldNotAllowAdminCreateAnotherAdmin() throws Exception {
        String token = registerLogInAndAuthenticateUser(RoleEnum.ADMIN);

        User savedUser = createUser();

        mockMvc.perform(put("/users/{id}/admin", savedUser.getId())
                        .headers(header -> header.setBearerAuth(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void shouldCreateAdminWithSuperAdminRole() throws Exception {
        String token = registerLogInAndAuthenticateUser(RoleEnum.SUPER_ADMIN);

        User savedUser = createUser();

        mockMvc.perform(put("/users/{id}/admin", savedUser.getId())
                        .headers(header -> header.setBearerAuth(token)))
                .andExpect(status().isOk());

        assertThat(savedUser.getRoles()).extracting(Role::getName).contains(ADMIN);
    }

    @Test
    @Transactional
    void shouldNotCreateAdminWithWrongId() throws Exception {
        String token = registerLogInAndAuthenticateUser(RoleEnum.SUPER_ADMIN);

        mockMvc.perform(put("/users/{id}/admin", 22343434)
                        .headers(header -> header.setBearerAuth(token)))
                .andExpect(status().isNotFound());
    }

    private String registerLogInAndAuthenticateUser(RoleEnum roleEnum) throws Exception {
        RegisterUserDto input = new RegisterUserDto(
                "joaodasilva@gmail.com", "Joao Da Silva", "joao123");

        User savedUser = new User(input.fullName(), input.email(), passwordEncoder.encode(input.password()));
        savedUser.addRole(roleRepo.findByName(roleEnum).orElseThrow());
        savedUser = userRepo.save(savedUser);

        LoginUserDto loginUserDto = new LoginUserDto(savedUser.getUsername(), input.password());

        MockHttpServletResponse response = mockMvc.perform(post("/auth/login")
                        .content(mapper.writeValueAsString(loginUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        String body = response.getContentAsString();
        return mapper.readTree(body).get("token").asString();
    }

    private User createUser() {
        User savedUser = new User("Fabin de Cria", "fabindecria@gmail.com", passwordEncoder.encode("fabin123"));
        savedUser.addRole(roleRepo.findByName(RoleEnum.USER).orElseThrow());
        return userRepo.save(savedUser);
    }
}
