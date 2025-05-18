import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Board board = null;

        System.out.print("Masukkan nama file input: ");
        String filename = scanner.nextLine();

        try (BufferedReader reader = new BufferedReader(new FileReader("../test/" + filename))) {
            // Baca dimensi baris dan kolom
            String[] dim = reader.readLine().trim().split("\\s+");
            if (dim.length < 2) {
                System.out.println("Error: Format baris pertama tidak valid.");
                return;
            }

            int rows = Integer.parseInt(dim[0]);
            int cols = Integer.parseInt(dim[1]);

            // Baca jumlah kendaraan
            int n = Integer.parseInt(reader.readLine().trim());

            // Baca seluruh sisa baris file ke list
            List<String> fullLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    fullLines.add(line);
                }
            }

            if (fullLines.size() < rows) {
                System.out.println("Error: File kurang dari " + rows + " baris konfigurasi.");
                return;
            }

            // Ambil baris pertama sebanyak rows dan kolom sebanyak cols sebagai grid
            String[] config = new String[rows];
            for (int i = 0; i < rows; i++) {
                String l = fullLines.get(i);
                if (l.length() < cols) {
                    l = String.format("%-" + cols + "s", l).replace(' ', '.');
                } else if (l.length() > cols) {
                    l = l.substring(0, cols);
                }
                config[i] = l;
            }

            // Cari posisi 'K'
            int exitRow = -1, exitCol = -1;
            outer:
            for (int i = 0; i < fullLines.size(); i++) {
                int kIndex = fullLines.get(i).indexOf('K');
                if (kIndex >= 0) {
                    exitRow = i;
                    exitCol = kIndex;
                    break outer;
                }
            }

            board = new Board(config);
            board.exitRow = exitRow;
            board.exitCol = exitCol;

            System.out.println("\nPapan Awal:");
            board.print();

            if (board.primaryPiece == null) {
                System.out.println("Error: Primary piece (P) tidak ditemukan di papan!");
                return;
            }
            if (board.exitRow == -1 || board.exitCol == -1) {
                System.out.println("Error: Pintu keluar (K) tidak ditemukan di papan!");
                return;
            }

            System.out.println("Primary piece: " + board.primaryPiece);
            System.out.println("Pintu keluar berada di: (" + board.exitRow + "," + board.exitCol + ")");

            if (board.isGoal()) {
                System.out.println("Primary piece sudah mencapai pintu keluar.");
                return;
            }

            System.out.println("Primary piece belum mencapai pintu keluar.");
            System.out.println("\nGerakan yang mungkin dari posisi awal:");
            for (Move move : board.getPossibleMoves()) {
                System.out.println("- " + move);
            }

        } catch (IOException e) {
            System.out.println("Gagal membaca file: " + e.getMessage());
            return;
        } catch (NumberFormatException e) {
            System.out.println("Error parsing angka dalam file input: " + e.getMessage());
            return;
        }

        // Algoritma dan Heuristic
        System.out.print("\nPilih algoritma (UCS/GBFS/A*/IDA*): ");
        String algo = scanner.nextLine().trim().toLowerCase();

        String heuristicChoice = "1"; // Default ke Manhattan
        if (algo.equals("ucs") || algo.equals("gbfs") || algo.equals("a*") || algo.equals("astar") || algo.equals("ida*") || algo.equals("idastar")) {
            System.out.println("Pilih heuristic:");
            System.out.println("1 = Manhattan Distance");
            System.out.println("2 = Euclidean Distance");
            System.out.println("3 = Chebyshev Distance");
            System.out.print("Pilihan (1-3): ");
            heuristicChoice = scanner.nextLine().trim();
            if (!heuristicChoice.matches("[123]")) {
                System.out.println("Heuristic tidak valid. Menggunakan Manhattan Distance (1).");
                heuristicChoice = "1";
            }
        }

        Solver solver = switch (algo) {
            case "ucs" -> new UCS(board, heuristicChoice);
            case "gbfs" -> new GBFS(board, heuristicChoice);
            case "a*", "astar" -> new AStar(board, heuristicChoice);
            case "ida*", "idastar" -> new IDAStar(board, heuristicChoice);
            default -> {
                System.out.println("Algoritma tidak dikenali. Menggunakan A* sebagai default.");
                yield new AStar(board, heuristicChoice);
            }
        };

        if (solver != null) {
            System.out.println("\nMenjalankan solver " + algo + "...");
            long start = System.nanoTime();
            solver.solve();
            long end = System.nanoTime();
            double durationMs = (end - start) / 1e6;

            List<Move> solution = solver.getSolutionPath();
            Board current = board;

            if (solution == null || solution.isEmpty()) {
                System.out.println("Tidak ditemukan solusi.");
            } else {
                System.out.println("Solusi ditemukan dengan " + algo.toUpperCase() + "!");
                System.out.println("\nPapan Awal:");
                current.print();

                int step = 1;
                for (Move move : solution) {
                    System.out.println("Gerakan " + step + ": " + move);
                    current = current.applyMove(move);
                    current.printWithHighlight(move);
                    step++;
                }
                System.out.println("Jumlah node dikunjungi: " + solver.getVisitedCount());
                System.out.println("Jumlah langkah: " + solution.size());
                System.out.printf("Waktu eksekusi: %.2f ms\n", durationMs);
            }
        }

        scanner.close();
    }
}
