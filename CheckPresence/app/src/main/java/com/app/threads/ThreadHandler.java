package com.app.threads;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damian on 16.05.2016.
 */
final public class ThreadHandler {

    private static List<Runnable> associatedRunnables;
    private static List<Thread> threads;

    private ThreadHandler(){}

    /**
     * Create new thread with Runnable object
     * @param threadedObject object which contains run() method
     */
    public static void createThread(Runnable threadedObject){
        if(threads == null){
            threads = new ArrayList<Thread>();
            associatedRunnables = new ArrayList<Runnable>();
        }
        threads.add(new Thread(threadedObject));
        associatedRunnables.add(threadedObject);
    }

    /**
     * Start all actual threads
     */
    public static void startThreads(){
        for (Thread thread:threads) {
            thread.start();
        }
    }

    /**
     * Wait to end of all actual threads
     * @throws InterruptedException
     */
    public static void joinThreads() throws InterruptedException {
        for (Thread thread:threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw e;
            }
        }

        //clearLists();
    }

    public static List<Runnable> getRunnables(){
        return associatedRunnables;
    }

    public static void clearLists(){
        //cleaning lists
        threads = new ArrayList<Thread>();
        associatedRunnables = new ArrayList<Runnable>();
    }
}
