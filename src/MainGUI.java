import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainGUI extends JFrame {
    private Board board;
    private Solver solver;
    private List<Move> solutionPath;
    private int currentStep;
    private JLabel[][] boardLabels;
    private JTextArea outputArea;
    private JComboBox<String> algoComboBox;
    private JComboBox<String> heuristicComboBox;
    private JPanel boardPanel;
    private JButton nextStepButton;
    private JButton prevStepButton;
    private JButton playButton;
    private Timer animationTimer;
    private JTextField rowsField;
    private JTextField colsField;
    private JTextField[][] inputGrid;
    private JPanel inputPanel;
    private JComboBox<String> edgeComboBox;
    private JComboBox<Integer> exitPositionComboBox;

    private static final Color PRIMARY_COLOR = Color.BLUE;
    private static final Color EXIT_COLOR = Color.RED;
    private static final Color MOVE_COLOR = Color.GREEN;
    private static final Color EMPTY_COLOR = Color.WHITE;
    private static final Color PIECE_COLOR = Color.GRAY;

    public MainGUI() {
        setTitle("Rush Hour Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        // Top panel for controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainPanel.add(controlPanel, BorderLayout.NORTH);

        // File chooser button
        JButton loadButton = new JButton("Load Board (.txt)");
        controlPanel.add(loadButton);

        // Algorithm selection
        String[] algorithms = {"UCS", "GBFS", "A*", "IDA*"};
        algoComboBox = new JComboBox<>(algorithms);
        controlPanel.add(new JLabel("Algorithm:"));
        controlPanel.add(algoComboBox);

        // Heuristic selection
        String[] heuristics = {"Manhattan", "Euclidean", "Chebyshev", "Combined"};
        heuristicComboBox = new JComboBox<>(heuristics);
        controlPanel.add(new JLabel("Heuristic:"));
        controlPanel.add(heuristicComboBox);

        // Add listener to show/hide heuristic based on algorithm selection
        algoComboBox.addActionListener(e -> {
            String selectedAlgo = (String) algoComboBox.getSelectedItem();
            boolean showHeuristic = !selectedAlgo.equals("UCS");
            heuristicComboBox.setVisible(showHeuristic);
            controlPanel.getComponent(controlPanel.getComponentCount() - 2).setVisible(showHeuristic); // Label
        });

        // Run button
        JButton runButton = new JButton("Run Solver");
        controlPanel.add(runButton);

        // Center panel with tabs for input and board display
        JTabbedPane centerPane = new JTabbedPane();
        mainPanel.add(centerPane, BorderLayout.CENTER);

        // Input panel for graphical configuration
        inputPanel = new JPanel(new BorderLayout(5, 5));
        centerPane.addTab("Input Board", inputPanel);

        // Input controls
        JPanel inputControlPanel = new JPanel(new FlowLayout());
        inputPanel.add(inputControlPanel, BorderLayout.NORTH);

        inputControlPanel.add(new JLabel("Rows:"));
        rowsField = new JTextField("6", 3);
        inputControlPanel.add(rowsField);

        inputControlPanel.add(new JLabel("Cols:"));
        colsField = new JTextField("6", 3);
        inputControlPanel.add(colsField);

        JButton initGridButton = new JButton("Initialize Grid");
        inputControlPanel.add(initGridButton);

        JButton clearGridButton = new JButton("Clear Grid");
        inputControlPanel.add(clearGridButton);

        inputControlPanel.add(new JLabel("Exit Edge:"));
        edgeComboBox = new JComboBox<>(new String[]{"Top", "Bottom", "Left", "Right"});
        inputControlPanel.add(edgeComboBox);

        inputControlPanel.add(new JLabel("Position:"));
        exitPositionComboBox = new JComboBox<>();
        inputControlPanel.add(exitPositionComboBox);

        JButton submitButton = new JButton("Submit Configuration");
        inputControlPanel.add(submitButton);

        // Instruction label
        JLabel instructionLabel = new JLabel("Enter pieces (e.g., PP or PPP for primary piece, AA for others, . for empty)");
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        inputPanel.add(instructionLabel, BorderLayout.SOUTH);

        // Board display panel
        boardPanel = new JPanel();
        boardPanel.setBackground(Color.LIGHT_GRAY);
        centerPane.addTab("Board Display", boardPanel);

        // Output panel
        outputArea = new JTextArea(10, 30);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        mainPanel.add(new JScrollPane(outputArea), BorderLayout.EAST);

        // Bottom panel for animation controls
        JPanel animationPanel = new JPanel(new FlowLayout());
        animationPanel.add(new JLabel("Animation Controls:"));
        prevStepButton = new JButton("Previous");
        animationPanel.add(prevStepButton);
        nextStepButton = new JButton("Next");
        animationPanel.add(nextStepButton);
        playButton = new JButton("Play");
        animationPanel.add(playButton);
        mainPanel.add(animationPanel, BorderLayout.SOUTH);

        // Disable buttons initially
        prevStepButton.setEnabled(false);
        nextStepButton.setEnabled(false);
        playButton.setEnabled(false);
        submitButton.setEnabled(false);

        // Drag-and-drop support for board panel
        boardPanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File file = files.get(0);
                        if (file.getName().endsWith(".txt")) {
                            loadBoardFromFile(file);
                            centerPane.setSelectedIndex(1); // Switch to board display
                            return true;
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainGUI.this, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }
        });

        // Load button action
        loadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser("../test");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
            if (fileChooser.showOpenDialog(MainGUI.this) == JFileChooser.APPROVE_OPTION) {
                loadBoardFromFile(fileChooser.getSelectedFile());
                centerPane.setSelectedIndex(1);
            }
        });

        // Initialize grid button action
        initGridButton.addActionListener(e -> initializeInputGrid());

        // Clear grid button action
        clearGridButton.addActionListener(e -> clearInputGrid());

        // Edge selection action
        edgeComboBox.addActionListener(e -> updateExitPositionComboBox());

        // Submit configuration button action
        submitButton.addActionListener(e -> submitConfiguration());

        // Run button action
        runButton.addActionListener(e -> runSolver());

        // Animation controls
        prevStepButton.addActionListener(e -> showPreviousStep());
        nextStepButton.addActionListener(e -> showNextStep());
        playButton.addActionListener(e -> toggleAnimation());

        // Animation timer (500ms per step)
        animationTimer = new Timer(500, e -> showNextStep());
        animationTimer.setRepeats(true);
    }

    private void initializeInputGrid() {
        try {
            int rows = Integer.parseInt(rowsField.getText().trim());
            int cols = Integer.parseInt(colsField.getText().trim());
            if (rows <= 0 || cols <= 0) {
                throw new IllegalArgumentException("Rows and columns must be positive");
            }

            inputPanel.removeAll();
            inputPanel.setLayout(new BorderLayout(5, 5));

            // Re-add control panel
            JPanel inputControlPanel = new JPanel(new FlowLayout());
            inputControlPanel.add(new JLabel("Rows:"));
            inputControlPanel.add(rowsField);
            inputControlPanel.add(new JLabel("Cols:"));
            inputControlPanel.add(colsField);
            JButton initGridButton = new JButton("Initialize Grid");
            inputControlPanel.add(initGridButton);
            initGridButton.addActionListener(e -> initializeInputGrid());
            JButton clearGridButton = new JButton("Clear Grid");
            inputControlPanel.add(clearGridButton);
            clearGridButton.addActionListener(e -> clearInputGrid());
            inputControlPanel.add(new JLabel("Exit Edge:"));
            edgeComboBox = new JComboBox<>(new String[]{"Top", "Bottom", "Left", "Right"});
            inputControlPanel.add(edgeComboBox);
            inputControlPanel.add(new JLabel("Position:"));
            exitPositionComboBox = new JComboBox<>();
            inputControlPanel.add(exitPositionComboBox);
            edgeComboBox.addActionListener(e -> updateExitPositionComboBox());
            JButton submitButton = new JButton("Submit Configuration");
            inputControlPanel.add(submitButton);
            submitButton.addActionListener(e -> submitConfiguration());
            inputPanel.add(inputControlPanel, BorderLayout.NORTH);

            // Create input grid
            JPanel gridPanel = new JPanel(new GridLayout(rows, cols));
            inputGrid = new JTextField[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    inputGrid[i][j] = new JTextField(".", 1);
                    inputGrid[i][j].setHorizontalAlignment(JTextField.CENTER);
                    inputGrid[i][j].setFont(new Font("Monospaced", Font.PLAIN, 16));
                    gridPanel.add(inputGrid[i][j]);
                }
            }
            inputPanel.add(gridPanel, BorderLayout.CENTER);

            // Re-add instruction label
            JLabel instructionLabel = new JLabel("Enter pieces (e.g., PP or PPP for primary piece, AA for others, . for empty)");
            instructionLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            inputPanel.add(instructionLabel, BorderLayout.SOUTH);

            // Update exit position options
            updateExitPositionComboBox();

            submitButton.setEnabled(true);
            inputPanel.revalidate();
            inputPanel.repaint();
            outputArea.setText("Input grid initialized (" + rows + "x" + cols + "). Enter pieces (e.g., PP or PPP for primary piece, AA for others, . for empty).\n");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearInputGrid() {
        if (inputGrid != null) {
            for (JTextField[] row : inputGrid) {
                for (JTextField field : row) {
                    field.setText(".");
                }
            }
            outputArea.setText("Input grid cleared. Enter new configuration.\n");
        }
    }

    private void updateExitPositionComboBox() {
        String edge = (String) edgeComboBox.getSelectedItem();
        exitPositionComboBox.removeAllItems();
        try {
            int rows = Integer.parseInt(rowsField.getText().trim());
            int cols = Integer.parseInt(colsField.getText().trim());
            if (edge.equals("Top") || edge.equals("Bottom")) {
                for (int j = 0; j < cols; j++) {
                    exitPositionComboBox.addItem(j);
                }
            } else { // Left or Right
                for (int i = 0; i < rows; i++) {
                    exitPositionComboBox.addItem(i);
                }
            }
        } catch (NumberFormatException e) {
            // Ignore if dimensions are not set
        }
    }

    private void submitConfiguration() {
        try {
            int rows = Integer.parseInt(rowsField.getText().trim());
            int cols = Integer.parseInt(colsField.getText().trim());

            // Read grid configuration
            String[] config = new String[rows];
            for (int i = 0; i < rows; i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < cols; j++) {
                    String text = inputGrid[i][j].getText().trim();
                    if (text.isEmpty()) text = ".";
                    if (text.length() > 1 || (!text.equals(".") && !Character.isLetter(text.charAt(0)))) {
                        throw new IllegalArgumentException("Invalid character at (" + i + "," + j + "). Use letters (A-Z, P) or .");
                    }
                    row.append(text);
                }
                config[i] = row.toString();
            }

            // Initialize board
            board = new Board(config);

            // Validate primary piece
            if (board.primaryPiece == null) {
                throw new IllegalArgumentException("Primary piece (P) not found. Ensure it is 2 or 3 cells long (e.g., PP or PPP for primary piece, AA for others, . for empty).");
            }
            if (board.primaryPiece.length < 2 || board.primaryPiece.length > 3) {
                throw new IllegalArgumentException("Primary piece (P) has invalid length: " + board.primaryPiece.length + ". Use 2 or 3 cells (e.g., PP or PPP).");
            }

            // Validate all pieces
            for (Piece piece : board.pieces) {
                if (piece.length < 2 || piece.length > 3) {
                    throw new IllegalArgumentException("Piece " + piece.id + " has invalid length: " + piece.length + ". Pieces must be 2 or 3 cells long.");
                }
            }

            // Count non-primary pieces
            int n = board.pieces.size() - 1; // Subtract primary piece

            // Get exit position
            String edge = (String) edgeComboBox.getSelectedItem();
            Integer position = (Integer) exitPositionComboBox.getSelectedItem();
            if (edge == null || position == null) {
                throw new IllegalArgumentException("Please select an exit edge and position.");
            }

            int exitRow = -1, exitCol = -1;
            if (edge.equals("Top")) {
                exitRow = -1;
                exitCol = position;
            } else if (edge.equals("Bottom")) {
                exitRow = rows;
                exitCol = position;
            } else if (edge.equals("Left")) {
                exitRow = position;
                exitCol = -1;
            } else if (edge.equals("Right")) {
                exitRow = position;
                exitCol = cols;
            }

            // Validate exit alignment with primary piece
            boolean isHorizontal = board.primaryPiece.isHorizontal;
            boolean validExit = false;
            if (isHorizontal && (edge.equals("Left") || edge.equals("Right"))) {
                validExit = true;
            } else if (!isHorizontal && (edge.equals("Top") || edge.equals("Bottom"))) {
                validExit = true;
            }
            if (!validExit) {
                throw new IllegalArgumentException("Exit edge (" + edge + ") must align with primary piece orientation (" + (isHorizontal ? "horizontal" : "vertical") + "). Use " + (isHorizontal ? "Left/Right" : "Top/Bottom") + " for " + (isHorizontal ? "horizontal" : "vertical") + " primary piece.");
            }

            // Validate exit position bounds
            if ((edge.equals("Top") || edge.equals("Bottom")) && (exitCol < 0 || exitCol >= cols)) {
                throw new IllegalArgumentException("Exit position " + exitCol + " is out of bounds for " + edge + " edge.");
            }
            if ((edge.equals("Left") || edge.equals("Right")) && (exitRow < 0 || exitRow >= rows)) {
                throw new IllegalArgumentException("Exit position " + exitRow + " is out of bounds for " + edge + " edge.");
            }

            board.exitRow = exitRow;
            board.exitCol = exitCol;

            // Display board
            displayBoard(board, null);
            outputArea.append("Configuration loaded successfully.\n");
            outputArea.append("Board: " + rows + "x" + cols + ", Non-primary pieces: " + n + "\n");
            outputArea.append("Primary piece: " + board.primaryPiece + " (" + (isHorizontal ? "horizontal" : "vertical") + ", length=" + board.primaryPiece.length + ")\n");
            outputArea.append("Exit at: (" + exitRow + "," + exitCol + ") on " + edge + " edge\n");
            prevStepButton.setEnabled(false);
            nextStepButton.setEnabled(false);
            playButton.setEnabled(false);
            solutionPath = null;
            currentStep = 0;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            board = null;
        }
    }

    private void loadBoardFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String[] dim = reader.readLine().trim().split("\\s+");
            if (dim.length < 2) {
                throw new IllegalArgumentException("Invalid dimension format.");
            }
            int rows = Integer.parseInt(dim[0]);
            int cols = Integer.parseInt(dim[1]);

            int n = Integer.parseInt(reader.readLine().trim());

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }

            if (lines.size() < rows) {
                throw new IllegalArgumentException("File has fewer than " + rows + " configuration lines.");
            }

            String[] config = new String[rows];
            for (int i = 0; i < rows; i++) {
                String l = lines.get(i);
                if (l.length() < cols) {
                    l = String.format("%-" + cols + "s", l).replace(' ', '.');
                } else if (l.length() > cols) {
                    l = l.substring(0, cols);
                }
                config[i] = l;
            }

            int exitRow = -1, exitCol = -1;
            for (int i = 0; i < lines.size(); i++) {
                int kIndex = lines.get(i).indexOf('K');
                if (kIndex >= 0) {
                    exitRow = i;
                    exitCol = kIndex;
                    break;
                }
            }

            board = new Board(config);
            if (board.primaryPiece == null) {
                throw new IllegalArgumentException("Primary piece (P) not found in file. Ensure it is 2 or 3 cells long (e.g., PP or PPP).");
            }
            if (board.primaryPiece.length < 2 || board.primaryPiece.length > 3) {
                throw new IllegalArgumentException("Primary piece (P) has invalid length: " + board.primaryPiece.length + ". Use 2 or 3 cells.");
            }

            board.exitRow = exitRow;
            board.exitCol = exitCol;

            if (board.exitRow == -1 && board.exitCol == -1) {
                throw new IllegalArgumentException("Exit (K) not found.");
            }

            displayBoard(board, null);
            outputArea.setText("Board loaded from file successfully.\nPrimary piece: " + board.primaryPiece + "\nExit at: (" + board.exitRow + "," + board.exitCol + ")\n");
            prevStepButton.setEnabled(false);
            nextStepButton.setEnabled(false);
            playButton.setEnabled(false);
            solutionPath = null;
            currentStep = 0;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            board = null;
            boardPanel.removeAll();
            boardPanel.revalidate();
            boardPanel.repaint();
            outputArea.setText("");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            board = null;
            boardPanel.removeAll();
            boardPanel.revalidate();
            boardPanel.repaint();
            outputArea.setText("");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            board = null;
            boardPanel.removeAll();
            boardPanel.revalidate();
            boardPanel.repaint();
            outputArea.setText("");
        }
    }

    private void displayBoard(Board board, Move highlightMove) {
        boardPanel.removeAll();
        boardPanel.setLayout(new GridLayout(board.rows, board.cols));
        boardLabels = new JLabel[board.rows][board.cols];

        for (int i = 0; i < board.rows; i++) {
            for (int j = 0; j < board.cols; j++) {
                char ch = board.grid[i][j];
                JLabel label = new JLabel(String.valueOf(ch), SwingConstants.CENTER);
                label.setOpaque(true);
                label.setFont(new Font("Monospaced", Font.BOLD, 16));
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                if (ch == 'K') {
                    label.setBackground(EXIT_COLOR);
                } else if (ch == 'P') {
                    label.setBackground(PRIMARY_COLOR);
                    label.setForeground(Color.WHITE);
                } else if (highlightMove != null && ch == highlightMove.pieceId) {
                    label.setBackground(MOVE_COLOR);
                } else if (ch == '.') {
                    label.setBackground(EMPTY_COLOR);
                } else {
                    label.setBackground(PIECE_COLOR);
                }

                boardLabels[i][j] = label;
                boardPanel.add(label);
            }
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void runSolver() {
        if (board == null) {
            JOptionPane.showMessageDialog(this, "Please load or create a board first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedAlgo = (String) algoComboBox.getSelectedItem();
        String selectedHeuristic = (String) heuristicComboBox.getSelectedItem();
        String heuristicChoice = switch (selectedHeuristic) {
            case "Manhattan" -> "1";
            case "Euclidean" -> "2";
            case "Chebyshev" -> "3";
            default -> "1";
        };

        solver = switch (selectedAlgo) {
            case "UCS" -> new UCS(board);
            case "GBFS" -> new GBFS(board, heuristicChoice);
            case "A*" -> new AStar(board, heuristicChoice);
            case "IDA*" -> new IDAStar(board, heuristicChoice);
            default -> new AStar(board, heuristicChoice);
        };

        // Clear previous solution
        solutionPath = null;
        currentStep = 0;
        outputArea.setText("");

        // Run solver
        long startTime = System.currentTimeMillis();
        solver.solve();
        long endTime = System.currentTimeMillis();

        solutionPath = solver.getSolutionPath();
        if (solutionPath != null && !solutionPath.isEmpty()) {
            // Enable animation controls
            prevStepButton.setEnabled(false);
            nextStepButton.setEnabled(true);
            playButton.setEnabled(true);

            // Display initial board
            displayBoard(board, null);

            // Show solution info
            outputArea.append("Solution found!\n");
            outputArea.append("Algorithm: " + selectedAlgo + "\n");
            if (!selectedAlgo.equals("UCS")) {
                outputArea.append("Heuristic: " + selectedHeuristic + "\n");
            }
            outputArea.append("Nodes visited: " + solver.getVisitedCount() + "\n");
            outputArea.append("Solution length: " + solutionPath.size() + "\n");
            outputArea.append("Time: " + (endTime - startTime) + " ms\n");
        } else {
            outputArea.append("No solution found.\n");
            prevStepButton.setEnabled(false);
            nextStepButton.setEnabled(false);
            playButton.setEnabled(false);
        }
    }

    private void showPreviousStep() {
        if (currentStep > 0) {
            currentStep--;
            Board currentBoard = board;
            for (int i = 0; i < currentStep; i++) {
                currentBoard = currentBoard.applyMove(solutionPath.get(i));
            }
            Move highlight = currentStep > 0 ? solutionPath.get(currentStep - 1) : null;
            displayBoard(currentBoard, highlight);
            outputArea.append("Step " + currentStep + ": " + (highlight != null ? highlight : "Initial board") + "\n");
            nextStepButton.setEnabled(true);
            prevStepButton.setEnabled(currentStep > 0);
            if (animationTimer.isRunning()) {
                animationTimer.stop();
                playButton.setText("Play");
            }
        }
    }

    private void showNextStep() {
        if (currentStep < solutionPath.size()) {
            Board currentBoard = board;
            for (int i = 0; i < currentStep; i++) {
                currentBoard = currentBoard.applyMove(solutionPath.get(i));
            }
            Move highlight = currentStep < solutionPath.size() ? solutionPath.get(currentStep) : null;
            displayBoard(currentBoard, highlight);
            outputArea.append("Step " + (currentStep + 1) + ": " + (highlight != null ? highlight : "Final board") + "\n");
            currentStep++;
            prevStepButton.setEnabled(true);
            nextStepButton.setEnabled(currentStep < solutionPath.size());
            if (currentStep >= solutionPath.size() && animationTimer.isRunning()) {
                animationTimer.stop();
                playButton.setText("Play");
            }
        }
    }

    private void toggleAnimation() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
            playButton.setText("Play");
        } else {
            if (currentStep >= solutionPath.size()) {
                currentStep = 0;
                displayBoard(board, null);
                outputArea.append("Restarting animation...\n");
            }
            animationTimer.start();
            playButton.setText("Stop");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}