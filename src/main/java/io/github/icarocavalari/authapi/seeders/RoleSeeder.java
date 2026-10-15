package io.github.icarocavalari.authapi.seeders;

import io.github.icarocavalari.authapi.entities.Role;
import io.github.icarocavalari.authapi.entities.RoleEnum;
import io.github.icarocavalari.authapi.entities.User;
import io.github.icarocavalari.authapi.repositories.RoleRepository;
import io.github.icarocavalari.authapi.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleSeeder implements ApplicationRunner {
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;


    public RoleSeeder(RoleRepository roleRepo, UserRepository userRepo, PasswordEncoder passwordEncoder, @Value("${super-admin.email}") String email,
                      @Value("${super-admin.password}") String password) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedRoles();
        seedSuperAdmin();
    }

    private void seedRoles() {
        // Extraindo todos as roles
        RoleEnum[] roles = RoleEnum.values();

        // Inicilizando as roles que não foram inicializadas ainda
        for (RoleEnum roleEnum: roles) {
            if (roleRepo.findByName(roleEnum).isEmpty()) {
                roleRepo.save(new Role(roleEnum));
            }
        }
    }

    private void seedSuperAdmin() {
        // Criando o super admin
        if (userRepo.findByEmail(email).isEmpty()) {
            User superAdmin = new User("Super Admin", email, passwordEncoder.encode(password));
            Role role = roleRepo.findByName(RoleEnum.SUPER_ADMIN).orElseThrow();

            superAdmin.addRole(role);
            userRepo.save(superAdmin);
        }
    }
}
