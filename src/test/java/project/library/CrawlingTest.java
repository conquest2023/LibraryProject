package project.library;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import project.library.service.CalculateService;
import project.library.service.CrawlingService;
import project.library.service.CrawlingServiceImpl;

import java.io.IOException;

@SpringBootTest
public class CrawlingTest {


    private final static String URL ="https://search.kyobobook.co.kr/search?keyword=";

    @Test
    void 크롤링테스트() throws IOException {

        CrawlingService crawlingService=new CrawlingServiceImpl();

        String newWord = URL.concat("컴퓨터밑바닥비밀");
        crawlingService.crawlData(newWord);
    }


}
