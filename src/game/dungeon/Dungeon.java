package game.dungeon;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import game.Game;
import game.dungeon.debug.DebugScreen;
import game.dungeon.inventory.Inventory;
import game.dungeon.room.Room;
import game.dungeon.room.entity.Player;
import game.dungeon.room.room_UI.PauseMenu;
import game.dungeon.room_factory.RoomFactory;
import game.dungeon.settings.DiffSettings;
import game.dungeon.settings.KeyBinds;
import game.game_components.GamePanel;
import game.game_components.UILayer;
import game.utilities.ActionUtilities;

public class Dungeon extends GamePanel {
    public static final int TILESIZE = 16;
    public static final int maxScreenRow = 48;
    public static final int maxScreenCol = 64;

    private static final int startingRoom = 1;

    private Room room;
    private Inventory inventory;
    private Player player;
    private RoomFactory roomFactory;

    private PauseMenu pauseMenu;
    private DebugScreen debugScreen;

    private final Action nextRoom = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            remove(room);
            room = roomFactory.getNextRoom(room);
            add(room, -1);
            debugScreen.updateRoom(room);
            revalidate();
        }
    };

    public Dungeon(Game game, UILayer UILayer) {
        super(game, UILayer);
        inventory = new Inventory(UILayer, DiffSettings.startingInventorySize);
        player = new Player(nextRoom, inventory);
        roomFactory = new RoomFactory(player, UILayer);
        room = roomFactory.getStartingRoom(startingRoom);
        // room = roomFactory.getStartingRoom(84);
        // room = roomFactory.createRandomRoom(21, 34);
        debugScreen = new DebugScreen(UILayer, room);
        add(room);
        add(inventory);
        pauseMenu = new PauseMenu(UILayer, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                reset();
                fadeIn();
            }
        }, ActionUtilities.combineActions(game.changePanel("mainMenu"), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        }), game.changePanel("title"));
        getInputMap(2).put(KeyBinds.escape, "pause");
        getActionMap().put("pause", UILayer.openPopupUI(pauseMenu));
        getInputMap(2).put(KeyBinds.debug, "debug");
        getActionMap().put("debug", UILayer.openPopupUI(debugScreen));
        
        if (Game.DEBUG) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    System.out.println(5 + " " + e.getY() / TILESIZE + " " + e.getX() / TILESIZE);
                }
            });
        }
    }

    public final void reset() {
        remove(room);
        remove(inventory);
        remove(UILayer);
        inventory = new Inventory(UILayer, DiffSettings.startingInventorySize);
        player = new Player(nextRoom, inventory);
        roomFactory = new RoomFactory(player, UILayer);
        room = roomFactory.getStartingRoom(startingRoom);
        add(room);
        add(inventory);
        UILayer.removeAll();
        add(UILayer);
    }

}