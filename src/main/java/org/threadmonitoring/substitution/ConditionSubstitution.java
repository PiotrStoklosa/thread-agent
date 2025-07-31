package org.threadmonitoring.substitution;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class ConditionSubstitution {

    public static void signal2(Condition condition) {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " wakes up one thread on " + condition.toString()
                , ConditionSubstitution.class
                , "INFO");
        condition.signal();
    }

    public static void signalAll2(Condition condition) {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " wakes up all threads on " + condition.toString()
                , ConditionSubstitution.class
                , "INFO");
        condition.signal();
    }

    public static void awaitUninterruptibly2(Condition condition) {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " await uninterruptedly on " + condition.toString()
                , ConditionSubstitution.class
                , "INFO");
        condition.awaitUninterruptibly();
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " woken up"
                , ConditionSubstitution.class
                , "INFO");
    }

    public static void await2(Condition condition) throws InterruptedException {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " awaits on " + condition.toString()
                , ConditionSubstitution.class
                , "INFO");
        condition.await();
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " woken up"
                , ConditionSubstitution.class
                , "INFO");
    }

    public static long awaitNanos2(Condition condition, long nanosTimeout) throws InterruptedException {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " awaits on " + condition.toString() + " for " +
                        nanosTimeout + " nanos"
                , ConditionSubstitution.class
                , "INFO");
        long retVal = condition.awaitNanos(nanosTimeout);
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " woken up"
                , ConditionSubstitution.class
                , "INFO");
        return retVal;
    }

    public static boolean await2(Condition condition, long time, TimeUnit unit) throws InterruptedException {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " awaits on " + condition.toString() +
                        " for " + time + " " + unit
                , ConditionSubstitution.class
                , "INFO");
        boolean retVal = condition.await(time, unit);
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " woken up"
                , ConditionSubstitution.class
                , "INFO");
        return retVal;
    }

}
