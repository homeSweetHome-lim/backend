package com.example.backend.repository;

import com.example.backend.entity.Favorite;
import com.example.backend.entity.Property;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Favorite;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(User user);

    boolean findByProperty(Property property);

    boolean existsByProperty(Property property);
}
