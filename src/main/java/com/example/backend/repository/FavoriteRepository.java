package com.example.backend.repository;

import com.example.backend.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite,Integer> {
}
