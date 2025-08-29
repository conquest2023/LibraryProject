package project.library.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.library.controller.dto.LocationDto;
import project.library.controller.dto.book.NearestLibrary;
import project.library.repository.collection.Library;
import project.library.service.LibraryServiceImpl;
import project.library.service.NearestLibraryDetail;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DistanceController {

    private final LibraryServiceImpl libraryService;
    @PostMapping("/location")
    public ResponseEntity<?> receiveLocation(@RequestBody LocationDto locationDto) {
        List<NearestLibraryDetail> nearestLibraries = libraryService.calculateDistance(locationDto);
        return ResponseEntity.ok(Map.of("nearest",nearestLibraries));
    }


}
