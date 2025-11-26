package base;

import java.util.ArrayList;
import java.util.List;

public class ThreadManager {

    private final List<SolverThread> threads = new ArrayList<>();

    public void startSolving(int n, StepListener listener) {
        threads.clear();
        for (int col = 0; col < n; col++) {
            SolverThread t = new SolverThread(n, col, listener);
            threads.add(t);
            t.start();
        }
    }

    public void stopAll() {
        for (SolverThread t : threads) t.requestStop();
    }
}

