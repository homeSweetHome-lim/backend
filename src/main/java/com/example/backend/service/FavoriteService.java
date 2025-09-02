package com.example.backend.service;

import com.example.backend.common.CommonStatus;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.dto.request.AddFavoriteRequest;
import com.example.backend.entity.Favorite;
import com.example.backend.entity.Property;
import com.example.backend.repository.FavoriteRepository;
import com.example.backend.repository.PropertyRepository;
import com.example.backend.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PropertyRepository propertyRepository;

    public void addFavoriteProperty(AuthUser authUser, AddFavoriteRequest request) {
        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new BusinessException(CommonStatus.PROPERTY_NOT_FOUND));
        List<Favorite> favorites = favoriteRepository.findByUser(authUser.user());

        if (favoriteRepository.existsByProperty(property)) {
            throw new BusinessException(CommonStatus.ALREADY_ADD_FAVORITE);
        }
    }
}
