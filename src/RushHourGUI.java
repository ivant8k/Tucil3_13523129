import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
                } else if (ch == 'K') {
                    rect.setFill(Color.GOLD);
                } else {
                    rect.setFill(Color.DARKCYAN);
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
            default:
                statusLabel.setText("Algoritma tidak dikenali.");
                return;
        }

        statusLabel.setText("Menjalankan solver " + algo + "...");
        solver.solve();

        // Setelah selesai, tampilkan hasil dan update papan
        Board result = solver.getResultBoard();
        if (result != null) {
            board = result;
            drawBoard();
            statusLabel.setText("Solver selesai. Status goal: " + (board.isGoal() ? "TERCAPAI" : "BELUM"));
        } else {
            statusLabel.setText("Solver tidak menemukan solusi.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
