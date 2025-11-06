import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TicTacToeEnhanced {
    private static final int BOARD_WIDTH = 600;
    private static final int BOARD_HEIGHT = 700;
    private static final Color BG_COLOR = new Color(45, 52, 54);
    private static final Color TILE_COLOR = new Color(99, 110, 114);
    private static final Color HOVER_COLOR = new Color(116, 125, 140);
    private static final Color WIN_COLOR = new Color(46, 213, 115);
    private static final Color TIE_COLOR = new Color(255, 159, 67);
    private static final Color PLAYER_X_COLOR = new Color(52, 152, 219);
    private static final Color PLAYER_O_COLOR = new Color(231, 76, 60);
    
    private JFrame frame;
    private JLabel textLabel;
    private JPanel boardPanel;
    private JButton restartButton;
    private JButton[][] board = new JButton[3][3];
    
    private final String playerX = "X";
    private final String playerO = "O";
    private String currentPlayer;
    private String humanPlayer;
    private String aiPlayer;
    
    private boolean gameOver = false;
    private boolean vsAI = false;
    private boolean aiMode = false; // true = strategic AI, false = player vs player
    private int xWins = 0;
    private int oWins = 0;
    private int ties = 0;

    public TicTacToeEnhanced(boolean enableAI) {
        this.vsAI = enableAI;
        this.aiMode = enableAI;
        this.currentPlayer = playerX; // X always starts
        
        if (vsAI) {
            // In AI mode, human hasn't chosen yet
            this.humanPlayer = null;
            this.aiPlayer = null;
        }
        
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Tic-Tac-Toe");
        frame.setSize(BOARD_WIDTH, BOARD_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(BG_COLOR);

        // Header panel with label and score
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        
        textLabel = new JLabel();
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("Arial", Font.BOLD, 36));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        updateTurnLabel();
        
        JLabel scoreLabel = new JLabel(getScoreText(), SwingConstants.CENTER);
        scoreLabel.setForeground(Color.LIGHT_GRAY);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        
        headerPanel.add(textLabel, BorderLayout.CENTER);
        headerPanel.add(scoreLabel, BorderLayout.SOUTH);
        frame.add(headerPanel, BorderLayout.NORTH);

        // Board panel
        boardPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        boardPanel.setBackground(BG_COLOR);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(boardPanel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBackground(BG_COLOR);
        
        restartButton = new JButton("New Game");
        restartButton.setFont(new Font("Arial", Font.BOLD, 20));
        restartButton.setBackground(new Color(108, 92, 231));
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusable(false);
        restartButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        restartButton.addActionListener(e -> restartGame());
        
        JButton menuButton = new JButton("Main Menu");
        menuButton.setFont(new Font("Arial", Font.BOLD, 20));
        menuButton.setBackground(new Color(200, 60, 60));
        menuButton.setForeground(Color.WHITE);
        menuButton.setFocusable(false);
        menuButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        menuButton.addActionListener(e -> returnToMenu());
        
        controlPanel.add(restartButton);
        controlPanel.add(menuButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        // Create board tiles
        createBoardTiles();
        
        frame.setVisible(true);
    }

    private void createBoardTiles() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton tile = new JButton();
                board[r][c] = tile;
                boardPanel.add(tile);

                tile.setBackground(TILE_COLOR);
                tile.setForeground(Color.WHITE);
                tile.setFont(new Font("Arial", Font.BOLD, 100));
                tile.setFocusable(false);
                tile.setBorder(BorderFactory.createLineBorder(BG_COLOR, 2));

                // Hover effect
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!gameOver && tile.getText().equals("")) {
                            tile.setBackground(HOVER_COLOR);
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!gameOver && tile.getText().equals("")) {
                            tile.setBackground(TILE_COLOR);
                        }
                    }
                });

                tile.addActionListener(e -> handleTileClick(tile));
            }
        }
    }

    private void handleTileClick(JButton tile) {
        if (gameOver || !tile.getText().equals("")) return;
        
        // In AI mode, if this is the first move, set player symbols
        if (aiMode && humanPlayer == null) {
            humanPlayer = currentPlayer;
            aiPlayer = humanPlayer.equals(playerX) ? playerO : playerX;
            textLabel.setText("You are " + humanPlayer + " - Your Turn");
        }
        
        // In AI mode, only allow human to click on their turn
        if (aiMode && !currentPlayer.equals(humanPlayer)) return;
        
        makeMove(tile, currentPlayer);
    }

    private void makeMove(JButton tile, String player) {
        tile.setText(player);
        
        // Set color based on player
        if (player.equals(playerX)) {
            tile.setForeground(PLAYER_X_COLOR);
        } else {
            tile.setForeground(PLAYER_O_COLOR);
        }
        tile.setBackground(TILE_COLOR);
        
        if (checkWinner()) {
            return;
        }
        
        currentPlayer = currentPlayer.equals(playerX) ? playerO : playerX;
        updateTurnLabel();
        
        if (aiMode && currentPlayer.equals(aiPlayer)) {
            textLabel.setText("AI thinking...");
            disableBoard();
            Timer timer = new Timer(600, e -> {
                aiMove();
                ((Timer)e.getSource()).stop();
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void aiMove() {
        int[] bestMove = findBestMove();
        if (bestMove != null) {
            makeMove(board[bestMove[0]][bestMove[1]], aiPlayer);
            enableBoard();
        }
    }

    private int[] findBestMove() {
        // First move optimization - if board is empty or only center is taken, pick strategically
        int emptyCount = 0;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].getText().equals("")) emptyCount++;
            }
        }
        
        // If it's AI's first move
        if (emptyCount == 8) {
            // If human took center, take a corner
            if (!board[1][1].getText().equals("")) {
                return new int[]{0, 0};
            }
            // If human took a corner, take center
            if (!board[0][0].getText().equals("") || !board[0][2].getText().equals("") ||
                !board[2][0].getText().equals("") || !board[2][2].getText().equals("")) {
                return new int[]{1, 1};
            }
            // Otherwise take center
            return new int[]{1, 1};
        }

        int bestScore = Integer.MIN_VALUE;
        int[] move = null;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].getText().equals("")) {
                    board[r][c].setText(aiPlayer);
                    int score = minimax(false, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
                    board[r][c].setText("");
                    if (score > bestScore) {
                        bestScore = score;
                        move = new int[]{r, c};
                    }
                }
            }
        }
        return move;
    }

    private int minimax(boolean isMaximizing, int alpha, int beta, int depth) {
        String winner = getWinner();
        if (winner != null) {
            if (winner.equals(aiPlayer)) return 10 - depth;
            if (winner.equals(humanPlayer)) return depth - 10;
            return 0;
        }

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (board[r][c].getText().equals("")) {
                        board[r][c].setText(aiPlayer);
                        int score = minimax(false, alpha, beta, depth + 1);
                        board[r][c].setText("");
                        bestScore = Math.max(score, bestScore);
                        alpha = Math.max(alpha, score);
                        if (beta <= alpha) return bestScore;
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (board[r][c].getText().equals("")) {
                        board[r][c].setText(humanPlayer);
                        int score = minimax(true, alpha, beta, depth + 1);
                        board[r][c].setText("");
                        bestScore = Math.min(score, bestScore);
                        beta = Math.min(beta, score);
                        if (beta <= alpha) return bestScore;
                    }
                }
            }
            return bestScore;
        }
    }

    private String getWinner() {
        // Check rows
        for (int r = 0; r < 3; r++) {
            if (!board[r][0].getText().equals("") &&
                board[r][0].getText().equals(board[r][1].getText()) &&
                board[r][1].getText().equals(board[r][2].getText())) {
                return board[r][0].getText();
            }
        }

        // Check columns
        for (int c = 0; c < 3; c++) {
            if (!board[0][c].getText().equals("") &&
                board[0][c].getText().equals(board[1][c].getText()) &&
                board[1][c].getText().equals(board[2][c].getText())) {
                return board[0][c].getText();
            }
        }

        // Check diagonals
        if (!board[0][0].getText().equals("") &&
            board[0][0].getText().equals(board[1][1].getText()) &&
            board[1][1].getText().equals(board[2][2].getText())) {
            return board[0][0].getText();
        }
        
        if (!board[0][2].getText().equals("") &&
            board[0][2].getText().equals(board[1][1].getText()) &&
            board[1][1].getText().equals(board[2][0].getText())) {
            return board[0][2].getText();
        }

        // Check for tie
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].getText().equals("")) {
                    return null;
                }
            }
        }
        
        return "Tie";
    }

    private boolean checkWinner() {
        String result = getWinner();
        if (result != null) {
            gameOver = true;
            
            if (result.equals("Tie")) {
                ties++;
                textLabel.setText("It's a Tie!");
                highlightTie();
            } else {
                if (result.equals(playerX)) xWins++;
                else oWins++;
                
                if (aiMode) {
                    if (result.equals(humanPlayer)) {
                        textLabel.setText("You Win! 🎉");
                    } else {
                        textLabel.setText("AI Wins!");
                    }
                } else {
                    textLabel.setText(result + " Wins!");
                }
                highlightWinner(result);
            }
            
            updateScoreLabel();
            return true;
        }
        return false;
    }

    private void highlightWinner(String winner) {
        // Check rows
        for (int r = 0; r < 3; r++) {
            if (board[r][0].getText().equals(winner) &&
                board[r][1].getText().equals(winner) &&
                board[r][2].getText().equals(winner)) {
                for (int i = 0; i < 3; i++) {
                    board[r][i].setBackground(WIN_COLOR);
                }
                return;
            }
        }
        
        // Check columns
        for (int c = 0; c < 3; c++) {
            if (board[0][c].getText().equals(winner) &&
                board[1][c].getText().equals(winner) &&
                board[2][c].getText().equals(winner)) {
                for (int i = 0; i < 3; i++) {
                    board[i][c].setBackground(WIN_COLOR);
                }
                return;
            }
        }
        
        // Check diagonals
        if (board[0][0].getText().equals(winner) &&
            board[1][1].getText().equals(winner) &&
            board[2][2].getText().equals(winner)) {
            for (int i = 0; i < 3; i++) {
                board[i][i].setBackground(WIN_COLOR);
            }
            return;
        }
        
        if (board[0][2].getText().equals(winner) &&
            board[1][1].getText().equals(winner) &&
            board[2][0].getText().equals(winner)) {
            board[0][2].setBackground(WIN_COLOR);
            board[1][1].setBackground(WIN_COLOR);
            board[2][0].setBackground(WIN_COLOR);
        }
    }

    private void highlightTie() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c].setBackground(TIE_COLOR);
            }
        }
    }

    private void updateTurnLabel() {
        if (gameOver) return;
        
        if (aiMode) {
            if (humanPlayer == null) {
                textLabel.setText("Your Turn - Choose X or O");
            } else if (currentPlayer.equals(humanPlayer)) {
                textLabel.setText("Your Turn (" + humanPlayer + ")");
            } else {
                textLabel.setText("AI's Turn (" + aiPlayer + ")");
            }
        } else {
            textLabel.setText(currentPlayer + "'s Turn");
        }
    }

    private String getScoreText() {
        if (aiMode && humanPlayer != null) {
            int humanWins = humanPlayer.equals(playerX) ? xWins : oWins;
            int aiWins = humanPlayer.equals(playerX) ? oWins : xWins;
            return String.format("You: %d  |  AI: %d  |  Ties: %d", humanWins, aiWins, ties);
        }
        return String.format("X: %d  |  O: %d  |  Ties: %d", xWins, oWins, ties);
    }

    private void updateScoreLabel() {
        Component[] components = ((JPanel)frame.getContentPane().getComponent(0)).getComponents();
        for (Component c : components) {
            if (c instanceof JLabel && c != textLabel) {
                ((JLabel)c).setText(getScoreText());
            }
        }
    }

    private void disableBoard() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c].setEnabled(false);
            }
        }
    }

    private void enableBoard() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].getText().equals("")) {
                    board[r][c].setEnabled(true);
                }
            }
        }
    }

    private void restartGame() {
        gameOver = false;
        currentPlayer = playerX;
        
        // Reset player assignments in AI mode
        if (aiMode) {
            humanPlayer = null;
            aiPlayer = null;
        }
        
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c].setText("");
                board[r][c].setForeground(Color.WHITE);
                board[r][c].setBackground(TILE_COLOR);
                board[r][c].setEnabled(true);
            }
        }
        
        updateTurnLabel();
    }

    private void returnToMenu() {
        frame.dispose();
        showMainMenu();
    }

    public static void showMainMenu() {
        JFrame startFrame = new JFrame("Tic-Tac-Toe");
        startFrame.setSize(500, 450);
        startFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startFrame.setLocationRelativeTo(null);
        startFrame.getContentPane().setBackground(new Color(45, 52, 54));
        startFrame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(45, 52, 54));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JLabel titleLabel = new JLabel("TIC-TAC-TOE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Choose your game mode");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(Color.LIGHT_GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton vsPlayerBtn = createMenuButton("Player vs Player");
        JButton vsAIBtn = createMenuButton("vs Smart AI");
        JButton exitBtn = createMenuButton("Exit");
        exitBtn.setBackground(new Color(200, 60, 60));

        vsPlayerBtn.addActionListener(e -> {
            startFrame.dispose();
            new TicTacToeEnhanced(false);
        });

        vsAIBtn.addActionListener(e -> {
            startFrame.dispose();
            new TicTacToeEnhanced(true);
        });

        exitBtn.addActionListener(e -> System.exit(0));

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(vsPlayerBtn);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(vsAIBtn);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(exitBtn);

        startFrame.add(mainPanel, BorderLayout.CENTER);
        startFrame.setVisible(true);
    }

    private static JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setBackground(new Color(108, 92, 231));
        button.setForeground(Color.WHITE);
        button.setFocusable(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(350, 60));
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TicTacToeEnhanced::showMainMenu);
    }
}