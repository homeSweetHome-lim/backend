package com.example.backend.repository;

import com.example.backend.entity.Favorite;
import com.example.backend.entity.Property;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Favorite;
import com.example.backend.security.AuthUser;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(User user);

    boolean existsByPropertyAndUser(Property property, User user);

    Optional<Favorite> findByUserAndProperty(User user, Property property);
}
