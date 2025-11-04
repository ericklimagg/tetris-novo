package com.tetris.controller;

import com.tetris.audio.AudioManager;
import com.tetris.model.Board;
import com.tetris.model.Theme;
import com.tetris.view.GameFrame;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * O Controller no padrão MVC.
 * ETAPA 7: Correções Finais (Menu, Controles, Reinício).
 */
public class GameController extends KeyAdapter implements ActionListener {

    public enum GameMode {
        ONE_PLAYER,
        TWO_PLAYER
    }

    private static final int INITIAL_DELAY = 400;
    private static final int GAME_LOOP_DELAY = 33; 

    private final GameFrame gameFrame;
    private final Board board1; 
    private final Board board2; 
    private final Timer timer;
    private final AudioManager audioManager;
    private final Set<Integer> pressedKeys;
    private final Set<Integer> singlePressKeys;

    private int currentThemeIndex = 0;
    private GameMode currentGameMode = GameMode.ONE_PLAYER;
    private int menuSelection = 0; // 0 = 1P, 1 = 2P
    
    private long lastPieceMoveTime1;
    private long lastPieceMoveTime2;

    public GameController(GameFrame gameFrame, Board board1, Board board2) {
        this.gameFrame = gameFrame;
        this.board1 = board1;
        this.board2 = board2;
        
        this.timer = new Timer(GAME_LOOP_DELAY, this);
        this.pressedKeys = new HashSet<>();
        this.singlePressKeys = new HashSet<>();
        
        this.audioManager = new AudioManager("/com/tetris/audio/background-music.wav");
        this.gameFrame.getGamePanel().addKeyListener(this);
        this.gameFrame.getGamePanel().setFocusable(true);
    }

    public void start() {
        long startTime = System.currentTimeMillis();
        lastPieceMoveTime1 = startTime; 
        lastPieceMoveTime2 = startTime; 
        timer.start();
        updateView(); 
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        long currentTime = System.currentTimeMillis();

        // --- LÓGICA DO JOGADOR 1 (Sempre corre) ---
        long delay1 = getDelayForLevel(board1);
        handlePlayerLogic(board1, currentTime, lastPieceMoveTime1, delay1);
        if (currentTime - lastPieceMoveTime1 > delay1) {
            lastPieceMoveTime1 = currentTime;
        }

        // --- LÓGICA DO JOGADOR 2 (Opcional) ---
        if (currentGameMode == GameMode.TWO_PLAYER) {
            long delay2 = getDelayForLevel(board2);
            handlePlayerLogic(board2, currentTime, lastPieceMoveTime2, delay2);
            if (currentTime - lastPieceMoveTime2 > delay2) {
                lastPieceMoveTime2 = currentTime;
            }
        }
        
        // --- LÓGICA DE LIXO (Opcional) ---
        if (currentGameMode == GameMode.TWO_PLAYER) {
            int p1_garbage = board1.getOutgoingGarbage();
            int p2_garbage = board2.getOutgoingGarbage();

            if (p1_garbage > 0 || p2_garbage > 0) {
                if (p1_garbage > p2_garbage) {
                    board2.addIncomingGarbage(p1_garbage - p2_garbage);
                } else if (p2_garbage > p1_garbage) {
                    board1.addIncomingGarbage(p2_garbage - p1_garbage);
                }
                board1.clearOutgoingGarbage();
                board2.clearOutgoingGarbage();
            }
        }

        // --- LÓGICA DE GAME OVER ---
        boolean p1_over = board1.isGameOver();
        boolean p2_over = (currentGameMode == GameMode.ONE_PLAYER) || board2.isGameOver(); 

        if (p1_over && p2_over) {
            if (timer.isRunning()) {
                timer.stop();
                audioManager.stopMusic();
            }
        }
        
        handleInput();
        updateView();
    }

    private void handleInput() {
        boolean isGameActive = board1.isStarted() || board2.isStarted();

        // --- LÓGICA DE REINÍCIO (ENTER) ---
        boolean p1_finished = !board1.isStarted() || board1.isGameOver();
        boolean p2_finished = (currentGameMode == GameMode.ONE_PLAYER) || !board2.isStarted() || board2.isGameOver();

        if (p1_finished && p2_finished && isSinglePress(KeyEvent.VK_ENTER)) {
            currentGameMode = (menuSelection == 0) ? GameMode.ONE_PLAYER : GameMode.TWO_PLAYER;
            gameFrame.getGamePanel().setMode(currentGameMode);
            gameFrame.packAndCenter();

            board1.start();
            if (currentGameMode == GameMode.TWO_PLAYER) {
                board2.start();
            }

            if (!timer.isRunning()) {
                timer.start();
            }
            long startTime = System.currentTimeMillis();
            lastPieceMoveTime1 = startTime;
            lastPieceMoveTime2 = startTime;
            audioManager.playMusic();
            return;
        }

        // --- LÓGICA DE PRÉ-JOGO (Seleção de Modo) ---
        if (!isGameActive) {
            if (isSinglePress(KeyEvent.VK_UP) || isSinglePress(KeyEvent.VK_W)) {
                menuSelection = 0; // 1P
            }
            if (isSinglePress(KeyEvent.VK_DOWN) || isSinglePress(KeyEvent.VK_S)) {
                menuSelection = 1; // 2P
            }
        }

        // --- TECLAS GLOBAIS (Pausa, Tema, Ghost) ---
        if (isSinglePress(KeyEvent.VK_T)) {
            currentThemeIndex = (currentThemeIndex + 1) % Theme.AVAILABLE_THEMES.length;
        }
        
        if (isSinglePress(KeyEvent.VK_G)) {
            board1.toggleGhostPiece();
            if (currentGameMode == GameMode.TWO_PLAYER) board2.toggleGhostPiece();
        }
        
        if (isSinglePress(KeyEvent.VK_P)) {
            if (isGameActive && (!board1.isGameOver() || (currentGameMode == GameMode.TWO_PLAYER && !board2.isGameOver()))) {
                 board1.togglePause();
                 board2.togglePause(); 
                
                 if (!board1.isPaused()) {
                    long currentTime = System.currentTimeMillis();
                    lastPieceMoveTime1 = currentTime;
                    lastPieceMoveTime2 = currentTime;
                 }
            }
        }

        // --- Controles de Jogo ---
        boolean p1_canPlay = board1.isStarted() && !board1.isGameOver() && !board1.isPaused() && !board1.isAnimatingLineClear();
        boolean p2_canPlay = (currentGameMode == GameMode.TWO_PLAYER) && 
                             board2.isStarted() && !board2.isGameOver() && !board2.isPaused() && !board2.isAnimatingLineClear();

        // Modo 1P: Controles Padrão
        if (currentGameMode == GameMode.ONE_PLAYER && p1_canPlay) {
            if (pressedKeys.contains(KeyEvent.VK_LEFT)) board1.moveLeft();
            if (pressedKeys.contains(KeyEvent.VK_RIGHT)) board1.moveRight();
            if (pressedKeys.contains(KeyEvent.VK_DOWN)) {
                board1.movePieceDown();
                lastPieceMoveTime1 = System.currentTimeMillis();
            }
            if (isSinglePress(KeyEvent.VK_UP)) board1.rotateRight();
            if (isSinglePress(KeyEvent.VK_Z)) board1.rotateLeft();
            if (isSinglePress(KeyEvent.VK_SPACE)) {
                board1.dropDown();
                lastPieceMoveTime1 = System.currentTimeMillis();
            }
        }

        // Modo 2P: Controles Separados
        if (currentGameMode == GameMode.TWO_PLAYER) {
            // --- Jogador 1 (W, A, S, D, Q, ESPAÇO) ---
            if (p1_canPlay) {
                if (pressedKeys.contains(KeyEvent.VK_A)) board1.moveLeft();
                if (pressedKeys.contains(KeyEvent.VK_D)) board1.moveRight();
                if (pressedKeys.contains(KeyEvent.VK_S)) {
                    board1.movePieceDown();
                    lastPieceMoveTime1 = System.currentTimeMillis();
                }
                if (isSinglePress(KeyEvent.VK_W)) board1.rotateRight();
                if (isSinglePress(KeyEvent.VK_Q)) board1.rotateLeft();
                if (isSinglePress(KeyEvent.VK_SPACE)) {
                    board1.dropDown();
                    lastPieceMoveTime1 = System.currentTimeMillis();
                }
            }

            // --- Jogador 2 (SETAS, UP, M, N) ---
            if (p2_canPlay) {
                if (pressedKeys.contains(KeyEvent.VK_LEFT)) board2.moveLeft();
                if (pressedKeys.contains(KeyEvent.VK_RIGHT)) board2.moveRight();
                if (pressedKeys.contains(KeyEvent.VK_DOWN)) {
                    board2.movePieceDown();
                    lastPieceMoveTime2 = System.currentTimeMillis();
                }
                if (isSinglePress(KeyEvent.VK_UP)) board2.rotateRight();
                if (isSinglePress(KeyEvent.VK_M)) board2.rotateLeft();
                if (isSinglePress(KeyEvent.VK_N)) {
                    board2.dropDown();
                    lastPieceMoveTime2 = System.currentTimeMillis();
                }
            }
        }
    }

    /**
     * Helper que executa a lógica de jogo para um único jogador.
     */
    private void handlePlayerLogic(Board board, long currentTime, long lastMoveTime, long delay) {
        if (board.isGameOver()) {
            return;
        }
        
        if (board.isAnimatingLineClear()) {
            board.decrementLineClearTimer();

            if (board.getLineClearTimer() <= 0) {
                board.finishLineClear();
                if (!board.isGameOver()) {
                    board.newPiece();
                }
            }
            return;
        }

        if (board.isStarted() && !board.isPaused()) {
            if (currentTime - lastMoveTime > delay) {
                board.movePieceDown();
            }
        }
    }


    private void updateView() {
        gameFrame.getGamePanel().getBoardPanel1().updateBoard(board1);
        gameFrame.getGamePanel().getInfoPanel1().updateInfo(board1);
        gameFrame.getGamePanel().getGarbageBar1().updateBoard(board1);
        
        gameFrame.getGamePanel().getBoardPanel2().updateBoard(board2);
        gameFrame.getGamePanel().getInfoPanel2().updateInfo(board2);
        gameFrame.getGamePanel().getGarbageBar2().updateBoard(board2);

        // Passa a seleção do menu para o Overlay
        gameFrame.getOverlayPanel().updateBoards(board1, board2, currentGameMode, menuSelection);

        Theme currentTheme = Theme.AVAILABLE_THEMES[currentThemeIndex];
        gameFrame.getGamePanel().updateTheme(currentTheme);

        gameFrame.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
        singlePressKeys.remove(e.getKeyCode());
    }

    private boolean isSinglePress(int keyCode) {
        if (pressedKeys.contains(keyCode) && !singlePressKeys.contains(keyCode)) {
            singlePressKeys.add(keyCode);
            return true;
        }
        return false;
    }

    private int getDelayForLevel(Board board) {
        return Math.max(100, INITIAL_DELAY - (board.getLevel() - 1) * 30);
    }
}