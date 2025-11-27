package gui;

import base.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;

public class GUI extends JFrame {
    private final StateManager stateManager;
    private final ThreadManager threadManager;
    private final Map<Integer, BoardPanel> threadBoardPanels;
    private final JPanel mainBoardsPanel;
    private final JPanel controlPanel;
    private JPanel solutionsPanel;
    private final JTextArea statsArea;
    private javax.swing.Timer updateTimer;
    
    private int boardSize = 8;
    private int numThreads = 4;
    private boolean isRunning = false;
    
    // Color schemes
    private static final Color BG_COLOR = new Color(20, 25, 35);
    private static final Color PANEL_BG = new Color(30, 35, 50);
    private static final Color ACCENT_BLUE = new Color(100, 150, 255);
    private static final Color ACCENT_GREEN = new Color(80, 200, 120);
    private static final Color ACCENT_PURPLE = new Color(180, 100, 255);
    private static final Color TEXT_COLOR = new Color(230, 230, 240);
    
    public GUI() {
        this.stateManager = new StateManager();
        this.threadManager = new ThreadManager();
        this.threadBoardPanels = new HashMap<>();
        
        setTitle("🔷 N-Queens Solver - Multi-Threading Visualization");
        setSize(1600, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BG_COLOR);
        
        // Main layout
        setLayout(new BorderLayout(10, 10));
        
        // Control Panel at top
        controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Center: Boards display
        mainBoardsPanel = new JPanel();
        mainBoardsPanel.setBackground(BG_COLOR);
        mainBoardsPanel.setLayout(new GridLayout(0, 2, 15, 15));
        mainBoardsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane boardsScroll = new JScrollPane(mainBoardsPanel);
        boardsScroll.setBackground(BG_COLOR);
        boardsScroll.getViewport().setBackground(BG_COLOR);
        boardsScroll.setBorder(null);
        add(boardsScroll, BorderLayout.CENTER);
        
        // Right panel: Solutions and stats
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.EAST);
        
        // Stats area
        statsArea = new JTextArea(3, 20);
        statsArea.setEditable(false);
        statsArea.setBackground(PANEL_BG);
        statsArea.setForeground(TEXT_COLOR);
        statsArea.setFont(new Font("Consolas", Font.BOLD, 12));
        statsArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(statsArea, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
        updateStats();
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(ACCENT_BLUE, 2)
        ));
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        // Board size
        JLabel sizeLabel = createStyledLabel("Board Size:");
        SpinnerNumberModel sizeModel = new SpinnerNumberModel(8, 4, 16, 1);
        JSpinner sizeSpinner = createStyledSpinner(sizeModel);
        sizeSpinner.addChangeListener(e -> boardSize = (int) sizeSpinner.getValue());
        
        // Thread count
        JLabel threadsLabel = createStyledLabel("Threads:");
        SpinnerNumberModel threadsModel = new SpinnerNumberModel(4, 1, 16, 1);
        JSpinner threadsSpinner = createStyledSpinner(threadsModel);
        threadsSpinner.addChangeListener(e -> numThreads = (int) threadsSpinner.getValue());
        
        // Buttons
        JButton startBtn = createStyledButton("▶ START", ACCENT_GREEN);
        startBtn.addActionListener(e -> startSolving());
        
        JButton stopBtn = createStyledButton("⏹ STOP", new Color(255, 100, 100));
        stopBtn.addActionListener(e -> stopSolving());
        
        JButton clearBtn = createStyledButton("🗑 CLEAR", ACCENT_PURPLE);
        clearBtn.addActionListener(e -> clearAll());
        
        panel.add(sizeLabel);
        panel.add(sizeSpinner);
        panel.add(threadsLabel);
        panel.add(threadsSpinner);
        panel.add(startBtn);
        panel.add(stopBtn);
        panel.add(clearBtn);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(10, 5, 10, 10));
        
        JLabel titleLabel = createStyledLabel("📊 SOLUTIONS FOUND");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        solutionsPanel = new JPanel();
        solutionsPanel.setLayout(new BoxLayout(solutionsPanel, BoxLayout.Y_AXIS));
        solutionsPanel.setBackground(BG_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(solutionsPanel);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }
    
    private JSpinner createStyledSpinner(SpinnerNumberModel model) {
        JSpinner spinner = new JSpinner(model);
        spinner.setPreferredSize(new Dimension(80, 35));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setBackground(PANEL_BG);
            ((JSpinner.DefaultEditor) editor).getTextField().setForeground(TEXT_COLOR);
            ((JSpinner.DefaultEditor) editor).getTextField().setFont(new Font("Arial", Font.BOLD, 14));
        }
        return spinner;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(140, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void startSolving() {
        if (isRunning) return;
        
        isRunning = true;
        clearAll();
        
        // Create board panels for each thread
        mainBoardsPanel.removeAll();
        threadBoardPanels.clear();
        
        int threadsToUse = Math.min(numThreads, boardSize);
        for (int i = 0; i < threadsToUse; i++) {
            BoardPanel panel = new BoardPanel(i, boardSize);
            threadBoardPanels.put(i, panel);
            mainBoardsPanel.add(panel);
        }
        
        mainBoardsPanel.revalidate();
        mainBoardsPanel.repaint();
        
        // Start solving in background
        new Thread(() -> {
            threadManager.startSolving(boardSize, threadsToUse, stateManager);
        }).start();
        
        // Start UI update timer
        updateTimer = new javax.swing.Timer(50, e -> updateUI());
        updateTimer.start();
    }
    
    private void stopSolving() {
        if (!isRunning) return;
        
        isRunning = false;
        threadManager.stopAll();
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
    
    private void clearAll() {
        stopSolving();
        mainBoardsPanel.removeAll();
        solutionsPanel.removeAll();
        threadBoardPanels.clear();
        stateManager.current_states.clear();
        stateManager.solutions.clear();
        mainBoardsPanel.revalidate();
        mainBoardsPanel.repaint();
        solutionsPanel.revalidate();
        solutionsPanel.repaint();
        updateStats();
    }
    
    private void updateUI() {
        // Update each thread's board
        for (Map.Entry<Integer, BoardPanel> entry : threadBoardPanels.entrySet()) {
            int threadId = entry.getKey();
            BoardPanel panel = entry.getValue();
            
            StepBoard stepBoard = stateManager.current_states.get(threadId);
            if (stepBoard != null) {
                panel.updateBoard(stepBoard);
            }
        }
        
        // Update solutions display
        updateSolutionsDisplay();
        updateStats();
    }
    
    private void updateSolutionsDisplay() {
        solutionsPanel.removeAll();
        
        int totalSolutions = 0;
        for (Integer threadId : stateManager.solutions.keySet()) {
            ArrayList<Solution> sols = stateManager.solutions.get(threadId);
            if (sols != null) {
                totalSolutions += sols.size();
                
                // Show mini preview of first few solutions per thread
                int toShow = Math.min(2, sols.size());
                for (int i = 0; i < toShow; i++) {
                    Solution sol = sols.get(i);
                    SolutionMiniPanel miniPanel = new SolutionMiniPanel(sol, threadId);
                    solutionsPanel.add(miniPanel);
                    solutionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }
        
        solutionsPanel.revalidate();
        solutionsPanel.repaint();
    }
    
    private void updateStats() {
        int totalSolutions = 0;
        for (ArrayList<Solution> sols : stateManager.solutions.values()) {
            if (sols != null) totalSolutions += sols.size();
        }
        
        statsArea.setText(String.format(
            "  🎯 Total Solutions: %d  |  📊 Threads: %d  |  🔷 Board: %dx%d  |  %s",
            totalSolutions,
            threadBoardPanels.size(),
            boardSize, boardSize,
            isRunning ? "⚡ RUNNING..." : "⏸ STOPPED"
        ));
    }
    
    // Inner class for individual thread board visualization
    class BoardPanel extends JPanel {
        private int threadId;
        private int size;
        private int[][] currentState;
        private base.Action lastAction;
        private long lastUpdateTime;
        private Map<String, javax.swing.Timer> cellAnimations;
        
        public BoardPanel(int threadId, int size) {
            this.threadId = threadId;
            this.size = size;
            this.currentState = new int[size][size];
            this.cellAnimations = new HashMap<>();
            this.lastUpdateTime = System.currentTimeMillis();
            
            setBackground(PANEL_BG);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getThreadColor(threadId), 3),
                new EmptyBorder(10, 10, 10, 10)
            ));
            setPreferredSize(new Dimension(400, 400));
        }
        
        public void updateBoard(StepBoard stepBoard) {
            if (stepBoard != null && stepBoard.board != null) {
                int[][] newState = stepBoard.board.getState();
                
                // Detect changes and animate
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        if (currentState[i][j] != newState[i][j]) {
                            triggerCellAnimation(i, j, newState[i][j] == 1);
                        }
                    }
                }
                
                currentState = newState;
                lastAction = stepBoard.action;
                lastUpdateTime = System.currentTimeMillis();
                repaint();
            }
        }
        
        private void triggerCellAnimation(int row, int col, boolean isPlace) {
            String key = row + "," + col;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth() - 20;
            int height = getHeight() - 60;
            int cellSize = Math.min(width, height) / size;
            int offsetX = (width - cellSize * size) / 2 + 10;
            int offsetY = (height - cellSize * size) / 2 + 40;
            
            // Draw title
            g2d.setColor(getThreadColor(threadId));
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String title = "Thread #" + threadId;
            FontMetrics fm = g2d.getFontMetrics();
            int titleX = (getWidth() - fm.stringWidth(title)) / 2;
            g2d.drawString(title, titleX, 25);
            
            // Draw board
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    int x = offsetX + col * cellSize;
                    int y = offsetY + row * cellSize;
                    
                    // Checkerboard pattern
                    if ((row + col) % 2 == 0) {
                        g2d.setColor(new Color(60, 70, 90));
                    } else {
                        g2d.setColor(new Color(40, 50, 70));
                    }
                    g2d.fillRect(x, y, cellSize, cellSize);
                    
                    // Draw queen if present
                    if (currentState[row][col] == 1) {
                        drawQueen(g2d, x, y, cellSize, getThreadColor(threadId));
                    }
                    
                    // Border
                    g2d.setColor(new Color(30, 35, 50));
                    g2d.drawRect(x, y, cellSize, cellSize);
                }
            }
        }
        
        private void drawQueen(Graphics2D g2d, int x, int y, int size, Color color) {
            int padding = size / 5;
            int queenSize = size - 2 * padding;
            
            // Glow effect
            for (int i = 0; i < 3; i++) {
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30 - i * 10));
                g2d.fillOval(x + padding - i * 2, y + padding - i * 2, queenSize + i * 4, queenSize + i * 4);
            }
            
            // Queen body
            g2d.setColor(color);
            g2d.fillOval(x + padding, y + padding, queenSize, queenSize);
            
            // Crown
            g2d.setColor(Color.YELLOW);
            int crownSize = queenSize / 3;
            g2d.fillOval(x + padding + queenSize / 3, y + padding + queenSize / 4, crownSize, crownSize);
        }
        
        private Color getThreadColor(int threadId) {
            Color[] colors = {
                new Color(100, 150, 255),  // Blue
                new Color(255, 100, 150),  // Pink
                new Color(100, 255, 150),  // Green
                new Color(255, 200, 100),  // Orange
                new Color(180, 100, 255),  // Purple
                new Color(100, 255, 255),  // Cyan
                new Color(255, 255, 100),  // Yellow
                new Color(255, 150, 100),  // Coral
            };
            return colors[threadId % colors.length];
        }
    }
    
    // Inner class for solution mini preview
    class SolutionMiniPanel extends JPanel {
        private Solution solution;
        private int threadId;
        
        public SolutionMiniPanel(Solution solution, int threadId) {
            this.solution = solution;
            this.threadId = threadId;
            
            setBackground(PANEL_BG);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getThreadColor(threadId), 2),
                new EmptyBorder(5, 5, 5, 5)
            ));
            setMaximumSize(new Dimension(300, 150));
            setPreferredSize(new Dimension(300, 150));
            
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    showSolutionDialog();
                }
                public void mouseEntered(MouseEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(getThreadColor(threadId).brighter(), 2),
                        new EmptyBorder(5, 5, 5, 5)
                    ));
                }
                public void mouseExited(MouseEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(getThreadColor(threadId), 2),
                        new EmptyBorder(5, 5, 5, 5)
                    ));
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int[][] state = solution.solved_board.getState();
            int size = solution.solved_board.getN();
            
            int width = getWidth() - 10;
            int height = getHeight() - 30;
            int cellSize = Math.min(width, height) / size;
            int offsetX = (width - cellSize * size) / 2 + 5;
            int offsetY = (height - cellSize * size) / 2 + 25;
            
            // Title
            g2d.setColor(getThreadColor(threadId));
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.drawString("Thread #" + threadId + " Solution (click to enlarge)", 5, 15);
            
            // Mini board
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    int x = offsetX + col * cellSize;
                    int y = offsetY + row * cellSize;
                    
                    if ((row + col) % 2 == 0) {
                        g2d.setColor(new Color(60, 70, 90));
                    } else {
                        g2d.setColor(new Color(40, 50, 70));
                    }
                    g2d.fillRect(x, y, cellSize, cellSize);
                    
                    if (state[row][col] == 1) {
                        g2d.setColor(getThreadColor(threadId));
                        int padding = cellSize / 4;
                        g2d.fillOval(x + padding, y + padding, cellSize - 2 * padding, cellSize - 2 * padding);
                    }
                }
            }
        }
        
        private void showSolutionDialog() {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Solution Preview", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(600, 650);
            dialog.setLocationRelativeTo(this);
            
            JPanel boardPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int[][] state = solution.solved_board.getState();
                    int size = solution.solved_board.getN();
                    
                    int width = getWidth() - 20;
                    int height = getHeight() - 20;
                    int cellSize = Math.min(width, height) / size;
                    int offsetX = (getWidth() - cellSize * size) / 2;
                    int offsetY = (getHeight() - cellSize * size) / 2;
                    
                    for (int row = 0; row < size; row++) {
                        for (int col = 0; col < size; col++) {
                            int x = offsetX + col * cellSize;
                            int y = offsetY + row * cellSize;
                            
                            if ((row + col) % 2 == 0) {
                                g2d.setColor(new Color(60, 70, 90));
                            } else {
                                g2d.setColor(new Color(40, 50, 70));
                            }
                            g2d.fillRect(x, y, cellSize, cellSize);
                            
                            if (state[row][col] == 1) {
                                Color color = getThreadColor(threadId);
                                // Glow
                                for (int i = 0; i < 5; i++) {
                                    g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40 - i * 8));
                                    int padding = cellSize / 5 - i * 2;
                                    g2d.fillOval(x + padding, y + padding, 
                                               cellSize - 2 * padding, cellSize - 2 * padding);
                                }
                                
                                g2d.setColor(color);
                                int padding = cellSize / 5;
                                g2d.fillOval(x + padding, y + padding, 
                                           cellSize - 2 * padding, cellSize - 2 * padding);
                                
                                g2d.setColor(Color.YELLOW);
                                g2d.fillOval(x + cellSize / 3, y + cellSize / 3, 
                                           cellSize / 3, cellSize / 3);
                            }
                            
                            g2d.setColor(new Color(30, 35, 50));
                            g2d.drawRect(x, y, cellSize, cellSize);
                        }
                    }
                }
            };
            boardPanel.setBackground(PANEL_BG);
            boardPanel.setPreferredSize(new Dimension(600, 600));
            
            JButton closeBtn = createStyledButton("Close", ACCENT_BLUE);
            closeBtn.addActionListener(e -> dialog.dispose());
            
            dialog.add(boardPanel, BorderLayout.CENTER);
            dialog.add(closeBtn, BorderLayout.SOUTH);
            dialog.setVisible(true);
        }
        
        private Color getThreadColor(int threadId) {
            Color[] colors = {
                new Color(100, 150, 255),
                new Color(255, 100, 150),
                new Color(100, 255, 150),
                new Color(255, 200, 100),
                new Color(180, 100, 255),
                new Color(100, 255, 255),
                new Color(255, 255, 100),
                new Color(255, 150, 100),
            };
            return colors[threadId % colors.length];
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}
