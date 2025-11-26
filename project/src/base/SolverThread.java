package base;

public class SolverThread extends Thread {

    private final NQueenSolver solver;
    private volatile boolean stop = false;

    public SolverThread(int n, int col, StepListener listener) {
//        this.startingColumn = col;
        //    private final int startingColumn;
        Board board = new Board(n, col);
        this.solver = new NQueenSolver(board);
        solver.setListener(listener);
    }

    @Override
    public void run() {
        // place queen in row 0
//        board.place(0, startingColumn);
        // backtracking from row 1
        solver.solveFromRow(1);
    }

    public void requestStop() { stop = true; }
}

