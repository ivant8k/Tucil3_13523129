public class Heuristic {
    /**
     * Menghitung jarak Manhattan dari primary piece ke pintu keluar
     * Manhattan distance = |x1 - x2| + |y1 - y2|
     */
    public static int manhattanDistance(Board board) {
        Piece p = board.primaryPiece;
        if (p == null) return Integer.MAX_VALUE;
        
        // Hitung posisi akhir primary piece
        int endRow = p.isHorizontal ? p.row : p.row + p.length - 1;
        int endCol = p.isHorizontal ? p.col + p.length - 1 : p.col;
        
        // Hitung jarak Manhattan ke exit
        return Math.abs(endRow - board.exitRow) + Math.abs(endCol - board.exitCol);
    }
    
    /**
     * Menghitung jarak Euclidean dari primary piece ke pintu keluar
     * Euclidean distance = sqrt((x1 - x2)^2 + (y1 - y2)^2)
     */
    public static int euclideanDistance(Board board) {
        Piece p = board.primaryPiece;
        if (p == null) return Integer.MAX_VALUE;
        
        // Hitung posisi akhir primary piece
        int endRow = p.isHorizontal ? p.row : p.row + p.length - 1;
        int endCol = p.isHorizontal ? p.col + p.length - 1 : p.col;
        
        // Hitung jarak Euclidean ke exit
        double dx = endRow - board.exitRow;
        double dy = endCol - board.exitCol;
        return (int) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Menghitung jarak Chebyshev dari primary piece ke pintu keluar
     * Chebyshev distance = max(|x1 - x2|, |y1 - y2|)
     */
    public static int chebyshevDistance(Board board) {
        Piece p = board.primaryPiece;
        if (p == null) return Integer.MAX_VALUE;
        
        // Hitung posisi akhir primary piece
        int endRow = p.isHorizontal ? p.row : p.row + p.length - 1;
        int endCol = p.isHorizontal ? p.col + p.length - 1 : p.col;
        
        // Hitung jarak Chebyshev ke exit
        return Math.max(Math.abs(endRow - board.exitRow), Math.abs(endCol - board.exitCol));
    }
    
    public static int estimate(Board board, String mode) {
        return switch (mode.toLowerCase()) {
            case "manhattan", "1" -> manhattanDistance(board);
            case "euclidean", "2" -> euclideanDistance(board);
            case "chebyshev", "3" -> chebyshevDistance(board);
            default -> manhattanDistance(board); // fallback ke Manhattan
        };
    }
}