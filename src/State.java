import java.util.List;

public class State implements Comparable<State> {
    public Board board;
    public List<Move> path;
    public int cost;
    public int heuristic;
    private String hashString;  // Menyimpan hash untuk mengurangi komputasi berulang

    public State(Board board, List<Move> path, int cost, int heuristic) {
        this.board = board;
        this.path = path;
        this.cost = cost;
        this.heuristic = heuristic;
        this.hashString = null;  // Akan dihitung saat diperlukan (lazy initialization)
    }

    @Override
    public int compareTo(State other) {
        // Bandingkan berdasarkan total cost + heuristic (untuk A*)
        int thisTotal = this.cost + this.heuristic;
        int otherTotal = other.cost + other.heuristic;
        return Integer.compare(thisTotal, otherTotal);
    }

    public String hash() {
        if (hashString == null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < board.rows; i++) {
                for (int j = 0; j < board.cols; j++) {
                    sb.append(board.grid[i][j]);
                }
            }
            hashString = sb.toString();
        }
        return hashString;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        State other = (State) obj;
        return hash().equals(other.hash());
    }
    
    @Override
    public int hashCode() {
        return hash().hashCode();
    }
}