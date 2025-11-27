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
    private volatile boolean isRunning = false;
    private volatile boolean stopRequested = false;
    private int delay = 10; // milliseconds for both algorithm and UI updates (minimum 1ms)
    private JSpinner delaySpinner;
    
    // Color schemes
    private static final Color BG_COLOR = new Color(20, 25, 35);
    private static final Color PANEL_BG = new Color(30, 35, 50);
    private static final Color ACCENT_BLUE = new Color(100, 150, 255);
    private static final Color ACCENT_GREEN = new Color(80, 200, 120);
    private static final Color ACCENT_PURPLE = new Color(180, 100, 255);
    private static final Color ACCENT_RED = new Color(255, 80, 100);
    private static final Color TEXT_COLOR = new Color(230, 230, 240);
    private static final Color PLACE_GLOW = new Color(80, 255, 120);
    private static final Color REMOVE_GLOW = new Color(255, 80, 80);
    
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
        boardsScroll.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        boardsScroll.getHorizontalScrollBar().setUnitIncrement(16);
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
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(PANEL_BG);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(ACCENT_BLUE, 2)
        ));
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
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
        
        // Animation delay input (controls both algorithm and UI)
        JLabel delayLabel = createStyledLabel("Animation Delay (ms):");
        SpinnerNumberModel delayModel = new SpinnerNumberModel(10, 1, 1000, 5);
        delaySpinner = new JSpinner(delayModel);
        delaySpinner.setPreferredSize(new Dimension(100, 35));
        
        // Make the text field editable
        JComponent editor = delaySpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor;
            spinnerEditor.getTextField().setEditable(true);
            spinnerEditor.getTextField().setBackground(PANEL_BG);
            spinnerEditor.getTextField().setForeground(TEXT_COLOR);
            spinnerEditor.getTextField().setFont(new Font("Arial", Font.BOLD, 14));
            spinnerEditor.getTextField().setHorizontalAlignment(JTextField.CENTER);
            spinnerEditor.getTextField().setCaretColor(TEXT_COLOR);
        }
        
        JLabel delayStatusLabel = createStyledLabel("Maximum");
        delayStatusLabel.setPreferredSize(new Dimension(80, 20));
        delayStatusLabel.setForeground(ACCENT_RED); // Initial color for 10ms
        
        delaySpinner.addChangeListener(e -> {
            delay = (int) delaySpinner.getValue();
            
            // Update status label and color based on speed
            if (delay <= 10) {
                delayStatusLabel.setText("Maximum");
                delayStatusLabel.setForeground(ACCENT_RED);
            } else if (delay <= 30) {
                delayStatusLabel.setText("Very Fast");
                delayStatusLabel.setForeground(ACCENT_RED);
            } else if (delay <= 80) {
                delayStatusLabel.setText("Balanced");
                delayStatusLabel.setForeground(ACCENT_GREEN);
            } else if (delay <= 150) {
                delayStatusLabel.setText("Slow");
                delayStatusLabel.setForeground(ACCENT_BLUE);
            } else {
                delayStatusLabel.setText("Very Slow");
                delayStatusLabel.setForeground(ACCENT_PURPLE);
            }
            
            // Update algorithm delay
            NQueenSolver.setStepDelay(delay);
            
            // Update UI timer if running
            if (updateTimer != null && updateTimer.isRunning()) {
                updateTimer.setDelay(delay); // Use same delay for UI
            }
        });
        
        // Buttons
        JButton startBtn = createStyledButton("▶ START", ACCENT_GREEN);
        startBtn.addActionListener(e -> startSolving());
        
        JButton stopBtn = createStyledButton("⏹ STOP", ACCENT_RED);
        stopBtn.addActionListener(e -> stopSolving());
        
        mainPanel.add(sizeLabel);
        mainPanel.add(sizeSpinner);
        mainPanel.add(threadsLabel);
        mainPanel.add(threadsSpinner);
        mainPanel.add(delayLabel);
        mainPanel.add(delaySpinner);
        mainPanel.add(delayStatusLabel);
        mainPanel.add(startBtn);
        mainPanel.add(stopBtn);
        
        return mainPanel;
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
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
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
            JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor;
            spinnerEditor.getTextField().setEditable(true);
            spinnerEditor.getTextField().setBackground(PANEL_BG);
            spinnerEditor.getTextField().setForeground(TEXT_COLOR);
            spinnerEditor.getTextField().setFont(new Font("Arial", Font.BOLD, 14));
            spinnerEditor.getTextField().setHorizontalAlignment(JTextField.CENTER);
            spinnerEditor.getTextField().setCaretColor(TEXT_COLOR);
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
        stopRequested = false;
        
        // Set algorithm delay
        NQueenSolver.setStepDelay(delay);
        
        // Debug print
        System.out.println("===========================================");
        System.out.println("🔄 CLEARING EVERYTHING AND STARTING FRESH");
        System.out.println("Board Size: " + boardSize + "x" + boardSize);
        System.out.println("Number of Threads: " + numThreads);
        System.out.println("Animation Delay: " + delay + "ms");
        System.out.println("===========================================");
        
        // CLEAR EVERYTHING - FRESH START
        mainBoardsPanel.removeAll();
        threadBoardPanels.clear();
        solutionsPanel.removeAll();
        stateManager.current_states.clear();
        stateManager.solutions.clear();
        
        // Force UI to update cleared state
        solutionsPanel.revalidate();
        solutionsPanel.repaint();
        mainBoardsPanel.revalidate();
        mainBoardsPanel.repaint();
        
        // Create NEW board panels for each thread
        int threadsToUse = Math.min(numThreads, boardSize);
        for (int i = 0; i < threadsToUse; i++) {
            BoardPanel panel = new BoardPanel(i, boardSize);
            threadBoardPanels.put(i, panel);
            mainBoardsPanel.add(panel);
        }
        
        mainBoardsPanel.revalidate();
        mainBoardsPanel.repaint();
        
        // Update stats to show 0 solutions
        updateStats();
        
        // Start solving in background with completion monitor
        new Thread(() -> {
            threadManager.startSolving(boardSize, threadsToUse, stateManager);
            
            // Wait for all threads to complete
            threadManager.waitForCompletion();
            
            // Auto-stop when complete
            SwingUtilities.invokeLater(() -> {
                if (isRunning && !stopRequested) {
                    // Count total solutions
                    int totalSolutions = 0;
                    for (ArrayList<Solution> sols : stateManager.solutions.values()) {
                        if (sols != null) totalSolutions += sols.size();
                    }
                    
                    System.out.println("===========================================");
                    System.out.println("✓ All threads completed successfully!");
                    System.out.println("Total solutions found: " + totalSolutions);
                    System.out.println("===========================================");
                    stopSolving(true); // Auto-complete stop
                }
            });
        }).start();
        
        // Start UI update timer
        if (updateTimer != null) {
            updateTimer.stop();
        }
        updateTimer = new javax.swing.Timer(delay, e -> updateUI());
        updateTimer.start();
    }
    
    private void stopSolving() {
        stopSolving(false);
    }
    
    private void stopSolving(boolean autoComplete) {
        if (!isRunning) return;
        
        if (!autoComplete) {
            System.out.println("===========================================");
            System.out.println("⏹ STOP button pressed - Stopping all threads...");
        }
        
        stopRequested = true;
        isRunning = false;
        
        // Stop all threads if manually stopped
        if (!autoComplete) {
            threadManager.stopAll();
            System.out.println("✓ All threads stopped successfully.");
            System.out.println("===========================================");
        }
        
        // Stop UI update timer
        if (updateTimer != null) {
            updateTimer.stop();
            updateTimer = null;
        }
        
        updateStats();
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
        // Only update if there are new solutions
        int totalSolutions = 0;
        for (ArrayList<Solution> sols : stateManager.solutions.values()) {
            if (sols != null) totalSolutions += sols.size();
        }
        
        // Check if we need to update
        if (solutionsPanel.getComponentCount() / 2 >= totalSolutions) {
            return; // No new solutions
        }
        
        solutionsPanel.removeAll();
        
        // Collect all solutions sorted by thread
        java.util.List<java.util.Map.Entry<Integer, Solution>> allSolutions = new ArrayList<>();
        for (Integer threadId : stateManager.solutions.keySet()) {
            ArrayList<Solution> sols = stateManager.solutions.get(threadId);
            if (sols != null) {
                for (Solution sol : sols) {
                    allSolutions.add(new java.util.AbstractMap.SimpleEntry<>(threadId, sol));
                }
            }
        }
        
        // Show ALL solutions efficiently
        for (int i = 0; i < allSolutions.size(); i++) {
            java.util.Map.Entry<Integer, Solution> entry = allSolutions.get(i);
            int threadId = entry.getKey();
            Solution sol = entry.getValue();
            
            SolutionMiniPanel miniPanel = new SolutionMiniPanel(sol, threadId, i + 1);
            solutionsPanel.add(miniPanel);
            solutionsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
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
        private int animationFrame = 0;
        private int changedRow = -1;
        private int changedCol = -1;
        
        public BoardPanel(int threadId, int size) {
            this.threadId = threadId;
            this.size = size;
            this.currentState = new int[size][size];
            this.lastUpdateTime = System.currentTimeMillis();
            this.lastAction = base.Action.PLACE;
            
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
                currentState = newState;
                lastAction = stepBoard.action;
                changedRow = stepBoard.changedRow;
                changedCol = stepBoard.changedCol;
                lastUpdateTime = System.currentTimeMillis();
                animationFrame = (animationFrame + 1) % 20;
                repaint();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth() - 20;
            int height = getHeight() - 80;
            int cellSize = Math.min(width, height) / size;
            int offsetX = (width - cellSize * size) / 2 + 10;
            int offsetY = (height - cellSize * size) / 2 + 50;
            
            // Draw title with action indicator
            g2d.setColor(getThreadColor(threadId));
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String title = "Thread #" + threadId;
            FontMetrics fm = g2d.getFontMetrics();
            int titleX = (getWidth() - fm.stringWidth(title)) / 2;
            g2d.drawString(title, titleX, 25);
            
            // Action indicator
            if (lastAction == base.Action.PLACE) {
                g2d.setColor(PLACE_GLOW);
                g2d.fillOval(titleX - 30, 13, 15, 15);
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
                g2d.drawString("PLACE", titleX + fm.stringWidth(title) + 10, 25);
            } else {
                g2d.setColor(REMOVE_GLOW);
                g2d.fillOval(titleX - 30, 13, 15, 15);
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
                g2d.drawString("BACKTRACK", titleX + fm.stringWidth(title) + 10, 25);
            }
            
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
                        // Check if this is the queen that just changed
                        boolean isChanged = (row == changedRow && col == changedCol);
                        drawQueen(g2d, x, y, cellSize, getThreadColor(threadId), lastAction, isChanged);
                    }
                    
                    // Border
                    g2d.setColor(new Color(30, 35, 50));
                    g2d.drawRect(x, y, cellSize, cellSize);
                }
            }
        }
        
        private void drawQueen(Graphics2D g2d, int x, int y, int size, Color threadColor, base.Action action, boolean isChanged) {
            int padding = size / 6;
            int centerX = x + size / 2;
            int centerY = y + size / 2;
            
            // Glow effect - only for the queen that just changed
            if (isChanged) {
                Color glowColor = (action == base.Action.PLACE) ? PLACE_GLOW : REMOVE_GLOW;
                float pulse = (float) Math.abs(Math.sin(animationFrame * 0.3));
                
                for (int i = 4; i >= 0; i--) {
                    int alpha = (int) (20 + pulse * 30 - i * 5);
                    g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), alpha));
                    int glowSize = size - 2 * padding + i * 6;
                    g2d.fillOval(centerX - glowSize / 2, centerY - glowSize / 2, glowSize, glowSize);
                }
            } else {
                // Subtle glow for other queens (thread color)
                for (int i = 2; i >= 0; i--) {
                    int alpha = 15 - i * 5;
                    g2d.setColor(new Color(threadColor.getRed(), threadColor.getGreen(), threadColor.getBlue(), alpha));
                    int glowSize = size - 2 * padding + i * 4;
                    g2d.fillOval(centerX - glowSize / 2, centerY - glowSize / 2, glowSize, glowSize);
                }
            }
            
            // Draw queen shape (chess piece style)
            int queenSize = size - 2 * padding;
            int baseY = centerY + queenSize / 3;
            
            // Base
            g2d.setColor(threadColor);
            int baseWidth = queenSize * 3 / 4;
            int baseHeight = queenSize / 5;
            g2d.fillRect(centerX - baseWidth / 2, baseY, baseWidth, baseHeight);
            
            // Body (trapezoid)
            int[] xPoints = {
                centerX - queenSize / 4, 
                centerX + queenSize / 4,
                centerX + queenSize / 3,
                centerX - queenSize / 3
            };
            int[] yPoints = {
                baseY,
                baseY,
                centerY - queenSize / 6,
                centerY - queenSize / 6
            };
            g2d.fillPolygon(xPoints, yPoints, 4);
            
            // Crown (5 points)
            g2d.setColor(new Color(255, 215, 0)); // Gold
            int crownY = centerY - queenSize / 3;
            int crownWidth = queenSize / 2;
            int pointSize = queenSize / 7;
            
            // Crown points
            for (int i = 0; i < 5; i++) {
                int pointX = centerX - crownWidth / 2 + (i * crownWidth / 4);
                if (i % 2 == 0) {
                    g2d.fillOval(pointX - pointSize / 2, crownY - pointSize, pointSize, pointSize);
                } else {
                    g2d.fillOval(pointX - pointSize / 2, crownY - pointSize / 2, pointSize, pointSize);
                }
            }
            
            // Crown base
            g2d.fillRect(centerX - crownWidth / 2, crownY, crownWidth, queenSize / 8);
            
            // Center jewel
            g2d.setColor(new Color(255, 100, 255)); // Magenta jewel
            g2d.fillOval(centerX - pointSize / 2, crownY - pointSize / 2, pointSize, pointSize);
        }
        
        private Color getThreadColor(int threadId) {
            Color[] colors = {
                new Color(100, 150, 255),  // Blue
                new Color(255, 100, 255),  // Magenta (replaced pink/red)
                new Color(100, 208, 255),  // Sky Blue (replaced green)
                new Color(255, 200, 100),  // Orange/Gold
                new Color(180, 100, 255),  // Purple
                new Color(100, 255, 255),  // Cyan
                new Color(255, 255, 100),  // Yellow
                new Color(128, 128, 255),  // Indigo (replaced coral/red)
            };
            return colors[threadId % colors.length];
        }
    }
    
    // Inner class for solution mini preview
    class SolutionMiniPanel extends JPanel {
        private Solution solution;
        private int threadId;
        private int solutionNumber;
        
        public SolutionMiniPanel(Solution solution, int threadId, int solutionNumber) {
            this.solution = solution;
            this.threadId = threadId;
            this.solutionNumber = solutionNumber;
            
            setBackground(PANEL_BG);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getThreadColor(threadId), 2),
                new EmptyBorder(5, 5, 5, 5)
            ));
            setMaximumSize(new Dimension(320, 140));
            setPreferredSize(new Dimension(320, 140));
            setMinimumSize(new Dimension(320, 140));
            
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Make the entire panel clickable
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    SwingUtilities.invokeLater(() -> showSolutionDialog());
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(getThreadColor(threadId).brighter(), 3),
                        new EmptyBorder(5, 5, 5, 5)
                    ));
                }
                
                @Override
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
            int height = getHeight() - 35;
            int cellSize = Math.min(width, height) / size;
            int offsetX = (width - cellSize * size) / 2 + 5;
            int offsetY = (height - cellSize * size) / 2 + 30;
            
            // Solution number and thread
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.drawString("Solution #" + solutionNumber, 5, 15);
            
            g2d.setColor(getThreadColor(threadId));
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString("Thread " + threadId, getWidth() - 60, 15);
            
            g2d.setFont(new Font("Arial", Font.ITALIC, 9));
            g2d.setColor(new Color(150, 160, 180));
            g2d.drawString("(click to enlarge)", 5, 27);
            
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
                        drawMiniQueen(g2d, x, y, cellSize, getThreadColor(threadId));
                    }
                }
            }
        }
        
        private void drawMiniQueen(Graphics2D g2d, int x, int y, int size, Color threadColor) {
            int centerX = x + size / 2;
            int centerY = y + size / 2;
            int queenSize = size * 2 / 3;
            
            // Simple glow
            g2d.setColor(new Color(threadColor.getRed(), threadColor.getGreen(), 
                                  threadColor.getBlue(), 40));
            g2d.fillOval(centerX - queenSize / 2 - 2, centerY - queenSize / 2 - 2, 
                        queenSize + 4, queenSize + 4);
            
            // Queen body - simplified for mini view
            g2d.setColor(threadColor);
            int baseY = centerY + queenSize / 4;
            int baseWidth = queenSize * 2 / 3;
            g2d.fillRect(centerX - baseWidth / 2, baseY, baseWidth, queenSize / 5);
            
            // Body trapezoid
            int[] xPoints = {
                centerX - queenSize / 4, 
                centerX + queenSize / 4,
                centerX + queenSize / 3,
                centerX - queenSize / 3
            };
            int[] yPoints = {
                baseY,
                baseY,
                centerY - queenSize / 6,
                centerY - queenSize / 6
            };
            g2d.fillPolygon(xPoints, yPoints, 4);
            
            // Crown - simplified
            g2d.setColor(new Color(255, 215, 0));
            int crownSize = queenSize / 3;
            g2d.fillRect(centerX - crownSize / 2, centerY - queenSize / 3, crownSize, queenSize / 8);
            
            // Crown points
            int pointSize = Math.max(2, size / 8);
            for (int i = 0; i < 3; i++) {
                g2d.fillOval(centerX - crownSize / 2 + i * crownSize / 2 - pointSize / 2, 
                            centerY - queenSize / 3 - pointSize / 2, pointSize, pointSize);
            }
        }
        
        private void showSolutionDialog() {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(GUI.this), 
                                         "Solution #" + solutionNumber + " - Thread #" + threadId, true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setSize(700, 750);
            dialog.setLocationRelativeTo(GUI.this);
            dialog.getContentPane().setBackground(BG_COLOR);
            
            JPanel boardPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int[][] state = solution.solved_board.getState();
                    int size = solution.solved_board.getN();
                    
                    int width = getWidth() - 40;
                    int height = getHeight() - 40;
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
                                drawQueenInDialog(g2d, x, y, cellSize, getThreadColor(threadId));
                            }
                            
                            g2d.setColor(new Color(30, 35, 50));
                            g2d.drawRect(x, y, cellSize, cellSize);
                        }
                    }
                }
                
                private void drawQueenInDialog(Graphics2D g2d, int x, int y, int size, Color threadColor) {
                    int padding = size / 6;
                    int centerX = x + size / 2;
                    int centerY = y + size / 2;
                    
                    // Glow effect
                    for (int i = 5; i >= 0; i--) {
                        int alpha = 40 - i * 7;
                        g2d.setColor(new Color(threadColor.getRed(), threadColor.getGreen(), 
                                              threadColor.getBlue(), alpha));
                        int glowSize = size - 2 * padding + i * 8;
                        g2d.fillOval(centerX - glowSize / 2, centerY - glowSize / 2, glowSize, glowSize);
                    }
                    
                    // Draw queen shape
                    int queenSize = size - 2 * padding;
                    int baseY = centerY + queenSize / 3;
                    
                    // Base
                    g2d.setColor(threadColor);
                    int baseWidth = queenSize * 3 / 4;
                    int baseHeight = queenSize / 5;
                    g2d.fillRect(centerX - baseWidth / 2, baseY, baseWidth, baseHeight);
                    
                    // Body
                    int[] xPoints = {
                        centerX - queenSize / 4, 
                        centerX + queenSize / 4,
                        centerX + queenSize / 3,
                        centerX - queenSize / 3
                    };
                    int[] yPoints = {
                        baseY,
                        baseY,
                        centerY - queenSize / 6,
                        centerY - queenSize / 6
                    };
                    g2d.fillPolygon(xPoints, yPoints, 4);
                    
                    // Crown
                    g2d.setColor(new Color(255, 215, 0));
                    int crownY = centerY - queenSize / 3;
                    int crownWidth = queenSize / 2;
                    int pointSize = queenSize / 7;
                    
                    for (int i = 0; i < 5; i++) {
                        int pointX = centerX - crownWidth / 2 + (i * crownWidth / 4);
                        if (i % 2 == 0) {
                            g2d.fillOval(pointX - pointSize / 2, crownY - pointSize, pointSize, pointSize);
                        } else {
                            g2d.fillOval(pointX - pointSize / 2, crownY - pointSize / 2, pointSize, pointSize);
                        }
                    }
                    
                    g2d.fillRect(centerX - crownWidth / 2, crownY, crownWidth, queenSize / 8);
                    
                    // Jewel
                    g2d.setColor(new Color(255, 100, 255));
                    g2d.fillOval(centerX - pointSize / 2, crownY - pointSize / 2, pointSize, pointSize);
                }
            };
            boardPanel.setBackground(PANEL_BG);
            boardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            boardPanel.setPreferredSize(new Dimension(660, 660));
            
            JButton closeBtn = createStyledButton("✓ Close", ACCENT_BLUE);
            closeBtn.setPreferredSize(new Dimension(150, 45));
            closeBtn.addActionListener(e -> dialog.dispose());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(BG_COLOR);
            buttonPanel.add(closeBtn);
            
            dialog.add(boardPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        }
        
        private Color getThreadColor(int threadId) {
            Color[] colors = {
                new Color(100, 150, 255),  // Blue
                new Color(255, 100, 255),  // Magenta (replaced pink/red)
                new Color(100, 208, 255),  // Sky Blue (replaced green)
                new Color(255, 200, 100),  // Orange/Gold
                new Color(180, 100, 255),  // Purple
                new Color(100, 255, 255),  // Cyan
                new Color(255, 255, 100),  // Yellow
                new Color(128, 128, 255),  // Indigo (replaced coral/red)
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
