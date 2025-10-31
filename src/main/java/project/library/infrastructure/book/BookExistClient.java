package project.library.infrastructure.book;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import project.library.controller.dto.UserLocation;
import project.library.controller.dto.book.search.BookSearchReseponseDto;

import java.net.URI;
import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Slf4j
@Component
@RequiredArgsConstructor
public class BookExistClient {


    @Qualifier("externalApiTaskExecutor")
    private final Executor externalApiTaskExecutor;

    private final RestTemplate restTemplate;

    private static final String API_BASE_URL = "http://data4library.kr/api";

    private static final String AUTH_KEY = "1df2f040d9555558e014f541e2908356008ca9e3aa7a1d9c43ec2c15e54f5f4b";



    public CompletableFuture<List<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>>>
    checkBookExistInParallel(UserLocation location, List<String> libraryCodes) {

        List<CompletableFuture<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>>> futures =
                libraryCodes.stream()
                        .map(libCode -> CompletableFuture
                                .supplyAsync(() -> callBookExist(libCode, location.getIsbn()), externalApiTaskExecutor)
                                .completeOnTimeout(null, 3, java.util.concurrent.TimeUnit.SECONDS)
                                .exceptionally(ex -> {
                                    log.warn("API 실패: {}", libCode, ex);
                                    return null;
                                })
                        ).toList();
        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .toList());

    }

    public AbstractMap.SimpleEntry<String, BookSearchReseponseDto> callBookExist(String libCode, String isbn){
        URI uri = UriComponentsBuilder.fromUriString(API_BASE_URL)
                .pathSegment("bookExist")
                .queryParam("authKey", AUTH_KEY)
                .queryParam("libCode", libCode)
                .queryParam("isbn13", isbn)
                .queryParam("format", "json")
                .encode()
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.ALL));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<BookSearchReseponseDto> res =
                restTemplate.exchange(uri, HttpMethod.GET, entity, BookSearchReseponseDto.class);

        return new AbstractMap.SimpleEntry<>(libCode, res.getBody());
    }
}
