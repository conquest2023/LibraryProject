package project.library.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.apache.tomcat.util.http.RequestUtil.normalize;


@Slf4j
@Service
public class CrawlingServiceImpl implements CrawlingService {
    @Override
    public Set<String> crawlData(String url) throws IOException {
        Set<String> result=new LinkedHashSet<>();
        Document doc = Jsoup.connect(url).get();
        Elements linkElements = doc.select(".prod_name_group a.prod_info");
        if (!linkElements.isEmpty()) {
            // 첫 번째 책의 링크를 추출
            String detailUrl = linkElements.first().attr("href");
            result=extractContent(detailUrl);
            return result;
        }

        return result;
    }



        private Set<String> extractContent(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                .referrer("https://product.kyobobook.co.kr/")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .timeout(15000)
                .get();


        // 2) 여러 셀렉터로 목차 후보 탐색 (페이지 유형별 대응)
        String[] selectors = new String[] {
                "ul.book_contents_list li.book_contents_item",
                "#infoset_toc .book_contents_list li",
                ".auto_overflow_wrap .book_contents_list li",
                "[class*=contents] .book_contents_list li"
        };
        Set<String> result = new LinkedHashSet<>();
        for (String sel : selectors) {
            Elements items = doc.select(sel);
            if (!items.isEmpty()) {
                for (Element li : items) {
                    String line = normalize(li.text());
                    if (!line.isBlank())
                        result.add(line);
                }
            }
        }
        return result;
    }
}
