package base;

import java.util.ArrayList;
import java.util.List;


public class ThreadManager {

    private final List<SolverThread> threads = new ArrayList<>();
    private final Object lock = new Object();

    public void startSolving(int n, int numThreads, StateManager manager) {
        threads.clear();
        
        if (n < 4) {
            throw new IllegalArgumentException("Board size must be at least 4");
        }
        
        if (numThreads > n) {
            numThreads = n; 
        }
        
        if (numThreads < 1) {
            throw new IllegalArgumentException("Number of threads must be at least 1");
        }
        
        // Calculate how many columns each thread should handle
        int colsPerThread = n / numThreads;
        int remainingCols = n % numThreads;

        int startCol = 0;

        for (int i = 0; i < numThreads; i++) {
            int colsForThisThread = colsPerThread + (i < remainingCols ? 1 : 0);
            int endCol = startCol + colsForThisThread;

            System.out.println("Thread " + i + " assigned columns: " + startCol + " to " + (endCol - 1));

            SolverThread t = new SolverThread(i, n, startCol, endCol, manager, lock);
            threads.add(t);
            t.start();

            startCol = endCol; // move to next chunk
        }
    }


    public void stopAll() {
        for (SolverThread t : threads) {
            t.requestStop();
        }
        
        // Wait for threads to finish (with timeout)
        for (SolverThread t : threads) {
            try {
                t.join(1000); // Wait up to 1 second per thread
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public void waitForCompletion() {
        // Wait for all threads to complete naturally
        for (SolverThread t : threads) {
            try {
                t.join(); // Wait indefinitely for thread to complete
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public boolean isRunning() {
        for (SolverThread t : threads) {
            if (t.isAlive()) {
                return true;
            }
        }
        return false;
    }
}

