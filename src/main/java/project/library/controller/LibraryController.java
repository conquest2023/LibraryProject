package project.library.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import project.library.controller.dto.book.BookDto;
import project.library.controller.dto.book.LibraryResponseDto;
import project.library.controller.dto.book.search.BookDocDto;
import project.library.controller.dto.book.search.BookSearchResponseDto;
import project.library.controller.dto.book.search.DocWrapperDto;

import java.net.URI;
import java.util.List;
import java.util.Map;


@Slf4j
@Controller
@RequiredArgsConstructor
public class LibraryController {

    private final RestTemplate restTemplate;

    private static final String API_BASE_URL = "http://data4library.kr/api";

    private static final String AUTH_KEY = "1df2f040d9555558e014f541e2908356008ca9e3aa7a1d9c43ec2c15e54f5f4b";


    @GetMapping("/")
    public String  indexPage(){
        return "/index.html";
    }

    @GetMapping("/search/book")
    @ResponseBody
    public ResponseEntity<?> getIsbn(@RequestParam String title){

        URI uri = UriComponentsBuilder
                .fromUriString(API_BASE_URL)
                .pathSegment("srchBooks")
                .queryParam("authKey", AUTH_KEY)
                .queryParam("title", title) // 입력받은 검색어
                .queryParam("pageNo", 1)
                .queryParam("pageSize", 20)
                .queryParam("format", "json") // 응답 형식을 JSON으로 지정
                .encode() // URL에 포함될 수 없는 문자를 인코딩
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.ALL));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<BookSearchResponseDto> responseEntity =
                restTemplate.exchange(uri, HttpMethod.GET, entity, BookSearchResponseDto.class);

// 2. DTO 객체로 깔끔하게 데이터 받기
        BookSearchResponseDto searchResponse = responseEntity.getBody();

// 3. 안전하고 명확하게 데이터 접근
        if (searchResponse != null && searchResponse.getResponse() != null) {
            List<DocWrapperDto> docWrappers = searchResponse.getResponse().getDocs();

            // Java Stream을 사용해 더 간결하게 책 목록만 추출
            List<BookDocDto> books = docWrappers.stream()
                    .map(DocWrapperDto::getDoc)
                    .toList();

            for (BookDocDto book : books) {
                log.info("책 제목: {}", book.getBookname());
                log.info("저자: {}", book.getAuthors());
            }
        }
        return  responseEntity;
    }

    @GetMapping("/detail/book")
    @ResponseBody
    public ResponseEntity<?> getBookDetail(@RequestParam String isbn){

        URI uri = UriComponentsBuilder
                .fromUriString(API_BASE_URL)
                .pathSegment("srchDtlList")
                .queryParam("authKey", AUTH_KEY)
                .queryParam("isbn13",  isbn)
                .queryParam("format", "json")
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        // 이 API는 Accept 헤더를 보내면 406 오류를 발생시키므로, 헤더를 비워두거나
        // 서버가 확실히 받아들일 만한 다른 타입으로 설정할 수 있습니다.
        // 여기서는 가장 일반적인 '어떤 것이든 받겠다'는 의미로 설정합니다.
        headers.setAccept(List.of(MediaType.ALL));

        // 3. 헤더를 담은 HttpEntity 객체를 생성합니다.
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<LibraryResponseDto> responseEntity =
                restTemplate.exchange(uri, HttpMethod.GET, entity, LibraryResponseDto.class);
        // 2. 응답 본문(Body)을 DTO 객체로 받음
        LibraryResponseDto libraryResponse = responseEntity.getBody();

        // 3. getter를 이용해 원하는 값에 쉽게 접근
        if (libraryResponse != null && libraryResponse.getResponse() != null &&
                !libraryResponse.getResponse().getDetail().isEmpty()) {

            // 첫 번째 책 정보를 가져옴
            BookDto book = libraryResponse.getResponse().getDetail().get(0).getBook();

            String bookTitle = book.getBookname();
            String author = book.getAuthors();


            // 클라이언트에게는 책 정보 객체(book)만 반환하거나, 가공해서 반환
            return ResponseEntity.ok(book);
        }

        return ResponseEntity.notFound().build();

    }


    @GetMapping("/recommend/book")
    @ResponseBody
    public ResponseEntity<?> getRecommendBooks(@RequestParam String isbn){

        URI uri = UriComponentsBuilder
                .fromUriString(API_BASE_URL)
                .pathSegment("recommandList")
                .queryParam("authKey", AUTH_KEY)
                .queryParam("type","reader")
                .queryParam("isbn", isbn)
                .encode()
                .build()
                .toUri();

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        return responseEntity;

    }



    @GetMapping("/search/library")
    @ResponseBody
    public ResponseEntity<?> getLibrary(@RequestParam String isbn,
                                        @RequestParam int region){

        URI uri = UriComponentsBuilder
                .fromUriString(API_BASE_URL)
                .pathSegment("libSrchByBook")
                .queryParam("authKey", AUTH_KEY)
                .queryParam("isbn", isbn) // 입력받은 검색어
                .queryParam("region",region)
                .queryParam("pageNo", 1)
                .queryParam("pageSize", 50)
                .queryParam("format", "json") // 응답 형식을 JSON으로 지정
                .encode() // URL에 포함될 수 없는 문자를 인코딩
                .build()
                .toUri();

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        return responseEntity;

    }

    @GetMapping("/check/book")
    public ResponseEntity<?> loanCheckBook(@RequestParam String isbn,
                                            @RequestParam String libCode) {

        URI uri = UriComponentsBuilder
                .fromUriString(API_BASE_URL)
                .pathSegment("bookExist")
                .queryParam("authKey", AUTH_KEY)
                .queryParam("libCode",libCode)
                .queryParam("isbn13", isbn)
                .queryParam("format", "json")
                .encode()
                .build()
                .toUri();

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        return responseEntity;
    }
}
