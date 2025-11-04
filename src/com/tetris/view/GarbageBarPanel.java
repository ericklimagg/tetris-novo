package com.tetris.view;

import com.tetris.model.Board;
import com.tetris.model.Theme;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Um painel que exibe uma barra vertical indicando o "lixo" (garbage)
 * pendente para o jogador.
 */
public class GarbageBarPanel extends JPanel {

    private static final int BAR_WIDTH = 20;

    private Board board;
    private Theme currentTheme;
    private boolean isPlayerOne; // Para desenhar a barra no lado correto

    public GarbageBarPanel(boolean isPlayerOne) {
        this.isPlayerOne = isPlayerOne;
        this.currentTheme = Theme.AVAILABLE_THEMES[0];
        
        // A altura é definida pelo getSquareSize, mas a largura é fixa
        int height = new BoardPanel().getPreferredSize().height;
        setPreferredSize(new Dimension(BAR_WIDTH, height));
        
        setBackground(currentTheme.uiBackground());
    }

    public void updateBoard(Board board) {
        this.board = board;
    }
    
    public void updateTheme(Theme theme) {
        this.currentTheme = theme;
        setBackground(theme.uiBackground());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (board == null) {
            return;
        }
        
        int incomingGarbage = board.getIncomingGarbage();
        if (incomingGarbage == 0) {
            return; // Não desenha nada se não houver lixo
        }

        // Cor da barra (fica mais vermelha quanto mais lixo)
        int red = Math.min(255, 100 + incomingGarbage * 15);
        g.setColor(new Color(red, 50, 50));

        // Calcula a altura da barra de lixo
        int squareSize = getPreferredSize().height / Board.BOARD_HEIGHT;
        int barHeight = Math.min(getPreferredSize().height, incomingGarbage * squareSize);
        
        // Desenha a barra (no fundo do painel)
        int y = getHeight() - barHeight;
        
        if (isPlayerOne) {
            // P1: Barra à direita do painel
            g.fillRect(0, y, BAR_WIDTH, barHeight);
        } else {
            // P2: Barra à esquerda do painel
            g.fillRect(0, y, BAR_WIDTH, barHeight);
        }
        
        // Desenha um contorno
        g.setColor(Color.WHITE);
        g.drawRect(0, y, BAR_WIDTH - 1, barHeight - 1);
    }
}