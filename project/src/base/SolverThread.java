package base;

public class SolverThread extends Thread {

    private final NQueenSolver solver;
    private volatile boolean stop = false;
    private final Object lock;
    private final int startCol;
    private final int endCol;
    private final int n;
    private final int thread_id;

    // Updated constructor to handle a range of columns
    public SolverThread(int thread_id, int n, int startCol, int endCol, StateManager manager, Object lock) {
        this.startCol = startCol;
        this.endCol = endCol;
        this.lock = lock;
        this.n = n;
        this.thread_id = thread_id;

        Board board = new Board(n);
        this.solver = new NQueenSolver(board);
        solver.setManager(manager);
    }

    @Override
    public void run() {
        for (int col = startCol; col < endCol; col++) {
            if (stop) break;

            Board board = new Board(n, col); // Place first queen at (0, col)
            NQueenSolver localSolver = new NQueenSolver(board, thread_id);
            localSolver.setManager(solver.getManager());
            localSolver.solveFromRow(1);
        }
    }

    public void requestStop() { stop = true; }
}
