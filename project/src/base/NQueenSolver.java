package base;

import java.util.ArrayList;

public class NQueenSolver {

    private final Board board;
    private StepListener listener;
    private final ArrayList<Solution> solutions = new ArrayList<>();

    public NQueenSolver(Board board) {
        this.board = board;
    }

    public boolean solveFromRow(int row) {
        if (row == board.getN()) {
            Solution solution = new Solution(board.copy());
            solutions.add(solution);
            return false;
        };

        for (int col=0; col < board.getN(); col++) {
            if (board.isSafe(row, col)) {
                board.place(row, col);

                // notify GUI if needed later
                notifyStep();

                if (solveFromRow(row + 1)) return false;

                board.remove(row, col);
                notifyStep();
            }
        }
        return false;
    }

    public ArrayList<Solution> getSolutions() {
        return solutions;
    }
    
    private void notifyStep() {
//        if (listener != null) listener.onStep(new StepEvent(board.copy().getState()));
    }

    public void setListener(StepListener listener) {
    }
}

