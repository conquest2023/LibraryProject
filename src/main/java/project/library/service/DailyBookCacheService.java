package project.library.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import project.library.controller.dto.book.BookDetailDto;
import project.library.controller.dto.book.BookDto;
import project.library.controller.dto.book.search.BookSearchReseponseDto;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DailyBookCacheService {


    private final RedisTemplate redisTemplate;

    private final RestTemplate restTemplate;


    private final ObjectMapper om;
    private  static final  String  REDIS_GEOKEY="libraries:locations";

    private static final String API_BASE_URL = "http://data4library.kr/api";

    private static final String AUTH_KEY = "1df2f040d9555558e014f541e2908356008ca9e3aa7a1d9c43ec2c15e54f5f4b";

    public List<BookDto> getLatestDailyBooks() {
        String json = (String) redisTemplate.opsForValue().get("d4lib:daily:latest");
        if (json == null)
            return List.of();
        try {
            JavaType t = om.getTypeFactory().constructCollectionType(List.class, BookDto.class);
            return om.readValue(json, t);
        } catch (Exception e) {
            return List.of();
        }
    }
    public void findBeforeOneDayIncreaseBook() {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        String key = "d4lib:daily:" + today;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key)))
            return;

        List<BookDto> all = new ArrayList<>();

        URI uri = UriComponentsBuilder
                .fromUriString(API_BASE_URL)
                .pathSegment("srchBooks")
                .queryParam("authKey", AUTH_KEY)
                .queryParam("sort", "pubYear")
                .queryParam("pageNo", 1)
                .queryParam("pageSize", 10)
                .queryParam("format", "json")
                .queryParam("searchDt", String.valueOf(LocalDate.now()))
                .encode()
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.ALL));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<BookSearchReseponseDto> responseEntity =
                restTemplate.exchange(uri, HttpMethod.GET, entity, BookSearchReseponseDto.class);

        // 2. DTO 객체로 깔끔하게 데이터 받기
        BookSearchReseponseDto searchResponse = responseEntity.getBody();


        List<BookDetailDto> docWrappers = searchResponse.getResponse().getDocs();


        all.addAll(docWrappers.stream().map(BookDetailDto::getDoc).toList());

        try {
            String json = om.writeValueAsString(all);
            redisTemplate.opsForValue().set(key, json, Duration.ofDays(1));
            // 최신 별칭 키 업데이트(읽기 단순화)
            redisTemplate.opsForValue().set("d4lib:daily:latest", json, Duration.ofDays(2));
        } catch (JsonProcessingException e) {
            // 실패 시 어제 데이터로 폴백
            String yKey = "d4lib:daily:" + today.minusDays(1);
            String yesterday = (String) redisTemplate.opsForValue().get(yKey);
            if (yesterday != null) {
                redisTemplate.opsForValue().set("d4lib:daily:latest", yesterday, Duration.ofDays(1));
            }
        }

    }
}
