package project.library.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import project.library.controller.dto.LocationDto;
import project.library.service.LibraryServiceImpl;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DistanceController {

    private final LibraryServiceImpl libraryService;
    @PostMapping("/location")
    public ResponseEntity<?> receiveLocation(@RequestBody LocationDto locationDto) {
        log.info("Received Latitude: " + locationDto.getLatitude());
        log.info("Received Longitude: " + locationDto.getLongitude());
        libraryService.calculateDistance(locationDto);
        return ResponseEntity.ok("Location data received successfully.");
    }
}
