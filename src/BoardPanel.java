import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardPanel extends JPanel {
    private Board board;
    private Move highlightedMove;
    private String currentFile;
    public static final int CELL_SIZE = 60;
    private static final int PADDING = 20;
    private static final Color[] VEHICLE_COLORS = {
        new Color(255, 0, 0),    // Red
        new Color(0, 255, 0),    // Green
        new Color(0, 0, 255),    // Blue
        new Color(255, 255, 0),  // Yellow
        new Color(255, 0, 255),  // Magenta
        new Color(0, 255, 255),  // Cyan
        new Color(128, 0, 0),    // Maroon
        new Color(0, 128, 0),    // Dark Green
        new Color(0, 0, 128),    // Navy
        new Color(128, 128, 0),  // Olive
        new Color(128, 0, 128),  // Purple
        new Color(0, 128, 128),  // Teal
        new Color(128, 128, 128) // Gray
    };

    private EditMode editMode = EditMode.NONE;
    private char currentVehicleId;
    private int currentVehicleLength;
    private int xOffset, yOffset;
    private boolean isSettingExit = false;

    public enum EditMode {
        NONE,
        ADD_VEHICLE,
        SET_EXIT,
        SET_PRIMARY
    }

    public BoardPanel() {
        setPreferredSize(new Dimension(400, 400));
        setBackground(Color.WHITE);
        
        // Enable drop support
        setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }
            
            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                
                try {
                    String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    String[] parts = data.split(",");
                    int length = Integer.parseInt(parts[0]);
                    boolean isHorizontal = Boolean.parseBoolean(parts[1]);
                    boolean isPrimary = Boolean.parseBoolean(parts[2]);
                    
                    Point dropPoint = support.getDropLocation().getDropPoint();
                    int col = (dropPoint.x - xOffset) / CELL_SIZE;
                    int row = (dropPoint.y - yOffset) / CELL_SIZE;
                    
                    if (row >= 0 && row < board.getRows() && col >= 0 && col < board.getCols()) {
                        addVehicle(row, col, length, isHorizontal, isPrimary);
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        // Add mouse listener for piece interaction
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (board == null) return;

                int col = (e.getX() - xOffset) / CELL_SIZE;
                int row = (e.getY() - yOffset) / CELL_SIZE;

                if (row >= 0 && row < board.getRows() && col >= 0 && col < board.getCols()) {
                    char pieceId = board.grid[row][col];
                    if (pieceId != '.' && pieceId != 'K') {
                        // Show context menu
                        JPopupMenu menu = new JPopupMenu();
                        
                        JMenuItem deleteItem = new JMenuItem("Delete Piece");
                        deleteItem.addActionListener(ev -> {
                            try {
                                board.removePiece(pieceId);
                                repaint();
                            } catch (IllegalArgumentException ex) {
                                JOptionPane.showMessageDialog(BoardPanel.this,
                                    "Error deleting piece: " + ex.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        menu.add(deleteItem);

                        if (pieceId != 'P') {
                            JMenuItem setPrimaryItem = new JMenuItem("Set as Primary");
                            setPrimaryItem.addActionListener(ev -> {
                                try {
                                    board.setPrimaryVehicle(pieceId);
                                    repaint();
                                } catch (IllegalArgumentException ex) {
                                    JOptionPane.showMessageDialog(BoardPanel.this,
                                        "Error setting primary piece: " + ex.getMessage(),
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                }
                            });
                            menu.add(setPrimaryItem);
                        }

                        menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private void addVehicle(int row, int col, int length, boolean isHorizontal, boolean isPrimary) {
        try {
            char vehicleId = (char)('A' + board.getPieces().length);
            board.addVehicle(vehicleId, isHorizontal, length, row, col);
            if (isPrimary) {
                board.setPrimaryVehicle(vehicleId);
            }
            repaint();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                "Cannot add vehicle: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setBoard(Board board) {
        this.board = board;
        repaint();
    }

    public void setHighlightedMove(Move move) {
        this.highlightedMove = move;
        repaint();
    }

    public void setCurrentFile(String filename) {
        this.currentFile = filename;
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public void setEditMode(EditMode mode) {
        this.editMode = mode;
        System.out.println("Edit mode set to: " + mode); // Debug print
        repaint();
    }

    public EditMode getEditMode() {
        return editMode;
    }

    public void setCurrentVehicleId(char id) {
        this.currentVehicleId = id;
        System.out.println("Current vehicle ID set to: " + id); // Debug print
    }

    public void setCurrentVehicleLength(int length) {
        this.currentVehicleLength = length;
        System.out.println("Current vehicle length set to: " + length); // Debug print
    }

    public void setSettingExit(boolean settingExit) {
        this.isSettingExit = settingExit;
        repaint();
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (board == null) {
            drawEmptyBoard(g2d);
            return;
        }

        // Calculate board dimensions
        int rows = board.getRows();
        int cols = board.getCols();
        int boardWidth = cols * CELL_SIZE;
        int boardHeight = rows * CELL_SIZE;

        // Center the board
        xOffset = (getWidth() - boardWidth) / 2;
        yOffset = (getHeight() - boardHeight) / 2;

        // Draw grid
        drawGrid(g2d, rows, cols, xOffset, yOffset);

        // Draw vehicles
        drawVehicles(g2d, xOffset, yOffset);

        // Draw exit
        drawExit(g2d, xOffset, yOffset);

        // Draw highlighted move
        if (highlightedMove != null) {
            drawHighlightedMove(g2d, xOffset, yOffset);
        }

        // Draw edit mode indicators
        if (editMode != EditMode.NONE) {
            drawEditModeIndicator(g2d);
        }

        // Draw drop target indicator
        if (isSettingExit) {
            drawDropTargetIndicator(g2d);
        }
    }

    private void drawEditModeIndicator(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 128));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        
        String message = switch (editMode) {
            case ADD_VEHICLE -> "Adding vehicle " + currentVehicleId + " (length: " + currentVehicleLength + ")";
            case SET_EXIT -> "Setting exit point";
            case SET_PRIMARY -> "Select primary vehicle";
            case NONE -> "";
        };
        
        if (!message.isEmpty()) {
            FontMetrics fm = g2d.getFontMetrics();
            Rectangle2D bounds = fm.getStringBounds(message, g2d);
            int x = (getWidth() - (int) bounds.getWidth()) / 2;
            int y = getHeight() - 10;
            g2d.drawString(message, x, y);
        }
    }

    private void drawDropTargetIndicator(Graphics2D g2d) {
        g2d.setColor(new Color(0, 255, 0, 64));
        g2d.fillRect(xOffset, yOffset, board.getCols() * CELL_SIZE, board.getRows() * CELL_SIZE);
    }

    private void drawEmptyBoard(Graphics2D g2d) {
        String message = "Drag and drop pieces to create your puzzle";
        FontMetrics fm = g2d.getFontMetrics();
        Rectangle2D bounds = fm.getStringBounds(message, g2d);
        
        int x = (getWidth() - (int) bounds.getWidth()) / 2;
        int y = (getHeight() - (int) bounds.getHeight()) / 2;
        
        g2d.setColor(Color.GRAY);
        g2d.drawString(message, x, y);
    }

    private void drawGrid(Graphics2D g2d, int rows, int cols, int xOffset, int yOffset) {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));

        // Draw vertical lines
        for (int i = 0; i <= cols; i++) {
            int x = xOffset + i * CELL_SIZE;
            g2d.drawLine(x, yOffset, x, yOffset + rows * CELL_SIZE);
        }

        // Draw horizontal lines
        for (int i = 0; i <= rows; i++) {
            int y = yOffset + i * CELL_SIZE;
            g2d.drawLine(xOffset, y, xOffset + cols * CELL_SIZE, y);
        }
    }

    private void drawVehicles(Graphics2D g2d, int xOffset, int yOffset) {
        for (Piece piece : board.getPieces()) {
            if (piece == null) continue;

            // Get vehicle color based on its ID
            Color color = piece.id == board.getPrimaryVehicleId() ? 
                Color.RED : VEHICLE_COLORS[piece.id % VEHICLE_COLORS.length];
            g2d.setColor(color);

            // Draw vehicle rectangle
            int x = xOffset + piece.col * CELL_SIZE;
            int y = yOffset + piece.row * CELL_SIZE;
            int width = piece.isHorizontal ? piece.length * CELL_SIZE : CELL_SIZE;
            int height = piece.isHorizontal ? CELL_SIZE : piece.length * CELL_SIZE;

            g2d.fillRect(x, y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, width, height);

            // Draw vehicle ID only for primary vehicle
            if (piece.id == board.getPrimaryVehicleId()) {
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                String id = "P"; // Always show 'P' for primary vehicle
                Rectangle2D bounds = fm.getStringBounds(id, g2d);
                int textX = x + (width - (int) bounds.getWidth()) / 2;
                int textY = y + (height + (int) bounds.getHeight()) / 2;
                g2d.drawString(id, textX, textY);
            }
        }
    }

    private void drawExit(Graphics2D g2d, int xOffset, int yOffset) {
        // Draw exit point outside the grid
        int x, y;
        if (board.exitCol == board.getCols()) {
            // Exit on right side
            x = xOffset + board.getCols() * CELL_SIZE;
            y = yOffset + board.exitRow * CELL_SIZE;
        } else if (board.exitCol == -1) {
            // Exit on left side
            x = xOffset - CELL_SIZE;
            y = yOffset + board.exitRow * CELL_SIZE;
        } else if (board.exitRow == board.getRows()) {
            // Exit on bottom
            x = xOffset + board.exitCol * CELL_SIZE;
            y = yOffset + board.getRows() * CELL_SIZE;
        } else {
            // Exit on top
            x = xOffset + board.exitCol * CELL_SIZE;
            y = yOffset - CELL_SIZE;
        }
        
        g2d.setColor(new Color(0, 255, 0, 128)); // Semi-transparent green
        g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
        
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, CELL_SIZE, CELL_SIZE);
        
        // Draw "EXIT" text
        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();
        String text = "EXIT";
        Rectangle2D bounds = fm.getStringBounds(text, g2d);
        int textX = x + (CELL_SIZE - (int) bounds.getWidth()) / 2;
        int textY = y + (CELL_SIZE + (int) bounds.getHeight()) / 2;
        g2d.drawString(text, textX, textY);
    }

    private void drawHighlightedMove(Graphics2D g2d, int xOffset, int yOffset) {
        Piece piece = board.getPieceById(highlightedMove.pieceId);
        if (piece == null) return;

        int x = xOffset + piece.col * CELL_SIZE;
        int y = yOffset + piece.row * CELL_SIZE;
        int width = piece.isHorizontal ? piece.length * CELL_SIZE : CELL_SIZE;
        int height = piece.isHorizontal ? CELL_SIZE : piece.length * CELL_SIZE;

        // Draw highlight
        g2d.setColor(new Color(255, 255, 0, 128)); // Semi-transparent yellow
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }
} 