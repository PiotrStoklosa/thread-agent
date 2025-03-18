package org.threadmonitoring.model;

import org.apache.logging.log4j.LogManager;
import org.threadmonitoring.advices.ThreadConstructorAdvice;

import java.util.*;
import java.util.concurrent.Executor;

public class ExecutorModel {

    public static Map<Executor, ExecutorModel> EXECUTOR_MAP;

    private final StackTraceElement[] constructorStackTrace;
    private final Map<String, Integer> submitPlaces = new HashMap<>();
    private final Map<String, Integer> executePlaces = new HashMap<>();
    private boolean active = true;

    public static void initialize(){

        EXECUTOR_MAP = new HashMap<>();

    }

    public ExecutorModel(StackTraceElement[] constructorStackTrace) {
        this.constructorStackTrace = constructorStackTrace;
    }

    public void addSubmitPlace(String place) {
        submitPlaces.merge(place, 1, Integer::sum);
    }

    public void addExecutePlace(String place) {
        executePlaces.merge(place, 1, Integer::sum);
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

    public void deactivate(){
        active = false;
    }
}
