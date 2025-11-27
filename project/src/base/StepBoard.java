package base;
import base.Action;

public class StepBoard {
    public Board board;
    public int step;
    public Action action;
    public int changedRow;
    public int changedCol;
    

    public StepBoard(Board board, Action action, int row, int col) {
        this.board = board;
        this.action = action;
        this.changedRow = row;
        this.changedCol = col;
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
