package org.threadmonitoring.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class ExecutorServiceModel {

    public static Map<ExecutorService, ExecutorServiceModel> EXECUTOR_SERVICE_MAP;

    private final String constructorPlace;
    private final Map<String, Integer> submitPlaces = new ConcurrentHashMap<>();
    private final Map<String, Integer> executePlaces = new ConcurrentHashMap<>();
    private boolean active = true;

    public static void initialize() {

        EXECUTOR_SERVICE_MAP = new ConcurrentHashMap<>();

    }

    public ExecutorServiceModel(String constructorPlace) {
        this.constructorPlace = constructorPlace;
    }

    public void addSubmitPlace(String place) {
        int count = submitPlaces.getOrDefault(place, 0);
        submitPlaces.put(place, count + 1);
    }

    public void addExecutePlace(String place) {
        int count = executePlaces.getOrDefault(place, 0);
        executePlaces.put(place, count + 1);
    }

    public String getConstructorPlace() {
        return constructorPlace;
    }


    public Map<String, Integer> getSubmitPlaces() {
        return submitPlaces;
    }

    public Map<String, Integer> getExecutePlaces() {
        return executePlaces;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}
