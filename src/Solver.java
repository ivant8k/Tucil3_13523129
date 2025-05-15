public abstract class Solver {
    protected Board startBoard;

    public Solver(Board board) {
        this.startBoard = board;
    }

    public abstract void solve();
}
