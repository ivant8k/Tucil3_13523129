import java.util.*;

public class Board {
    public static boolean DEBUG = false;

    public char[][] grid;
    public List<Piece> pieces = new ArrayList<>();
    public Piece primaryPiece;
    public int exitRow = -1, exitCol = -1;
    public int rows, cols;
    public char primaryVehicleId;

    public Board(String[] config) {
        this.rows = config.length;
        this.cols = config[0].length();
        this.grid = new char[rows][cols];

        if (DEBUG) {
            System.out.println("DEBUG: Konfigurasi input:");
            for (String line : config) {
                System.out.println(line);
            }
        }

        Map<Character, List<int[]>> positions = new HashMap<>();

        for (int i = 0; i < rows; i++) {
            String line = config[i];
            for (int j = 0; j < cols; j++) {
                char c = line.charAt(j);
                grid[i][j] = c;

                if (c != '.' && c != 'K') {
                    positions.putIfAbsent(c, new ArrayList<>());
                    positions.get(c).add(new int[]{i, j});
                }
            }
        }

        pieces.clear();
        primaryPiece = null;

        for (Map.Entry<Character, List<int[]>> entry : positions.entrySet()) {
            char id = entry.getKey();
            List<int[]> coords = entry.getValue();

            coords.sort(Comparator.comparingInt(a -> a[0] * cols + a[1]));

            int len = coords.size();
            int[] first = coords.get(0);
            int[] second = len > 1 ? coords.get(1) : first;
            boolean isHorizontal = first[0] == second[0];

            Piece p = new Piece(id, isHorizontal, len, first[0], first[1]);
            if (id == 'P') primaryPiece = p;
            pieces.add(p);
        }
    }

    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new char[rows][cols];
        this.pieces = new ArrayList<>();
        this.exitRow = -1;
        this.exitCol = -1;
        this.primaryVehicleId = 'A';
        
        // Initialize grid with empty spaces
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = '.';
            }
        }
    }

    public void print() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(grid[i][j]);
            }
            System.out.println();
        }
    }

    public boolean isGoal() {
        if (primaryPiece == null) return false;

        if (exitRow < 0 || exitCol < 0) return false;

        if (primaryPiece.isHorizontal) {
            int tailCol = primaryPiece.col + primaryPiece.length - 1;
            return (primaryPiece.row == exitRow && tailCol + 1 == exitCol);
        } else {
            int tailRow = primaryPiece.row + primaryPiece.length - 1;
            return (primaryPiece.col == exitCol && tailRow + 1 == exitRow);
        }
    }

    public List<Move> getPossibleMoves() {
        List<Move> moves = new ArrayList<>();

        for (Piece p : pieces) {
            int r = p.row;
            int c = p.col;

            if (p.isHorizontal) {
                int leftSpaces = 0;
                while (c - leftSpaces - 1 >= 0 && (grid[r][c - leftSpaces - 1] == '.' || grid[r][c - leftSpaces - 1] == 'K')) {
                    leftSpaces++;
                    moves.add(new Move(p.id, "kiri", leftSpaces));
                }

                int rightSpaces = 0;
                while (c + p.length + rightSpaces < cols && (grid[r][c + p.length + rightSpaces] == '.' || grid[r][c + p.length + rightSpaces] == 'K')) {
                    rightSpaces++;
                    moves.add(new Move(p.id, "kanan", rightSpaces));
                }
            } else {
                int upSpaces = 0;
                while (r - upSpaces - 1 >= 0 && (grid[r - upSpaces - 1][c] == '.' || grid[r - upSpaces - 1][c] == 'K')) {
                    upSpaces++;
                    moves.add(new Move(p.id, "atas", upSpaces));
                }

                int downSpaces = 0;
                while (r + p.length + downSpaces < rows && (grid[r + p.length + downSpaces][c] == '.' || grid[r + p.length + downSpaces][c] == 'K')) {
                    downSpaces++;
                    moves.add(new Move(p.id, "bawah", downSpaces));
                }
            }
        }

        return moves;
    }

    public Board applyMove(Move move) {
        char[][] newGrid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, newGrid[i], 0, cols);
        }

        String[] newConfig = new String[rows];
        for (int i = 0; i < rows; i++) {
            newConfig[i] = new String(newGrid[i]);
        }

        Board newBoard = new Board(newConfig);
        newBoard.exitRow = this.exitRow;
        newBoard.exitCol = this.exitCol;

        for (Piece p : newBoard.pieces) {
            if (p.id == move.pieceId) {
                if (p.isHorizontal) {
                    for (int i = 0; i < p.length; i++) {
                        int rr = p.row;
                        int cc = p.col + i;
                        if (newBoard.grid[rr][cc] != 'K') {
                            newBoard.grid[rr][cc] = '.';
                        }
                    }

                    if (move.direction.equals("kiri")) {
                        p.col -= move.amount;
                    } else if (move.direction.equals("kanan")) {
                        p.col += move.amount;
                    }

                    for (int i = 0; i < p.length; i++) {
                        if (newBoard.grid[p.row][p.col + i] != 'K') {
                            newBoard.grid[p.row][p.col + i] = p.id;
                        }
                    }
                } else {
                    for (int i = 0; i < p.length; i++) {
                        int rr = p.row + i;
                        int cc = p.col;
                        if (newBoard.grid[rr][cc] != 'K') {
                            newBoard.grid[rr][cc] = '.';
                        }
                    }

                    if (move.direction.equals("atas")) {
                        p.row -= move.amount;
                    } else if (move.direction.equals("bawah")) {
                        p.row += move.amount;
                    }

                    for (int i = 0; i < p.length; i++) {
                        if (newBoard.grid[p.row + i][p.col] != 'K') {
                            newBoard.grid[p.row + i][p.col] = p.id;
                        }
                    }
                }
                break;
            }
        }

        return newBoard;
    }

    public void printWithHighlight(Move move) {
        final String RESET = "\u001B[0m";
        final String BLUE = "\u001B[34m";    // Primary piece
        final String YELLOW = "\u001B[33m";  // Exit
        final String RED = "\u001B[31m";     // Piece yang digerakkan
    
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char ch = grid[i][j];
    
                if (ch == 'K') {
                    System.out.print(YELLOW + ch + RESET);
                } else if (primaryPiece != null && isInsidePiece(i, j, primaryPiece)) {
                    System.out.print(BLUE + ch + RESET);
                } else if (ch == move.pieceId) {
                    System.out.print(RED + ch + RESET);
                } else {
                    System.out.print(ch);
                }
            }
            System.out.println();
        }
    }
    private boolean isInsidePiece(int row, int col, Piece piece) {
        if (piece.isHorizontal) {
            return row == piece.row && col >= piece.col && col < piece.col + piece.length;
        } else {
            return col == piece.col && row >= piece.row && row < piece.row + piece.length;
        }
    }
    
    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Piece[] getPieces() {
        return pieces.toArray(new Piece[0]);
    }

    public Piece getPieceById(char id) {
        for (Piece piece : pieces) {
            if (piece != null && piece.id == id) {
                return piece;
            }
        }
        return null;
    }

    public void addVehicle(char id, boolean isHorizontal, int length, int row, int col) {
        // Check if position is valid
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IllegalArgumentException("Vehicle position must be within board boundaries");
        }

        // Check if vehicle fits
        if (isHorizontal) {
            if (col + length > cols) {
                throw new IllegalArgumentException("Vehicle does not fit horizontally");
            }
        } else {
            if (row + length > rows) {
                throw new IllegalArgumentException("Vehicle does not fit vertically");
            }
        }

        // Check if space is available
        for (int i = 0; i < length; i++) {
            int r = isHorizontal ? row : row + i;
            int c = isHorizontal ? col + i : col;
            if (grid[r][c] != '.') {
                throw new IllegalArgumentException("Space is already occupied");
            }
        }

        // Create new piece with next available ID
        char vehicleId = getNextAvailableId();
        Piece piece = new Piece(vehicleId, isHorizontal, length, row, col);
        
        // Add to pieces array
        pieces.add(piece);
        if (id == primaryVehicleId) {
            primaryPiece = piece;
        }

        // Update grid
        for (int i = 0; i < length; i++) {
            int r = isHorizontal ? row : row + i;
            int c = isHorizontal ? col + i : col;
            grid[r][c] = vehicleId;
        }
    }

    private char getNextAvailableId() {
        // Start from 'A'
        char nextId = 'A';
        
        // Find the next available ID
        while (true) {
            boolean idExists = false;
            for (Piece piece : pieces) {
                if (piece.id == nextId) {
                    idExists = true;
                    break;
                }
            }
            
            if (!idExists) {
                return nextId;
            }
            
            // Simply increment to next letter
            nextId++;
            if (nextId > 'Z') {
                nextId = 'A'; // Reset to 'A' if we reach 'Z'
            }
        }
    }

    public void removePiece(char id) {
        Piece pieceToRemove = null;
        for (Piece piece : pieces) {
            if (piece.id == id) {
                pieceToRemove = piece;
                break;
            }
        }

        if (pieceToRemove == null) {
            throw new IllegalArgumentException("Piece not found");
        }

        // Remove from grid
        for (int i = 0; i < pieceToRemove.length; i++) {
            int r = pieceToRemove.isHorizontal ? pieceToRemove.row : pieceToRemove.row + i;
            int c = pieceToRemove.isHorizontal ? pieceToRemove.col + i : pieceToRemove.col;
            grid[r][c] = '.';
        }

        // Remove from pieces list
        pieces.remove(pieceToRemove);

        // Update primary piece if needed
        if (pieceToRemove.id == primaryVehicleId) {
            primaryPiece = null;
            primaryVehicleId = 'A';
        }
    }

    public void setPrimaryVehicle(char id) {
        boolean found = false;
        Piece newPrimary = null;
        for (Piece piece : pieces) {
            if (piece.id == id) {
                found = true;
                newPrimary = piece;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Vehicle with ID " + id + " not found");
        }

        // Remove old primary piece's P from grid
        if (primaryPiece != null) {
            for (int i = 0; i < primaryPiece.length; i++) {
                int r = primaryPiece.isHorizontal ? primaryPiece.row : primaryPiece.row + i;
                int c = primaryPiece.isHorizontal ? primaryPiece.col + i : primaryPiece.col;
                grid[r][c] = '.';
            }
        }

        // Set new primary piece
        primaryPiece = newPrimary;
        primaryVehicleId = 'P';

        // Update grid with new primary piece
        for (int i = 0; i < primaryPiece.length; i++) {
            int r = primaryPiece.isHorizontal ? primaryPiece.row : primaryPiece.row + i;
            int c = primaryPiece.isHorizontal ? primaryPiece.col + i : primaryPiece.col;
            grid[r][c] = 'P';
        }
    }

    public char getPrimaryVehicleId() {
        return primaryVehicleId;
    }

    public void setExit(int row, int col) {
        // Check if exit is on the edge
        boolean isValidExit = (row == -1 || row == rows || col == -1 || col == cols);
        if (!isValidExit) {
            throw new IllegalArgumentException("Exit point must be on the edge of the board");
        }

        // Check if exit is on a valid position
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            throw new IllegalArgumentException("Exit point must be outside the grid");
        }

        this.exitRow = row;
        this.exitCol = col;
    }
}
