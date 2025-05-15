public class Piece {
    public char id;
    public boolean isHorizontal;
    public int length;
    public int row, col;

    public Piece(char id, boolean isHorizontal, int length, int row, int col) {
        this.id = id;
        this.isHorizontal = isHorizontal;
        this.length = length;
        this.row = row;
        this.col = col;
    }

    @Override
    public String toString() {
        return id + " at (" + row + "," + col + ") " + (isHorizontal ? "H" : "V") + ", len=" + length;
    }
}