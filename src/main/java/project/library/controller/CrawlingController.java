package project.library.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.library.service.CrawlingService;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class CrawlingController {

    private final CrawlingService crawlingService;


    private final static String URL ="https://search.kyobobook.co.kr/search?keyword=";


    @GetMapping("/crawling")
    public ResponseEntity<?> getCrawling(@RequestParam String bookName) throws IOException {
        String newURL=URL.concat(bookName);
        Set<String> result = crawlingService.crawlData(newURL);
        return ResponseEntity.ok(
                Map.of("result",result));
    }
}
