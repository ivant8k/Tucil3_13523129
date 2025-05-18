// Util.java
public class Util {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";

    public static String colorChar(char ch, Move move, Board board) {
        if (ch == 'P') return BLUE + ch + RESET;
        if (ch == 'K') return RED + ch + RESET;
        if (move != null && ch == move.pieceId) return GREEN + ch + RESET;
        return String.valueOf(ch);
    }

    public static void printBoard(Board board, Move move) {
        for (int i = 0; i < board.rows; i++) {
            for (int j = 0; j < board.cols; j++) {
                System.out.print(Util.colorChar(board.grid[i][j], move, board));
            }
            System.out.println();
        }
    }
}