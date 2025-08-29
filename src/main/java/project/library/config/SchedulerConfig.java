package project.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import project.library.service.LibraryServiceImpl;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private final LibraryServiceImpl bookService;

    public SchedulerConfig(LibraryServiceImpl bookService) {
        this.bookService = bookService;
    }

    // 매일 새벽 04:10 KST (서버가 UTC여도 KST 기준으로 계산되도록 cronZone 지정)
    @Scheduled(cron = "0 10 2 * * *", zone = "Asia/Seoul")
    public void fetchDailyBooks() {
        bookService.findBeforeOneDayIncreaseBook();
    }
}