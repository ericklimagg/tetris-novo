package com.tetris.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList; 
import java.util.List;
import java.util.Random; 

/**
 * Representa o estado completo do tabuleiro de jogo.
 * ATUALIZADO: Adiciona contador de Vitórias e HighScore estático (global).
 */
public class Board {

    // --- Constantes do Jogo ---
    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 20;
    private static final int LEVEL_UP_LINES = 10;
    private static final String HIGHSCORE_FILE = "highscore.txt";
    private static final int LINE_CLEAR_ANIMATION_TICKS = 8; 

    // --- Estado Global (Estático) ---
    private static int highScore = 0;
    private static boolean isHighScoreLoaded = false; // <<< NOVO

    // --- Estado da Instância ---
    private boolean isStarted = false;
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private boolean isGhostPieceEnabled = true;

    private int score = 0;
    private int level = 1;
    private int linesCleared = 0;
    private int totalPieces = 0;
    private int tetrisCount = 0;
    private int wins = 0; // <<< NOVO: Contador de vitórias
    
    // --- Variáveis de Lixo (Garbage) ---
    private int incomingGarbage = 0;
    private int outgoingGarbage = 0;
    private Random garbageHoleRandom = new Random();


    private Piece currentPiece;
    private Piece nextPiece;
    private Shape.Tetrominoe[] boardGrid;

    // --- Variáveis de Animação ---
    private boolean isAnimatingLineClear = false;
    private List<Integer> linesBeingCleared = new ArrayList<>();
    private int lineClearTimer = 0;


    public Board() {
        boardGrid = new Shape.Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];
        currentPiece = new Piece();
        nextPiece = new Piece();
        
        // --- ATUALIZADO: Carrega o HighScore apenas uma vez ---
        if (!isHighScoreLoaded) {
            loadHighScore();
            isHighScoreLoaded = true;
        }
        // --- FIM ---
        clearBoard();
    }

    public void resetForMenu() {
        isStarted = false;
        isGameOver = false;
        isPaused = false;
        clearBoard();
    }

    public void start() {
        isStarted = true;
        isGameOver = false;
        isPaused = false;
        isAnimatingLineClear = false; 
        linesBeingCleared.clear();    
        score = 0;
        level = 1;
        linesCleared = 0; 
        totalPieces = 0;
        tetrisCount = 0;
        // 'wins' não é resetado no start(), pois persiste pela sessão
        
        incomingGarbage = 0;
        outgoingGarbage = 0;
        
        clearBoard();
        
        nextPiece.setRandomShape();
        newPiece();
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_WIDTH * BOARD_HEIGHT; i++) {
            boardGrid[i] = Shape.Tetrominoe.NoShape;
        }
    }

    public void newPiece() {
        if (this.incomingGarbage > 0) {
            applyGarbageLines(this.incomingGarbage);
            this.incomingGarbage = 0;
        }

        currentPiece = nextPiece;
        nextPiece = new Piece(); 
        nextPiece.setRandomShape(); 
        currentPiece.setX(BOARD_WIDTH / 2);
        currentPiece.setY(BOARD_HEIGHT - 1 + currentPiece.minY());
        
        totalPieces++; 

        if (!tryMove(currentPiece, currentPiece.getX(), currentPiece.getY())) {
            isGameOver = true;
            currentPiece.setShape(Shape.Tetrominoe.NoShape);
            
            // --- ATUALIZADO: Salva o HighScore global ---
            if (score > highScore) {
                highScore = score;
                saveHighScore();
            }
            // --- FIM ---
        }
    }

    private boolean tryMove(Piece piece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + piece.x(i);
            int y = newY - piece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0) {
                return false;
            }

            if (y < BOARD_HEIGHT && shapeAt(x, y) != Shape.Tetrominoe.NoShape) {
                return false;
            }
        }

        currentPiece = piece;
        currentPiece.setX(newX);
        currentPiece.setY(newY);
        return true;
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = currentPiece.getX() + currentPiece.x(i);
            int y = currentPiece.getY() - currentPiece.y(i);
            if (y >= 0 && y < BOARD_HEIGHT) {
                boardGrid[y * BOARD_WIDTH + x] = currentPiece.getShape();
            }
        }
        
        detectFullLines(); 
        
        if (!isGameOver && !isAnimatingLineClear) {
            newPiece();
        }
    }

    private void detectFullLines() {
        linesBeingCleared.clear();

        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (shapeAt(j, i) == Shape.Tetrominoe.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                linesBeingCleared.add(i);
            }
        }

        if (!linesBeingCleared.isEmpty()) {
            isAnimatingLineClear = true;
            lineClearTimer = LINE_CLEAR_ANIMATION_TICKS;
        }
    }

    public void finishLineClear() {
        if (linesBeingCleared.isEmpty()) {
            return;
        }

        for (int row : linesBeingCleared) {
            for (int k = row; k < BOARD_HEIGHT - 1; k++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    boardGrid[k * BOARD_WIDTH + j] = shapeAt(j, k + 1);
                }
            }
            for (int j = 0; j < BOARD_WIDTH; j++) {
                boardGrid[(BOARD_HEIGHT - 1) * BOARD_WIDTH + j] = Shape.Tetrominoe.NoShape;
            }
        }

        int numFullLines = linesBeingCleared.size();
        
        if (numFullLines == 2) {
            outgoingGarbage = 1;
        } else if (numFullLines == 3) {
            outgoingGarbage = 2;
        } else if (numFullLines == 4) {
            outgoingGarbage = 4;
            tetrisCount++;
        }

        updateScore(numFullLines);
        linesCleared += numFullLines;
        
        if (linesCleared / LEVEL_UP_LINES >= level) {
            level++;
        }

        linesBeingCleared.clear();
        isAnimatingLineClear = false;
        lineClearTimer = 0;
    }

    private void applyGarbageLines(int lines) {
        for (int y = BOARD_HEIGHT - lines; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (shapeAt(x, y) != Shape.Tetrominoe.NoShape) {
                    isGameOver = true;
                    return;
                }
            }
        }

        for (int y = BOARD_HEIGHT - 1; y >= lines; y--) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                boardGrid[y * BOARD_WIDTH + x] = shapeAt(x, y - lines);
            }
        }

        int hole = garbageHoleRandom.nextInt(BOARD_WIDTH);
        
        for (int y = 0; y < lines; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                boardGrid[y * BOARD_WIDTH + x] = (x == hole) ? 
                    Shape.Tetrominoe.NoShape : 
                    Shape.Tetrominoe.GarbageShape;
            }
        }
        
        if (currentPiece.getShape() != Shape.Tetrominoe.NoShape) {
             currentPiece.setY(currentPiece.getY() + lines);
        }
    }


    // --- Ações do Jogador ---
    public void moveLeft() {
        tryMove(currentPiece, currentPiece.getX() - 1, currentPiece.getY());
    }
    public void moveRight() {
        tryMove(currentPiece, currentPiece.getX() + 1, currentPiece.getY());
    }
    public void rotateLeft() {
        tryMove(currentPiece.rotateLeft(), currentPiece.getX(), currentPiece.getY());
    }
    public void rotateRight() {
        tryMove(currentPiece.rotateRight(), currentPiece.getX(), currentPiece.getY());
    }
    public void dropDown() {
        int newY = getGhostPieceY();
        tryMove(currentPiece, currentPiece.getX(), newY);
        pieceDropped();
    }
    public void movePieceDown() {
        if (!tryMove(currentPiece, currentPiece.getX(), currentPiece.getY() - 1)) {
            pieceDropped();
        }
    }

    // --- Gestão de Estado do Jogo ---
    public void togglePause() {
        if (!isStarted || isGameOver) return;
        isPaused = !isPaused;
    }
    public void toggleGhostPiece() {
        isGhostPieceEnabled = !isGhostPieceEnabled;
    }
    public int getGhostPieceY() {
        int y = currentPiece.getY();
        while (true) {
            if (!canMoveTo(currentPiece, currentPiece.getX(), y - 1)) {
                return y;
            }
            y--;
        }
    }
    private boolean canMoveTo(Piece piece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + piece.x(i);
            int y = newY - piece.y(i);
            if (x < 0 || x >= BOARD_WIDTH || y < 0) return false;
            if (y < BOARD_HEIGHT && shapeAt(x, y) != Shape.Tetrominoe.NoShape) return false;
        }
        return true;
    }

    // --- Pontuação ---
    private void updateScore(int lines) {
        int[] points = {0, 40, 100, 300, 1200};
        score += points[lines] * level;
    }
    
    // --- ATUALIZADO: Métodos de HighScore agora são ESTÁTICOS ---
    private static void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGHSCORE_FILE))) {
            highScore = Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
        }
    }
    private static void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGHSCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            System.err.println("Erro ao salvar o high score: " + e.getMessage());
        }
    }
    // --- FIM ---
    
    public void decrementLineClearTimer() {
        if (lineClearTimer > 0) {
            lineClearTimer--;
        }
    }
    
    // --- NOVO: Métodos de Vitória ---
    public void addWin() {
        this.wins++;
    }
    public int getWins() {
        return this.wins;
    }
    // --- FIM NOVO ---

    // --- Getters para o View e Controller ---
    public Shape.Tetrominoe shapeAt(int x, int y) {
        return boardGrid[y * BOARD_WIDTH + x];
    }
    public boolean isStarted() { return isStarted; }
    public boolean isPaused() { return isPaused; }
    public boolean isGameOver() { return isGameOver; }
    public boolean isGhostPieceEnabled() { return isGhostPieceEnabled; }
    public int getScore() { return score; }
    
    // HighScore agora é estático
    public static int getHighScore() { return highScore; } 
    
    public int getLevel() { return level; }
    public int getLinesCleared() { return linesCleared; }
    public Piece getCurrentPiece() { return currentPiece; }
    public Piece getNextPiece() { return nextPiece; }
    public boolean isAnimatingLineClear() { return isAnimatingLineClear; }
    public List<Integer> getLinesBeingCleared() { return linesBeingCleared; }
    public int getLineClearTimer() { return lineClearTimer; }
    public int getTotalPieces() { return totalPieces; }
    public int getTetrisCount() { return tetrisCount; }
    
    // --- Getters/Setters de Lixo ---
    public void addIncomingGarbage(int lines) { this.incomingGarbage += lines; }
    public int getOutgoingGarbage() { return this.outgoingGarbage; }
    public void clearOutgoingGarbage() { this.outgoingGarbage = 0; }
    public int getIncomingGarbage() { return this.incomingGarbage; }
}