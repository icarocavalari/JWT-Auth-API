package io.github.icarocavalari.authapi.services;

import io.github.icarocavalari.authapi.dtos.LoginUserDto;
import io.github.icarocavalari.authapi.dtos.RegisterUserDto;
import io.github.icarocavalari.authapi.entities.Role;
import io.github.icarocavalari.authapi.entities.User;
import io.github.icarocavalari.authapi.exceptions.EmailAlreadyExistsException;
import io.github.icarocavalari.authapi.repositories.RoleRepository;
import io.github.icarocavalari.authapi.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static io.github.icarocavalari.authapi.entities.RoleEnum.USER;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    RoleRepository roleRepo;

    @InjectMocks
    AuthenticationService authenticationService;

    @Test
    void shouldRejectDuplicateEmailAndDoNotSaveData() {
        RegisterUserDto input = new RegisterUserDto("test@example.com", "Testing Method", "plainpassword");

        User user = new User(input.fullName(), input.email(), input.password());

        when(userRepository.findByEmail(input.email())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> {
            authenticationService.signup(input);
        }).isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldNotStorePlainPassword() {
        RegisterUserDto input = new RegisterUserDto("test@example.com", "Testing Method", "plainpassword");

        when(passwordEncoder.encode("plainpassword")).thenReturn("drowssapnialp");
        when(roleRepo.findByName(USER)).thenReturn(Optional.of(new Role(USER)));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authenticationService.signup(input);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertThat(savedUser.getPassword()).isEqualTo("drowssapnialp");
    }

    @Test
    void shouldReturnUserCorrectlyAuthenticated() {
        LoginUserDto input = new LoginUserDto("test@example.com", "drowssapnialp");
        User user = new User("joaozinho123", input.email(), input.password());

        when(userRepository.findByEmail(input.email())).thenReturn(Optional.of(user));

        User authenticatedUser = authenticationService.authenticate(input);
        assertThat(authenticatedUser).isSameAs(user);
    }

    @Test
    void shouldNotAuthenticateUserWithWrongPassword() {
        LoginUserDto user = new LoginUserDto("test@example.com", "senha123");

        when(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException.class);

        assertThatThrownBy(() -> {
            authenticationService.authenticate(user);
        }).isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void shouldNotAuthenticateUserThatWasNotFound() {
        LoginUserDto user = new LoginUserDto("test@example.com", "senha123");

        assertThatThrownBy(() -> {
            authenticationService.authenticate(user);
        }).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldHaveUserRoleAfterRegistration() {
        RegisterUserDto input = new RegisterUserDto("test@example.com", "Testing Method", "plainpassword");

        when(roleRepo.findByName(USER)).thenReturn(Optional.of(new Role(USER)));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authenticationService.signup(input);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertThat(savedUser.getRoles()).extracting(Role::getName).contains(USER);

    }
}
