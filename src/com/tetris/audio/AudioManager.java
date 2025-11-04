package com.tetris.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;

/**
 * Gerencia a reprodução de áudio no jogo.
 * Carrega e toca clipes de áudio, como a música de fundo.
 */
public class AudioManager {

    private Clip musicClip;

    public AudioManager(String path) {
        // CORREÇÃO: Usa um método mais robusto para encontrar o recurso.
        // Garante que o caminho seja tratado como absoluto a partir da raiz do classpath.
        String correctedPath = path.startsWith("/") ? path.substring(1) : path;

        System.out.println("AudioManager: Tentando carregar áudio de: " + correctedPath);
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(correctedPath);

            if (url == null) {
                System.err.println("************************************************************");
                System.err.println("ERRO CRÍTICO: Recurso de áudio não encontrado!");
                System.err.println("Caminho procurado: " + correctedPath);
                System.err.println("Verifique se o arquivo de áudio existe no diretório 'bin' com a mesma estrutura de pastas que 'src'.");
                System.err.println("Exemplo: 'src/com/tetris/audio/music.wav' deve estar em 'bin/com/tetris/audio/music.wav'.");
                System.err.println("************************************************************");
                return;
            }
            
            System.out.println("AudioManager: Arquivo de áudio encontrado em: " + url);

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
            System.out.println("AudioManager: AudioInputStream criado com sucesso.");
            
            musicClip = AudioSystem.getClip();
            System.out.println("AudioManager: Clip obtido com sucesso.");
            
            musicClip.open(audioStream);
            System.out.println("AudioManager: Clip aberto com sucesso. Pronto para tocar.");

        } catch (UnsupportedAudioFileException e) {
            System.err.println("************************************************************");
            System.err.println("ERRO CRÍTICO: Formato de áudio não suportado!");
            System.err.println("O ficheiro .wav pode estar corrompido ou num formato inválido.");
            System.err.println("Por favor, tente converter o ficheiro para WAV (PCM Signed, 16 bit) novamente.");
            System.err.println("************************************************************");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("************************************************************");
            System.err.println("ERRO CRÍTICO: Erro de I/O ao ler o ficheiro de áudio.");
            System.err.println("************************************************************");
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("************************************************************");
            System.err.println("ERRO CRÍTICO: Linha de áudio não disponível.");
            System.err.println("Pode haver um problema com o sistema de som do seu computador.");
            System.err.println("************************************************************");
            e.printStackTrace();
        }
    }

    public void playMusic() {
        if (musicClip != null) {
            System.out.println("AudioManager: A tocar música...");
            musicClip.setFramePosition(0); // Reinicia a música do início
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            System.err.println("AudioManager: Não é possível tocar música porque o clip é nulo.");
        }
    }

    public void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            System.out.println("AudioManager: A parar a música...");
            musicClip.stop();
        }
    }
}