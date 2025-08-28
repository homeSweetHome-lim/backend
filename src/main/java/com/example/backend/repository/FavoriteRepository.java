package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
}
