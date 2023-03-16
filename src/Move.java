public class Move {
    private int type, col, row;

    public Move(int t, int r, int c) {
        type = t;
        row = r;
        col = c; 
    }

    public boolean isValid(int size) {
        if (type != -1 && type != 1) return false;
        if (col < 0 || col >= size || row < 0 || row >= size) return false;
        return true;
    }

    public int getType() {
        return type;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }
}
