package com.example.backend.service;

import com.example.backend.common.CommonStatus;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.dto.request.AddFavoriteRequest;
import com.example.backend.dto.response.GetPropertyInfoResponse;
import com.example.backend.entity.Favorite;
import com.example.backend.entity.Property;
import com.example.backend.entity.PropertyDetail;
import com.example.backend.repository.FavoriteRepository;
import com.example.backend.repository.PropertyDetailRepository;
import com.example.backend.repository.PropertyRepository;
import com.example.backend.security.AuthUser;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyDetailRepository propertyDetailRepository;

    @Transactional
    public void addFavoriteProperty(AuthUser authUser, Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new BusinessException(CommonStatus.PROPERTY_NOT_FOUND));
        List<Favorite> favorites = favoriteRepository.findByUser(authUser.user());

        if (favoriteRepository.existsByPropertyAndUser(property, authUser.user())) {
            throw new BusinessException(CommonStatus.ALREADY_ADD_FAVORITE);
        }

        Favorite newFavorite = Favorite.builder()
            .property(property)
            .user(authUser.user())
            .createdAt(LocalDateTime.now())
            .build();
        favoriteRepository.save(newFavorite);
    }

    @Transactional
    public void deleteFavoriteProperty(AuthUser authUser, Long propertyId) {
        Property findProperty = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new BusinessException(CommonStatus.PROPERTY_NOT_FOUND));

        Favorite userFavorite = favoriteRepository.findByUserAndProperty(authUser.user(), findProperty)
            .orElseThrow(() -> new BusinessException(CommonStatus.FAVORITE_NOT_FOUND));

        favoriteRepository.delete(userFavorite);
    }

    public List<GetPropertyInfoResponse> getPropertyList(AuthUser authUser) {
        List<Long> userPropertyIds = favoriteRepository.findByUser(authUser.user())
            .stream().map(f -> f.getProperty().getPropertyId()).toList();

        List<Property> properties = propertyRepository.findAllById(userPropertyIds);

        return properties.stream().map(
            p -> GetPropertyInfoResponse.builder()
                .propertyId(p.getPropertyId())
                .state(p.getLawdCode().getState())
                .si(p.getLawdCode().getSi())
                .dong(p.getLawdCode().getDong())
                .maxPrice(getPrice(propertyDetailRepository.findByProperty(p).stream().map(
                        PropertyDetail::getPrice).toList())[1])
                .minPrice(getPrice(propertyDetailRepository.findByProperty(p).stream().map(
                        PropertyDetail::getPrice).toList())[0])
                .area(p.getArea())
                .floor(p.getFloor())
                .buildYear(p.getBuildYear())
                .dealDate(p.getDealDate())
                .aptName(p.getAptName())
                .propertyType(p.getPropertyType())
                .build()).toList();
    }

    private int[] getPrice(List<String> prices){
        int[] minMaxPrice = new int[2];
        IntSummaryStatistics stats = prices.stream()
                .mapToInt(p -> Integer.parseInt(p.replace(",", "")))
                .summaryStatistics();
        minMaxPrice[0] = stats.getMin();
        minMaxPrice[1] = stats.getMax();

        return minMaxPrice;
    }
}
