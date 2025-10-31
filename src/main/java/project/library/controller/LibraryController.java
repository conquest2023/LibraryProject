package project.library.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import project.library.controller.dto.book.BookDetailDto;
import project.library.controller.dto.book.BookDto;
import project.library.controller.dto.book.LibraryResponseDto;
import project.library.controller.dto.book.search.BookSearchReseponseDto;
import project.library.service.DailyBookCacheService;
import project.library.service.LibraryService;

import java.net.URI;
import java.util.List;
import java.util.Map;


@Slf4j
@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class LibraryController {


    private final DailyBookCacheService cacheService;


    private final LibraryService libraryService;

    private final RestTemplate restTemplate;

    private static final String API_BASE_URL = "http://data4library.kr/api";

    private static final String AUTH_KEY = "1df2f040d9555558e014f541e2908356008ca9e3aa7a1d9c43ec2c15e54f5f4b";


//    @GetMapping("/")
//    public String  indexPage(){
//        return "/index.html";
//    }

    @GetMapping("/increase")
    @ResponseBody
    public  ResponseEntity<?> getIncreaseBook(){
        List<BookDto> increaseBook = cacheService.getLatestDailyBooks();

        return ResponseEntity.ok(Map.of("books",increaseBook));
    }

    @GetMapping("/search/book")
    @ResponseBody
    public ResponseEntity<?> getIsbn(
            @RequestParam(required = false) String title,
            HttpSession session) {

        List<BookDto> books = libraryService.searchBook(session.getId(),title);

        return ResponseEntity.ok(Map.of("books", books));
    }

    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<?> getHistory(
            HttpSession session) {

        List<String> books = libraryService.getHistoryBook(session.getId());

        if(!books.isEmpty()){
            return ResponseEntity.ok(Map.of("books", books));

        }

        return ResponseEntity.ok(Map.of("books","최근 검색하신 책이 없습니다"));

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
        headers.setAccept(List.of(MediaType.ALL));
        // 3. 헤더를 담은 HttpEntity 객체를 생성합니다.
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<LibraryResponseDto> responseEntity =
                restTemplate.exchange(uri, HttpMethod.GET, entity, LibraryResponseDto.class);



        // 2. 응답 본문(Body)을 DTO 객체로 받음
        LibraryResponseDto libraryResponse = responseEntity.getBody();

        if (libraryResponse != null && libraryResponse.getResponse() != null &&
                !libraryResponse.getResponse().getDetail().isEmpty()) {

            // 첫 번째 책 정보를 가져옴
            BookDto book = libraryResponse.getResponse().getDetail().get(0).getBook();

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
                .queryParam("isbn13", isbn)
                .queryParam("format", "json")
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

        // Java Stream을 사용해 더 간결하게 책 목록만 추출
        List<BookDto> books = docWrappers.stream()
                    .map(BookDetailDto::getBook)
                    .toList();

        return ResponseEntity.ok(Map.of("books",books));

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





//    @GetMapping("/search/library")
//    @ResponseBody
//    public ResponseEntity<?> getLibrary(@RequestParam String isbn,
//                                        @RequestParam String region){
//
//        URI uri = UriComponentsBuilder
//                .fromUriString(API_BASE_URL)
//                .pathSegment("libSrchByBook")
//                .queryParam("authKey", AUTH_KEY)
//                .queryParam("isbn", isbn) // 입력받은 검색어
//                .queryParam("region",region)
//                .queryParam("pageNo", 1)
//                .queryParam("pageSize", 50)
//                .queryParam("format", "json") // 응답 형식을 JSON으로 지정
//                .encode()
//                .build()
//                .toUri();
//
//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
//
//        return responseEntity;
//
//    }


}

