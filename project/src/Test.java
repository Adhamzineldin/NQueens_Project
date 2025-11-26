import base.Board;
import base.NQueenSolver;

public class Test {
    public static void main(String[] args) {
        Board board = new Board(8);
        System.out.println("Board size: " + board.getN());
        NQueenSolver solver = new NQueenSolver(board);
        System.out.println("Solver created for board of size: " + solver);
        solver.solveFromRow(0);
        for (var solution : solver.getSolutions()) {
            solution.printSolution();
        }
        System.out.println("\nSolutions found for board: " + solver.getSolutions().size());
    }
}
