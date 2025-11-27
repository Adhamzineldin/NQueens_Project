package base;

public class Solution {
    public Board solved_board;
    public int thread_id;
    
    public Solution(Board board, int id) {
        this.solved_board = board;
        this.thread_id = id;
    }
    
    public Solution(Board board) {
        this.solved_board = board;
        this.thread_id = -1;
    }

    public boolean isZeros() {
        int n = solved_board.getN();
        int[][] state = solved_board.getState();
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                if (state[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public void printSolution() {
       this.solved_board.printBoard();
    }
    
}
