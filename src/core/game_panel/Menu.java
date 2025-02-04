package core.game_panel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;

import core.Game;
import core.dungeon.settings.KeyBinds;
import core.game_components.GamePanel;
import core.game_components.UIButton;
import core.game_components.UILayer;
import core.utilities.ImageUtilities;

public class Menu extends GamePanel {
    private static BufferedImage image;

    public Menu(Game game, UILayer UILayer) {
        super(game, UILayer);
        if (image == null) {
            image = ImageUtilities.getImage("menu_images", "menu");
        }
        add(new UIButton(game.changePanel("dungeon"), new Rectangle(104, 380, 384, 96),
                ImageUtilities.getImage("UI", "StartButton")));
        add(new UIButton(game.changePanel("options"), new Rectangle(104, 500, 384, 96),
                ImageUtilities.getImage("UI", "OptionsButton")));
        add(new UIButton(game.changePanel("rules"), new Rectangle(104, 620, 384, 96),
                ImageUtilities.getImage("UI", "RulesButton")));
        getInputMap(2).put(KeyBinds.ESC, "exit");
        getActionMap().put("exit", UILayer.createAndOpenConfirmUI(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        }));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(image, 0, 0, null);
    }

}