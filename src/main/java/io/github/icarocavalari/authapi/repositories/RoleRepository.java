package io.github.icarocavalari.authapi.repositories;

import io.github.icarocavalari.authapi.entities.Role;
import io.github.icarocavalari.authapi.entities.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleEnum name);
}
