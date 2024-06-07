package core.utilities;

import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import core.window.GamePanel;

public class ImageUtilities {
    private static final HashMap<String, BufferedImage> map = new HashMap<>();
    private static final int tileSize = 16;
    private static final double scale = (double) GamePanel.TILESIZE / tileSize;

    public static BufferedImage getImage(String folder, String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(
                    new StringBuilder("res/").append(folder).append('/').append(name).append(".png").toString()));
        } catch (IOException e) {
            System.err.println(new StringBuilder("Image \"").append(folder).append('\\').append(name).append("\" not found").toString());
            System.exit(-1);
        }
        image = resize(image, (int) (scale * image.getWidth()), (int) (scale * image.getHeight()));
        map.put(name, image);
        return image;
    }

    public static BufferedImage getImage(String folder, String name, int r, int c) {
        return getImage(folder, name, r, c, 1);
    }

    public static BufferedImage getImage(String folder, String name, int r, int c, int scale) {
        return getImage(folder, name).getSubimage(scale * GamePanel.TILESIZE * c, scale * GamePanel.TILESIZE * r,
                scale * GamePanel.TILESIZE, scale * GamePanel.TILESIZE);
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        BufferedImage image = gd.getDefaultConfiguration().createCompatibleImage(newW, newH, Transparency.TRANSLUCENT);
        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();
        return image;
    }
}
