package com.smartgarden.backend.reading;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface EnvironmentalReadingRepository extends JpaRepository<EnvironmentalReading, Long>,
        JpaSpecificationExecutor<EnvironmentalReading> {

    @Override
    @EntityGraph(attributePaths = "device")
    org.springframework.data.domain.Page<EnvironmentalReading> findAll(
            Specification<EnvironmentalReading> specification,
            org.springframework.data.domain.Pageable pageable
    );

    Optional<EnvironmentalReading> findFirstByDeviceDeviceCodeOrderByRecordedAtDesc(String deviceCode);

    @Query("""
            select distinct r
            from EnvironmentalReading r
            join fetch r.device d
            where r.recordedAt = (
                select max(r2.recordedAt)
                from EnvironmentalReading r2
                where r2.device = r.device
            )
            order by d.name asc
            """)
    List<EnvironmentalReading> findLatestReadingPerDevice();

    long countByRecordedAtGreaterThanEqual(OffsetDateTime startAt);

    @Query("select avg(r.temperatureC) from EnvironmentalReading r where r.recordedAt >= :startAt")
    Double averageTemperatureSince(OffsetDateTime startAt);

    @Query("select avg(r.humidityPercent) from EnvironmentalReading r where r.recordedAt >= :startAt")
    Double averageHumiditySince(OffsetDateTime startAt);
}
