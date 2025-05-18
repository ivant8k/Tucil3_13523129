public class Heuristic {
    /**
     * Menghitung jumlah kendaraan yang menghalangi primary piece menuju exit
     */
    public static int blockingCars(Board board) {
        Piece p = board.primaryPiece;
        if (p == null) return 0;
        
        int count = 0;

        // Untuk primary piece horizontal, hitung kendaraan menghalangi di sebelah kanan
        if (p.isHorizontal) {
            int row = p.row;
            int col = p.col + p.length;
            while (col < board.cols) {
                char cell = board.grid[row][col];
                if (cell != '.' && cell != 'K') {
                    count++;
                    // Skip sampai ujung piece saat ini
                    char id = board.grid[row][col];
                    while (col < board.cols && board.grid[row][col] == id) {
                        col++;
                    }
                } else {
                    col++;
                }
            }
        } 
        // Untuk primary piece vertikal, hitung kendaraan menghalangi di bawah
        else {
            int col = p.col;
            int row = p.row + p.length;
            while (row < board.rows) {
                char cell = board.grid[row][col];
                if (cell != '.' && cell != 'K') {
                    count++;
                    // Skip sampai ujung piece saat ini
                    char id = board.grid[row][col];
                    while (row < board.rows && board.grid[row][col] == id) {
                        row++;
                    }
                } else {
                    row++;
                }
            }
        }

        return count;
    }
    
    /**
     * Menghitung jarak dari primary piece ke pintu keluar
     */
    public static int distanceToExit(Board board) {
        Piece p = board.primaryPiece;
        if (p == null) return Integer.MAX_VALUE;
        
        if (p.isHorizontal) {
            // Untuk mobil horizontal, hitung jarak horizontal ke pintu keluar
            int endOfPiece = p.col + p.length - 1;
            return Math.abs(endOfPiece - (board.exitCol - 1));
        } else {
            // Untuk mobil vertikal, hitung jarak vertikal ke pintu keluar
            int endOfPiece = p.row + p.length - 1;
            return Math.abs(endOfPiece - (board.exitRow - 1));
        }
    }
    
    /**
     * Heuristik gabungan yang menggabungkan jumlah kendaraan yang menghalangi
     * dan jarak ke pintu keluar
     */
    public static int combinedHeuristic(Board board) {
        return blockingCars(board) * 2 + distanceToExit(board);
    }
    
    public static int estimate(Board board, String mode) {
        return switch (mode.toLowerCase()) {
            case "distance", "1" -> distanceToExit(board);
            case "blocking", "2" -> blockingCars(board);
            case "combined", "3" -> combinedHeuristic(board);
            default -> combinedHeuristic(board); // fallback
        };
    }
}