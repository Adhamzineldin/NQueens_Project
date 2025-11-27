package base;
import base.Action;

public class StepBoard {
    public Board board;
    public int step;
    public Action action;
    

    public StepBoard(Board board, Action action) {
        this.board = board;
        this.action = action;
    }
    
    public void printStepBoard() {
        board.printBoard();
    }
    
    public Board getBoard() {
        return board;
    }
    
    public Action getAction() {
        return action;
    }
}
