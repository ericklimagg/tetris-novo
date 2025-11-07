package com.tetris.view;

import com.tetris.controller.GameController; 
import com.tetris.controller.GameController.GameScreen; 
import com.tetris.model.Board;
import com.tetris.model.Theme; 

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D; 
import java.awt.RenderingHints; 

/**
 * Um painel transparente que desenha os 'overlays' (telas por cima do jogo).
 * ATUALIZADO: Adiciona explicação de Nível nas Regras.
 */
public class OverlayPanel extends JPanel {

    private Board board1;
    private Board board2;
    private GameController.GameMode currentGameMode;
    private Theme currentTheme; 
    
    // --- Variáveis de Estado do Menu ---
    private GameScreen currentScreen;
    private int mainMenuSelection;
    private int modeSelectSelection;
    private int gameOverSelection; 
    private int pauseMenuSelection; 

    public OverlayPanel() {
        setOpaque(false); // Este painel é transparente por padrão
    }
    
    public void updateTheme(Theme theme) {
        this.currentTheme = theme;
    }

    public void updateMenuState(Board board1, Board board2, GameController.GameMode mode, 
                                GameScreen screen, int mainSelection, int modeSelection,
                                int gameOverSelection, int pauseSelection) { 
        this.board1 = board1;
        this.board2 = board2;
        this.currentGameMode = mode;
        this.currentScreen = screen;
        this.mainMenuSelection = mainSelection;
        this.modeSelectSelection = modeSelection;
        this.gameOverSelection = gameOverSelection;
        this.pauseMenuSelection = pauseSelection; 
    }
    
    public void updateBoards(Board board1, Board board2) {
         this.board1 = board1;
         this.board2 = board2;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (board1 == null || board2 == null || currentTheme == null) {
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean isGameActive = board1.isStarted() || board2.isStarted();
        boolean isGameOver = board1.isGameOver() || (currentGameMode == GameController.GameMode.TWO_PLAYER && board2.isGameOver());
        boolean isPaused = (currentScreen == GameScreen.PAUSED_MAIN ||
                            currentScreen == GameScreen.PAUSED_CONTROLS ||
                            currentScreen == GameScreen.PAUSED_RULES);

        if (isGameActive) {
            // --- Jogo está ATIVO ---
            if (isGameOver) {
                g.setColor(new Color(0, 0, 0, 200)); 
                g.fillRect(0, 0, getWidth(), getHeight());
                drawGameOver(g2d); 
            } else if (isPaused) {
                g.setColor(new Color(0, 0, 0, 180)); 
                g.fillRect(0, 0, getWidth(), getHeight());
                
                if (currentScreen == GameScreen.PAUSED_MAIN) {
                    drawPausedScreen(g2d);
                } else if (currentScreen == GameScreen.PAUSED_CONTROLS) {
                    drawPausedControlsScreen(g2d);
                } else if (currentScreen == GameScreen.PAUSED_RULES) {
                    drawPausedRulesScreen(g2d);
                }
            }
            // Se o jogo estiver rodando, não desenha nada por cima
            
        } else {
            // --- Jogo NÃO está ativo (Estamos no Menu Principal) ---
            g.setColor(currentTheme.uiBackground()); // Cor sólida do tema
            g.fillRect(0, 0, getWidth(), getHeight());
            
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, getWidth(), getHeight());

            switch (currentScreen) {
                case MAIN_MENU:
                    drawStartScreen(g2d);
                    break;
                case MODE_SELECT:
                    drawModeSelectScreen(g2d);
                    break;
                case RANKING_SCREEN:
                    drawRankingScreen(g2d);
                    break;
                case RULES_SCREEN:
                    drawRulesScreen(g2d);
                    break;
                case CONTROLS_SCREEN:
                    drawControlsScreen(g2d);
                    break;
            }
        }
    }

    // --- ============ NOVO DESIGN DE MENU ============ ---
    
    private void drawMenuCard(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(new Color(20, 20, 30, 220)); 
        g.fillRoundRect(x, y, width, height, 25, 25);
        g.setColor(currentTheme.grid().brighter()); 
        g.drawRoundRect(x, y, width, height, 25, 25);
    }
    
    private void drawMenuTitle(Graphics2D g, String title) {
        g.setFont(new Font("Consolas", Font.BOLD, 72));
        g.setColor(Color.WHITE);
        drawCenteredString(g, title, getWidth() / 2, 120);
        
        g.setColor(Color.CYAN);
        g.fillRect(getWidth() / 2 - 100, 140, 200, 4);
    }
    
    private void drawFooterHint(Graphics2D g, String text) {
        g.setFont(new Font("Consolas", Font.PLAIN, 16));
        g.setColor(Color.GRAY);
        drawCenteredString(g, text, getWidth() / 2, getHeight() - 60);
    }

    private void drawStartScreen(Graphics2D g) {
        drawMenuTitle(g, "T E T R I S");
        
        int cardWidth = 350;
        int cardHeight = 280;
        int x = getWidth() / 2 - cardWidth / 2;
        int y = getHeight() / 2 - cardHeight / 2;
        
        drawMenuCard(g, x, y, cardWidth, cardHeight);
        
        g.setFont(new Font("Consolas", Font.PLAIN, 28));
        int y_menu = y + 60;
        
        String[] options = {"Jogar", "Ranking", "Regras", "Controles", "Sair"};
        boolean cursorVisible = (System.currentTimeMillis() / 400) % 2 == 0;
        String selector = cursorVisible ? ">" : " ";
        
        for (int i = 0; i < options.length; i++) {
            if (i == mainMenuSelection) {
                g.setColor(Color.YELLOW);
                drawCenteredString(g, selector + " " + options[i], getWidth() / 2, y_menu);
            } else {
                g.setColor(Color.WHITE);
                drawCenteredString(g, options[i], getWidth() / 2, y_menu);
            }
            y_menu += 45;
        }
        
        drawFooterHint(g, "(Use ↑↓ para selecionar, ENTER para confirmar)");
    }

    private void drawModeSelectScreen(Graphics2D g) {
        drawMenuTitle(g, "T E T R I S");

        int cardWidth = 350;
        int cardHeight = 150;
        int x = getWidth() / 2 - cardWidth / 2;
        int y = getHeight() / 2 - cardHeight / 2;
        
        drawMenuCard(g, x, y, cardWidth, cardHeight);
        
        g.setFont(new Font("Consolas", Font.PLAIN, 28));
        int y_menu = y + 50;

        String[] options = {"1 Jogador", "2 Jogadores"};
        boolean cursorVisible = (System.currentTimeMillis() / 400) % 2 == 0;
        String selector = cursorVisible ? ">" : " ";
        
        for (int i = 0; i < options.length; i++) {
            if (i == modeSelectSelection) {
                g.setColor(Color.YELLOW);
                drawCenteredString(g, selector + " " + options[i], getWidth() / 2, y_menu);
            } else {
                g.setColor(Color.WHITE);
                drawCenteredString(g, options[i], getWidth() / 2, y_menu);
            }
            y_menu += 45;
        }
        
        drawFooterHint(g, "(Pressione ESC para Voltar)");
    }

    private void drawRankingScreen(Graphics2D g) {
        int cardWidth = 400;
        int cardHeight = 450;
        int x = getWidth() / 2 - cardWidth / 2;
        int y = 100;
        
        drawMenuCard(g, x, y, cardWidth, cardHeight);
        
        g.setColor(Color.CYAN);
        g.setFont(new Font("Consolas", Font.BOLD, 32));
        drawCenteredString(g, "RANKING", getWidth() / 2, y + 50);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 20));
        int y_list = y + 120;
        
        drawCenteredString(g, "EM BREVE...", getWidth() / 2, y_list);
        
        g.setFont(new Font("Consolas", Font.BOLD, 16));
        g.setColor(Color.GRAY);
        y_list += 60;
        drawCenteredString(g, "Aqui você poderá ver as", getWidth() / 2, y_list);
        y_list += 25;
        drawCenteredString(g, "10 maiores pontuações", getWidth() / 2, y_list);
        y_list += 25;
        drawCenteredString(g, "salvas em arquivo ou", getWidth() / 2, y_list);
        y_list += 25;
        drawCenteredString(g, "no banco de dados.", getWidth() / 2, y_list);

        drawFooterHint(g, "(Pressione ENTER ou ESC para Voltar)");
    }

    /**
     * ATUALIZADO: Adiciona a regra de Nível.
     */
    private void drawRulesScreen(Graphics2D g) {
        int cardWidth = 700; 
        int cardHeight = 500; // <<< Aumenta a altura do card
        int x = getWidth() / 2 - cardWidth / 2;
        int y = 100;
        
        drawMenuCard(g, x, y, cardWidth, cardHeight);
        
        g.setColor(Color.CYAN);
        g.setFont(new Font("Consolas", Font.BOLD, 32));
        drawCenteredString(g, "REGRAS E PONTUAÇÃO", getWidth() / 2, y + 50);

        int y_col = y + 120;
        int x_col1 = x + 50;
        int x_col2 = x + 370;

        // --- Coluna 1: Pontuação ---
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 22));
        g.drawString("PONTUAÇÃO", x_col1, y_col);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 18));
        y_col += 40; 
        g.drawString("Pontos = Valor Base * Nível", x_col1, y_col);
        
        g.setColor(Color.CYAN);
        y_col += 40; g.drawString("1 Linha   :  40 pts", x_col1, y_col);
        y_col += 30; g.drawString("2 Linhas  : 100 pts", x_col1, y_col);
        y_col += 30; g.drawString("3 Linhas  : 300 pts", x_col1, y_col);
        g.setColor(Color.ORANGE);
        y_col += 30; g.drawString("TETRIS (4): 1200 pts", x_col1, y_col);
        
        // --- CORREÇÃO: Adiciona Regra de Nível ---
        y_col += 50; // Espaço extra
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 22));
        g.drawString("NÍVEL", x_col1, y_col);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 16));
        y_col += 30;
        String textNivel = "Você avança de nível a cada\n" +
                           "10 linhas limpas. Um nível\n" +
                           "mais alto aumenta a velocidade\n" +
                           "do jogo e seus pontos.";
        y_col = drawMultiLineString(g, textNivel, x_col1, y_col);
        // --- FIM DA CORREÇÃO ---

        // --- Coluna 2: Lixo (Garbage) ---
        y_col = y + 120; // Reseta Y
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 22));
        g.drawString("LIXO (Modo 2P)", x_col2, y_col);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 16));
        y_col += 40;
        String textLixo = "No modo 2P, limpar linhas envia\n" +
                          "'Lixo' (linhas cinzas) para o \n" +
                          "oponente. O lixo aparece na \n" +
                          "base do tabuleiro, empurrando \n" +
                          "as peças dele para cima.";
        y_col = drawMultiLineString(g, textLixo, x_col2, y_col);

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Consolas", Font.PLAIN, 18));
        y_col += 30; g.drawString("2 Linhas  -> Envia 1 Linha", x_col2, y_col);
        y_col += 30; g.drawString("3 Linhas  -> Envia 2 Linhas", x_col2, y_col);
        y_col += 30; g.drawString("TETRIS    -> Envia 4 Linhas", x_col2, y_col);

        drawFooterHint(g, "(Pressione ENTER ou ESC para Voltar)");
    }

    private void drawControlsScreen(Graphics2D g) {
        int cardWidth = 700;
        int cardHeight = 450;
        int x = getWidth() / 2 - cardWidth / 2;
        int y = 100;
        
        drawMenuCard(g, x, y, cardWidth, cardHeight);
        
        g.setColor(Color.CYAN);
        g.setFont(new Font("Consolas", Font.BOLD, 32));
        drawCenteredString(g, "CONTROLES", getWidth() / 2, y + 50);

        int y_start = y + 120;
        int x_p1 = x + 50;
        int x_p2 = x + 370;
        int y_col;

        // --- Coluna 1: Controles 1P ---
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 18));
        y_col = y_start;
        g.drawString("MODO 1 JOGADOR", x_p1, y_col);
        drawControls1P(g, x_p1, y_col + 30); // Helper
        
        // --- Coluna 2: Controles 2P ---
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 18));
        y_col = y_start;
        g.drawString("MODO 2 JOGADORES", x_p2, y_col);
        drawControls2P(g, x_p2, y_col + 30); // Helper
        
        // --- Controles Globais ---
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 18));
        y_col = y_start + 230; // Posição fixa
        g.drawString("CONTROLES GLOBAIS", x_p1, y_col);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        y_col += 30; g.drawString("P   Pausar Jogo", x_p1, y_col);
        y_col += 20; g.drawString("T   Mudar Tema Visual", x_p1, y_col);
        y_col += 20; g.drawString("G   Ativar/Desativar Prévia", x_p1, y_col);
        
        drawFooterHint(g, "(Pressione ENTER ou ESC para Voltar)");
    }

    // --- ============ NOVOS OVERLAYS DE JOGO ============ ---

    private void drawGameOver(Graphics2D g) {
        int cardWidth = 450;
        int cardHeight = 300;
        int x = getWidth() / 2 - cardWidth / 2;
        int y = getHeight() / 2 - cardHeight / 2;
        
        drawMenuCard(g, x, y, cardWidth, cardHeight);
        
        int y_center = y + 70;
        int p1_x_center = getWidth() / 2 - 110;
        int p2_x_center = getWidth() / 2 + 110;
        
        if (currentGameMode == GameController.GameMode.ONE_PLAYER) {
            g.setFont(new Font("Consolas", Font.BOLD, 36));
            g.setColor(Color.RED);
            drawCenteredString(g, "GAME OVER", getWidth() / 2, y_center); 
        } else {
            if (board1.isGameOver() && board2.isGameOver()) {
                g.setFont(new Font("Consolas", Font.BOLD, 36));
                g.setColor(Color.WHITE);
                drawCenteredString(g, "EMPATE", getWidth() / 2, y_center);
            } else if (board1.isGameOver()) {
                g.setFont(new Font("Consolas", Font.BOLD, 28));
                g.setColor(Color.RED);
                drawCenteredString(g, "P1 PERDEU", p1_x_center, y_center);
                g.setFont(new Font("Consolas", Font.BOLD, 28));
                g.setColor(Color.GREEN);
                drawCenteredString(g, "P2 VENCEU!", p2_x_center, y_center);
            } else if (board2.isGameOver()) {
                g.setFont(new Font("Consolas", Font.BOLD, 28));
                g.setColor(Color.GREEN);
                drawCenteredString(g, "P1 VENCEU!", p1_x_center, y_center);
                g.setFont(new Font("Consolas", Font.BOLD, 28));
                g.setColor(Color.RED);
                drawCenteredString(g, "P2 PERDEU", p2_x_center, y_center);
            }
        }
        
        g.setFont(new Font("Consolas", Font.PLAIN, 24));
        int y_menu = y_center + 80;
        
        String[] options = {"Reiniciar", "Voltar ao Menu"};
        
        for (int i = 0; i < options.length; i++) {
            if (i == gameOverSelection) {
                g.setColor(Color.YELLOW);
                drawCenteredString(g, "> " + options[i], getWidth() / 2, y_menu);
            } else {
                g.setColor(Color.WHITE);
                drawCenteredString(g, options[i], getWidth() / 2, y_menu);
            }
            y_menu += 40;
        }
    }
    
    private void drawPausedScreen(Graphics2D g) {
        int cardWidth = 450;
        int cardHeight = 280;
        int x = getWidth() / 2 - cardWidth / 2;
        int y = getHeight() / 2 - cardHeight / 2;
        
        drawMenuCard(g, x, y, cardWidth, cardHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 36));
        drawCenteredString(g, "PAUSADO", getWidth() / 2, y + 60);

        g.setFont(new Font("Consolas", Font.PLAIN, 24));
        int y_menu = y + 110;
        
        String[] options = {"Voltar ao Jogo", "Controles", "Regras", "Sair para o Menu"};
        
        for (int i = 0; i < options.length; i++) {
            if (i == pauseMenuSelection) {
                g.setColor(Color.YELLOW);
                drawCenteredString(g, "> " + options[i], getWidth() / 2, y_menu);
            } else {
                g.setColor(Color.WHITE);
                drawCenteredString(g, options[i], getWidth() / 2, y_menu);
            }
            y_menu += 40;
        }
    }
    
    /**
     * ATUALIZADO: Aumenta a altura do Card para a regra de Nível.
     */
    private void drawPausedRulesScreen(Graphics2D g) {
        int cardWidth = 400; 
        int cardHeight = 500; // <<< Aumenta a altura
        int x = getWidth() / 2 - cardWidth / 2;
        int y = getHeight() / 2 - cardHeight / 2;
        
        drawMenuCard(g, x, y, cardWidth, cardHeight);
        
        g.setColor(Color.CYAN);
        g.setFont(new Font("Consolas", Font.BOLD, 32));
        drawCenteredString(g, "REGRAS", getWidth() / 2, y + 50);

        int y_col = y + 100;
        int x_col = x + 50;

        // --- Coluna 1: Pontuação ---
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 22));
        g.drawString("PONTUAÇÃO", x_col, y_col);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 18));
        y_col += 40; 
        g.drawString("Pontos = Valor Base * Nível", x_col, y_col);
        
        g.setColor(Color.CYAN);
        y_col += 40; g.drawString("1 Linha   :  40 pts", x_col, y_col);
        y_col += 30; g.drawString("2 Linhas  : 100 pts", x_col, y_col);
        y_col += 30; g.drawString("3 Linhas  : 300 pts", x_col, y_col);
        g.setColor(Color.ORANGE);
        y_col += 30; g.drawString("TETRIS (4): 1200 pts", x_col, y_col);
        
        // --- Adiciona Regra de Nível ---
        y_col += 40; 
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 22));
        g.drawString("NÍVEL", x_col, y_col);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 16));
        y_col += 30;
        String textNivel = "A cada 10 linhas limpas,\n" +
                           "você avança de nível.";
        y_col = drawMultiLineString(g, textNivel, x_col, y_col);
        // --- Fim Nível ---

        // --- Coluna 2: Lixo (Garbage) ---
        if (currentGameMode == GameController.GameMode.TWO_PLAYER) {
            y_col += 40;
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Consolas", Font.BOLD, 22));
            g.drawString("LIXO (Modo 2P)", x_col, y_col);
            
            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font("Consolas", Font.PLAIN, 18));
            y_col += 30; g.drawString("2 Linhas  -> Envia 1 Linha", x_col, y_col);
            y_col += 30; g.drawString("3 Linhas  -> Envia 2 Linhas", x_col, y_col);
            y_col += 30; g.drawString("TETRIS    -> Envia 4 Linhas", x_col, y_col);
        }

        drawFooterHint(g, "(Pressione ENTER ou ESC para Voltar)");
    }
    
    private void drawPausedControlsScreen(Graphics2D g) {
        int cardWidth = 400; 
        int cardHeight = 350;
        int x = getWidth() / 2 - cardWidth / 2;
        int y = getHeight() / 2 - cardHeight / 2;
        
        drawMenuCard(g, x, y, cardWidth, cardHeight);
        
        g.setColor(Color.CYAN);
        g.setFont(new Font("Consolas", Font.BOLD, 32));
        drawCenteredString(g, "CONTROLES", getWidth() / 2, y + 50);

        int y_start = y + 100;
        int x_col = getWidth() / 2 - 150; 

        if (currentGameMode == GameController.GameMode.ONE_PLAYER) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Consolas", Font.BOLD, 18));
            g.drawString("MODO 1 JOGADOR", x_col, y_start);
            drawControls1P(g, x_col, y_start + 30);
        } else {
            x_col = getWidth() / 2 - 160;
            drawControls2P(g, x_col, y_start);
        }
        
        drawFooterHint(g, "(Pressione ENTER ou ESC para Voltar)");
    }
    
    // --- ============ Helpers de Desenho ============ ---
    
    private int drawControls1P(Graphics g, int x, int y) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.drawString("←   Mover Esquerda", x, y); y += 20;
        g.drawString("→   Mover Direita", x, y); y += 20;
        g.drawString("↑   Girar (Horário)", x, y); y += 20;
        g.drawString("Z   Girar (Anti-horário)", x, y); y += 20;
        g.drawString("↓   Acelerar Queda", x, y); y += 20;
        g.drawString("Espaço   Cair Imediatamente", x, y);
        return y;
    }
    
    private int drawControls2P(Graphics g, int x, int y) {
        g.setColor(Color.CYAN);
        g.setFont(new Font("Consolas", Font.BOLD, 16));
        g.drawString("JOGADOR 1 (Esquerda)", x, y);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        y += 25; g.drawString("A / D   Esquerda / Direita", x, y);
        y += 20; g.drawString("W / Q   Girar Hor / Anti-hor", x, y);
        y += 20; g.drawString("S       Acelerar Queda", x, y);
        y += 20; g.drawString("Espaço  Cair Imediatamente", x, y);

        y += 35;
        g.setColor(Color.CYAN);
        g.setFont(new Font("Consolas", Font.BOLD, 16));
        g.drawString("JOGADOR 2 (Direita)", x, y);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        y += 25; g.drawString("← / →   Esquerda / Direita", x, y);
        y += 20; g.drawString("↑ / M   Girar Hor / Anti-hor", x, y);
        y += 20; g.drawString("↓       Acelerar Queda", x, y);
        y += 20; g.drawString("N       Cair Imediatamente", x, y);
        return y;
    }
    
    private void drawCenteredString(Graphics g, String text, int x_center, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = x_center - (metrics.stringWidth(text) / 2);
        g.drawString(text, x, y);
    }
    
    private int drawMultiLineString(Graphics g, String text, int x, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int lineHeight = metrics.getHeight();
        for (String line : text.split("\n")) {
            g.drawString(line, x, y);
            y += lineHeight;
        }
        return y;
    }
}