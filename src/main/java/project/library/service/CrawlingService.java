package project.library.service;


import java.io.IOException;
import java.util.Set;

public interface CrawlingService{

    Set<String> crawlData(String url) throws IOException;
}
