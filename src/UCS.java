import java.util.*;

public class UCS extends Solver {
    public UCS(Board board) {
        super(board);
    }

    @Override
    public void solve() {
        long startTime = System.currentTimeMillis();

        // Gunakan comparator yang lebih konkret untuk menghindari inconsistent ordering
        PriorityQueue<State> pq = new PriorityQueue<>(
            (a, b) -> {
                if (a.cost != b.cost) {
                    return Integer.compare(a.cost, b.cost);
                }
                // Tie-breaker untuk konsistensi
                return Integer.compare(a.hashCode(), b.hashCode());
            }
        );
        
        Set<String> visited = new HashSet<>();

        State start = new State(startBoard, new ArrayList<>(), 0, 0);
        pq.add(start);

        int nodesVisited = 0;

        while (!pq.isEmpty()) {
            State current = pq.poll();
            String currentHash = current.hash();

            if (visited.contains(currentHash)) continue;
            visited.add(currentHash);
            nodesVisited++;

            if (current.board.isGoal()) {
                // Selesai!
                long endTime = System.currentTimeMillis();
                System.out.println("Solusi ditemukan dengan UCS!");
                System.out.println("Jumlah node dikunjungi: " + nodesVisited);
                System.out.println("Jumlah langkah: " + current.path.size());
                System.out.println("Waktu eksekusi: " + (endTime - startTime) + " ms");
                this.resultBoard = current.board;
                printPath(current);
                return;
            }

            for (Move move : current.board.getPossibleMoves()) {
                Board newBoard = current.board.applyMove(move);
                List<Move> newPath = new ArrayList<>(current.path);
                newPath.add(move);
                pq.add(new State(newBoard, newPath, current.cost + move.amount, 0));
            }
        }

        System.out.println("Tidak ada solusi ditemukan.");
    }

    private void printPath(State finalState) {
        System.out.println("Papan Awal:");
        startBoard.print();
        Board current = startBoard;

        int step = 1;
        for (Move move : finalState.path) {
            System.out.println("Gerakan " + step + ": " + move);
            current = current.applyMove(move);
            current.print();
            step++;
        }
    }
}