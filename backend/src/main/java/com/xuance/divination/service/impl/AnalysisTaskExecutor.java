package com.xuance.divination.service.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class AnalysisTaskExecutor {
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public void submit(Runnable task) {
        executor.submit(task);
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
