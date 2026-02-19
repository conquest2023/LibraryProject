package project.library.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ApiMonitor {

//    private final Counter externalApiCallCounter;

    private final MeterRegistry registry;

    public ApiMonitor(MeterRegistry registry) {
        this.registry = registry;
    }
    public void incrementCallCount(String apiName, String status) {
        // API 이름과 상태(성공/실패)를 태그로 사용하여 카운터를 찾거나 새로 생성
        Counter.builder("external_api_quota_usage_total")
                .description("Total number of external API calls per API")
                .tags("api_name", apiName, "status", status) // 태그 활용
                .register(registry)
                .increment();
    }
}
