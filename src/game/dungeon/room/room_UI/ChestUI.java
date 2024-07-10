package game.dungeon.room.room_UI;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import game.dungeon.items.Item;
import game.game_components.PopupUI;
import game.game_components.UILayer;
import game.utilities.ActionUtilities;
import game.utilities.ImageUtilities;

public class ChestUI extends PopupUI {
	private Action check;

	public ChestUI(UILayer UILayer, Item item, Action flash) {
		super(UILayer, 320, 256, 8, "ChestFloor");
		GetItemButton getItemButton = new GetItemButton(this, ActionUtilities.combineActions(flash, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				getInputMap(2).put(KeyStroke.getKeyStroke("pressed F"), "close");
			}
		}), item, new Rectangle(getWidth() / 2 - 32, getHeight() / 2 - 32, 64, 64));
		add(getItemButton);	
		getInputMap(2).put(KeyStroke.getKeyStroke("pressed F"), "getAll");
		getActionMap().put("getAll", getItemButton.getAction());
		check = UILayer.createAndOpenConfirmUI((Action) (new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				ChestUI.super.exitPanel();
			}
		}));
	}

	@Override
	public void drawComponent(Graphics2D g2d) {
		super.drawComponent(g2d);
		g2d.drawImage(ImageUtilities.getImage("UI", "ChestSlot"), getWidth() / 2 - 46, getHeight() / 2 - 46, null);
	}

	// TODO
	// @Override
	// public void exitPanel() {
		// if (getComponentCount() == 0) {
			// super.exitPanel();
		// } else {
		// 	check.actionPerformed(null);
		// }
	// }

}
