package base;

public class SolverThread extends Thread {

    private final NQueenSolver solver;
    private volatile boolean stop = false;
    private final Object lock;
    private final int startCol;
    private final int endCol;
    private final int n;

    // Updated constructor to handle a range of columns
    public SolverThread(int n, int startCol, int endCol, StepListener listener, Object lock) {
        this.startCol = startCol;
        this.endCol = endCol;
        this.lock = lock;
        this.n = n;

        Board board = new Board(n);
        this.solver = new NQueenSolver(board);
        solver.setListener(listener);
    }

    @Override
    public void run() {
        for (int col = startCol; col < endCol; col++) {
            if (stop) break;

            Board board = new Board(n, col); // Place first queen at (0, col)
            NQueenSolver localSolver = new NQueenSolver(board);
//            localSolver.setListener(solver.getListener());
            localSolver.solveFromRow(1);

            // Lock when printing solutions to avoid interleaving
            synchronized (lock) {
                for (var solution : localSolver.getSolutions()) {
                    if (stop) break;
                    solution.printSolution();
                    System.out.println();
                }
            }
        }
    }

    public void requestStop() { stop = true; }
}
