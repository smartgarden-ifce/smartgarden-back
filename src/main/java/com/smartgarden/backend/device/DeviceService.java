package com.smartgarden.backend.device;

import com.smartgarden.backend.common.ResourceAlreadyExistsException;
import com.smartgarden.backend.device.dto.CreateDeviceRequest;
import com.smartgarden.backend.device.dto.DeviceResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> listDevices() {
        return deviceRepository.findAllByOrderByNameAsc().stream()
                .map(DeviceResponse::fromEntity)
                .toList();
    }

    @Transactional
    public DeviceResponse createDevice(CreateDeviceRequest request) {
        if (deviceRepository.existsByDeviceCode(request.deviceCode())) {
            throw new ResourceAlreadyExistsException("Ja existe um dispositivo com o codigo informado.");
        }

        Device device = new Device();
        device.setDeviceCode(request.deviceCode());
        device.setName(request.name());
        device.setLocation(request.location());
        device.setActive(true);

        return DeviceResponse.fromEntity(deviceRepository.save(device));
    }

    @Transactional
    public Device findOrCreateByCode(String deviceCode) {
        return deviceRepository.findByDeviceCode(deviceCode)
                .map(existing -> {
                    existing.setLastSeenAt(OffsetDateTime.now());
                    return existing;
                })
                .orElseGet(() -> {
                    Device device = new Device();
                    device.setDeviceCode(deviceCode);
                    device.setName("ESP32 " + deviceCode);
                    device.setActive(true);
                    device.setLastSeenAt(OffsetDateTime.now());
                    return deviceRepository.save(device);
                });
    }

    @Transactional(readOnly = true)
    public long countDevices() {
        return deviceRepository.count();
    }

    @Transactional(readOnly = true)
    public long countActiveDevices() {
        return deviceRepository.countByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Device> listDeviceEntities() {
        return deviceRepository.findAllByOrderByNameAsc();
    }
}
