import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.TransferHandler;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.nio.file.Files;

public class GUI extends JFrame {
    private Board board;
    private BoardPanel boardPanel;
    private JPanel piecePanel;
    private JPanel controlPanel;
    private JComboBox<String> algorithmCombo;
    private JButton solveButton;
    private JButton nextMoveButton;
    private JButton prevMoveButton;
    private JLabel statusLabel;
    private List<Move> solutionPath;
    private int currentMoveIndex = -1;
    private Timer animationTimer;
    private static final int ANIMATION_DELAY = 500;
    private int boardRows = 6;
    private int boardCols = 6;
    private JSpinner rowsSpinner;
    private JSpinner colsSpinner;
    private JButton resizeButton;
    private JSpinner exitRowSpinner;
    private JSpinner exitColSpinner;
    private JButton setExitButton;
    private JComboBox<String> exitPositionCombo;
    private JSpinner exitIndexSpinner;
    private boolean isSettingExit = false;
    private char currentVehicleId = 'A';
    private boolean isPrimaryVehicle = true;

    public GUI() {
        setTitle("Rush Hour Puzzle Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create components
        createComponents();
        
        // Layout components
        layoutComponents();
        
        // Add event listeners
        addEventListeners();

        // Create initial empty board
        createEmptyBoard();

        // Add menu bar
        createMenuBar();

        pack();
        setLocationRelativeTo(null);
    }

    private void createComponents() {
        // Board panel
        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(400, 400));
        
        // Piece panel (left side)
        createPiecePanel();
        
        // Control panel (right side)
        createControlPanel();

        // Status label
        statusLabel = new JLabel("Drag and drop pieces to create your puzzle");
    }

    private void createPiecePanel() {
        piecePanel = new JPanel();
        piecePanel.setLayout(new BoxLayout(piecePanel, BoxLayout.Y_AXIS));
        piecePanel.setBorder(BorderFactory.createTitledBorder("Available Pieces"));

        // Create draggable piece buttons
        JButton piece1x2 = createDraggablePieceButton(2, true);
        JButton piece1x3 = createDraggablePieceButton(3, true);
        JButton piece2x1 = createDraggablePieceButton(2, false);
        JButton piece3x1 = createDraggablePieceButton(3, false);

        piecePanel.add(Box.createVerticalStrut(10));
        piecePanel.add(piece1x2);
        piecePanel.add(Box.createVerticalStrut(5));
        piecePanel.add(piece1x3);
        piecePanel.add(Box.createVerticalStrut(5));
        piecePanel.add(piece2x1);
        piecePanel.add(Box.createVerticalStrut(5));
        piecePanel.add(piece3x1);
        piecePanel.add(Box.createVerticalGlue());
    }

    private JButton createDraggablePieceButton(int length, boolean isHorizontal) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(120, 40));
        button.setMaximumSize(new Dimension(120, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create visual representation of the piece
        button.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = isHorizontal ? length * 20 : 20;
                int height = isHorizontal ? 20 : length * 20;
                
                g2d.setColor(Color.BLUE);
                g2d.fillRect(x + 10, y + 10, width, height);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x + 10, y + 10, width, height);
            }

            @Override
            public int getIconWidth() {
                return 100;
            }

            @Override
            public int getIconHeight() {
                return 40;
            }
        });

        // Enable drag
        button.setTransferHandler(new TransferHandler() {
            @Override
            public int getSourceActions(JComponent c) {
                return COPY_OR_MOVE;
            }

            @Override
            protected Transferable createTransferable(JComponent c) {
                return new StringSelection(length + "," + isHorizontal + ",false");
            }
        });

        // Add mouse listener for drag start
        button.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, TransferHandler.COPY);
            }
        });

        return button;
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        // Board size controls
        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sizePanel.add(new JLabel("Rows:"));
        rowsSpinner = new JSpinner(new SpinnerNumberModel(6, 3, 10, 1));
        sizePanel.add(rowsSpinner);
        sizePanel.add(new JLabel("Cols:"));
        colsSpinner = new JSpinner(new SpinnerNumberModel(6, 3, 10, 1));
        sizePanel.add(colsSpinner);
        resizeButton = new JButton("Resize Board");
        sizePanel.add(resizeButton);

        // Exit point controls
        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        exitPanel.add(new JLabel("Exit Position:"));
        String[] positions = {"Left", "Right", "Top", "Bottom"};
        exitPositionCombo = new JComboBox<>(positions);
        exitPanel.add(exitPositionCombo);
        exitPanel.add(new JLabel("Row/Col:"));
        exitIndexSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 5, 1));
        exitPanel.add(exitIndexSpinner);
        setExitButton = new JButton("Set Exit");
        exitPanel.add(setExitButton);

        // Algorithm selection
        JPanel algorithmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        algorithmPanel.add(new JLabel("Algorithm:"));
        String[] algorithms = {"UCS", "GBFS", "A*", "IDA*"};
        algorithmCombo = new JComboBox<>(algorithms);
        algorithmPanel.add(algorithmCombo);

        // Heuristic selection
        JPanel heuristicPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        heuristicPanel.add(new JLabel("Heuristic:"));
        String[] heuristics = {"Manhattan Distance", "Euclidean Distance", "Chebyshev Distance"};
        JComboBox<String> heuristicCombo = new JComboBox<>(heuristics);
        heuristicPanel.add(heuristicCombo);

        // Solve button
        solveButton = new JButton("Solve Puzzle");
        solveButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Navigation buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        prevMoveButton = new JButton("Previous");
        nextMoveButton = new JButton("Next");
        navPanel.add(prevMoveButton);
        navPanel.add(nextMoveButton);
        prevMoveButton.setEnabled(false);
        nextMoveButton.setEnabled(false);

        // Add components to control panel
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(sizePanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(exitPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(algorithmPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(heuristicPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(navPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(solveButton);
        controlPanel.add(Box.createVerticalGlue());

        // Add event listener for solve button
        solveButton.addActionListener(e -> {
            if (board == null || board.getPieces().length == 0) {
                JOptionPane.showMessageDialog(this,
                    "Please add some pieces to the board first!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (board.getPrimaryVehicleId() == 'A') {
                JOptionPane.showMessageDialog(this,
                    "Please set a primary vehicle first!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (board.exitRow == -1 || board.exitCol == -1) {
                JOptionPane.showMessageDialog(this,
                    "Please set an exit point first!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedAlgorithm = (String) algorithmCombo.getSelectedItem();
            String selectedHeuristic = (String) heuristicCombo.getSelectedItem();
            String heuristicMode = switch (selectedHeuristic) {
                case "Manhattan Distance" -> "1";
                case "Euclidean Distance" -> "2";
                case "Chebyshev Distance" -> "3";
                default -> "1";
            };

            Solver solver = switch (selectedAlgorithm) {
                case "UCS" -> new UCS(board, heuristicMode);
                case "GBFS" -> new GBFS(board, heuristicMode);
                case "A*" -> new AStar(board, heuristicMode);
                case "IDA*" -> new IDAStar(board, heuristicMode);
                default -> new UCS(board, heuristicMode);
            };

            try {
                long startTime = System.currentTimeMillis();
                solver.solve();
                long endTime = System.currentTimeMillis();

                if (solver.getSolutionPath() != null) {
                    solutionPath = solver.getSolutionPath();
                    currentMoveIndex = -1;
                    nextMoveButton.setEnabled(true);
                    prevMoveButton.setEnabled(false);

                    StringBuilder message = new StringBuilder();
                    message.append("Solution found!\n\n");
                    message.append("Algorithm: ").append(selectedAlgorithm).append("\n");
                    message.append("Heuristic: ").append(selectedHeuristic).append("\n");
                    message.append("Number of moves: ").append(solutionPath.size()).append("\n");
                    message.append("Time taken: ").append(endTime - startTime).append(" ms\n\n");
                    message.append("Moves:\n");
                    for (Move move : solutionPath) {
                        message.append(move).append("\n");
                    }
                    JOptionPane.showMessageDialog(this, message.toString(), "Solution", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No solution found!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error solving puzzle: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void layoutComponents() {
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(piecePanel, BorderLayout.WEST);
        contentPanel.add(boardPanel, BorderLayout.CENTER);
        contentPanel.add(controlPanel, BorderLayout.EAST);
        
        // Add components to frame
        add(contentPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void addEventListeners() {
        resizeButton.addActionListener(e -> {
            int rows = (int) rowsSpinner.getValue();
            int cols = (int) colsSpinner.getValue();
            board = new Board(rows, cols);
            boardPanel.setBoard(board);
            boardPanel.repaint();
        });

        setExitButton.addActionListener(e -> {
            String position = (String) exitPositionCombo.getSelectedItem();
            int index = (int) exitIndexSpinner.getValue();
            int row, col;

            switch (position) {
                case "Left":
                    row = index;
                    col = -1;
                    break;
                case "Right":
                    row = index;
                    col = board.getCols();
                    break;
                case "Top":
                    row = -1;
                    col = index;
                    break;
                case "Bottom":
                    row = board.getRows();
                    col = index;
                    break;
                default:
                    return;
            }

            try {
                board.setExit(row, col);
                boardPanel.repaint();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error setting exit: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        nextMoveButton.addActionListener(e -> {
            if (solutionPath == null || currentMoveIndex >= solutionPath.size() - 1) return;

            currentMoveIndex++;
            Move move = solutionPath.get(currentMoveIndex);
            board = board.applyMove(move);
            boardPanel.setBoard(board);
            boardPanel.setHighlightedMove(move);
            
            prevMoveButton.setEnabled(true);
            nextMoveButton.setEnabled(currentMoveIndex < solutionPath.size() - 1);
            
            statusLabel.setText(String.format("Move %d/%d: %s", 
                currentMoveIndex + 1, solutionPath.size(), move));
            
            repaint();
        });

        prevMoveButton.addActionListener(e -> {
            if (solutionPath == null || currentMoveIndex < 0) return;

            try {
                // Reset board and replay moves up to previous position
                createEmptyBoard();
                for (int i = 0; i < currentMoveIndex; i++) {
                    board = board.applyMove(solutionPath.get(i));
                }
                
                currentMoveIndex--;
                boardPanel.setBoard(board);
                boardPanel.setHighlightedMove(currentMoveIndex >= 0 ? solutionPath.get(currentMoveIndex) : null);
                
                prevMoveButton.setEnabled(currentMoveIndex >= 0);
                nextMoveButton.setEnabled(true);
                
                statusLabel.setText(String.format("Move %d/%d: %s", 
                    currentMoveIndex + 1, solutionPath.size(), 
                    currentMoveIndex >= 0 ? solutionPath.get(currentMoveIndex) : "Initial position"));
                
                repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error reloading puzzle: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void createEmptyBoard() {
        board = new Board(boardRows, boardCols);
        boardPanel.setBoard(board);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem loadItem = new JMenuItem("Load from File");
        loadItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    File selectedFile = fileChooser.getSelectedFile();
                    List<String> lines = Files.readAllLines(selectedFile.toPath());
                    
                    // Read dimensions
                    String[] dim = lines.get(0).trim().split("\\s+");
                    int rows = Integer.parseInt(dim[0]);
                    int cols = Integer.parseInt(dim[1]);
                    
                    // Skip number of vehicles line
                    
                    // Get board configuration
                    String[] config = new String[rows];
                    for (int i = 0; i < rows; i++) {
                        String line = lines.get(i + 2);
                        if (line.length() < cols) {
                            line = String.format("%-" + cols + "s", line).replace(' ', '.');
                        } else if (line.length() > cols) {
                            line = line.substring(0, cols);
                        }
                        config[i] = line;
                    }
                    
                    // Find exit point (K)
                    int exitRow = -1, exitCol = -1;
                    outer:
                    for (int i = 2; i < lines.size(); i++) {
                        int kIndex = lines.get(i).indexOf('K');
                        if (kIndex >= 0) {
                            exitRow = i - 2; // Adjust for the two header lines
                            exitCol = kIndex;
                            break outer;
                        }
                    }
                    
                    // Create new board from file
                    board = new Board(config);
                    board.exitRow = exitRow;
                    board.exitCol = exitCol;
                    
                    boardPanel.setBoard(board);
                    boardPanel.repaint();
                    
                    // Update board size spinners
                    rowsSpinner.setValue(rows);
                    colsSpinner.setValue(cols);
                    
                    statusLabel.setText("Board loaded from file: " + selectedFile.getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error loading file: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error parsing file: Invalid number format",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        fileMenu.add(loadItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}

class PieceTransferable implements Transferable {
    private final int length;
    private final boolean isHorizontal;
    private final boolean isPrimary;
    private static final DataFlavor[] FLAVORS = {DataFlavor.stringFlavor};

    public PieceTransferable(int length, boolean isHorizontal, boolean isPrimary) {
        this.length = length;
        this.isHorizontal = isHorizontal;
        this.isPrimary = isPrimary;
    }

    public int getLength() { return length; }
    public boolean isHorizontal() { return isHorizontal; }
    public boolean isPrimary() { return isPrimary; }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return FLAVORS;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.stringFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return length + "," + isHorizontal + "," + isPrimary;
    }
} 