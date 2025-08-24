package project.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "externalApiTaskExecutor")
    public Executor externalApiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);      // 유지할 코어 스레드 수
        executor.setMaxPoolSize(40);       // 최대 스레드 수 (큐가 꽉 차면 증가)
        executor.setQueueCapacity(100);    // 대기 큐 크기
        executor.setThreadNamePrefix("api-call-"); // 스레드 이름(로그/모니터링 용이)
        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}