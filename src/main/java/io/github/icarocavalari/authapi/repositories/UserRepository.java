package io.github.icarocavalari.authapi.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.github.icarocavalari.authapi.entities.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
}
