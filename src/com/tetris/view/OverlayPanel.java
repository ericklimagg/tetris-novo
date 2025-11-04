package com.tetris.view;

import com.tetris.controller.GameController; 
import com.tetris.model.Board;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * Um painel transparente que desenha os 'overlays' (telas por cima do jogo).
 * ATUALIZADO PARA Correções Finais.
 */
public class OverlayPanel extends JPanel {

    private Board board1;
    private Board board2;
    private GameController.GameMode currentGameMode;
    private int menuSelection; // NOVO

    public OverlayPanel() {
        setOpaque(false); 
    }

    // NOVO: Recebe a seleção do menu
    public void updateBoards(Board board1, Board board2, GameController.GameMode mode, int menuSelection) {
        this.board1 = board1;
        this.board2 = board2;
        this.currentGameMode = mode;
        this.menuSelection = menuSelection;
    }
    
    // Sobrescreve o método antigo para segurança
    public void updateBoards(Board board1, Board board2) {
         this.board1 = board1;
         this.board2 = board2;
         this.currentGameMode = (board2 != null && board2.isStarted()) ? 
                                GameController.GameMode.TWO_PLAYER : 
                                GameController.GameMode.ONE_PLAYER;
         this.menuSelection = (this.currentGameMode == GameController.GameMode.ONE_PLAYER) ? 0 : 1;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (board1 == null || board2 == null) {
            return;
        }

        // Decide qual overlay desenhar
        if (!board1.isStarted() && !board2.isStarted()) {
            drawStartScreen(g);
        } else if (board1.isGameOver() || (currentGameMode == GameController.GameMode.TWO_PLAYER && board2.isGameOver())) {
            drawGameOver(g); 
        } else if (board1.isPaused()) { // Pausa é global
            drawPaused(g);
        }
    }

    private void drawStartScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 36));
        g.drawString("TETRIS", getWidth() / 2 - 60, getHeight() / 2 - 150);

        g.setFont(new Font("Consolas", Font.PLAIN, 18));
        int y = getHeight() / 2 - 80;
        int x = getWidth() / 2 - 100;
        
        // Mostra a seleção de modo com base no menuSelection
        if (menuSelection == 0) { // 1P
            g.setColor(Color.YELLOW);
            g.drawString("> 1 Jogador", x, y);
            g.setColor(Color.WHITE);
            g.drawString("  2 Jogadores", x, y + 30);
        } else { // 2P
            g.setColor(Color.WHITE);
            g.drawString("  1 Jogador", x, y);
            g.setColor(Color.YELLOW);
            g.drawString("> 2 Jogadores", x, y + 30);
        }
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.drawString("(Use ↑↓ para selecionar)", x, y + 60);

        g.setFont(new Font("Consolas", Font.BOLD, 18));
        y += 100;
        g.drawString("Pressione ENTER para Jogar", getWidth() / 2 - 130, y);
    }

    private void drawGameOver(Graphics g) {
        // CORREÇÃO: Tela escurece se qualquer jogador perder
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.WHITE);
        
        // Se 1P
        if (currentGameMode == GameController.GameMode.ONE_PLAYER) {
            g.setFont(new Font("Consolas", Font.BOLD, 36));
            g.drawString("GAME OVER", getWidth()/2 - 80, getHeight() / 2);
            g.setFont(new Font("Consolas", Font.PLAIN, 18));
            g.drawString("ENTER para reiniciar", getWidth()/2 - 110, getHeight() / 2 + 40);
            return;
        }
        
        // Se 2P
        int p1_area_width = getWidth() / 2; 
        int p2_x = getWidth() / 2;     

        if (board1.isGameOver() && board2.isGameOver()) {
            g.setFont(new Font("Consolas", Font.BOLD, 36));
            g.drawString("GAME OVER", getWidth() / 2 - 80, getHeight() / 2);
            g.setFont(new Font("Consolas", Font.PLAIN, 18));
            g.drawString("ENTER para reiniciar", getWidth() / 2 - 110, getHeight() / 2 + 40);
        } else if (board1.isGameOver()) {
            g.setFont(new Font("Consolas", Font.BOLD, 28));
            g.drawString("P1 GAME OVER", p1_area_width / 2, getHeight() / 2);
            g.setFont(new Font("Consolas", Font.BOLD, 20));
            g.drawString("P2 VENCEU!", p2_x + (p1_area_width / 2) - 80, getHeight() / 2);
        } else if (board2.isGameOver()) {
            g.setFont(new Font("Consolas", Font.BOLD, 28));
            g.drawString("P2 GAME OVER", p2_x + (p1_area_width / 2) - 80, getHeight() / 2);
            g.setFont(new Font("Consolas", Font.BOLD, 20));
            g.drawString("P1 VENCEU!", p1_area_width / 2, getHeight() / 2);
        }
    }
    
    private void drawPaused(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 36));
        g.drawString("PAUSADO", getWidth() / 2 - 70, getHeight() / 2 - 150);

        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        int y = getHeight() / 2 - 80;
        int x_center = getWidth() / 2;
        
        // Mostra controlos corretos para o modo
        if (currentGameMode == GameController.GameMode.TWO_PLAYER) {
            drawControls(g, x_center - 250, y, true); // Modo 2P
        } else {
            drawControls(g, x_center - 125, y, false); // Modo 1P
        }

        g.setFont(new Font("Consolas", Font.BOLD, 18));
        y = getHeight() - 150;
        g.drawString("Pressione P para continuar", getWidth() / 2 - 125, y);
    }

    /**
     * ATUALIZADO: Mostra os controlos corretos (com anti-horário).
     * @param showTwoPlayers Se deve mostrar os controlos de 2P.
     */
    private void drawControls(Graphics g, int x, int y, boolean showTwoPlayers) {
        if (showTwoPlayers) {
            int columnSpacing = 280;
            // Coluna Jogador 1
            g.drawString("JOGADOR 1 (Esquerda)", x, y);
            y += 30;
            g.drawString("A   Mover Esquerda", x, y); y += 20;
            g.drawString("D   Mover Direita", x, y); y += 20;
            g.drawString("W   Girar (Horário)", x, y); y += 20;
            g.drawString("Q   Girar (Anti-horário)", x, y); y += 20;
            g.drawString("S   Acelerar Queda", x, y); y += 20;
            g.drawString("Espaço   Cair Imediatamente", x, y);

            // Reset Y e define X para a Coluna 2
            y = getHeight() / 2 - 80;
            x += columnSpacing;

            // Coluna Jogador 2
            g.drawString("JOGADOR 2 (Direita)", x, y);
            y += 30;
            g.drawString("←   Mover Esquerda", x, y); y += 20;
            g.drawString("→   Mover Direita", x, y); y += 20;
            g.drawString("↑   Girar (Horário)", x, y); y += 20;
            g.drawString("M   Girar (Anti-horário)", x, y); y += 20;
            g.drawString("↓   Acelerar Queda", x, y); y += 20;
            g.drawString("N   Cair Imediatamente", x, y);
            
            // Controlos Globais
            y += 40;
            x -= (columnSpacing / 2); // Centraliza
            g.drawString("P   Pausar Jogo", x, y); y += 20;
            g.drawString("T   Mudar Tema Visual", x, y); y += 20;
            g.drawString("G   Ativar/Desativar Prévia", x, y);
        
        } else {
            // Controlos de 1P (Padrão)
            g.drawString("MANUAL DE CONTROLES (1P)", x, y);
            y += 30;
            g.drawString("←   Mover Esquerda", x, y); y += 20;
            g.drawString("→   Mover Direita", x, y); y += 20;
            g.drawString("↑   Girar (Horário)", x, y); y += 20;
            g.drawString("Z   Girar (Anti-horário)", x, y); y += 20;
            g.drawString("↓   Acelerar Queda", x, y); y += 20;
            g.drawString("Espaço   Cair Imediatamente", x, y); y += 30;
            
            g.drawString("P   Pausar Jogo", x, y); y += 20;
            g.drawString("T   Mudar Tema Visual", x, y); y += 20;
            g.drawString("G   Ativar/Desativar Prévia", x, y);
        }
    }
}