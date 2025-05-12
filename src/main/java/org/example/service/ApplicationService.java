package org.example.service;

import org.example.model.sports.Sports;
import org.example.model.result.EventResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    private static final int THREAD_POOL_SIZE = 3;
    private static final int MAX_CONCURRENT_TASKS = 2;

    private final SportService sportService;
    private final LeagueService leagueService;
    private final Semaphore semaphore;

    public ApplicationService() {
        this.sportService = new SportService();
        this.leagueService = new LeagueService();
        this.semaphore = new Semaphore(MAX_CONCURRENT_TASKS);
    }

    public void init() {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try {
            List<Sports> filteredSportList = sportService.getFilteredSportList();
            Map<String, List<EventResult>> resultsMap = process(filteredSportList, executorService);
            printResults(filteredSportList, resultsMap);
        } finally {
            shutdownExecutorService(executorService);
        }
    }

    private Map<String, List<EventResult>> process(
            List<Sports> filteredSportList,
            ExecutorService executorService) {

        List<CompletableFuture<List<EventResult>>> futureList = filteredSportList.stream()
                .map(sports -> CompletableFuture.supplyAsync(
                        () -> processWithSemaphore(sports),
                        executorService
                ))
                .collect(Collectors.toList());

        return futureList.stream()
                .map(this::getFutureResults)
                .filter(results -> !results.isEmpty())
                .collect(Collectors.toMap(
                        results -> results.get(0).getSportName(),
                        results -> results,
                        // In case of duplicate keys, keep the first value
                        (v1, v2) -> v1
                ));
    }

    private List<EventResult> processWithSemaphore(Sports sports) {
        try {
            semaphore.acquire();
            try {
                return leagueService.getPrintEventsInRegionTopLeagueResults(sports.getRegions());
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while acquiring semaphore", e);
            Thread.currentThread().interrupt();
            return List.of();
        }
    }

    private List<EventResult> getFutureResults(
            CompletableFuture<List<EventResult>> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while processing sports events", e);
            Thread.currentThread().interrupt();
            return List.of();
        } catch (ExecutionException e) {
            logger.error("Error executing sports events processing", e);
            return List.of();
        }
    }

    private void shutdownExecutorService(ExecutorService executorService) {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            logger.error("ExecutorService shutdown interrupted", e);
        }
    }

    private void printResults(
            List<Sports> filteredSportList,
            Map<String, List<EventResult>> resultsMap) {

        filteredSportList.forEach(sports -> {
            List<EventResult> printResults = resultsMap.get(sports.getName());
            if (printResults != null && !printResults.isEmpty()) {
                printResults.forEach(EventResult::print);
            }
        });
    }
}