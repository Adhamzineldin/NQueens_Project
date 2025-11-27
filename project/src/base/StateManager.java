package base;

import java.util.ArrayList;
import java.util.HashMap;

public class StateManager {
    public HashMap<Integer, StepBoard> current_states = new HashMap<>(); // thread_id -> StepBoard
    public HashMap<Integer, ArrayList<Solution>> solutions = new HashMap<>();
    
    public StateManager() {
        
    }
    
    public synchronized void updateState(int thread_id, StepBoard step_board) {
        current_states.put(thread_id, step_board);
        // Disabled for performance - uncomment to debug
        // step_board.printStepBoard();
    }
    
    public synchronized void addSolution(int thread_id, Solution solution) {
        if (!solutions.containsKey(thread_id)) {
            solutions.put(thread_id, new ArrayList<Solution>());
        }
        solutions.get(thread_id).add(solution);
        System.out.println("Thread " + thread_id + " found solution #" + solutions.get(thread_id).size() + 
                          " for " + solution.solved_board.getN() + "x" + solution.solved_board.getN() + " board");
        // Disabled printing full board - uncomment to debug
        // solution.printSolution();
    }
    
    public ArrayList<Solution> getSolutions(int thread_id) {
        return solutions.get(thread_id);
    }
    
    
    
}
