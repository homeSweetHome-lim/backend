package com.example.backend.controller;

import java.util.List;

import com.example.backend.common.response.ApiResponse;
import com.example.backend.common.response.ApiResponseFactory;
import com.example.backend.dto.request.AddFavoriteRequest;
import com.example.backend.dto.response.GetPropertyInfoResponse;
import com.example.backend.entity.Favorite;
import com.example.backend.security.AuthUser;
import com.example.backend.service.FavoriteService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/favorite")
@RestController
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(description = "Property id를 통해 유저가 좋아요한 매물을 저장")
    @PostMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long propertyId
            ) {
        favoriteService.addFavoriteProperty(authUser, propertyId);
        return ApiResponseFactory.success();
    }

    @Operation(description = "저장된 좋아요 중 유저가 원하는 property id로 favorite 테이블에서 찾아서 해당 property 삭제")
    @DeleteMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<Void>> deleteFavorite(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long propertyId
    ){
        favoriteService.deleteFavoriteProperty(authUser, propertyId);
        return ApiResponseFactory.success();
    }

    @Operation(description = "favorite에 저장된 property의 정보 리스트를 반환")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GetPropertyInfoResponse>>> getFavorites(
        @AuthenticationPrincipal AuthUser authUser
    ){
        return ApiResponseFactory.success(favoriteService.getPropertyList(authUser));
    }
}
