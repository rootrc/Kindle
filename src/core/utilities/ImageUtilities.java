package core.utilities;

import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import core.utilities.roomgenerator.SimplexNoise;

// Opens, edits, and processes images
public class ImageUtilities {
    private static final HashMap<String, BufferedImage> MAP = new HashMap<>();
    public static final int default3x3TilesetScale = 32;

    public static BufferedImage getImage(String folder, String name) {
        if (MAP.containsKey(name)) {
            return MAP.get(name);
        }
        try {
            BufferedImage image = ImageIO.read(new File(
                    new StringBuilder("res/").append(folder).append('/').append(name).append(".png").toString()));
            MAP.put(name, image);
            return image;
        } catch (IOException e) {
            System.err.println(new StringBuilder("Image \"").append(folder).append('\\').append(name)
                    .append("\" not found").toString());
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    public static BufferedImage getImage(BufferedImage image, int r, int c, int width, int height) {
        return image.getSubimage(width * c, height * r, width, height);
    }

    public static BufferedImage getImage(String folder, String name, int r, int c, int width, int height) {
        return getImage(getImage(folder, name), r, c, width, height);
    }

    public static BufferedImage getImageFrom3x3Tileset(String folder, String name, int width, int height, int size) {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        BufferedImage image = gd.getDefaultConfiguration().createCompatibleImage(width, height,
                Transparency.BITMASK);
        Graphics2D g2d = image.createGraphics();
        for (int i = 0; i < width / size; i++) {
            for (int j = 0; j < height / size; j++) {
                int r = 1;
                int c = 1;
                if (i == 0) {
                    c--;
                } else if (i == width / size - 1) {
                    c++;
                }
                if (j == 0) {
                    r--;
                } else if (j == height / size - 1) {
                    r++;
                }
                g2d.drawImage(ImageUtilities.getImage(folder, name, r, c, 32, 32), size * i, size * j, null);
            }
        }
        g2d.dispose();
        return image;
    }

    public static BufferedImage getImageFrom3x3Tileset(String folder, String name, int width, int height) {
        return getImageFrom3x3Tileset(folder, name, width, height, default3x3TilesetScale);
    }

    public static BufferedImage resize(BufferedImage oldImage, int newW, int newH) {
        if (newW == 1 && newH == 1) {
            return oldImage;
        }
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        BufferedImage newImage = gd.getDefaultConfiguration().createCompatibleImage(newW, newH,
                Transparency.TRANSLUCENT);
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(oldImage.getScaledInstance(newW, newH, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();
        return newImage;
    }

    public static ImageIcon resize(ImageIcon imageIcon, int newW, int newH) {
        return new ImageIcon(resize((BufferedImage) imageIcon.getImage(), newW, newH));
    }

    public static BufferedImage getSimplexNoiseFilter(int width, int height) {
        width = Math.max(width, height);
        height = Math.max(width, height);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        BufferedImage image = gd.getDefaultConfiguration().createCompatibleImage(width, height, Transparency.OPAQUE);
        SimplexNoise simplexNoise = new SimplexNoise(150, 0.67, (int) (System.nanoTime() % 1000000007));
        int[] pix = ((DataBufferInt) (image.getRaster().getDataBuffer())).getData();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double temp = simplexNoise.getNoise(i, j);
                int r = (int) (255 * temp);
                int g = (int) (255 * temp);
                int b = (int) (245 * temp + 5);
                pix[i * height + j] = -16777216 + (r << 16) + (g << 8) + b;
            }
        }
        return image;
    }
}