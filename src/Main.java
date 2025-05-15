import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Aktifkan debug Board jika diperlukan
        Board.DEBUG = false;

        System.out.print("Masukkan nama file input: ");
        String filename = scanner.nextLine();

        Board board = null;

        try (BufferedReader reader = new BufferedReader(new FileReader("../test/" + filename))) {
            // Baca dimensi baris dan kolom
            String[] dim = reader.readLine().split(" ");
            int rows = Integer.parseInt(dim[0]);
            int cols = Integer.parseInt(dim[1]);

            // Baca jumlah kendaraan (belum digunakan)
            int n = Integer.parseInt(reader.readLine());

            // Baca seluruh sisa baris file ke list
            List<String> fullLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                fullLines.add(line);
            }

            // Pastikan cukup baris untuk konfigurasi grid
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

            // Cari posisi pintu keluar 'K' di seluruh file
            int exitRow = -1, exitCol = -1;
            outer:
            for (int i = 0; i < fullLines.size(); i++) {
                String l = fullLines.get(i);
                int kIndex = l.indexOf('K');
                if (kIndex >= 0) {
                    exitRow = i;
                    exitCol = kIndex;
                    break outer;
                }
            }

            // Buat Board dan set posisi pintu keluar
            board = new Board(config);
            board.exitRow = exitRow;
            board.exitCol = exitCol;

            System.out.println("\nPapan Awal:");
            board.print();

            if (board.primaryPiece == null) {
                System.out.println("Error: Primary piece (P) tidak ditemukan di papan!");
                return;
            }
            System.out.println("Primary piece: " + board.primaryPiece);
            System.out.println("Pintu keluar berada di: (" + board.exitRow + "," + board.exitCol + ")");

            if (board.exitRow == -1 || board.exitCol == -1) {
                System.out.println("Error: Pintu keluar (K) tidak ditemukan di papan!");
                return;
            }

            if (board.isGoal()) {
                System.out.println("Primary piece sudah mencapai pintu keluar.");
            } else {
                System.out.println("Primary piece belum mencapai pintu keluar.");
                System.out.println("\nGerakan yang mungkin dari posisi awal:");
                List<Move> possibleMoves = board.getPossibleMoves();
                for (Move move : possibleMoves) {
                    System.out.println("- " + move);
                }
            }
        } catch (IOException e) {
            System.out.println("Gagal membaca file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Format file tidak valid: " + e.getMessage());
        }

        if (board != null) {
            System.out.print("\nPilih algoritma (UCS/GBFS/A*): ");
            String algo = scanner.nextLine().trim().toLowerCase();

            Solver solver = null;
            switch (algo) {
                case "ucs":
                    solver = new UCS(board);
                    break;
                case "gbfs":
                    solver = new GBFS(board);
                    break;
                case "a*":
                case "astar":
                    solver = new AStar(board);
                    break;
                default:
                    System.out.println("Algoritma tidak dikenali. Menggunakan A* sebagai default.");
                    solver = new AStar(board);
            }

            if (solver != null) {
                System.out.println("\nMenjalankan solver " + algo + "...");
                solver.solve();
            }
        }

        scanner.close();
    }
}
