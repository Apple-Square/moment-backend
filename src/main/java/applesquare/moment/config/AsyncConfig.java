package applesquare.moment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    private final int CORE_POOL_SIZE=10;
    private final int MAX_POOL_SIZE=100;
    private final int QUEUE_CAPACITY=1000;

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);  // 기본 스레드 수
        executor.setMaxPoolSize(MAX_POOL_SIZE);  // 최대 스레드 수
        executor.setQueueCapacity(QUEUE_CAPACITY);  // 대기 큐 크기
        executor.setThreadNamePrefix("ASYNC-");  // 스레드 이름 접두사

        // 큐 초과 시 정책 설정
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
