import java.util.List;

public abstract class Solver {
    protected Board startBoard;
    protected Board resultBoard = null;
    protected List<Move> solutionPath = null;

    public Solver(Board board) {
        this.startBoard = board;
    }

    public Board getResultBoard() {
        return resultBoard;
    }

    public List<Move> getSolutionPath() {
        return solutionPath;
    }

    public abstract void solve();
}
