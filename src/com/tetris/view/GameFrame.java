package com.tetris.view;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import java.awt.Dimension;

/**
 * A janela principal do jogo (o JFrame).
 * Utiliza um JLayeredPane para sobrepor o painel do jogo e o painel de overlays.
 */
public class GameFrame extends JFrame {

    private GamePanel gamePanel;
    private OverlayPanel overlayPanel;
    private JLayeredPane layeredPane;

    public GameFrame() {
        initComponents();
    }

    private void initComponents() {
        layeredPane = new JLayeredPane();
        
        gamePanel = new GamePanel();
        overlayPanel = new OverlayPanel();

        Dimension size = gamePanel.getPreferredSize();
        layeredPane.setPreferredSize(size);
        
        // CORREÇÃO: Define o layout do LayeredPane como nulo
        // para que setBounds funcione
        layeredPane.setLayout(null); 
        
        gamePanel.setBounds(0, 0, size.width, size.height);
        overlayPanel.setBounds(0, 0, size.width, size.height);
        overlayPanel.setOpaque(false);

        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(overlayPanel, JLayeredPane.PALETTE_LAYER);
        
        add(layeredPane);

        setTitle("Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack(); // Pack inicial
        setLocationRelativeTo(null);
    }
    
    /**
     * Método chamado pelo Controller para redimensionar a janela
     * após a seleção do modo de jogo.
     */
    public void packAndCenter() {
        // Atualiza os tamanhos dos painéis internos antes de empacotar
        Dimension size = gamePanel.getPreferredSize();
        layeredPane.setPreferredSize(size);
        gamePanel.setBounds(0, 0, size.width, size.height);
        overlayPanel.setBounds(0, 0, size.width, size.height);
        
        pack(); // Re-calcula o tamanho da janela
        setLocationRelativeTo(null); // Re-centraliza
    }

    // --- Métodos de acesso para o Controller ---

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public OverlayPanel getOverlayPanel() {
        return overlayPanel;
    }
}