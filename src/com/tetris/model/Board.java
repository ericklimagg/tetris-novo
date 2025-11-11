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
 * Representa o estado completo de um único tabuleiro de jogo.
 * Inclui a grade de peças, a peça atual, pontuação, nível e
 * lógica de "garbage" (lixo) para o modo 2P.
 * Esta classe não tem conhecimento do banco de dados; o highscore
 * é gerenciado externamente pelo GameController.
 */
public class Board {

    // --- Constantes do Jogo ---
    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 20;
    private static final int LEVEL_UP_LINES = 10;
    private static final int LINE_CLEAR_ANIMATION_TICKS = 8; // Duração da animação de linha

    // --- Estado da Instância ---
    private boolean isStarted = false;
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private boolean isGhostPieceEnabled = true;

    // --- Estatísticas da Partida ---
    private int score = 0;
    private int level = 1;
    private int linesCleared = 0;
    private int totalPieces = 0;
    private int tetrisCount = 0; // Quantidade de "Tetris" (4 linhas)
    private int wins = 0; // Contagem de vitórias (relevante para modo 2P)
    
    // --- Variáveis de Lixo (Garbage) ---
    private int incomingGarbage = 0; // Lixo a ser recebido
    private int outgoingGarbage = 0; // Lixo a ser enviado
    
    // (O garbageHoleRandom não é mais necessário se as linhas são sólidas)
    // private Random garbageHoleRandom = new Random(); 

    // --- Estado das Peças ---
    private Piece currentPiece;
    private Piece nextPiece;
    private Shape.Tetrominoe[] boardGrid; // A grade principal do jogo

    // --- Variáveis de Animação ---
    private boolean isAnimatingLineClear = false;
    private List<Integer> linesBeingCleared = new ArrayList<>();
    private int lineClearTimer = 0;


    public Board() {
        boardGrid = new Shape.Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];
        currentPiece = new Piece();
        nextPiece = new Piece();
        clearBoard();
    }

    /**
     * Reseta o tabuleiro para um estado limpo, usado ao voltar para o menu.
     */
    public void resetForMenu() {
        isStarted = false;
        isGameOver = false;
        isPaused = false;
        
        // --- CORREÇÃO ---
        // Zera as vitórias da sessão ao voltar para o menu
        resetWins(); 
        
        clearBoard();
    }
    
    /**
     * NOVO MÉTODO: Zera o contador de vitórias da sessão.
     * Chamado ao voltar ao menu ou iniciar uma nova sessão 2P.
     */
    public void resetWins() {
        this.wins = 0;
    }

    /**
     * Inicia (ou reinicia) uma nova partida neste tabuleiro.
     */
    public void start() {
        isStarted = true;
        isGameOver = false;
        isPaused = false;
        isAnimatingLineClear = false; 
        linesBeingCleared.clear();    
        
        // Reseta estatísticas da partida
        score = 0;
        level = 1;
        linesCleared = 0; 
        totalPieces = 0;
        tetrisCount = 0;
        
        // --- INÍCIO DA CORREÇÃO ---
        // A linha 'wins = 0;' foi REMOVIDA daqui.
        // Agora o 'wins' persiste entre "Reiniciar".
        // --- FIM DA CORREÇÃO ---
        
        incomingGarbage = 0;
        outgoingGarbage = 0;
        
        clearBoard();
        
        // Prepara a primeira peça
        nextPiece.setRandomShape();
        newPiece();
    }

    /**
     * Preenche a grade do tabuleiro com 'NoShape'.
     */
    private void clearBoard() {
        for (int i = 0; i < BOARD_WIDTH * BOARD_HEIGHT; i++) {
            boardGrid[i] = Shape.Tetrominoe.NoShape;
        }
    }

    /**
     * Gera uma nova peça no topo do tabuleiro.
     * Primeiro, aplica qualquer "lixo" pendente.
     */
    public void newPiece() {
        // Aplica lixo pendente antes de gerar a nova peça
        if (this.incomingGarbage > 0) {
            applyGarbageLines(this.incomingGarbage);
            this.incomingGarbage = 0;
        }

        currentPiece = nextPiece;
        nextPiece = new Piece(); 
        nextPiece.setRandomShape(); 
        
        // Define a posição inicial da peça
        currentPiece.setX(BOARD_WIDTH / 2);
        currentPiece.setY(BOARD_HEIGHT - 1 + currentPiece.minY());
        
        totalPieces++; 

        // Verifica se a nova peça colide imediatamente (Game Over)
        if (!tryMove(currentPiece, currentPiece.getX(), currentPiece.getY())) {
            isGameOver = true;
            currentPiece.setShape(Shape.Tetrominoe.NoShape);
        }
    }

    /**
     * Tenta mover a peça para uma nova posição (newX, newY).
     * @return true se o movimento for bem-sucedido, false se houver colisão.
     */
    private boolean tryMove(Piece piece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + piece.x(i);
            int y = newY - piece.y(i);

            // Verifica colisão com as bordas do tabuleiro
            if (x < 0 || x >= BOARD_WIDTH || y < 0) {
                return false;
            }

            // Verifica colisão com peças já existentes na grade
            if (y < BOARD_HEIGHT && shapeAt(x, y) != Shape.Tetrominoe.NoShape) {
                return false;
            }
        }

        // Se não houve colisão, atualiza a posição da peça
        currentPiece = piece;
        currentPiece.setX(newX);
        currentPiece.setY(newY);
        return true;
    }

    /**
     * "Fixa" a peça atual na grade do tabuleiro após ela colidir com o chão.
     */
    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = currentPiece.getX() + currentPiece.x(i);
            int y = currentPiece.getY() - currentPiece.y(i);
            if (y >= 0 && y < BOARD_HEIGHT) {
                boardGrid[y * BOARD_WIDTH + x] = currentPiece.getShape();
            }
        }
        
        detectFullLines(); 
        
        // Gera uma nova peça se o jogo não acabou e não estamos animando
        if (!isGameOver && !isAnimatingLineClear) {
            newPiece();
        }
    }

    /**
     * Verifica o tabuleiro por linhas completas e as marca para remoção.
     */
    private void detectFullLines() {
        linesBeingCleared.clear();

        // Itera de baixo para cima
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;
            
            // (Esta é a correção do bug anterior: não limpar lixo)
            for (int j = 0; j < BOARD_WIDTH; j++) {
                Shape.Tetrominoe shape = shapeAt(j, i);
                
                if (shape == Shape.Tetrominoe.NoShape || 
                    shape == Shape.Tetrominoe.GarbageShape) { 
                    
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                linesBeingCleared.add(i);
            }
        }

        // Se linhas foram encontradas, inicia a animação
        if (!linesBeingCleared.isEmpty()) {
            isAnimatingLineClear = true;
            lineClearTimer = LINE_CLEAR_ANIMATION_TICKS;
        }
    }

    /**
     * Remove as linhas marcadas (após a animação) e "desce" as linhas acima.
     */
    public void finishLineClear() {
        if (linesBeingCleared.isEmpty()) {
            return;
        }

        // Remove as linhas completas e move as de cima para baixo
        for (int row : linesBeingCleared) {
            for (int k = row; k < BOARD_HEIGHT - 1; k++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    boardGrid[k * BOARD_WIDTH + j] = shapeAt(j, k + 1);
                }
            }
            // Preenche a linha superior com vazio
            for (int j = 0; j < BOARD_WIDTH; j++) {
                boardGrid[(BOARD_HEIGHT - 1) * BOARD_WIDTH + j] = Shape.Tetrominoe.NoShape;
            }
        }

        int numFullLines = linesBeingCleared.size();
        
        // Define quanto "lixo" (garbage) será enviado ao oponente
        if (numFullLines == 2) {
            outgoingGarbage = 1;
        } else if (numFullLines == 3) {
            outgoingGarbage = 2;
        } else if (numFullLines == 4) {
            outgoingGarbage = 4;
            tetrisCount++; // Incrementa contagem de "Tetris"
        }

        updateScore(numFullLines);
        linesCleared += numFullLines;
        
        // Verifica se subiu de nível
        if (linesCleared / LEVEL_UP_LINES >= level) {
            level++;
        }

        // Finaliza o estado de animação
        linesBeingCleared.clear();
        isAnimatingLineClear = false;
        lineClearTimer = 0;
    }

    /**
     * Adiciona 'lines' de lixo na base do tabuleiro, empurrando tudo para cima.
     */
    private void applyGarbageLines(int lines) {
        // Verifica se o lixo recebido causará Game Over imediato
        for (int y = BOARD_HEIGHT - lines; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (shapeAt(x, y) != Shape.Tetrominoe.NoShape) {
                    isGameOver = true;
                    return;
                }
            }
        }

        // Move as peças existentes para cima
        for (int y = BOARD_HEIGHT - 1; y >= lines; y--) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                boardGrid[y * BOARD_WIDTH + x] = shapeAt(x, y - lines);
            }
        }

        // (Esta é a correção do bug anterior: lixo sólido)
        for (int y = 0; y < lines; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                boardGrid[y * BOARD_WIDTH + x] = Shape.Tetrominoe.GarbageShape;
            }
        }
        
        // Empurra a peça atual para cima junto com o resto
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
    
    /**
     * "Hard drop" - Joga a peça instantaneamente para a posição mais baixa possível.
     */
    public void dropDown() {
        int newY = getGhostPieceY();
        tryMove(currentPiece, currentPiece.getX(), newY);
        pieceDropped();
    }
    
    /**
     * "Soft drop" - Move a peça um passo para baixo.
     */
    public void movePieceDown() {
        if (!tryMove(currentPiece, currentPiece.getX(), currentPiece.getY() - 1)) {
            pieceDropped(); // Se não puder mover, fixa a peça
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
    
    /**
     * Calcula a coordenada Y onde a peça fantasma (prévia) deve ser desenhada.
     */
    public int getGhostPieceY() {
        int y = currentPiece.getY();
        while (true) {
            if (!canMoveTo(currentPiece, currentPiece.getX(), y - 1)) {
                return y;
            }
            y--;
        }
    }
    
    /**
     * Verifica se uma peça *poderia* se mover para uma posição, sem movê-la.
     * Usado pela lógica da peça fantasma e rotação.
     */
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
        // Pontuação baseia-se no número de linhas e no nível atual
        int[] points = {0, 40, 100, 300, 1200}; // 0, 1, 2, 3, 4 (Tetris)
        score += points[lines] * level;
    }
    
    public void decrementLineClearTimer() {
        if (lineClearTimer > 0) {
            lineClearTimer--;
        }
    }
    
    // --- Métodos de Vitória (para 2P) ---
    
    public void addWin() {
        this.wins++;
    }
    public int getWins() {
        return this.wins;
    }

    // --- Getters para o View e Controller ---
    
    public Shape.Tetrominoe shapeAt(int x, int y) {
        return boardGrid[y * BOARD_WIDTH + x];
    }
    public boolean isStarted() { return isStarted; }
    public boolean isPaused() { return isPaused; }
    public boolean isGameOver() { return isGameOver; }
    public boolean isGhostPieceEnabled() { return isGhostPieceEnabled; }
    public int getScore() { return score; }
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