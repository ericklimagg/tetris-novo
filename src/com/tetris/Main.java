package com.tetris;

import com.tetris.controller.GameController;
import com.tetris.model.Board;
import com.tetris.view.GameFrame;
import javax.swing.SwingUtilities;

/**
 * Ponto de entrada principal da aplicação.
 * Responsável por instanciar e conectar o Model, a View e o Controller.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. Cria os Models (um para cada jogador)
            Board board1 = new Board();
            Board board2 = new Board();

            // 2. Cria a View principal
            GameFrame gameFrame = new GameFrame();

            // 3. Cria o Controller e conecta os Models e a View
            GameController gameController = new GameController(gameFrame, board1, board2);

            // 4. Inicia o jogo e exibe a janela
            gameController.start();
            gameFrame.setVisible(true);
        });
    }
}