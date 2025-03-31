package org.threadmonitoring.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class ExecutorModel {

    public static Map<Executor, ExecutorModel> EXECUTOR_MAP;

    private final StackTraceElement[] constructorStackTrace;
    private final Map<String, Integer> submitPlaces = new HashMap<>();
    private final Map<String, Integer> executePlaces = new HashMap<>();
    private boolean active = true;

    public static void initialize() {

        EXECUTOR_MAP = new HashMap<>();

    }

    public ExecutorModel(StackTraceElement[] constructorStackTrace) {
        this.constructorStackTrace = constructorStackTrace;
    }

    public void addSubmitPlace(String place) {
        int count = submitPlaces.getOrDefault(place, 0);
        submitPlaces.put(place, count + 1);
    }

    public void addExecutePlace(String place) {
        int count = executePlaces.getOrDefault(place, 0);
        executePlaces.put(place, count + 1);
    }

    public StackTraceElement[] getConstructorStackTrace() {
        return constructorStackTrace;
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
