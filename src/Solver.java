public abstract class Solver {
    protected Board startBoard;
    protected Board resultBoard = null;  // hasil akhir pencarian

    public Solver(Board board) {
        this.startBoard = board;
    }

    // Method yang dipanggil GUI untuk ambil hasil akhir
    public Board getResultBoard() {
        return resultBoard;
    }

    // Harus diset di kelas turunan jika ditemukan solusi
    public abstract void solve();
}
