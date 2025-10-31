package project.library.infrastructure.book;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import project.library.controller.dto.book.BookDetailDto;
import project.library.controller.dto.book.BookDto;
import project.library.controller.dto.book.search.BookSearchReseponseDto;

import java.net.URI;
import java.util.List;


@Component
@RequiredArgsConstructor
public class BookSearchClient {

    private final RestTemplate restTemplate;

    private static final String API_BASE_URL = "http://data4library.kr/api";

    private static final String AUTH_KEY = "1df2f040d9555558e014f541e2908356008ca9e3aa7a1d9c43ec2c15e54f5f4b";
    public List<BookDto> searchBook(String title){

        URI uri = UriComponentsBuilder
                .fromUriString(API_BASE_URL)
                .pathSegment("srchBooks")
                .queryParam("authKey", AUTH_KEY)
                .queryParam("title", title.replaceAll("\\s+", "")) // 입력받은 검색어
                .queryParam("sort","pubYear")
                .queryParam("pageNo", 1)
                .queryParam("pageSize", 40)
                .queryParam("format", "json")
                .encode() // URL에 포함될 수 없는 문자를 인코딩
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



        // Java Stream을 사용해 더 간결하게 책 목록만 추출
        return docWrappers.stream()
                .map(BookDetailDto::getDoc)
                .toList();
    }


}
