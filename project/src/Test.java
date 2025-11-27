import base.Board;
import base.NQueenSolver;
import base.StateManager;
import base.ThreadManager;

public class Test {
    public static void main(String[] args) {
//        Board board = new Board(8);
//        System.out.println("Board size: " + board.getN());
//        NQueenSolver solver = new NQueenSolver(board);
//        System.out.println("Solver created for board of size: " + solver);
//        solver.solveFromRow(0);
//        for (var solution : solver.getSolutions()) {
//            solution.printSolution();
//        }
//        System.out.println("\nSolutions found for board: " + solver.getSolutions().size());

        ThreadManager tm = new ThreadManager();
        StateManager sm = new StateManager();
        
        
        try {
            tm.startSolving(4, 4,  sm);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }
   
      
        
        
        
        
    }
}
