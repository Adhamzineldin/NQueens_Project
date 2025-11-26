package base;

import java.util.ArrayList;
import java.util.List;


public class ThreadManager {

    private final List<SolverThread> threads = new ArrayList<>();
    private final Object lock = new Object();

    public void startSolving(int n, int numThreads, StepListener listener) {
        threads.clear();

        // Calculate how many columns each thread should handle
        int colsPerThread = n / numThreads;
        int remainingCols = n % numThreads;

        int startCol = 0;

        for (int i = 0; i < numThreads; i++) {
            int colsForThisThread = colsPerThread + (i < remainingCols ? 1 : 0);
            int endCol = startCol + colsForThisThread;

            SolverThread t = new SolverThread(n, startCol, endCol, listener, lock);
            threads.add(t);
            t.start();

            startCol = endCol; // move to next chunk
        }
    }


    public void stopAll() {
        for (SolverThread t : threads) t.requestStop();
    }
}

