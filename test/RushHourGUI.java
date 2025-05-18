import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.*;
import java.util.List;

public class RushHourGUI extends Application {

    private Board board;
    private GridPane gridPane = new GridPane();
    private Label statusLabel = new Label("Load puzzle dulu...");
    private ComboBox<String> algoComboBox = new ComboBox<>();
    private Button solveButton = new Button("Jalankan Solver");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rush Hour Solver");

        // Setup kontrol
        Button loadButton = new Button("Load Puzzle");
        loadButton.setOnAction(e -> loadPuzzle(primaryStage));

        algoComboBox.getItems().addAll("UCS", "GBFS", "A*");
        algoComboBox.setValue("A*");

        solveButton.setDisable(true);
        solveButton.setOnAction(e -> runSolver());

        HBox controls = new HBox(10, loadButton, algoComboBox, solveButton);
        controls.setAlignment(Pos.CENTER);

        VBox root = new VBox(10, controls, statusLabel, gridPane);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(root, 600, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadPuzzle(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih file puzzle");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
                if (lines.size() < 3) {
                    statusLabel.setText("File tidak valid.");
                    return;
                }
                // Parse dimensi
                String[] dim = lines.get(0).split(" ");
                int rows = Integer.parseInt(dim[0]);
                int cols = Integer.parseInt(dim[1]);
                int n = Integer.parseInt(lines.get(1));

                String[] config = new String[rows];
                for (int i = 0; i < rows; i++) {
                    config[i] = lines.get(i + 2);
                }

                board = new Board(config);
                board.exitRow = findExitRow(lines);
                board.exitCol = findExitCol(lines);

                statusLabel.setText("Puzzle loaded. Ukuran: " + rows + "x" + cols);
                drawBoard();
                solveButton.setDisable(false);

            } catch (IOException | NumberFormatException ex) {
                statusLabel.setText("Error baca file: " + ex.getMessage());
            }
        }
    }

    private int findExitRow(List<String> lines) {
        for (int i = 2; i < lines.size(); i++) {
            if (lines.get(i).contains("K")) return i - 2; // offset karena baris dimensi
        }
        return -1;
    }

    private int findExitCol(List<String> lines) {
        for (int i = 2; i < lines.size(); i++) {
            int idx = lines.get(i).indexOf('K');
            if (idx >= 0) return idx;
        }
        return -1;
    }

    private void drawBoard() {
        gridPane.getChildren().clear();
        if (board == null) return;

        for (int r = 0; r < board.rows; r++) {
            for (int c = 0; c < board.cols; c++) {
                char ch = board.grid[r][c];
                Rectangle rect = new Rectangle(50, 50);
                rect.setStroke(Color.BLACK);

                if (ch == '.') {
                    rect.setFill(Color.LIGHTGRAY);
                } else {
                    rect.setFill(getColorForSymbol(ch));
                }
                

                Label label = new Label(String.valueOf(ch));
                StackPane cell = new StackPane(rect, label);
                gridPane.add(cell, c, r);
            }
        }
    }

    private void runSolver() {
        if (board == null) {
            statusLabel.setText("Load puzzle dulu.");
            return;
        }

        String algo = algoComboBox.getValue();
        Solver solver = null;

        switch (algo.toLowerCase()) {
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
            case "ida*":
            case "idastar":
                solver = new IDAStar(board);
                break;
            default:
                statusLabel.setText("Algoritma tidak dikenali.");
                return;
        }

        statusLabel.setText("Menjalankan solver " + algo + "...");
        solver.solve();

        // Setelah selesai, tampilkan hasil dan update papan
        Board result = solver.getResultBoard();
        if (result != null) {
            List<Move> solution = solver.getSolutionPath();
            animateSolution(solution);
        } else {
            statusLabel.setText("Solver tidak menemukan solusi.");
        }
        
    }

    private void animateSolution(List<Move> moves) {
        if (board == null || moves == null || moves.isEmpty()) return;
    
        Timeline timeline = new Timeline();
        Board current = board;
    
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            Board next = current.applyMove(move);
            int step = i;
            KeyFrame frame = new KeyFrame(Duration.seconds(0.5 * (step + 1)), e -> {
                board = next;
                drawBoard();
                statusLabel.setText("Langkah ke-" + (step + 1) + ": " + move);
            });
            timeline.getKeyFrames().add(frame);
            current = next;
        }
    
        timeline.play();
    }
    
    private static Color getColorForSymbol(char symbol) {
        return switch (symbol) {
            case 'A' -> Color.RED;
            case 'B' -> Color.BLUE;
            case 'C' -> Color.GREEN;
            case 'D' -> Color.ORANGE;
            case 'E' -> Color.MAGENTA;
            case 'F' -> Color.CYAN;
            case 'G' -> Color.PINK;
            case 'H' -> Color.rgb(255, 165, 0);       // Orange terang
            case 'I' -> Color.rgb(128, 0, 128);       // Ungu
            case 'J' -> Color.rgb(0, 255, 255);       // Aqua
            case 'K' -> Color.rgb(255, 215, 0);       // Emas
            case 'L' -> Color.rgb(139, 69, 19);       // Coklat
            case 'M' -> Color.rgb(255, 0, 255);       // Fuchsia
            case 'N' -> Color.rgb(0, 128, 128);       // Teal
            case 'O' -> Color.rgb(128, 128, 0);       // Olive
            case 'P' -> Color.rgb(0, 0, 128);         // Navy
            case 'Q' -> Color.rgb(178, 34, 34);       // Merah gelap
            case 'R' -> Color.rgb(70, 130, 180);      // Baja Biru
            case 'S' -> Color.rgb(218, 112, 214);     // Orchid
            case 'T' -> Color.rgb(240, 230, 140);     // Khaki
            case 'U' -> Color.rgb(154, 205, 50);      // Hijau Kuning
            case 'V' -> Color.rgb(255, 99, 71);       // Tomato
            case 'W' -> Color.rgb(173, 216, 230);     // Biru Muda
            case 'X' -> Color.rgb(199, 21, 133);      // Deep Pink
            case 'Y' -> Color.rgb(0, 191, 255);       // Deep Sky Blue
            case 'Z' -> Color.rgb(144, 238, 144);     // Hijau Muda
            default -> Color.LIGHTGRAY;
        };
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
}
