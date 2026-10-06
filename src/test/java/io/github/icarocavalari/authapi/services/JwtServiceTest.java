package io.github.icarocavalari.authapi.services;

import io.github.icarocavalari.authapi.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtServiceTest {

    JwtService jwtService;
    User user;
    String secretKey = "GqQ957o3/gMmrPI0O8VBK5iYNjexURa0QPhE91aRFcg=";
    long expiration = 9000000;

    @BeforeEach void createUserAndJwtService() {
        user = new User();
        user.setFullName("username");
        user.setEmail("test@example.com");

        jwtService = new JwtService(secretKey, expiration);
    }

    @Test
    void shouldReturnTheSameUsernameOnTheTokenPayload() {
        String jwt = jwtService.generateToken(user);
        String username = jwtService.extractUsername(jwt);
        assertThat(username).isEqualTo(user.getEmail());
    }

    @Test
    void shouldRejectTokenExpired() {
        final long ALREADY_EXPIRED = -100000;
        jwtService = new JwtService(secretKey, ALREADY_EXPIRED);
        String jwt = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(jwt, user)).isFalse();
    }

    @Test
    void shouldRejectAlteredToken() {
        String jwt = jwtService.generateToken(user);

        StringBuilder tampered = new StringBuilder(jwt);
        int index = jwt.lastIndexOf(".") + 10;

        tampered.setCharAt(index, jwt.charAt(index) == 'A' ? 'B' : 'A');
        jwt = tampered.toString();
        assertThat(jwtService.isTokenValid(jwt, user)).isFalse();
    }

    @Test
    void shouldRejectTokenWithWrongKey() {
        String jwt = jwtService.generateToken(user);

        JwtService wrongJwtService = new JwtService("NXh2EQbwUeOTr+KqG5mBvVFrtTxt+9ct50dxtiBC8Ec=", expiration);

        assertThat(wrongJwtService.isTokenValid(jwt, user)).isFalse();
    }

    @Test
    void shouldRejectTokenWithWrongUser() {
        String jwt = jwtService.generateToken(user);

        User wrongUser = new User();
        wrongUser.setFullName("Full name");
        wrongUser.setEmail("wrongemail@wrong.com");

        assertThat(jwtService.isTokenValid(jwt, wrongUser)).isFalse();
    }
}
