package com.tetris.controller;

import com.tetris.model.Board;
import com.tetris.model.Theme;
import com.tetris.view.GameFrame;
import com.tetris.audio.AudioManager;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * O Controller no padrão MVC.
 * ATUALIZADO: Adiciona lógica de 'addWin()' no Game Over.
 */
public class GameController extends KeyAdapter implements ActionListener {

    // --- Enum para Modo de Jogo ---
    public enum GameMode {
        ONE_PLAYER,
        TWO_PLAYER
    }
    
    // --- Enum para Telas do Jogo ---
    public enum GameScreen {
        MAIN_MENU,
        MODE_SELECT,
        RANKING_SCREEN,
        RULES_SCREEN,
        CONTROLS_SCREEN,
        PAUSED_MAIN,
        PAUSED_CONTROLS,
        PAUSED_RULES
    }

    private static final int INITIAL_DELAY = 400;
    private static final int GAME_LOOP_DELAY = 33; 

    private final GameFrame gameFrame;
    private final Board board1; 
    private final Board board2; 
    private final Timer timer;
    private final AudioManager backgroundMusic;

    private int currentThemeIndex = 0;
    private GameMode currentGameMode = GameMode.ONE_PLAYER;
    
    // --- Variáveis de Estado do Menu ---
    private GameScreen currentScreen = GameScreen.MAIN_MENU;
    private int mainMenuSelection = 0;
    private final int MAIN_MENU_OPTIONS = 5; 
    private int modeSelectSelection = 0;
    private final int MODE_SELECT_OPTIONS = 2; 
    
    private int gameOverSelection = 0; // 0 = Reiniciar, 1 = Menu
    private final int GAMEOVER_MENU_OPTIONS = 2;
    
    private int pauseMenuSelection = 0; 
    private final int PAUSE_MENU_OPTIONS = 4;
    
    private long lastPieceMoveTime1;
    private long lastPieceMoveTime2;

    public GameController(GameFrame gameFrame, Board board1, Board board2) {
        this.gameFrame = gameFrame;
        this.board1 = board1;
        this.board2 = board2;
        
        this.timer = new Timer(GAME_LOOP_DELAY, this);
        
        this.gameFrame.getGamePanel().addKeyListener(this);
        this.gameFrame.getGamePanel().setFocusable(true);

        System.out.println("GameController: Tentando inicializar o AudioManager...");
        this.backgroundMusic = new AudioManager("src/com/tetris/audio/background-music.wav");
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
        
        boolean isGameRunning = board1.isStarted() && 
                                currentScreen != GameScreen.PAUSED_MAIN &&
                                currentScreen != GameScreen.PAUSED_CONTROLS &&
                                currentScreen != GameScreen.PAUSED_RULES;
                                
        if (isGameRunning) {
            
            long currentTime = System.currentTimeMillis();

            // --- LÓGICA DO JOGADOR 1 ---
            long delay1 = getDelayForLevel(board1);
            handlePlayerLogic(board1, currentTime, lastPieceMoveTime1, delay1);
            if (currentTime - lastPieceMoveTime1 > delay1) {
                lastPieceMoveTime1 = currentTime;
            }

            // --- LÓGICA DO JOGADOR 2 ---
            if (currentGameMode == GameMode.TWO_PLAYER) {
                long delay2 = getDelayForLevel(board2);
                handlePlayerLogic(board2, currentTime, lastPieceMoveTime2, delay2);
                if (currentTime - lastPieceMoveTime2 > delay2) {
                    lastPieceMoveTime2 = currentTime;
                }
            }
            
            // --- LÓGICA DE LIXO ---
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
                    
                    if (backgroundMusic != null) {
                        backgroundMusic.stopMusic();
                    }
                    gameOverSelection = 0;
                    
                    // --- LÓGICA DE VITÓRIA (Modo 2P) ---
                    if (currentGameMode == GameMode.TWO_PLAYER) {
                        if (board1.isGameOver() && !board2.isGameOver()) {
                            board2.addWin(); // P2 vence
                        } else if (board2.isGameOver() && !board1.isGameOver()) {
                            board1.addWin(); // P1 vence
                        }
                        // Se ambos perdem ao mesmo tempo, é empate, ninguém ganha.
                    }
                    // --- FIM ---
                }
            }
        }
        
        updateView();
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

        if (board.isStarted()) {
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
        
        Theme currentTheme = Theme.AVAILABLE_THEMES[currentThemeIndex];
        
        gameFrame.getOverlayPanel().updateTheme(currentTheme);

        gameFrame.getOverlayPanel().updateMenuState(
            board1, 
            board2, 
            currentGameMode, 
            currentScreen, 
            mainMenuSelection, 
            modeSelectSelection,
            gameOverSelection,
            pauseMenuSelection 
        );

        gameFrame.getGamePanel().updateTheme(currentTheme);
        gameFrame.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keycode = e.getKeyCode();
        
        boolean isGameActive = board1.isStarted() || board2.isStarted();
        
        if (isGameActive) {
            
            boolean isGameOver;
            if (currentGameMode == GameMode.ONE_PLAYER) {
                isGameOver = board1.isGameOver();
            } else {
                isGameOver = board1.isGameOver() || board2.isGameOver();
            }
            
            if (isGameOver) {
                handleGameOverKeys(keycode);
            } else if (currentScreen == GameScreen.PAUSED_MAIN ||
                       currentScreen == GameScreen.PAUSED_CONTROLS ||
                       currentScreen == GameScreen.PAUSED_RULES) {
                handlePausedKeys(keycode);
            } else {
                handleGameKeys(keycode);
            }
        } else {
            handleMenuKeys(keycode);
        }
        
        updateView();
    }

    /**
     * Helper para iniciar o jogo com o modo selecionado.
     */
    private void startGame(GameMode mode) {
        currentGameMode = mode;
        
        gameFrame.getGamePanel().setMode(currentGameMode); 
        gameFrame.packAndCenter();
        
        board1.start();
        if (currentGameMode == GameMode.TWO_PLAYER) {
            board2.start();
        }

        if (backgroundMusic != null) {
            backgroundMusic.playMusic();
        }

        if (!timer.isRunning()) {
            timer.start();
        }
        long startTime = System.currentTimeMillis();
        lastPieceMoveTime1 = startTime; 
        lastPieceMoveTime2 = startTime;
        
        currentScreen = null; 
    }

    /**
     * Lida com teclas quando o jogo ACABOU.
     */
    private void handleGameOverKeys(int keycode) {
        if (keycode == KeyEvent.VK_UP || keycode == KeyEvent.VK_W) {
            gameOverSelection = (gameOverSelection - 1 + GAMEOVER_MENU_OPTIONS) % GAMEOVER_MENU_OPTIONS;
        }
        if (keycode == KeyEvent.VK_DOWN || keycode == KeyEvent.VK_S) {
            gameOverSelection = (gameOverSelection + 1) % GAMEOVER_MENU_OPTIONS;
        }
        
        if (keycode == KeyEvent.VK_ENTER) {
            if (gameOverSelection == 0) {
                // 0 = Reiniciar
                startGame(currentGameMode); 
            } else {
                // 1 = Voltar ao Menu
                goToMenu();
            }
        }
    }

    /**
     * Lida com teclas quando o jogo está PAUSADO.
     */
    private void handlePausedKeys(int keycode) {
        
        switch (currentScreen) {
            
            case PAUSED_MAIN: 
                if (keycode == KeyEvent.VK_P) { 
                    unpauseGame();
                    return;
                }
                if (keycode == KeyEvent.VK_UP || keycode == KeyEvent.VK_W) {
                    pauseMenuSelection = (pauseMenuSelection - 1 + PAUSE_MENU_OPTIONS) % PAUSE_MENU_OPTIONS;
                }
                if (keycode == KeyEvent.VK_DOWN || keycode == KeyEvent.VK_S) {
                    pauseMenuSelection = (pauseMenuSelection + 1) % PAUSE_MENU_OPTIONS;
                }
                if (keycode == KeyEvent.VK_ENTER) {
                    switch (pauseMenuSelection) {
                        case 0: unpauseGame(); break; // Voltar ao Jogo
                        case 1: currentScreen = GameScreen.PAUSED_CONTROLS; break; // Controles
                        case 2: currentScreen = GameScreen.PAUSED_RULES; break; // Regras
                        case 3: goToMenu(); break; // Sair para o Menu
                    }
                }
                break;
                
            case PAUSED_CONTROLS:
            case PAUSED_RULES:
                if (keycode == KeyEvent.VK_ENTER || keycode == KeyEvent.VK_ESCAPE || keycode == KeyEvent.VK_BACK_SPACE) {
                    currentScreen = GameScreen.PAUSED_MAIN;
                }
                break;
        }
    }
    
    /**
     * NOVO: Helper para despausar o jogo.
     */
    private void unpauseGame() {
        currentScreen = null; 
        if (backgroundMusic != null) {
            backgroundMusic.playMusic();
        }
        long currentTime = System.currentTimeMillis();
        lastPieceMoveTime1 = currentTime;
        lastPieceMoveTime2 = currentTime;
    }
    
    /**
     * NOVO: Helper centralizado para voltar ao menu
     */
    private void goToMenu() {
        if (backgroundMusic != null) {
            backgroundMusic.stopMusic();
        }
        
        board1.resetForMenu();
        board2.resetForMenu();
        
        gameFrame.getGamePanel().setMode(GameController.GameMode.TWO_PLAYER);
        gameFrame.packAndCenter(); 
        gameFrame.getGamePanel().setMode(GameController.GameMode.ONE_PLAYER); 

        currentScreen = GameScreen.MAIN_MENU;
        mainMenuSelection = 0;
    }

    /**
     * Lida com teclas quando o jogo está ATIVO (não pausado, não game over).
     */
    private void handleGameKeys(int keycode) {
        
        if (keycode == KeyEvent.VK_T) {
            currentThemeIndex = (currentThemeIndex + 1) % Theme.AVAILABLE_THEMES.length;
            return;
        }
        if (keycode == KeyEvent.VK_G) {
            board1.toggleGhostPiece();
            if (currentGameMode == GameMode.TWO_PLAYER) board2.toggleGhostPiece();
            return;
        }
        if (keycode == KeyEvent.VK_P) {
             currentScreen = GameScreen.PAUSED_MAIN;
             pauseMenuSelection = 0; 
             if (backgroundMusic != null) {
                 backgroundMusic.stopMusic();
             }
             return;
        }
        
        // --- Controles de Jogo ---
        boolean p1_canPlay = !board1.isAnimatingLineClear();
        boolean p2_canPlay = (currentGameMode == GameMode.TWO_PLAYER) && !board2.isAnimatingLineClear();

        // Modo 1P: Controles Padrão
        if (currentGameMode == GameMode.ONE_PLAYER && p1_canPlay) {
            switch (keycode) {
                case KeyEvent.VK_LEFT: board1.moveLeft(); break;
                case KeyEvent.VK_RIGHT: board1.moveRight(); break;
                case KeyEvent.VK_DOWN:
                    board1.movePieceDown();
                    lastPieceMoveTime1 = System.currentTimeMillis();
                    break;
                case KeyEvent.VK_UP: board1.rotateRight(); break;
                case KeyEvent.VK_Z: board1.rotateLeft(); break;
                case KeyEvent.VK_SPACE:
                    board1.dropDown();
                    lastPieceMoveTime1 = System.currentTimeMillis();
                    break;
            }
        }
        
        // Modo 2P: Controles Separados
        if (currentGameMode == GameMode.TWO_PLAYER) {
            switch (keycode) {
                // P1 (Esquerda)
                case KeyEvent.VK_A: if (p1_canPlay) board1.moveLeft(); break;
                case KeyEvent.VK_D: if (p1_canPlay) board1.moveRight(); break;
                case KeyEvent.VK_S: 
                    if (p1_canPlay) {
                        board1.movePieceDown();
                        lastPieceMoveTime1 = System.currentTimeMillis();
                    }
                    break;
                case KeyEvent.VK_W: if (p1_canPlay) board1.rotateRight(); break;
                case KeyEvent.VK_Q: if (p1_canPlay) board1.rotateLeft(); break;
                case KeyEvent.VK_SPACE: 
                    if (p1_canPlay) {
                        board1.dropDown();
                        lastPieceMoveTime1 = System.currentTimeMillis();
                    }
                    break;

                // P2 (Direita)
                case KeyEvent.VK_LEFT: if (p2_canPlay) board2.moveLeft(); break;
                case KeyEvent.VK_RIGHT: if (p2_canPlay) board2.moveRight(); break;
                case KeyEvent.VK_DOWN: 
                    if (p2_canPlay) {
                        board2.movePieceDown();
                        lastPieceMoveTime2 = System.currentTimeMillis();
                    }
                    break;
                case KeyEvent.VK_UP: if (p2_canPlay) board2.rotateRight(); break;
                case KeyEvent.VK_M: if (p2_canPlay) board2.rotateLeft(); break;
                case KeyEvent.VK_N: 
                    if (p2_canPlay) {
                        board2.dropDown();
                        lastPieceMoveTime2 = System.currentTimeMillis();
                    }
                    break;
            }
        }
    }

    /**
     * Lida com teclas quando estamos nos MENUS (pré-jogo).
     */
    private void handleMenuKeys(int keycode) {
        
        switch (currentScreen) {
            
            case MAIN_MENU:
                if (keycode == KeyEvent.VK_UP || keycode == KeyEvent.VK_W) {
                    mainMenuSelection = (mainMenuSelection - 1 + MAIN_MENU_OPTIONS) % MAIN_MENU_OPTIONS;
                }
                if (keycode == KeyEvent.VK_DOWN || keycode == KeyEvent.VK_S) {
                    mainMenuSelection = (mainMenuSelection + 1) % MAIN_MENU_OPTIONS;
                }
                if (keycode == KeyEvent.VK_ENTER) {
                    switch (mainMenuSelection) {
                        case 0: currentScreen = GameScreen.MODE_SELECT; break; // Jogar
                        case 1: currentScreen = GameScreen.RANKING_SCREEN; break; // Ranking
                        case 2: currentScreen = GameScreen.RULES_SCREEN; break; // Regras
                        case 3: currentScreen = GameScreen.CONTROLS_SCREEN; break; // Controles
                        case 4: System.exit(0); break; // Sair
                    }
                }
                break;
                
            case MODE_SELECT:
                if (keycode == KeyEvent.VK_UP || keycode == KeyEvent.VK_W) {
                    modeSelectSelection = (modeSelectSelection - 1 + MODE_SELECT_OPTIONS) % MODE_SELECT_OPTIONS;
                }
                if (keycode == KeyEvent.VK_DOWN || keycode == KeyEvent.VK_S) {
                    modeSelectSelection = (modeSelectSelection + 1) % MODE_SELECT_OPTIONS;
                }
                if (keycode == KeyEvent.VK_ENTER) {
                    if (modeSelectSelection == 0) {
                        startGame(GameMode.ONE_PLAYER);
                    } else {
                        startGame(GameMode.TWO_PLAYER);
                    }
                }
                if (keycode == KeyEvent.VK_ESCAPE || keycode == KeyEvent.VK_BACK_SPACE) {
                    currentScreen = GameScreen.MAIN_MENU;
                }
                break;
                
            case RANKING_SCREEN:
            case RULES_SCREEN:
            case CONTROLS_SCREEN:
                if (keycode == KeyEvent.VK_ENTER || keycode == KeyEvent.VK_ESCAPE || keycode == KeyEvent.VK_BACK_SPACE) {
                    currentScreen = GameScreen.MAIN_MENU;
                }
                break;
            
            // Os casos de PAUSE são tratados em handlePausedKeys
        }
    }

    private int getDelayForLevel(Board board) {
        return Math.max(100, INITIAL_DELAY - (board.getLevel() - 1) * 30);
    }
}