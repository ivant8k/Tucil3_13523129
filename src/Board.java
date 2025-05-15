import java.util.*;

public class Board {
    public static boolean DEBUG = false;

    public char[][] grid;
    public List<Piece> pieces = new ArrayList<>();
    public Piece primaryPiece;
    public int exitRow = -1, exitCol = -1;
    public int rows, cols;

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
}
