import java.util.*;

public class AStar extends Solver {
    public AStar(Board board) {
        super(board);
    }

    @Override
    public void solve() {
        long startTime = System.currentTimeMillis();

        // Gunakan comparator yang lebih konkret untuk menghindari inconsistent ordering
        PriorityQueue<State> pq = new PriorityQueue<>(
            (a, b) -> {
                int scoreA = a.cost + a.heuristic;
                int scoreB = b.cost + b.heuristic;
                if (scoreA != scoreB) {
                    return Integer.compare(scoreA, scoreB);
                }
                // Tie-breaker untuk konsistensi (bisa menggunakan path length atau hashCode)
                return Integer.compare(a.hashCode(), b.hashCode());
            }
        );
        
        Set<String> visited = new HashSet<>();

        State start = new State(startBoard, new ArrayList<>(), 0, Heuristic.blockingCars(startBoard));
        pq.add(start);

        int nodesVisited = 0;

        while (!pq.isEmpty()) {
            State current = pq.poll();
            String currentHash = current.hash();
            
            if (visited.contains(currentHash)) continue;
            visited.add(currentHash);
            nodesVisited++;

            if (current.board.isGoal()) {
                long endTime = System.currentTimeMillis();
                System.out.println("Solusi ditemukan dengan A*!");
                System.out.println("Jumlah node dikunjungi: " + nodesVisited);
                System.out.println("Jumlah langkah: " + current.path.size());
                System.out.println("Waktu eksekusi: " + (endTime - startTime) + " ms");
                printPath(current);
                return;
            }

            for (Move move : current.board.getPossibleMoves()) {
                Board newBoard = current.board.applyMove(move);
                List<Move> newPath = new ArrayList<>(current.path);
                newPath.add(move);
                int h = Heuristic.blockingCars(newBoard);
                pq.add(new State(newBoard, newPath, current.cost + move.amount, h));
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