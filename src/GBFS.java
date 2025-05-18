import java.util.*;

public class GBFS extends Solver {
    private String heuristicMode;

    public GBFS(Board board, String heuristicMode) {
        super(board);
        this.heuristicMode = heuristicMode;
    }

    @Override
    public void solve() {
        long startTime = System.currentTimeMillis();

        // Gunakan comparator yang lebih konkret untuk menghindari inconsistent ordering
        PriorityQueue<State> pq = new PriorityQueue<>(
            (a, b) -> {
                if (a.heuristic != b.heuristic) {
                    return Integer.compare(a.heuristic, b.heuristic);
                }
                // Tie-breaker untuk konsistensi
                return Integer.compare(a.hashCode(), b.hashCode());
            }
        );
        
        Set<String> visited = new HashSet<>();

        State start = new State(startBoard, new ArrayList<>(), 0, Heuristic.estimate(startBoard, heuristicMode));
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
                System.out.println("Solusi ditemukan dengan GBFS!");
                System.out.println("Jumlah node dikunjungi: " + nodesVisited);
                System.out.println("Jumlah langkah: " + current.path.size());
                System.out.println("Waktu eksekusi: " + (endTime - startTime) + " ms");
                this.solutionPath = current.path;
                printPath(current);
                return;
            }

            for (Move move : current.board.getPossibleMoves()) {
                Board newBoard = current.board.applyMove(move);
                List<Move> newPath = new ArrayList<>(current.path);
                newPath.add(move);
                int h = Heuristic.estimate(newBoard, heuristicMode);
                pq.add(new State(newBoard, newPath, current.path.size() + 1, h));
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