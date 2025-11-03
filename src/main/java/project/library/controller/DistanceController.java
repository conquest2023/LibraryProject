package project.library.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.library.controller.dto.UserLocation;
import project.library.service.LibraryService;
import project.library.repository.NearestLibraryDetail;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DistanceController {


    private final LibraryService calculateService;

    @PostMapping("/location")
    public ResponseEntity<?> receiveLocation(@RequestBody UserLocation locationDto) {
        log.info(locationDto.toString());
        long start = System.currentTimeMillis();
        List<NearestLibraryDetail> nearestLibraries = calculateService.findNearbyLibrary(locationDto);
        long end = System.currentTimeMillis();
        log.info("resultTime={}",end-start);
        return ResponseEntity.ok(Map.of("nearest",nearestLibraries));
    }


    @PostMapping("/location/redis")
    public ResponseEntity<?> receiveTestLocation(@RequestBody UserLocation locationDto) {
        long start = System.currentTimeMillis();
        List<NearestLibraryDetail> nearestLibraries = calculateService.findTestNearbyLibrary(locationDto);
        long end = System.currentTimeMillis();
        log.info("resultTime={}",end-start);
        return ResponseEntity.ok(Map.of("nearest",nearestLibraries));
    }


}
