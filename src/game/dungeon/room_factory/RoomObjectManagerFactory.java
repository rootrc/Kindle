package game.dungeon.room_factory;

import game.dungeon.room.entity.Player;
import game.dungeon.room.object_utilities.RoomObjectFactory;
import game.dungeon.room.object_utilities.RoomObjectManager;
import game.dungeon.room.object_utilities.RoomObjectFactory.RoomObjectData;
import game.game_components.Factory;

class RoomObjectManagerFactory extends Factory<RoomObjectManager> {
    Player player;

    public RoomObjectManagerFactory(Player player) {
        this.player = player;
    }

    RoomObjectManager getRoomObjectManager(RoomFileData file) {
        RoomObjectManager objectManager = new RoomObjectManager(player);
        RoomObjectFactory RoomObjectFactory = new RoomObjectFactory();
        for (RoomObjectData data : file.getObjects()) {
            objectManager.add(RoomObjectFactory.getRoomObject(data), -1);
        }
        return objectManager;
    }
}