package com.smartgarden.backend.device;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceCode(String deviceCode);

    boolean existsByDeviceCode(String deviceCode);

    long countByActiveTrue();

    List<Device> findAllByOrderByNameAsc();
}
