import java.util.List;

public abstract class Solver {
    protected Board startBoard;
    protected List<Move> solutionPath;
    protected int visitedCount = 0;
    protected long executionTimeMs = 0;

    public Solver(Board board) {
        this.startBoard = board;
    }

    public abstract void solve();

    public List<Move> getSolutionPath() {
        return solutionPath;
    }

    public int getVisitedCount() {
        return visitedCount;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public Board getResultBoard() {
        if (solutionPath == null || solutionPath.isEmpty()) {
            return null;
        }
        Board current = startBoard;
        for (Move move : solutionPath) {
            current = current.applyMove(move);
        }
        return current;
    }
}
