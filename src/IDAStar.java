import java.util.*;

public class IDAStar extends Solver {
    private int nodesVisited = 0;
    private long startTime;
    private String heuristicMode;
    
    public IDAStar(Board board, String heuristicMode) {
        super(board);
        this.heuristicMode = heuristicMode;
    }

    @Override
    public void solve() {
        startTime = System.currentTimeMillis();
        int threshold = Heuristic.estimate(startBoard, heuristicMode);
    
        while (true) {
            Set<String> visited = new HashSet<>();
            Result result = dfs(new State(startBoard, new ArrayList<>(), 0, 0), threshold, visited);
    
            if (result.found) {
                this.solutionPath = result.state.path;
                long endTime = System.currentTimeMillis();
                this.visitedCount = nodesVisited;
                this.executionTimeMs = endTime - startTime;
                return;
            }
    
            if (result.nextThreshold == Integer.MAX_VALUE) {
                System.out.println("Tidak ada solusi ditemukan.");
                return;
            }
    
            threshold = result.nextThreshold;
        }
    }
    

    private Result dfs(State current, int threshold, Set<String> visited) {
        int f = current.cost + Heuristic.estimate(current.board, heuristicMode);
        if (f > threshold) {
            return new Result(false, null, f);
        }

        if (current.board.isGoal()) {
            return new Result(true, current, f);
        }

        visited.add(current.board.toString());
        int minThreshold = Integer.MAX_VALUE;

        for (Move move : current.board.getPossibleMoves()) {
            Board nextBoard = current.board.applyMove(move);
            if (visited.contains(nextBoard.toString())) continue;

            List<Move> newPath = new ArrayList<>(current.path);
            newPath.add(move);
            State nextState = new State(nextBoard, newPath, current.cost + 1, 0);

            Result result = dfs(nextState, threshold, visited);
            if (result.found) return result;

            minThreshold = Math.min(minThreshold, result.nextThreshold);
        }
        nodesVisited++;
        return new Result(false, null, minThreshold);
    }

    private static class Result {
        boolean found;
        State state;
        int nextThreshold;

        Result(boolean found, State state, int nextThreshold) {
            this.found = found;
            this.state = state;
            this.nextThreshold = nextThreshold;
        }
    }
}
