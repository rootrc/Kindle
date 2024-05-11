package core.game;

import core.dungeon.Dungeon;
import core.window.GameFrame;
import core.window.GamePanel;

public class Game implements Runnable {
    public final int FPS = 60;
    public final int UPS = 60;

    public final static boolean DEBUG = false;

    private GamePanel gamePanel;
    private Thread gameThread;

    public Game() {
        gamePanel = new Dungeon();
        new GameFrame(gamePanel);
        startGameThread();
    }

    private void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta1 = 0;
        long lastTime1 = System.nanoTime();
        long currentTime1;

        double updateInterval = 1000000000 / UPS;
        double delta2 = 0;
        long lastTime2 = System.nanoTime();
        long currentTime2;

        while (gameThread != null) {
            currentTime1 = System.nanoTime();
            delta1 += (currentTime1 - lastTime1) / drawInterval;
            lastTime1 = currentTime1;
            if (delta1 > 1) {
                gamePanel.repaint();
                delta1--;
            }
            currentTime2 = System.nanoTime();
            delta2 += (currentTime2 - lastTime2) / updateInterval;
            lastTime2 = currentTime2;
            if (delta2 > 1) {
                gamePanel.update();
                delta2--;
            }
        }
    }
}