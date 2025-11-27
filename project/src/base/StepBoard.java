package base;
import base.Action;

public class StepBoard {
    Board board;
    int step;
    Action action;
    

    public StepBoard(Board board, Action action) {
        this.board = board;
    }
    
    public void printStepBoard() {
        board.printBoard();
    }
}
