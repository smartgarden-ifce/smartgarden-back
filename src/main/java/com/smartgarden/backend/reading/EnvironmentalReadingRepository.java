package com.smartgarden.backend.reading;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface EnvironmentalReadingRepository extends JpaRepository<EnvironmentalReading, Long> {

    List<EnvironmentalReading> findByDeviceDeviceCodeOrderByRecordedAtDesc(String deviceCode, Pageable pageable);

    List<EnvironmentalReading> findByRecordedAtBetweenOrderByRecordedAtDesc(
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            Pageable pageable
    );

    List<EnvironmentalReading> findByDeviceDeviceCodeAndRecordedAtBetweenOrderByRecordedAtDesc(
            String deviceCode,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            Pageable pageable
    );

    List<EnvironmentalReading> findAllByOrderByRecordedAtDesc(Pageable pageable);

    Optional<EnvironmentalReading> findFirstByDeviceDeviceCodeOrderByRecordedAtDesc(String deviceCode);
}

