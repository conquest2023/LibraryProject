package project.library.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import project.library.repository.LibraryRepository;
import project.library.repository.collection.Library;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryServiceImpl implements  LibraryService{


    private final RedisTemplate redisTemplate;

    private final LibraryRepository repository;



    public List<Library> findAll(){

         return  repository.findAll();
    }





    public void putGeotRedisLibrary(){
        List<Library> all = repository.findAll();

        for (Library library : all) {
            redisTemplate.opsForGeo().add(
                    "libraries:locations", // Redis Key
                    new Point(library.getLongitude(), library.getLatitude()), // GeoLocation 객체
                    library.getLibCode() // Member
            );
        }
    }
}
