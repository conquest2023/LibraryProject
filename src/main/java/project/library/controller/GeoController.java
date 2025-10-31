package project.library.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import project.library.service.domain.LibraryGeoService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class GeoController {



    private final LibraryGeoService libraryGeoService;
    @GetMapping("/test")
    public ResponseEntity<?> getAllLibrary(){

        libraryGeoService.putGeotRedisLibrary();

        return ResponseEntity.of(Optional.of(Map.of("library", "OK")));
    }


    @GetMapping("/put")
    public ResponseEntity<?> putLibrary(){

        libraryGeoService.putRedisLibrary();

        return ResponseEntity.of(Optional.of(Map.of("library", "OK")));
    }
}
