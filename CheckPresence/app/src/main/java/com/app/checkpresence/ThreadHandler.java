package com.app.checkpresence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damian on 16.05.2016.
 */
final public class ThreadHandler {

    static List<Runnable> associatedRunnables;
    static List<Thread> threads;

    private ThreadHandler(){}

    public static void createThread(Runnable threadedObject){
        if(threads == null){
            threads = new ArrayList<Thread>();
            associatedRunnables = new ArrayList<Runnable>();
        }
        threads.add(new Thread(threadedObject));
        associatedRunnables.add(threadedObject);
    }

    public static void startThreads(){
        for (Thread thread:threads) {
            thread.start();
        }
    }

    public static void joinThreads() throws InterruptedException {
        for (Thread thread:threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw e;
            }
        }

        //cleaning lists
        threads = new ArrayList<Thread>();
        associatedRunnables = new ArrayList<Runnable>();
    }
}
