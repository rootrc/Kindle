package core.dungeon.room.entity;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;

import core.Game;
import core.dungeon.dungeon_ui.WarningDisplay;
import core.dungeon.items.Inventory;
import core.dungeon.room.Room;
import core.dungeon.room.object.Ladder;
import core.dungeon.room.object_utilities.CollisionBox;
import core.dungeon.room.object_utilities.DirectionUtilities;
import core.dungeon.room.object_utilities.RoomObject;
import core.dungeon.room.object_utilities.SpriteSheet;
import core.dungeon.room.tile.Tile;
import core.dungeon.settings.DiffSettings;
import core.dungeon.settings.KeyBinds;
import core.game_components.GamePanel;
import core.utilities.ImageUtilities;

public class Player extends Entity {
    private static final int HIGH_MAX_SPEED = 3 * Tile.SIZE / 16;
    private static final int LOW_MAX_SPEED = Tile.SIZE / 8;
    private static final double NORMALIZE_VECTOR = Math.sqrt(2) / 2;

    private GamePanel gamePanel;

    private boolean movingUp;
    private boolean movingLeft;
    private boolean movingDown;
    private boolean movingRight;
    private Inventory inventory;
    private Stats passiveItemStats;
    private ArrayList<RoomObject> interactables = new ArrayList<>();
    private Action nextRoom;
    private Action death;
    private int updateCnt;

    private int interactionCooldown;
    private int dashCooldown;

    private double health;
    private int healthPoints;

    private int hitDirection;
    private int hitCnt;

    private SpriteSheet lastSpriteSheet;
    private WarningDisplay warningDisplay;

    private int interactionCnt;

    // Description: The constructor of the class
    // Parameters: The gamepanel the player belongs to, next room and death actions,
    // inventory, and warning display
    // Return: Nothing
    public Player(GamePanel gamePanel, Action nextRoom, Action death, Inventory inventory,
            WarningDisplay warningDisplay) {
        // Sets spritesheet, hitbox, interactbox, max speed, accleration speed, and
        // decleration speed
        super(new SpriteSheet(ImageUtilities.getImage("entities", "player"), 4, 8, 8),
                CollisionBox.getCollisionBox(0.5, 1.75, 1, 1),
                CollisionBox.getCollisionBox(0, 1.25, 2, 2),
                HIGH_MAX_SPEED, Tile.SIZE / 40.0, Tile.SIZE / 80.0);
        this.gamePanel = gamePanel;
        this.nextRoom = nextRoom;
        this.death = death;
        this.inventory = inventory;
        this.warningDisplay = warningDisplay;
        passiveItemStats = new Stats();
        health = DiffSettings.startingHealth;
        healthPoints = DiffSettings.startingHealth;
        setKeyBinds();
        interactionCooldown = 1000;
        dashCooldown = 1000;
    }

    // Description: Sets all the keybinds
    // Parameters: Nothing
    // Return: Nothing
    private void setKeyBinds() {
        getInputMap(2).put(KeyBinds.INTERACT, "interact");
        getActionMap().put("interact", interact);
        getInputMap(2).put(KeyBinds.SLOW_DOWN_TOGGLE, "slowDownToggle");
        getActionMap().put("slowDownToggle", slowDownToggle);
        getInputMap(2).put(KeyBinds.DASH, "dash");
        getActionMap().put("dash", dash);
        getInputMap(2).put(KeyBinds.UP_PRESSED, "acc up");
        getInputMap(2).put(KeyBinds.UP_RELEASED, "decel up");
        getInputMap(2).put(KeyBinds.LEFT_PRESSED, "acc left");
        getInputMap(2).put(KeyBinds.LEFT_RELEASED, "decel left");
        getInputMap(2).put(KeyBinds.DOWN_PRESSED, "acc down");
        getInputMap(2).put(KeyBinds.DOWN_RELEASED, "decel down");
        getInputMap(2).put(KeyBinds.RIGHT_PRESSED, "acc right");
        getInputMap(2).put(KeyBinds.RIGHT_RELEASED, "decel right");
        getActionMap().put("acc up", accelerateUp);
        getActionMap().put("decel up", decelerateUp);
        getActionMap().put("acc left", accelerateLeft);
        getActionMap().put("decel left", decelerateLeft);
        getActionMap().put("acc down", accelerateDown);
        getActionMap().put("decel down", decelerateDown);
        getActionMap().put("acc right", accelerateRight);
        getActionMap().put("decel right", decelerateRight);
    }

    // Description: Updates the player
    // Parameters: Nothing
    // Return: Nothing
    @Override
    public void update() {
        updateCnt++;
        interactionCooldown++;
        dashCooldown++;
        setLightRadius(2 * passiveItemStats.lightRadiusFactor * Math.sqrt(health));
        // Adds health every second
        if (updateCnt % Game.UPS == 0) {
            updateCnt = 0;
            addHealth(passiveItemStats.healthRegenPerSecond);
        }
        // Moves animation
        if (isMoving()) {
            if (hitCnt <= Game.UPS) {
                getSpriteSheet().next();
            }
        } else {
            getSpriteSheet().setFrame(getSpriteSheet().getFrame() / 2);
        }

        // Moves player as a result of a dsah
        if (dashCooldown < 10) {
            setSpeedX(NORMALIZE_VECTOR * getMaxSpeed() * DirectionUtilities.getXMovement(Player.this.getDirection()));
            setSpeedY(NORMALIZE_VECTOR * getMaxSpeed() * DirectionUtilities.getYMovement(Player.this.getDirection()));
        } else {
            setDirection(DirectionUtilities.getMovingDirection(movingUp, movingLeft, movingDown, movingRight));
        }
        if (dashCooldown == 10) {
            setMaxSpeed(getMaxSpeed() / 3);
        }

        // Does various things based on how long ago the player was hit
        if (hitCnt > 0) {
            if (hitCnt == Game.UPS) {
                replaceSpriteSheet(lastSpriteSheet);
            }
            hitCnt--;
            // Knockback
            if (hitCnt > 11.0 / 12 * Game.UPS) {
                moveX(3 * getMaxSpeed() * DirectionUtilities.getXMovement(hitDirection));
                moveY(3 * getMaxSpeed() * DirectionUtilities.getYMovement(hitDirection));
            } else {
                // Flashes white
                if (hitCnt % 10 == 5) {
                    replaceSpriteSheet(ImageUtilities.getImage("entities", "playerWhiteFill"));
                } else if (hitCnt % 10 == 0) {
                    replaceSpriteSheet(lastSpriteSheet);
                }
            }
        }
        super.update();
    }

    // Description: Moves the player
    // Parameters: Nothing
    // Return: Nothing
    @Override
    public void move() {
        // If just dashed or being knocked back, don't move
        if (dashCooldown < 4 | hitCnt >= 11.0 / 12 * Game.UPS) {
            return;
        }
        double ax = 0;
        double ay = 0;
        // Get x and y accleration
        if (movingUp && Math.abs(getSpeedY()) <= getMaxSpeed()) {
            ay -= getAccSpeed();
        }
        if (movingLeft && Math.abs(getSpeedX()) <= getMaxSpeed()) {
            ax -= getAccSpeed();
        }
        if (movingDown && Math.abs(getSpeedY()) <= getMaxSpeed()) {
            ay += getAccSpeed();
        }
        if (movingRight && Math.abs(getSpeedX()) <= getMaxSpeed()) {
            ax += getAccSpeed();
        }
        // Sets x and y speed based on accleration
        if (ax == 0) {
            if (getSpeedX() > 0) {
                setSpeedX(Math.max(getSpeedX() - getDeaccSpeed(), 0));
            } else if (getSpeedX() < 0) {
                setSpeedX(Math.min(getSpeedX() + getDeaccSpeed(), 0));
            }
        }
        if (ay == 0) {
            if (getSpeedY() > 0) {
                setSpeedY(Math.max(getSpeedY() - getDeaccSpeed(), 0));
            } else if (getSpeedY() < 0) {
                setSpeedY(Math.min(getSpeedY() + getDeaccSpeed(), 0));
            }
        }
        if (ax != 0 && ay == 0) {
            setSpeedX(Math.max(Math.min(getSpeedX() + ax, getMaxSpeed()), -getMaxSpeed()));
        } else if (ax == 0 && ay != 0) {
            setSpeedY(Math.max(Math.min(getSpeedY() + ay, getMaxSpeed()), -getMaxSpeed()));
        } else if (ax != 0 && ay != 0) {
            setSpeedX(Math.max(Math.min(getSpeedX() + NORMALIZE_VECTOR * ax, NORMALIZE_VECTOR * getMaxSpeed()),
                    -NORMALIZE_VECTOR * getMaxSpeed()));
            setSpeedY(Math.max(Math.min(getSpeedY() + NORMALIZE_VECTOR * ay, NORMALIZE_VECTOR * getMaxSpeed()),
                    -NORMALIZE_VECTOR * getMaxSpeed()));
        }
    }

    // Description: What happens when colliding with Player (Exists because Abstract
    // method from RoomObject)
    // Parameters: Player
    // Return: Nothing
    public void collides(Player player) {
    }

    // Description: What happens when interacting with Player (Exists because
    // Abstract method from RoomObject)
    // Parameters: Player
    // Return: Nothing
    public void interaction(Player player) {
    }

    // Description: Player taking damage
    // Parameters: Nothing
    // Return: Nothing
    public void takeDamage() {
        if (hitCnt > 0) { // If still in process of being hit, you have invincibility frames
            return;
        }
        // Reduce health
        health -= passiveItemStats.damageMulti;
        if (health < 1.0 / 6 && passiveItemStats.reviveCnt > 0) { // Revive if you have revive
            passiveItemStats.reviveCnt--;
            health = 1.0 / 6;
        }
        // If health is between 0 and 1, change sprite sheet and display warning
        if (1.0 / 6 < health && health <= 1 + 1.0 / 6) {
            replaceSpriteSheet(ImageUtilities.getImage("entities", "playerRedOutline"));
            warningDisplay.oneHealthWarning();
        }
        // Resets speed
        setSpeedX(0);
        setSpeedY(0);
        // Sets red tint to screen
        gamePanel.fadeIn();
        gamePanel.setFadingEffectAlpha(60);
        gamePanel.setFadingEffectColor(Color.RED);
        // Freezes frames and screen shake
        Game.setFreezeFrame(6);
        Room.setScreenShakeDuration(15);
        Room.setScreenShakeStrength(15);
        if (dashCooldown < 1.0 / 6 * Game.UPS) {
            setMaxSpeed((int) (getMaxSpeed() / 3));
        }
        dashCooldown = 1000;
        // If dead, set spritesheet to red and do death action
        if (health <= 1.0 / 6) {
            replaceSpriteSheet(ImageUtilities.getImage("entities", "playerRedFill"));
            death.actionPerformed(null);
            return;
        }
        hitCnt = 60;
        hitDirection = DirectionUtilities.reverseDirection(getDirection());
        lastSpriteSheet = getSpriteSheet();
        replaceSpriteSheet(ImageUtilities.getImage("entities", "playerRedFill"));
    }

    // The following are lots of setters and getters and simple code

    public void clearInteractables() {
        interactables.clear();
    }

    public void addInteractable(RoomObject object) {
        interactables.add(object);
    }

    public double getHealth() {
        return health;
    }

    public void addHealth(double delta) {
        health = Math.min(health + delta, healthPoints);
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public void addHealthPoints(int delta) {
        healthPoints += delta;
        health = Math.min(health, healthPoints);
    }

    public Ladder getLadder() {
        for (RoomObject interactable : interactables) {
            interactable.interaction(Player.this);
            if (interactable instanceof Ladder) {
                return (Ladder) interactable;
            }
        }
        return null;
    }

    public int getDepthMovement() {
        return -getLadderDirection();
    }

    public int getLadderDirection() {
        if (getLadder() == null) {
            return 0;
        }
        return getLadder().getDirection();
    }

    public Stats getStats() {
        return passiveItemStats;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getInteractionCnt() {
        return interactionCnt;
    }

    private final Action interact = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (interactables == null || interactionCooldown < 30) {
                return;
            }
            interactionCooldown = 0;
            for (int i = 0; i < interactables.size(); i++) {
                if (interactables.size() == 0) {
                    break;
                }
                RoomObject interactable = interactables.get(i);
                interactable.interaction(Player.this);
                interactionCnt++;
                if (interactable instanceof Ladder) {
                    nextRoom.actionPerformed(e);
                }
            }
        }
    };

    private final Action slowDownToggle = new AbstractAction() {
        // Description: Toggle player slowing down
        // Parameters: ActionEvent
        // Return: Nothing
        public void actionPerformed(ActionEvent e) {
            if (getMaxSpeed() == HIGH_MAX_SPEED) {
                setMaxSpeed(LOW_MAX_SPEED);
            } else if (getMaxSpeed() == LOW_MAX_SPEED) {
                setMaxSpeed(HIGH_MAX_SPEED);
            }
        }
    };

    private final Action dash = new AbstractAction() {
        // Description: Perform a dash
        // Parameters: ActionEvent
        // Return: Nothing
        public void actionPerformed(ActionEvent e) {
            if (dashCooldown < 5.0 / 12 * Game.UPS) {
                return;
            }
            if (movingUp && movingDown || movingLeft && movingRight) {
                return;
            }
            dashCooldown = 0;
            setMaxSpeed((int) (3 * getMaxSpeed()));
            Game.setFreezeFrame(3);
            Room.setScreenShakeDuration(2);
            Room.setScreenShakeStrength(2);
        }
    };

    // Following code is self explanatory

    private final Action accelerateUp = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            movingUp = true;
        }
    };

    private final Action decelerateUp = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            movingUp = false;
        }
    };

    private final Action accelerateLeft = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            movingLeft = true;
        }
    };

    private final Action decelerateLeft = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            movingLeft = false;
        }
    };

    private final Action accelerateDown = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            movingDown = true;
        }
    };

    private final Action decelerateDown = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            movingDown = false;
        }
    };

    private final Action accelerateRight = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            movingRight = true;
        }
    };

    private final Action decelerateRight = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            movingRight = false;
        }
    };

    // Also mostly self explanatory:
    public static class Stats {
        private double healthRegenPerSecond;
        private double damageMulti;
        private int reviveCnt;

        private double lightRadiusFactor;
        private double detectionRadiusSquared;
        private double vision;

        private double fogOfWarMulti;

        private boolean canSeeAbove;
        private double snowVisibilityMulti;

        private double trapAppearPercent;
        private boolean explosivesExplode;
        private boolean teleportationWorks;
        private boolean teleportationDisappears;
        private boolean spikeCanDetect;

        private double pointMultiplier;

        private Stats() {
            healthRegenPerSecond = 0;
            damageMulti = 1;
            lightRadiusFactor = DiffSettings.playerLightRadiusFactor;
            detectionRadiusSquared = DiffSettings.playerDetectionRadiusSquared;
            vision = 0.8;
            fogOfWarMulti = 1;
            snowVisibilityMulti = 1;
            trapAppearPercent = 1;
            explosivesExplode = true;
            teleportationWorks = true;
            teleportationDisappears = true;
            spikeCanDetect = true;
            pointMultiplier = 1;
        }

        public void addHealthRegenPerSecond(double delta) {
            healthRegenPerSecond += delta;
        }

        public void multiDamageMulti(double multi) {
            damageMulti *= multi;
        }

        public void addRevive(int delta) {
            this.reviveCnt += delta;
        }

        public void multiLightRadius(double multi) {
            lightRadiusFactor *= multi;
        }

        public double getDetectionRadiusSquared() {
            return detectionRadiusSquared;
        }

        public void multiDetectionRadius(double multi) {
            detectionRadiusSquared *= multi * multi;
        }

        public double getVision() {
            return vision;
        }

        public void multiVision(double multi) {
            vision *= multi;
        }

        public double getFogOfWarMulti() {
            return fogOfWarMulti;
        }

        public void multiFogOfWarMulti(double multi) {
            fogOfWarMulti *= multi;
        }

        public boolean canSeeAbove() {
            return canSeeAbove;
        }

        public void setSeeAbove(boolean canSeeAbove) {
            this.canSeeAbove = canSeeAbove;
        }

        public double getSnowVisibilityMulti() {
            return snowVisibilityMulti;
        }

        public void setSnowVisibilityMulti(double snowVisibilityMulti) {
            this.snowVisibilityMulti = snowVisibilityMulti;
        }

        public double getTrapAppear() {
            return trapAppearPercent;
        }

        public void multiTrapAppear(double multi) {
            trapAppearPercent *= multi;
        }

        public boolean doExplosivesExplode() {
            return explosivesExplode;
        }

        public void setExplosivesExplode(boolean explosivesExplode) {
            this.explosivesExplode = explosivesExplode;
        }

        public boolean doesTeleportationWorks() {
            return teleportationWorks;
        }

        public void setTeleportationWorks(boolean teleportationWorks) {
            this.teleportationWorks = teleportationWorks;
        }

        public boolean doTeleportationDisappears() {
            return teleportationDisappears;
        }

        public void setTeleportationDisappears(boolean teleportationDisappears) {
            this.teleportationDisappears = teleportationDisappears;
        }

        public boolean spikeCanDetect() {
            return spikeCanDetect;
        }

        public void setSpikeCanDetect(boolean spikeCanDetect) {
            this.spikeCanDetect = spikeCanDetect;
        }

        public double getPointMultiplier() {
            return pointMultiplier;
        }

        public void multiPointMultiplier(double multi) {
            pointMultiplier *= multi;
        }
    }
}