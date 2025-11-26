package base;

public class Board {


    private final int n;
    private final int[][] cells;

    public Board(int n) {
        this.n = n;
        this.cells = new int[n][n];
    }
    
    public Board(int n, int queen_col) {
        this.n = n;
        this.cells = new int[n][n];
        this.cells[0][queen_col] = 1;
    }

    public boolean isSafe(int row, int col) { 
        // Check this column on upper side
        for (int i = 0; i < row; i++)
            if (cells[i][col] == 1)
                return false;

        // Check upper diagonal on left side
        for (int i = row, j = col; i >= 0 && j >= 0; i--, j--)
            if (cells[i][j] == 1)
                return false;

        // Check upper diagonal on right side
        for (int i = row, j = col; i >= 0 && j < n; i--, j++)
            if (cells[i][j] == 1)
                return false;

        return true;
    }
    
    public void place(int row, int col) { 
        cells[row][col] = 1; 
    }
    
    public void remove(int row, int col) { 
        cells[row][col] = 0; 
    }
    
    public int[][] getState() {
        return cells;
    
    }
    
    public Board copy() {
        Board newBoard = new Board(n);
        for (int i = 0; i < n; i++) {
            System.arraycopy(this.cells[i], 0, newBoard.cells[i], 0, n);
        }
        return newBoard;
    }
    
    public void printBoard() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(cells[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }


    public int getN() {
        return n;
    }
    
}
