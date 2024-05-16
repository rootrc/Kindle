package core.dungeon.mechanics.inventory;

import core.dungeon.Dungeon;
import core.dungeon.mechanics.inventory.items.ItemFactory;
import core.utilities.Manager;
import core.window.GamePanel;

public class HotbarManager extends Manager.SubScreen<Hotbar> {
    private Dungeon dungeon;
    private int size;
    private ItemFactory itemFactory;

    public HotbarManager(Dungeon dungeon) {
        this.dungeon = dungeon;
        size = 8;
        set(new Hotbar(size));
        get().setLocation(GamePanel.screenWidth / 2 - getWidth() / 2, GamePanel.screenHeight - getHeight());
        itemFactory = new ItemFactory();

        // Test
        get().inventorySlots[1].setItem(itemFactory.getItem(5, 0));
        get().getActionMap().put(1, get().inventorySlots[1].getAction());
    }

    // @Override
    // public void update() {
    //     super.update();
    // }
}
