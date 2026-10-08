package io.github.icarocavalari.authapi.services;

import io.github.icarocavalari.authapi.dtos.UserDto;
import io.github.icarocavalari.authapi.entities.User;
import io.github.icarocavalari.authapi.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock UserRepository userRepo;
    @InjectMocks UserService userService;

    @Test
    void shouldReturnCorrectNumberOfUsersAndCorrectData() {
        User first = new User("First user test", "first@example.com", "123123");
        User second = new User("Second user test", "second@example.com", "321321");

        when(userRepo.findAll()).thenReturn(List.of(first, second));

        List<UserDto> usersFromService = userService.allUsers();

        assertThat(usersFromService.size()).isEqualTo(2);


        assertThat(usersFromService)
                .extracting(UserDto::username)
                .containsExactly("first@example.com", "second@example.com");

        assertThat(usersFromService)
                .extracting(UserDto::fullName)
                .containsExactly("First user test", "Second user test");
    }
}
