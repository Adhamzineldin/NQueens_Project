package base;

import java.util.ArrayList;

public class NQueenSolver {

    private final Board board;
    private StateManager manager;
    private final ArrayList<Solution> solutions = new ArrayList<>();
    private int thread_id;
    private static volatile int stepDelay = 0; // milliseconds to wait between steps
    private volatile boolean stopRequested = false;
    private int lastChangedRow = -1;
    private int lastChangedCol = -1;

    public NQueenSolver(Board board) {
        this.board = board;
    }
    
    public NQueenSolver(Board board, int thread_id) {
        this.board = board;
        this.thread_id = thread_id;
    }

    public boolean solveFromRow(int row) {
        // Check if stop was requested
        if (stopRequested || Thread.currentThread().isInterrupted()) {
            return true; // Stop solving
        }
        
        if (row == board.getN()) {
            Solution solution = new Solution(board.copy());
            solutions.add(solution);
            if (manager != null) manager.addSolution(this.thread_id, solution);
            return false;
        };

        for (int col=0; col < board.getN(); col++) {
            // Check stop flag before each iteration
            if (stopRequested || Thread.currentThread().isInterrupted()) {
                return true; // Stop solving
            }
            
            if (board.isSafe(row, col)) {
                board.place(row, col);
                lastChangedRow = row;
                lastChangedCol = col;

                // notify GUI if needed later
                notifyStep(Action.PLACE, row, col);
                sleepIfNeeded();
                
                if (solveFromRow(row + 1)) return true; // Propagate stop signal

                board.remove(row, col);
                lastChangedRow = row;
                lastChangedCol = col;
                notifyStep(Action.REMOVE, row, col);
                sleepIfNeeded();
            }
        }
        return false;
    }
    
    public void requestStop() {
        stopRequested = true;
    }
    
    public void resetStopFlag() {
        stopRequested = false;
    }
    
    private void sleepIfNeeded() {
        if (stepDelay > 0) {
            try {
                Thread.sleep(stepDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void setStepDelay(int delayMs) {
        stepDelay = delayMs;
    }
    
    public static int getStepDelay() {
        return stepDelay;
    }

    public ArrayList<Solution> getSolutions() {
        return solutions;
    }
    
    private void notifyStep(Action action, int row, int col) {
        if (manager != null) manager.updateState(this.thread_id, new StepBoard(board.copy(), action, row, col));
    }

    public void setManager(StateManager manager) {
        this.manager = manager;
    }
    
    public StateManager getManager() {
        return this.manager;
    }
    
}

