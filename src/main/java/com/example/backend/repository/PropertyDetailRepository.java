package com.example.backend.repository;

import com.example.backend.entity.Property;
import com.example.backend.entity.PropertyDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PropertyDetailRepository extends JpaRepository<PropertyDetail,Long> {
    List<PropertyDetail> findByProperty(Property property);

    List<PropertyDetail> findByPropertyIn(Collection<Property> properties);
    
    /**
     * 특정 아파트 이름의 모든 PropertyDetail에서 최소/최대 가격을 조회
     * @param aptName 아파트 이름
     * @return [최소가격, 최대가격] 배열 (데이터가 없으면 null 반환)
     */
    @Query("SELECT MIN(CAST(REPLACE(REPLACE(pd.price, ',', ''), ' ', '') AS long)), " +
           "MAX(CAST(REPLACE(REPLACE(pd.price, ',', ''), ' ', '') AS long)) " +
           "FROM PropertyDetail pd JOIN pd.property p " +
           "WHERE p.aptName = :aptName AND pd.price IS NOT NULL AND pd.price != ''")
    Object[] findMinMaxPriceByAptName(@Param("aptName") String aptName);

    /**
     * 여러 아파트의 최소/최대 가격을 한 번에 조회 (효율적인 일괄 처리용)
     * @param aptNames 아파트 이름 목록
     * @return 아파트 이름, 최소가격, 최대가격을 포함하는 결과 리스트
     */
    @Query("SELECT p.aptName, " +
           "MIN(CAST(REPLACE(REPLACE(pd.price, ',', ''), ' ', '') AS long)), " +
           "MAX(CAST(REPLACE(REPLACE(pd.price, ',', ''), ' ', '') AS long)) " +
           "FROM PropertyDetail pd JOIN pd.property p " +
           "WHERE p.aptName IN :aptNames AND pd.price IS NOT NULL AND pd.price != '' " +
           "GROUP BY p.aptName")
    List<Object[]> findMinMaxPricesByAptNames(@Param("aptNames") Collection<String> aptNames);

    /**
     * 특정 아파트 이름의 모든 PropertyDetail에서 최소/최대 면적을 조회
     * @param aptName 아파트 이름
     * @return [최소면적, 최대면적] 배열 (데이터가 없으면 null 반환)
     */
    @Query("SELECT MIN(pd.area), MAX(pd.area) " +
           "FROM PropertyDetail pd JOIN pd.property p " +
           "WHERE p.aptName = :aptName AND pd.area IS NOT NULL")
    Object[] findMinMaxAreaByAptName(@Param("aptName") String aptName);

    /**
     * 여러 아파트의 최소/최대 면적을 한 번에 조회 (효율적인 일괄 처리용)
     * @param aptNames 아파트 이름 목록
     * @return 아파트 이름, 최소면적, 최대면적을 포함하는 결과 리스트
     */
    @Query("SELECT p.aptName, MIN(pd.area), MAX(pd.area) " +
           "FROM PropertyDetail pd JOIN pd.property p " +
           "WHERE p.aptName IN :aptNames AND pd.area IS NOT NULL " +
           "GROUP BY p.aptName")
    List<Object[]> findMinMaxAreasByAptNames(@Param("aptNames") Collection<String> aptNames);
}
