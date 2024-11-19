package core.dungeon.mechanics.particles;

import java.awt.Color;

import core.dungeon.room.tile.Tile;
import core.utilities.RNGUtilities;

public class SnowParticles extends ParticleSystem {
    private static final double SPEED = 1.5;
    private int N;
    private double depth;

    public SnowParticles() {
        super();
        N = (int) ((double) getHeight() / SPEED / 10.0);
        for (int i = 0; i < getHeight(); i++) {
            if (RNGUtilities.getBoolean()) {
                update();
            } else {
                super.update();
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (getParticleCount() < N) {
            if (RNGUtilities.getBoolean((N - getParticleCount()) / 10.0)) {
                addParticle(new SnowParticle());
            }
        }
    }

    public void setDepth(int depth) {
        N = (int) Math.round(N * Math.pow(1.5, (depth - this.depth)));
        for (Particle particle : getParticles()) {
            particle.ySpeed = (int) Math.round(particle.ySpeed * Math.pow(1.5, (depth - this.depth)));
        }
        this.depth = depth;
    }

    private class SnowParticle extends Particle {
        SnowParticle() {
            super(3, Color.white, depth / 10.0 + RNGUtilities.getDouble(0.1), RNGUtilities.getInt(getWidth()), 0,
                    RNGUtilities.getDouble(depth / 10.0), SPEED + RNGUtilities.getDouble(1), 0, 0.05);
        }

        protected boolean isInvalid() {
            return y > getHeight() - Tile.SIZE;
        }

        protected boolean shouldDraw() {
            return true;
        }
    }
}