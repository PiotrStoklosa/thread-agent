package org.threadmonitoring.bootstrap;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
/*
* Class loading handling, including the use of bootstrap methods,
* in the Thread Agent partially uses an advanced dynamic method binding mechanism.
* This allows classes loaded by class loaders other than the agent's to have access to interceptors.
* This solution is inspired by the implementation in Elastic's apm-agent-java project:
* https://github.com/elastic/apm-agent-java/blob/main/apm-agent-core/src/main/java/co/elastic/apm/agent/bci/IndyBootstrap.java#L77
*/
public class Bootstrapper {

    public static volatile MethodHandle bootstrap;

    public static CallSite bootstrap(MethodHandles.Lookup sourceMethodLookup,
                                     String adviceMethodName,
                                     MethodType adviceMethodType,
                                     String adviceClassName,
                                     Object... args) {
        if (bootstrap != null) {
            try {
                return (CallSite) bootstrap.invoke(null, adviceMethodName, adviceMethodType, adviceClassName, args);
            } catch (Throwable e) {
                System.err.println("Error occurred during invoking bootstrap method: " + e);
                throw new RuntimeException(e);

            }
        } else{
            System.err.println("Bootstrap method not initialized");
            throw new RuntimeException("Bootstrap method not initialized");
        }
    }

    public static void setBootstrap(MethodHandle bootstrap) {
        Bootstrapper.bootstrap = bootstrap;
    }
}