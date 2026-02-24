package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where u.username = :username and u.isActive is true")
    Optional<User> findByUsernameAndIsActive(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}