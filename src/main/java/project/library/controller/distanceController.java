package project.library.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import project.library.controller.dto.LocationDto;
@Slf4j
@RestController
@RequiredArgsConstructor
public class distanceController {

    @PostMapping("/location")
    public ResponseEntity<?> receiveLocation(@RequestBody LocationDto locationDto) {
        log.info("Received Latitude: " + locationDto.getLatitude());
        log.info("Received Longitude: " + locationDto.getLongitude());

        return ResponseEntity.ok("Location data received successfully.");
    }
}
