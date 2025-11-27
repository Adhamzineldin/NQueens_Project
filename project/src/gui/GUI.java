package gui;

import base.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;

/**
 * Main GUI class for the N-Queens Solver visualization
 * Displays real-time solving progress across multiple threads
 */
public class GUI extends JFrame {
    // Core managers for state and thread coordination
    private final StateManager stateManager; // Manages the current state of all boards and solutions
    private final ThreadManager threadManager; // Manages all worker threads
    private final Map<Integer, BoardPanel> threadBoardPanels; // Maps thread IDs to their visual board panels
    private final JPanel mainBoardsPanel; // Container panel for all board visualizations
    private final JPanel controlPanel; // Top control panel with buttons and settings
    private JPanel solutionsPanel; // Right panel showing all found solutions
    private final JTextArea statsArea; // Bottom statistics display area
    private javax.swing.Timer updateTimer; // Timer for refreshing the UI periodically
    
    // Configuration parameters
    private int boardSize = 8; // Size of the N-Queens board (default 8x8)
    private int numThreads = 4; // Number of parallel threads to use (default 4)
    private volatile boolean isRunning = false; // Flag indicating if solving is in progress
    private volatile boolean stopRequested = false; // Flag to signal threads to stop
    private int delay = 10; // Milliseconds delay for both algorithm steps and UI updates (minimum 1ms)
    private JSpinner delaySpinner; // Spinner control for adjusting animation speed
    
    // Color scheme constants for consistent UI appearance
    private static final Color BG_COLOR = new Color(20, 25, 35); // Dark background color
    private static final Color PANEL_BG = new Color(30, 35, 50); // Panel background color
    private static final Color ACCENT_BLUE = new Color(100, 150, 255); // Blue accent for UI elements
    private static final Color ACCENT_GREEN = new Color(80, 200, 120); // Green accent for start/success
    private static final Color ACCENT_PURPLE = new Color(180, 100, 255); // Purple accent for variety
    private static final Color ACCENT_RED = new Color(255, 80, 100); // Red accent for stop/danger
    private static final Color TEXT_COLOR = new Color(230, 230, 240); // Light text color
    private static final Color PLACE_GLOW = new Color(80, 255, 120); // Green glow for placing queens
    private static final Color REMOVE_GLOW = new Color(255, 80, 80); // Red glow for removing queens (backtracking)
    
    /**
     * GUI Constructor - Initializes all components and sets up the main window layout
     */
    public GUI() {
        // Initialize core managers
        this.stateManager = new StateManager(); // Manages board states and solutions
        this.threadManager = new ThreadManager(); // Manages worker threads
        this.threadBoardPanels = new HashMap<>(); // Maps thread IDs to their board panels
        
        // Configure main window properties
        setTitle("🔷 N-Queens Solver - Multi-Threading Visualization"); // Set window title
        setSize(1600, 900); // Set window dimensions
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit application when window closes
        setBackground(BG_COLOR); // Set background color
        
        // Main layout using BorderLayout for organized component placement
        setLayout(new BorderLayout(10, 10)); // 10px gaps between regions
        
        // Control Panel at top - contains start/stop buttons and configuration controls
        controlPanel = createControlPanel(); // Create the control panel
        add(controlPanel, BorderLayout.NORTH); // Position at top
        
        // Center: Boards display - shows all thread boards in a grid
        mainBoardsPanel = new JPanel(); // Create container for board panels
        mainBoardsPanel.setBackground(BG_COLOR); // Match background color
        mainBoardsPanel.setLayout(new GridLayout(0, 2, 15, 15)); // 2 columns, auto rows, 15px gaps
        mainBoardsPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding around edges
        
        // Wrap boards panel in a scroll pane for many threads
        JScrollPane boardsScroll = new JScrollPane(mainBoardsPanel); // Make boards scrollable
        boardsScroll.setBackground(BG_COLOR); // Match background color
        boardsScroll.getViewport().setBackground(BG_COLOR); // Match viewport background
        boardsScroll.setBorder(null); // Remove default border
        boardsScroll.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling speed
        boardsScroll.getHorizontalScrollBar().setUnitIncrement(16); // Smooth horizontal scrolling
        add(boardsScroll, BorderLayout.CENTER); // Position in center
        
        // Right panel: Solutions and stats - displays all found solutions
        JPanel rightPanel = createRightPanel(); // Create the solutions panel
        add(rightPanel, BorderLayout.EAST); // Position on right side
        
        // Stats area at bottom - displays summary statistics
        statsArea = new JTextArea(3, 20); // Create text area for stats (3 rows, 20 cols)
        statsArea.setEditable(false); // Make it read-only
        statsArea.setBackground(PANEL_BG); // Set background color
        statsArea.setForeground(TEXT_COLOR); // Set text color
        statsArea.setFont(new Font("Consolas", Font.BOLD, 12)); // Use monospace font for alignment
        statsArea.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding
        add(statsArea, BorderLayout.SOUTH); // Position at bottom
        
        // Center window on screen
        setLocationRelativeTo(null); // Center the window relative to screen
        updateStats(); // Initialize stats display with default values
    }
    
    /**
     * Creates the top control panel with all configuration controls and buttons
     * @return JPanel containing all control elements
     */
    private JPanel createControlPanel() {
        // Create main panel with styling
        JPanel mainPanel = new JPanel(); // Container for all controls
        mainPanel.setBackground(PANEL_BG); // Set background color
        mainPanel.setBorder(BorderFactory.createCompoundBorder( // Create compound border (padding + line)
            new EmptyBorder(10, 10, 10, 10), // Outer padding
            BorderFactory.createLineBorder(ACCENT_BLUE, 2) // Blue border line
        ));
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Center-aligned flow layout with gaps
        
        // Board size configuration spinner
        JLabel sizeLabel = createStyledLabel("Board Size:"); // Label for board size
        SpinnerNumberModel sizeModel = new SpinnerNumberModel(8, 4, 16, 1); // Model: default=8, min=4, max=16, step=1
        JSpinner sizeSpinner = createStyledSpinner(sizeModel); // Create styled spinner
        sizeSpinner.addChangeListener(e -> boardSize = (int) sizeSpinner.getValue()); // Update boardSize when changed
        
        // Thread count configuration spinner
        JLabel threadsLabel = createStyledLabel("Threads:"); // Label for thread count
        SpinnerNumberModel threadsModel = new SpinnerNumberModel(4, 1, 16, 1); // Model: default=4, min=1, max=16, step=1
        JSpinner threadsSpinner = createStyledSpinner(threadsModel); // Create styled spinner
        threadsSpinner.addChangeListener(e -> numThreads = (int) threadsSpinner.getValue()); // Update numThreads when changed
        
        // Animation delay input (controls both algorithm and UI refresh rate)
        JLabel delayLabel = createStyledLabel("Animation Delay (ms):"); // Label for delay control
        SpinnerNumberModel delayModel = new SpinnerNumberModel(10, 1, 1000, 5); // Model: default=10ms, min=1ms, max=1000ms, step=5ms
        delaySpinner = new JSpinner(delayModel); // Create spinner for delay adjustment
        delaySpinner.setPreferredSize(new Dimension(100, 35)); // Set spinner size
        
        // Customize the spinner's text field for better appearance
        JComponent editor = delaySpinner.getEditor(); // Get the spinner's editor component
        if (editor instanceof JSpinner.DefaultEditor) { // Check if it's the default editor
            JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor; // Cast to default editor
            spinnerEditor.getTextField().setEditable(true); // Allow direct text editing
            spinnerEditor.getTextField().setBackground(PANEL_BG); // Match background color
            spinnerEditor.getTextField().setForeground(TEXT_COLOR); // Set text color
            spinnerEditor.getTextField().setFont(new Font("Arial", Font.BOLD, 14)); // Set font
            spinnerEditor.getTextField().setHorizontalAlignment(JTextField.CENTER); // Center align text
            spinnerEditor.getTextField().setCaretColor(TEXT_COLOR); // Set cursor color
        }
        
        // Status label to show speed description
        JLabel delayStatusLabel = createStyledLabel("Maximum"); // Label showing current speed setting
        delayStatusLabel.setPreferredSize(new Dimension(80, 20)); // Set label size
        delayStatusLabel.setForeground(ACCENT_RED); // Initial color for 10ms (maximum speed)
        
        // Add listener to update status label and algorithm when delay changes
        delaySpinner.addChangeListener(e -> {
            delay = (int) delaySpinner.getValue(); // Get new delay value
            
            // Update status label text and color based on speed category
            if (delay <= 10) { // Very fast animation
                delayStatusLabel.setText("Maximum"); // Maximum speed text
                delayStatusLabel.setForeground(ACCENT_RED); // Red color for maximum
            } else if (delay <= 30) { // Fast animation
                delayStatusLabel.setText("Very Fast"); // Very fast text
                delayStatusLabel.setForeground(ACCENT_RED); // Red color for very fast
            } else if (delay <= 80) { // Balanced animation
                delayStatusLabel.setText("Balanced"); // Balanced text
                delayStatusLabel.setForeground(ACCENT_GREEN); // Green color for balanced
            } else if (delay <= 150) { // Slow animation
                delayStatusLabel.setText("Slow"); // Slow text
                delayStatusLabel.setForeground(ACCENT_BLUE); // Blue color for slow
            } else { // Very slow animation
                delayStatusLabel.setText("Very Slow"); // Very slow text
                delayStatusLabel.setForeground(ACCENT_PURPLE); // Purple color for very slow
            }
            
            // Update algorithm step delay to match user selection
            NQueenSolver.setStepDelay(delay); // Set delay for algorithm execution
            
            // Update UI refresh timer if currently running
            if (updateTimer != null && updateTimer.isRunning()) { // Check if timer exists and is active
                updateTimer.setDelay(delay); // Update timer delay to match animation speed
            }
        });
        
        // Create action buttons
        JButton startBtn = createStyledButton("▶ START", ACCENT_GREEN); // Create start button with green color
        startBtn.addActionListener(e -> startSolving()); // Trigger solving when clicked
        
        JButton stopBtn = createStyledButton("⏹ STOP", ACCENT_RED); // Create stop button with red color
        stopBtn.addActionListener(e -> stopSolving()); // Stop solving when clicked
        
        // Add all components to the control panel in order
        mainPanel.add(sizeLabel); // Add board size label
        mainPanel.add(sizeSpinner); // Add board size spinner
        mainPanel.add(threadsLabel); // Add threads label
        mainPanel.add(threadsSpinner); // Add threads spinner
        mainPanel.add(delayLabel); // Add delay label
        mainPanel.add(delaySpinner); // Add delay spinner
        mainPanel.add(delayStatusLabel); // Add delay status label
        mainPanel.add(startBtn); // Add start button
        mainPanel.add(stopBtn); // Add stop button
        
        return mainPanel; // Return the completed control panel
    }
    
    /**
     * Creates the right panel that displays all found solutions
     * @return JPanel containing solutions display area
     */
    private JPanel createRightPanel() {
        // Create main panel with BorderLayout
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // BorderLayout with 5px gaps
        panel.setPreferredSize(new Dimension(350, 0)); // Set fixed width, flexible height
        panel.setBackground(BG_COLOR); // Match background color
        panel.setBorder(new EmptyBorder(10, 5, 10, 10)); // Add padding around edges
        
        // Create and configure title label
        JLabel titleLabel = createStyledLabel("📊 SOLUTIONS FOUND"); // Title for solutions section
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Larger font for title
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center align title
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Add bottom padding
        panel.add(titleLabel, BorderLayout.NORTH); // Position title at top
        
        // Create panel to hold solution previews
        solutionsPanel = new JPanel(); // Container for individual solution panels
        solutionsPanel.setLayout(new BoxLayout(solutionsPanel, BoxLayout.Y_AXIS)); // Stack solutions vertically
        solutionsPanel.setBackground(BG_COLOR); // Match background color
        
        // Wrap solutions panel in scroll pane for many solutions
        JScrollPane scrollPane = new JScrollPane(solutionsPanel); // Make solutions scrollable
        scrollPane.setBackground(BG_COLOR); // Match background color
        scrollPane.getViewport().setBackground(BG_COLOR); // Match viewport background
        scrollPane.setBorder(null); // Remove default border
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling speed
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16); // Smooth horizontal scrolling
        panel.add(scrollPane, BorderLayout.CENTER); // Position scroll pane in center
        
        return panel; // Return the completed right panel
    }
    
    /**
     * Creates a styled label with consistent appearance
     * @param text The text to display in the label
     * @return JLabel with styling applied
     */
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text); // Create label with text
        label.setForeground(TEXT_COLOR); // Set text color
        label.setFont(new Font("Arial", Font.BOLD, 14)); // Set font and size
        return label; // Return styled label
    }
    
    /**
     * Creates a styled number spinner with consistent appearance
     * @param model The number model for the spinner
     * @return JSpinner with styling applied
     */
    private JSpinner createStyledSpinner(SpinnerNumberModel model) {
        JSpinner spinner = new JSpinner(model); // Create spinner with model
        spinner.setPreferredSize(new Dimension(80, 35)); // Set spinner size
        JComponent editor = spinner.getEditor(); // Get the spinner's editor component
        if (editor instanceof JSpinner.DefaultEditor) { // Check if it's the default editor
            JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor; // Cast to default editor
            spinnerEditor.getTextField().setEditable(true); // Allow direct text editing
            spinnerEditor.getTextField().setBackground(PANEL_BG); // Match background color
            spinnerEditor.getTextField().setForeground(TEXT_COLOR); // Set text color
            spinnerEditor.getTextField().setFont(new Font("Arial", Font.BOLD, 14)); // Set font
            spinnerEditor.getTextField().setHorizontalAlignment(JTextField.CENTER); // Center align text
            spinnerEditor.getTextField().setCaretColor(TEXT_COLOR); // Set cursor color
        }
        return spinner; // Return styled spinner
    }
    
    /**
     * Creates a styled button with hover effects
     * @param text The button text
     * @param color The button background color
     * @return JButton with styling and hover effects
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text); // Create button with text
        button.setBackground(color); // Set background color
        button.setForeground(Color.WHITE); // Set text color to white
        button.setFont(new Font("Arial", Font.BOLD, 14)); // Set font
        button.setFocusPainted(false); // Remove focus border
        button.setBorderPainted(false); // Remove button border
        button.setPreferredSize(new Dimension(140, 40)); // Set button size
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Change cursor to hand on hover
        
        // Add hover effect to brighten button on mouse over
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { // When mouse enters button area
                button.setBackground(color.brighter()); // Brighten the background color
            }
            public void mouseExited(MouseEvent e) { // When mouse leaves button area
                button.setBackground(color); // Restore original background color
            }
        });
        
        return button; // Return styled button
    }
    
    /**
     * Starts the N-Queens solving process with multiple threads
     * Initializes all boards, starts worker threads, and begins UI updates
     */
    private void startSolving() {
        if (isRunning) return; // Prevent starting if already running
        
        isRunning = true; // Set running flag to true
        stopRequested = false; // Clear stop request flag
        
        // Set algorithm delay to match user selection
        NQueenSolver.setStepDelay(delay); // Configure step delay for algorithm
        
        // Print debug information to console
        System.out.println("==========================================="); // Separator
        System.out.println("🔄 CLEARING EVERYTHING AND STARTING FRESH"); // Starting message
        System.out.println("Board Size: " + boardSize + "x" + boardSize); // Display board dimensions
        System.out.println("Number of Threads: " + numThreads); // Display thread count
        System.out.println("Animation Delay: " + delay + "ms"); // Display animation speed
        System.out.println("==========================================="); // Separator
        
        // CLEAR EVERYTHING - FRESH START (remove all previous data)
        mainBoardsPanel.removeAll(); // Remove all board panels from display
        threadBoardPanels.clear(); // Clear thread-to-panel mapping
        solutionsPanel.removeAll(); // Remove all solution previews
        stateManager.current_states.clear(); // Clear current board states
        stateManager.solutions.clear(); // Clear found solutions
        
        // Force UI to update and show cleared state
        solutionsPanel.revalidate(); // Recalculate solutions panel layout
        solutionsPanel.repaint(); // Redraw solutions panel
        mainBoardsPanel.revalidate(); // Recalculate boards panel layout
        mainBoardsPanel.repaint(); // Redraw boards panel
        
        // Create NEW board panels for each thread
        int threadsToUse = Math.min(numThreads, boardSize); // Limit threads to board size (can't have more threads than rows)
        for (int i = 0; i < threadsToUse; i++) { // For each thread to create
            BoardPanel panel = new BoardPanel(i, boardSize); // Create new board panel for this thread
            threadBoardPanels.put(i, panel); // Map thread ID to panel
            mainBoardsPanel.add(panel); // Add panel to display
        }
        
        // Update UI to show new panels
        mainBoardsPanel.revalidate(); // Recalculate layout with new panels
        mainBoardsPanel.repaint(); // Redraw boards panel
        
        // Update stats to show 0 solutions initially
        updateStats(); // Refresh statistics display
        
        // Start solving in background thread to avoid blocking UI
        new Thread(() -> { // Create and start background thread
            threadManager.startSolving(boardSize, threadsToUse, stateManager); // Start all worker threads solving
            
            // Wait for all threads to complete their work
            threadManager.waitForCompletion(); // Block until all threads finish
            
            // Auto-stop when complete (update UI on Swing thread)
            SwingUtilities.invokeLater(() -> { // Run on UI thread
                if (isRunning && !stopRequested) { // Only if still running and not manually stopped
                    // Count total solutions found by all threads
                    int totalSolutions = 0; // Initialize counter
                    for (ArrayList<Solution> sols : stateManager.solutions.values()) { // For each thread's solutions
                        if (sols != null) totalSolutions += sols.size(); // Add solution count
                    }
                    
                    // Print completion information to console
                    System.out.println("==========================================="); // Separator
                    System.out.println("✓ All threads completed successfully!"); // Success message
                    System.out.println("Total solutions found: " + totalSolutions); // Display total count
                    System.out.println("==========================================="); // Separator
                    stopSolving(true); // Auto-complete stop (pass true to indicate auto-completion)
                }
            });
        }).start(); // Start the background thread
        
        // Start UI update timer to refresh display periodically
        if (updateTimer != null) { // If timer already exists
            updateTimer.stop(); // Stop the old timer
        }
        updateTimer = new javax.swing.Timer(delay, e -> updateUI()); // Create new timer with current delay
        updateTimer.start(); // Start the timer to trigger updateUI() periodically
    }
    
    /**
     * Stops the solving process (called by stop button)
     * Delegates to stopSolving(boolean) with false parameter
     */
    private void stopSolving() {
        stopSolving(false); // Call with autoComplete=false (manual stop)
    }
    
    /**
     * Stops the solving process and all worker threads
     * @param autoComplete true if stopped automatically when complete, false if manually stopped
     */
    private void stopSolving(boolean autoComplete) {
        if (!isRunning) return; // Do nothing if not running
        
        // Print stop message only for manual stops
        if (!autoComplete) { // If manually stopped by user
            System.out.println("==========================================="); // Separator
            System.out.println("⏹ STOP button pressed - Stopping all threads..."); // Stop message
        }
        
        stopRequested = true; // Set flag to signal threads to stop
        isRunning = false; // Clear running flag
        
        // Stop all threads if manually stopped (not if auto-completed)
        if (!autoComplete) { // If manually stopped
            threadManager.stopAll(); // Stop all worker threads
            System.out.println("✓ All threads stopped successfully."); // Success message
            System.out.println("==========================================="); // Separator
        }
        
        // Stop UI update timer
        if (updateTimer != null) { // If timer exists
            updateTimer.stop(); // Stop the timer
            updateTimer = null; // Clear timer reference
        }
        
        updateStats(); // Update statistics display
    }
    
    /**
     * Clears all boards, solutions, and stops solving
     * Resets the entire GUI to initial state
     */
    private void clearAll() {
        stopSolving(); // Stop any running solving process
        mainBoardsPanel.removeAll(); // Remove all board panels
        solutionsPanel.removeAll(); // Remove all solution panels
        threadBoardPanels.clear(); // Clear thread-to-panel mapping
        stateManager.current_states.clear(); // Clear current board states
        stateManager.solutions.clear(); // Clear found solutions
        mainBoardsPanel.revalidate(); // Recalculate boards panel layout
        mainBoardsPanel.repaint(); // Redraw boards panel
        solutionsPanel.revalidate(); // Recalculate solutions panel layout
        solutionsPanel.repaint(); // Redraw solutions panel
        updateStats(); // Update statistics display
    }
    
    /**
     * Updates the UI by refreshing all board displays and solutions
     * Called periodically by the update timer
     */
    private void updateUI() {
        // Update each thread's board display with current state
        for (Map.Entry<Integer, BoardPanel> entry : threadBoardPanels.entrySet()) { // For each thread's board panel
            int threadId = entry.getKey(); // Get thread ID
            BoardPanel panel = entry.getValue(); // Get board panel
            
            StepBoard stepBoard = stateManager.current_states.get(threadId); // Get current state for this thread
            if (stepBoard != null) { // If state exists
                panel.updateBoard(stepBoard); // Update the board display
            }
        }
        
        // Update solutions display with any new solutions
        updateSolutionsDisplay(); // Refresh solutions panel
        updateStats(); // Update statistics
    }
    
    /**
     * Updates the solutions panel to display all found solutions
     * Only updates if new solutions have been found
     */
    private void updateSolutionsDisplay() {
        // Count total solutions across all threads
        int totalSolutions = 0; // Initialize counter
        for (ArrayList<Solution> sols : stateManager.solutions.values()) { // For each thread's solutions
            if (sols != null) totalSolutions += sols.size(); // Add solution count
        }
        
        // Check if we need to update (avoid unnecessary redraws)
        if (solutionsPanel.getComponentCount() / 2 >= totalSolutions) { // Each solution has panel + spacer = 2 components
            return; // No new solutions, skip update
        }
        
        solutionsPanel.removeAll(); // Clear existing solution displays
        
        // Collect all solutions from all threads into a single list
        java.util.List<java.util.Map.Entry<Integer, Solution>> allSolutions = new ArrayList<>(); // List to hold all solutions
        for (Integer threadId : stateManager.solutions.keySet()) { // For each thread that has solutions
            ArrayList<Solution> sols = stateManager.solutions.get(threadId); // Get this thread's solutions
            if (sols != null) { // If solutions exist
                for (Solution sol : sols) { // For each solution
                    allSolutions.add(new java.util.AbstractMap.SimpleEntry<>(threadId, sol)); // Add with thread ID
                }
            }
        }
        
        // Show ALL solutions with mini previews
        for (int i = 0; i < allSolutions.size(); i++) { // For each solution
            java.util.Map.Entry<Integer, Solution> entry = allSolutions.get(i); // Get solution entry
            int threadId = entry.getKey(); // Get thread ID that found this solution
            Solution sol = entry.getValue(); // Get solution object
            
            SolutionMiniPanel miniPanel = new SolutionMiniPanel(sol, threadId, i + 1); // Create mini preview panel
            solutionsPanel.add(miniPanel); // Add panel to display
            solutionsPanel.add(Box.createRigidArea(new Dimension(0, 8))); // Add spacing between solutions
        }
        
        // Update UI to show new solutions
        solutionsPanel.revalidate(); // Recalculate layout
        solutionsPanel.repaint(); // Redraw panel
    }
    
    /**
     * Updates the statistics display at the bottom of the window
     * Shows total solutions, thread count, board size, and running status
     */
    private void updateStats() {
        // Count total solutions across all threads
        int totalSolutions = 0; // Initialize counter
        for (ArrayList<Solution> sols : stateManager.solutions.values()) { // For each thread's solutions
            if (sols != null) totalSolutions += sols.size(); // Add solution count
        }
        
        // Format and display statistics string
        statsArea.setText(String.format( // Set text with formatted string
            "  🎯 Total Solutions: %d  |  📊 Threads: %d  |  🔷 Board: %dx%d  |  %s", // Format template
            totalSolutions, // Total solutions found
            threadBoardPanels.size(), // Number of active threads
            boardSize, boardSize, // Board dimensions
            isRunning ? "⚡ RUNNING..." : "⏸ STOPPED" // Current status
        ));
    }
    
    /**
     * Inner class for visualizing a single thread's board state
     * Displays the current board configuration and animates changes
     */
    class BoardPanel extends JPanel {
        private int threadId; // ID of the thread this panel represents
        private int size; // Size of the N-Queens board
        private int[][] currentState; // Current board state (0=empty, 1=queen)
        private base.Action lastAction; // Last action taken (PLACE or REMOVE)
        private long lastUpdateTime; // Timestamp of last update
        private int animationFrame = 0; // Current animation frame for pulsing effects
        private int changedRow = -1; // Row of the most recently changed queen
        private int changedCol = -1; // Column of the most recently changed queen
        
        /**
         * Constructor for BoardPanel
         * @param threadId ID of the thread this board represents
         * @param size Size of the board (NxN)
         */
        public BoardPanel(int threadId, int size) {
            this.threadId = threadId; // Set thread ID
            this.size = size; // Set board size
            this.currentState = new int[size][size]; // Initialize empty board state
            this.lastUpdateTime = System.currentTimeMillis(); // Set initial timestamp
            this.lastAction = base.Action.PLACE; // Default action is PLACE
            
            setBackground(PANEL_BG); // Set panel background color
            setBorder(BorderFactory.createCompoundBorder( // Create compound border
                BorderFactory.createLineBorder(getThreadColor(threadId), 3), // Colored border matching thread
                new EmptyBorder(10, 10, 10, 10) // Inner padding
            ));
            setPreferredSize(new Dimension(400, 400)); // Set panel size
        }
        
        /**
         * Updates the board with a new state from the solver
         * @param stepBoard The new board state to display
         */
        public void updateBoard(StepBoard stepBoard) {
            if (stepBoard != null && stepBoard.board != null) { // Check if valid step board
                int[][] newState = stepBoard.board.getState(); // Get new board state
                currentState = newState; // Update current state
                lastAction = stepBoard.action; // Update last action (PLACE/REMOVE)
                changedRow = stepBoard.changedRow; // Update changed row
                changedCol = stepBoard.changedCol; // Update changed column
                lastUpdateTime = System.currentTimeMillis(); // Update timestamp
                animationFrame = (animationFrame + 1) % 20; // Increment animation frame (cycle 0-19)
                repaint(); // Trigger repaint to show new state
            }
        }
        
        /**
         * Paints the board panel with title, board grid, and queens
         * @param g Graphics context for drawing
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Call parent paint method
            Graphics2D g2d = (Graphics2D) g; // Cast to Graphics2D for advanced features
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Enable anti-aliasing for smooth graphics
            
            // Calculate board dimensions and positioning
            int width = getWidth() - 20; // Available width (minus padding)
            int height = getHeight() - 80; // Available height (minus title space)
            int cellSize = Math.min(width, height) / size; // Size of each cell (square)
            int offsetX = (width - cellSize * size) / 2 + 10; // X offset to center board
            int offsetY = (height - cellSize * size) / 2 + 50; // Y offset to center board below title
            
            // Draw title with thread color
            g2d.setColor(getThreadColor(threadId)); // Set color for this thread
            g2d.setFont(new Font("Arial", Font.BOLD, 16)); // Set title font
            String title = "Thread #" + threadId; // Create title text
            FontMetrics fm = g2d.getFontMetrics(); // Get font metrics for centering
            int titleX = (getWidth() - fm.stringWidth(title)) / 2; // Calculate X position to center title
            g2d.drawString(title, titleX, 25); // Draw title text
            
            // Action indicator - shows whether thread is placing or backtracking
            if (lastAction == base.Action.PLACE) { // If last action was placing a queen
                g2d.setColor(PLACE_GLOW); // Use green glow color
                g2d.fillOval(titleX - 30, 13, 15, 15); // Draw indicator circle left of title
                g2d.setFont(new Font("Arial", Font.BOLD, 11)); // Set smaller font for action text
                g2d.drawString("PLACE", titleX + fm.stringWidth(title) + 10, 25); // Draw "PLACE" text right of title
            } else { // If last action was backtracking (removing a queen)
                g2d.setColor(REMOVE_GLOW); // Use red glow color
                g2d.fillOval(titleX - 30, 13, 15, 15); // Draw indicator circle left of title
                g2d.setFont(new Font("Arial", Font.BOLD, 11)); // Set smaller font for action text
                g2d.drawString("BACKTRACK", titleX + fm.stringWidth(title) + 10, 25); // Draw "BACKTRACK" text right of title
            }
            
            // Draw board grid with checkerboard pattern
            for (int row = 0; row < size; row++) { // For each row
                for (int col = 0; col < size; col++) { // For each column
                    int x = offsetX + col * cellSize; // Calculate cell X position
                    int y = offsetY + row * cellSize; // Calculate cell Y position
                    
                    // Create checkerboard pattern with alternating colors
                    if ((row + col) % 2 == 0) { // If sum is even
                        g2d.setColor(new Color(60, 70, 90)); // Light cell color
                    } else { // If sum is odd
                        g2d.setColor(new Color(40, 50, 70)); // Dark cell color
                    }
                    g2d.fillRect(x, y, cellSize, cellSize); // Fill cell with color
                    
                    // Draw queen if present at this position
                    if (currentState[row][col] == 1) { // If queen is present
                        // Check if this is the queen that just changed (for special glow effect)
                        boolean isChanged = (row == changedRow && col == changedCol); // True if this is the recently changed queen
                        drawQueen(g2d, x, y, cellSize, getThreadColor(threadId), lastAction, isChanged); // Draw queen with glow
                    }
                    
                    // Draw cell border for clarity
                    g2d.setColor(new Color(30, 35, 50)); // Dark border color
                    g2d.drawRect(x, y, cellSize, cellSize); // Draw cell border
                }
            }
        }
        
        /**
         * Draws a chess queen piece with glow effects
         * @param g2d Graphics2D context for drawing
         * @param x X position of the cell
         * @param y Y position of the cell
         * @param size Size of the cell
         * @param threadColor Color for this thread
         * @param action Current action (PLACE or REMOVE)
         * @param isChanged Whether this queen was just changed
         */
        private void drawQueen(Graphics2D g2d, int x, int y, int size, Color threadColor, base.Action action, boolean isChanged) {
            int padding = size / 6; // Padding around queen within cell
            int centerX = x + size / 2; // Center X position of cell
            int centerY = y + size / 2; // Center Y position of cell
            
            // Glow effect - only for the queen that just changed
            if (isChanged) { // If this queen was just placed or removed
                Color glowColor = (action == base.Action.PLACE) ? PLACE_GLOW : REMOVE_GLOW; // Green for place, red for remove
                float pulse = (float) Math.abs(Math.sin(animationFrame * 0.3)); // Calculate pulsing value (0-1)
                
                // Draw multiple layers of glow for depth effect
                for (int i = 4; i >= 0; i--) { // Draw 5 layers from outer to inner
                    int alpha = (int) (20 + pulse * 30 - i * 5); // Calculate transparency (pulsing)
                    g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), alpha)); // Set color with transparency
                    int glowSize = size - 2 * padding + i * 6; // Calculate glow circle size
                    g2d.fillOval(centerX - glowSize / 2, centerY - glowSize / 2, glowSize, glowSize); // Draw glow circle
                }
            } else { // For queens that haven't changed recently
                // Subtle glow for other queens (thread color)
                for (int i = 2; i >= 0; i--) { // Draw 3 subtle layers
                    int alpha = 15 - i * 5; // Calculate subtle transparency
                    g2d.setColor(new Color(threadColor.getRed(), threadColor.getGreen(), threadColor.getBlue(), alpha)); // Set color with transparency
                    int glowSize = size - 2 * padding + i * 4; // Calculate glow circle size
                    g2d.fillOval(centerX - glowSize / 2, centerY - glowSize / 2, glowSize, glowSize); // Draw glow circle
                }
            }
            
            // Draw queen shape (chess piece style)
            int queenSize = size - 2 * padding; // Size of queen within cell
            int baseY = centerY + queenSize / 3; // Y position of queen base
            
            // Base - rectangular bottom part of queen
            g2d.setColor(threadColor); // Use thread color for queen body
            int baseWidth = queenSize * 3 / 4; // Width of base (75% of queen size)
            int baseHeight = queenSize / 5; // Height of base
            g2d.fillRect(centerX - baseWidth / 2, baseY, baseWidth, baseHeight); // Draw base rectangle
            
            // Body (trapezoid) - main body of queen piece
            int[] xPoints = { // X coordinates of trapezoid vertices
                centerX - queenSize / 4, // Bottom left
                centerX + queenSize / 4, // Bottom right
                centerX + queenSize / 3, // Top right
                centerX - queenSize / 3  // Top left
            };
            int[] yPoints = { // Y coordinates of trapezoid vertices
                baseY, // Bottom left
                baseY, // Bottom right
                centerY - queenSize / 6, // Top right
                centerY - queenSize / 6  // Top left
            };
            g2d.fillPolygon(xPoints, yPoints, 4); // Draw trapezoid body
            
            // Crown (5 points) - decorative crown on top of queen
            g2d.setColor(new Color(255, 215, 0)); // Gold color for crown
            int crownY = centerY - queenSize / 3; // Y position of crown base
            int crownWidth = queenSize / 2; // Width of crown
            int pointSize = queenSize / 7; // Size of crown points
            
            // Crown points - 5 decorative circles on top
            for (int i = 0; i < 5; i++) { // Draw 5 crown points
                int pointX = centerX - crownWidth / 2 + (i * crownWidth / 4); // Calculate X position for this point
                if (i % 2 == 0) { // Even points (0, 2, 4) are taller
                    g2d.fillOval(pointX - pointSize / 2, crownY - pointSize, pointSize, pointSize); // Draw higher point
                } else { // Odd points (1, 3) are lower
                    g2d.fillOval(pointX - pointSize / 2, crownY - pointSize / 2, pointSize, pointSize); // Draw lower point
                }
            }
            
            // Crown base - rectangular band below crown points
            g2d.fillRect(centerX - crownWidth / 2, crownY, crownWidth, queenSize / 8); // Draw crown base rectangle
            
            // Center jewel - decorative gem in center of crown
            g2d.setColor(new Color(255, 100, 255)); // Magenta color for jewel
            g2d.fillOval(centerX - pointSize / 2, crownY - pointSize / 2, pointSize, pointSize); // Draw jewel circle
        }
        
        /**
         * Gets the color associated with a specific thread ID
         * @param threadId The thread ID
         * @return Color for this thread
         */
        private Color getThreadColor(int threadId) {
            Color[] colors = { // Array of distinct colors for different threads
                new Color(100, 150, 255),  // Blue - Thread 0
                new Color(255, 100, 255),  // Magenta - Thread 1
                new Color(100, 208, 255),  // Sky Blue - Thread 2
                new Color(255, 200, 100),  // Orange/Gold - Thread 3
                new Color(180, 100, 255),  // Purple - Thread 4
                new Color(100, 255, 255),  // Cyan - Thread 5
                new Color(255, 255, 100),  // Yellow - Thread 6
                new Color(128, 128, 255),  // Indigo - Thread 7
            };
            return colors[threadId % colors.length]; // Cycle through colors if more than 8 threads
        }
    }
    
    /**
     * Inner class for displaying a mini preview of a found solution
     * Shows a small board view that can be clicked to enlarge
     */
    class SolutionMiniPanel extends JPanel {
        private Solution solution; // The solution to display
        private int threadId; // Thread that found this solution
        private int solutionNumber; // Sequential solution number
        
        /**
         * Constructor for SolutionMiniPanel
         * @param solution The solution to display
         * @param threadId Thread that found this solution
         * @param solutionNumber Sequential number of this solution
         */
        public SolutionMiniPanel(Solution solution, int threadId, int solutionNumber) {
            this.solution = solution; // Store solution reference
            this.threadId = threadId; // Store thread ID
            this.solutionNumber = solutionNumber; // Store solution number
            
            // Configure panel appearance
            setBackground(PANEL_BG); // Set background color
            setBorder(BorderFactory.createCompoundBorder( // Create compound border
                BorderFactory.createLineBorder(getThreadColor(threadId), 2), // Colored border matching thread
                new EmptyBorder(5, 5, 5, 5) // Inner padding
            ));
            setMaximumSize(new Dimension(320, 140)); // Set maximum size
            setPreferredSize(new Dimension(320, 140)); // Set preferred size
            setMinimumSize(new Dimension(320, 140)); // Set minimum size
            
            setCursor(new Cursor(Cursor.HAND_CURSOR)); // Show hand cursor on hover to indicate clickable
            
            // Make the entire panel clickable to show enlarged solution
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { // When panel is clicked
                    SwingUtilities.invokeLater(() -> showSolutionDialog()); // Show enlarged solution in dialog
                }
                
                @Override
                public void mouseEntered(MouseEvent e) { // When mouse enters panel area
                    setBorder(BorderFactory.createCompoundBorder( // Update border for hover effect
                        BorderFactory.createLineBorder(getThreadColor(threadId).brighter(), 3), // Brighter, thicker border
                        new EmptyBorder(5, 5, 5, 5) // Same padding
                    ));
                }
                
                @Override
                public void mouseExited(MouseEvent e) { // When mouse leaves panel area
                    setBorder(BorderFactory.createCompoundBorder( // Restore original border
                        BorderFactory.createLineBorder(getThreadColor(threadId), 2), // Original border
                        new EmptyBorder(5, 5, 5, 5) // Same padding
                    ));
                }
            });
        }
        
        /**
         * Paints the mini solution preview with board and labels
         * @param g Graphics context for drawing
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Call parent paint method
            Graphics2D g2d = (Graphics2D) g; // Cast to Graphics2D for advanced features
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Enable anti-aliasing
            
            // Get solution board state and size
            int[][] state = solution.solved_board.getState(); // Get board state array
            int size = solution.solved_board.getN(); // Get board size
            
            // Calculate board dimensions and positioning
            int width = getWidth() - 10; // Available width
            int height = getHeight() - 35; // Available height (leave space for labels)
            int cellSize = Math.min(width, height) / size; // Size of each cell
            int offsetX = (width - cellSize * size) / 2 + 5; // X offset to center board
            int offsetY = (height - cellSize * size) / 2 + 30; // Y offset to center board below labels
            
            // Draw solution number label at top left
            g2d.setColor(TEXT_COLOR); // Use light text color
            g2d.setFont(new Font("Arial", Font.BOLD, 11)); // Set font for solution number
            g2d.drawString("Solution #" + solutionNumber, 5, 15); // Draw solution number text
            
            // Draw thread ID label at top right
            g2d.setColor(getThreadColor(threadId)); // Use thread color
            g2d.setFont(new Font("Arial", Font.BOLD, 10)); // Set font for thread ID
            g2d.drawString("Thread " + threadId, getWidth() - 60, 15); // Draw thread ID text
            
            // Draw "click to enlarge" hint below solution number
            g2d.setFont(new Font("Arial", Font.ITALIC, 9)); // Set smaller italic font
            g2d.setColor(new Color(150, 160, 180)); // Use subtle gray color
            g2d.drawString("(click to enlarge)", 5, 27); // Draw hint text
            
            // Draw mini board with checkerboard pattern and queens
            for (int row = 0; row < size; row++) { // For each row
                for (int col = 0; col < size; col++) { // For each column
                    int x = offsetX + col * cellSize; // Calculate cell X position
                    int y = offsetY + row * cellSize; // Calculate cell Y position
                    
                    // Create checkerboard pattern
                    if ((row + col) % 2 == 0) { // If sum is even
                        g2d.setColor(new Color(60, 70, 90)); // Light cell color
                    } else { // If sum is odd
                        g2d.setColor(new Color(40, 50, 70)); // Dark cell color
                    }
                    g2d.fillRect(x, y, cellSize, cellSize); // Fill cell with color
                    
                    // Draw queen if present at this position
                    if (state[row][col] == 1) { // If queen is present
                        drawMiniQueen(g2d, x, y, cellSize, getThreadColor(threadId)); // Draw mini queen
                    }
                }
            }
        }
        
        /**
         * Draws a simplified mini queen for the solution preview
         * @param g2d Graphics2D context for drawing
         * @param x X position of the cell
         * @param y Y position of the cell
         * @param size Size of the cell
         * @param threadColor Color for this thread
         */
        private void drawMiniQueen(Graphics2D g2d, int x, int y, int size, Color threadColor) {
            int centerX = x + size / 2; // Center X position of cell
            int centerY = y + size / 2; // Center Y position of cell
            int queenSize = size * 2 / 3; // Size of queen (2/3 of cell size)
            
            // Simple glow effect around queen
            g2d.setColor(new Color(threadColor.getRed(), threadColor.getGreen(), 
                                  threadColor.getBlue(), 40)); // Thread color with transparency
            g2d.fillOval(centerX - queenSize / 2 - 2, centerY - queenSize / 2 - 2, 
                        queenSize + 4, queenSize + 4); // Draw glow circle
            
            // Queen body - simplified for mini view (just base rectangle)
            g2d.setColor(threadColor); // Use thread color for body
            int baseY = centerY + queenSize / 4; // Y position of base
            int baseWidth = queenSize * 2 / 3; // Width of base
            g2d.fillRect(centerX - baseWidth / 2, baseY, baseWidth, queenSize / 5); // Draw base rectangle
            
            // Body trapezoid - main body shape
            int[] xPoints = { // X coordinates of trapezoid vertices
                centerX - queenSize / 4, // Bottom left
                centerX + queenSize / 4, // Bottom right
                centerX + queenSize / 3, // Top right
                centerX - queenSize / 3  // Top left
            };
            int[] yPoints = { // Y coordinates of trapezoid vertices
                baseY, // Bottom left
                baseY, // Bottom right
                centerY - queenSize / 6, // Top right
                centerY - queenSize / 6  // Top left
            };
            g2d.fillPolygon(xPoints, yPoints, 4); // Draw trapezoid body
            
            // Crown - simplified gold crown
            g2d.setColor(new Color(255, 215, 0)); // Gold color for crown
            int crownSize = queenSize / 3; // Size of crown
            g2d.fillRect(centerX - crownSize / 2, centerY - queenSize / 3, crownSize, queenSize / 8); // Draw crown base
            
            // Crown points - 3 decorative circles on top
            int pointSize = Math.max(2, size / 8); // Size of crown points (minimum 2 pixels)
            for (int i = 0; i < 3; i++) { // Draw 3 crown points
                g2d.fillOval(centerX - crownSize / 2 + i * crownSize / 2 - pointSize / 2, // X position
                            centerY - queenSize / 3 - pointSize / 2, pointSize, pointSize); // Draw crown point circle
            }
        }
        
        /**
         * Shows a dialog with an enlarged view of the solution
         * Creates a modal dialog displaying the full solution board
         */
        private void showSolutionDialog() {
            // Create modal dialog for enlarged solution view
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(GUI.this), // Parent window
                                         "Solution #" + solutionNumber + " - Thread #" + threadId, true); // Title and modal flag
            dialog.setLayout(new BorderLayout(10, 10)); // BorderLayout with gaps
            dialog.setSize(700, 750); // Set dialog size
            dialog.setLocationRelativeTo(GUI.this); // Center relative to main window
            dialog.getContentPane().setBackground(BG_COLOR); // Set background color
            
            // Create custom panel to draw the enlarged solution board
            JPanel boardPanel = new JPanel() {
                /**
                 * Paints the enlarged solution board
                 * @param g Graphics context for drawing
                 */
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g); // Call parent paint method
                    Graphics2D g2d = (Graphics2D) g; // Cast to Graphics2D
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Enable anti-aliasing
                    
                    // Get solution board state and size
                    int[][] state = solution.solved_board.getState(); // Get board state array
                    int size = solution.solved_board.getN(); // Get board size
                    
                    // Calculate board dimensions and positioning
                    int width = getWidth() - 40; // Available width (with padding)
                    int height = getHeight() - 40; // Available height (with padding)
                    int cellSize = Math.min(width, height) / size; // Size of each cell (square)
                    int offsetX = (getWidth() - cellSize * size) / 2; // X offset to center board
                    int offsetY = (getHeight() - cellSize * size) / 2; // Y offset to center board
                    
                    // Draw board grid with checkerboard pattern and queens
                    for (int row = 0; row < size; row++) { // For each row
                        for (int col = 0; col < size; col++) { // For each column
                            int x = offsetX + col * cellSize; // Calculate cell X position
                            int y = offsetY + row * cellSize; // Calculate cell Y position
                            
                            // Create checkerboard pattern
                            if ((row + col) % 2 == 0) { // If sum is even
                                g2d.setColor(new Color(60, 70, 90)); // Light cell color
                            } else { // If sum is odd
                                g2d.setColor(new Color(40, 50, 70)); // Dark cell color
                            }
                            g2d.fillRect(x, y, cellSize, cellSize); // Fill cell with color
                            
                            // Draw queen if present at this position
                            if (state[row][col] == 1) { // If queen is present
                                drawQueenInDialog(g2d, x, y, cellSize, getThreadColor(threadId)); // Draw queen
                            }
                            
                            // Draw cell border
                            g2d.setColor(new Color(30, 35, 50)); // Dark border color
                            g2d.drawRect(x, y, cellSize, cellSize); // Draw cell border
                        }
                    }
                }
                
                /**
                 * Draws a queen piece in the dialog (full size with glow)
                 * @param g2d Graphics2D context for drawing
                 * @param x X position of the cell
                 * @param y Y position of the cell
                 * @param size Size of the cell
                 * @param threadColor Color for this thread
                 */
                private void drawQueenInDialog(Graphics2D g2d, int x, int y, int size, Color threadColor) {
                    int padding = size / 6; // Padding around queen within cell
                    int centerX = x + size / 2; // Center X position of cell
                    int centerY = y + size / 2; // Center Y position of cell
                    
                    // Glow effect around queen
                    for (int i = 5; i >= 0; i--) { // Draw 6 layers from outer to inner
                        int alpha = 40 - i * 7; // Calculate transparency (decreasing outward)
                        g2d.setColor(new Color(threadColor.getRed(), threadColor.getGreen(), 
                                              threadColor.getBlue(), alpha)); // Set color with transparency
                        int glowSize = size - 2 * padding + i * 8; // Calculate glow circle size
                        g2d.fillOval(centerX - glowSize / 2, centerY - glowSize / 2, glowSize, glowSize); // Draw glow circle
                    }
                    
                    // Draw queen shape (chess piece style)
                    int queenSize = size - 2 * padding; // Size of queen within cell
                    int baseY = centerY + queenSize / 3; // Y position of queen base
                    
                    // Base - rectangular bottom part of queen
                    g2d.setColor(threadColor); // Use thread color for queen body
                    int baseWidth = queenSize * 3 / 4; // Width of base
                    int baseHeight = queenSize / 5; // Height of base
                    g2d.fillRect(centerX - baseWidth / 2, baseY, baseWidth, baseHeight); // Draw base rectangle
                    
                    // Body (trapezoid) - main body of queen piece
                    int[] xPoints = { // X coordinates of trapezoid vertices
                        centerX - queenSize / 4, // Bottom left
                        centerX + queenSize / 4, // Bottom right
                        centerX + queenSize / 3, // Top right
                        centerX - queenSize / 3  // Top left
                    };
                    int[] yPoints = { // Y coordinates of trapezoid vertices
                        baseY, // Bottom left
                        baseY, // Bottom right
                        centerY - queenSize / 6, // Top right
                        centerY - queenSize / 6  // Top left
                    };
                    g2d.fillPolygon(xPoints, yPoints, 4); // Draw trapezoid body
                    
                    // Crown (5 points) - decorative crown on top
                    g2d.setColor(new Color(255, 215, 0)); // Gold color for crown
                    int crownY = centerY - queenSize / 3; // Y position of crown base
                    int crownWidth = queenSize / 2; // Width of crown
                    int pointSize = queenSize / 7; // Size of crown points
                    
                    // Crown points - 5 decorative circles
                    for (int i = 0; i < 5; i++) { // Draw 5 crown points
                        int pointX = centerX - crownWidth / 2 + (i * crownWidth / 4); // Calculate X position
                        if (i % 2 == 0) { // Even points (0, 2, 4) are taller
                            g2d.fillOval(pointX - pointSize / 2, crownY - pointSize, pointSize, pointSize); // Draw higher point
                        } else { // Odd points (1, 3) are lower
                            g2d.fillOval(pointX - pointSize / 2, crownY - pointSize / 2, pointSize, pointSize); // Draw lower point
                        }
                    }
                    
                    // Crown base - rectangular band below crown points
                    g2d.fillRect(centerX - crownWidth / 2, crownY, crownWidth, queenSize / 8); // Draw crown base rectangle
                    
                    // Jewel - decorative gem in center of crown
                    g2d.setColor(new Color(255, 100, 255)); // Magenta color for jewel
                    g2d.fillOval(centerX - pointSize / 2, crownY - pointSize / 2, pointSize, pointSize); // Draw jewel circle
                }
            };
            // Configure board panel appearance
            boardPanel.setBackground(PANEL_BG); // Set background color
            boardPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Add padding around board
            boardPanel.setPreferredSize(new Dimension(660, 660)); // Set board panel size
            
            // Create close button
            JButton closeBtn = createStyledButton("✓ Close", ACCENT_BLUE); // Create styled button
            closeBtn.setPreferredSize(new Dimension(150, 45)); // Set button size
            closeBtn.addActionListener(e -> dialog.dispose()); // Close dialog when clicked
            
            // Create panel for button
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Center-aligned flow layout
            buttonPanel.setBackground(BG_COLOR); // Match background color
            buttonPanel.add(closeBtn); // Add close button to panel
            
            // Add components to dialog
            dialog.add(boardPanel, BorderLayout.CENTER); // Add board panel to center
            dialog.add(buttonPanel, BorderLayout.SOUTH); // Add button panel to bottom
            dialog.setVisible(true); // Show dialog (blocks until closed)
        }
        
        /**
         * Gets the color associated with a specific thread ID
         * @param threadId The thread ID
         * @return Color for this thread
         */
        private Color getThreadColor(int threadId) {
            Color[] colors = { // Array of distinct colors for different threads
                new Color(100, 150, 255),  // Blue - Thread 0
                new Color(255, 100, 255),  // Magenta - Thread 1
                new Color(100, 208, 255),  // Sky Blue - Thread 2
                new Color(255, 200, 100),  // Orange/Gold - Thread 3
                new Color(180, 100, 255),  // Purple - Thread 4
                new Color(100, 255, 255),  // Cyan - Thread 5
                new Color(255, 255, 100),  // Yellow - Thread 6
                new Color(128, 128, 255),  // Indigo - Thread 7
            };
            return colors[threadId % colors.length]; // Cycle through colors if more than 8 threads
        }
    }
    
    /**
     * Main method - Entry point of the application
     * Initializes and displays the GUI
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { // Run GUI creation on Event Dispatch Thread
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // Use native system look and feel
            } catch (Exception e) { // Catch any look and feel errors
                e.printStackTrace(); // Print error stack trace
            }
            
            GUI gui = new GUI(); // Create main GUI instance
            gui.setVisible(true); // Make GUI visible
        });
    }
}
