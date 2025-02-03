package org.threadmonitoring.model;

import java.util.*;
import java.util.concurrent.Executor;

public class ExecutorModel {

    public static final Map<Executor, ExecutorModel> EXECUTOR_MAP = new HashMap<>();

    private StackTraceElement[] constructorStackTrace;
    private final Set<String> submitPlaces = new HashSet<>();
    private boolean active = true;

    public ExecutorModel(StackTraceElement[] constructorStackTrace) {
        this.constructorStackTrace = constructorStackTrace;
    }

    public void addSubmitPlace(String place) {
        submitPlaces.add(place);
    }

    public void addConstructorStackTrace(StackTraceElement[] stackTrace) {
        constructorStackTrace = stackTrace;
    }

    public StackTraceElement[] getConstructorStackTrace() {
        return constructorStackTrace;
    }


    public Set<String> getSubmitPlaces() {
        return submitPlaces;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate(){
        active = false;
    }
}
