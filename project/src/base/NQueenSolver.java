package base;

import java.util.ArrayList;

public class NQueenSolver {

    private final Board board;
    private StateManager manager;
    private final ArrayList<Solution> solutions = new ArrayList<>();
    private int thread_id;

    public NQueenSolver(Board board) {
        this.board = board;
    }
    
    public NQueenSolver(Board board, int thread_id) {
        this.board = board;
        this.thread_id = thread_id;
    }

    public boolean solveFromRow(int row) {
        if (row == board.getN()) {
            Solution solution = new Solution(board.copy());
            solutions.add(solution);
            if (manager != null) manager.addSolution(this.thread_id, solution);
            return false;
        };

        for (int col=0; col < board.getN(); col++) {
            if (board.isSafe(row, col)) {
                board.place(row, col);

                // notify GUI if needed later
                notifyStep(Action.PLACE);
                
                if (solveFromRow(row + 1)) return false;

                board.remove(row, col);
                notifyStep(Action.REMOVE);
            }
        }
        return false;
    }

    public ArrayList<Solution> getSolutions() {
        return solutions;
    }
    
    private void notifyStep(Action action) {
        if (manager != null) manager.updateState(this.thread_id, new StepBoard(board.copy(), action));
    }

    public void setManager(StateManager manager) {
        this.manager = manager;
    }
    
    public StateManager getManager() {
        return this.manager;
    }
    
}

