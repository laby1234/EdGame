package org.example.entity.player;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.layout.StackPane;
import org.example.config.GameConfig;
import org.example.entity.EntityType;

import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.getInput;
import static com.almasb.fxgl.dsl.FXGL.texture;

public class PlayerComponent extends Component {

    private double vy = 0;
    private boolean onGround = false;
    private boolean jumpReq = false;
    private boolean dead = false;
    private Runnable onDeath;

    private double animationTimer = 0;
    private double animationFrameDuration = 0.15; // Duration per frame in seconds
    private int currentRunFrame = 0;
    private int currentIdleFrame = 0;
    private String lastAnimationState = "idle";

    private boolean movingLeft = false;
    private boolean movingRight = false;

    private StackPane textureContainer;

    public void setTextureContainer(StackPane container) {
        this.textureContainer = container;
    }

    public void setOnDeath(Runnable onDeath) {
        this.onDeath = onDeath;
    }

    public void setMovingLeft(boolean moving) {
        this.movingLeft = moving;
    }
    
    public void setMovingRight(boolean moving) {
        this.movingRight = moving;
    }

    public void requestJump() {
        jumpReq = true;
    }

    public boolean isDead() {
        return dead;
    }

    @Override
    public void onUpdate(double tpf) {
        if (dead) return;
        double dt = Math.min(tpf, 1.0 / 30.0);

        // Update position based on current key states
        if (movingLeft) {
            entity.translateX(-GameConfig.PLAYER_SPEED * dt);
            clampX();
        }
        if (movingRight) {
            entity.translateX(GameConfig.PLAYER_SPEED * dt);
            clampX();
        }
        
        vy += GameConfig.PLAYER_GRAVITY * dt;
        double dy = vy * dt;
        entity.translateY(dy);

        if (entity.getY() < 0) {
            entity.setY(0);
            vy = 0;
        }

        onGround = false;

        if (entity.getY() + GameConfig.PLAYER_SIZE >= GameConfig.GROUND_Y) {
            entity.setY(GameConfig.GROUND_Y - GameConfig.PLAYER_SIZE);
            vy = 0;
            onGround = true;
        }

        checkPlatformCollisions(dy);

        // Fall out of world
        if (entity.getY() > GameConfig.WORLD_HEIGHT + 200) {
            die();
            return;
        }

        // Obstacle touch
        if (checkObstacleCollision()) {
            die();
            return;
        }

        if (jumpReq) {
            if (onGround) {
                vy = GameConfig.PLAYER_JUMP_STRENGTH;
            }
            jumpReq = false;
        }

        try {
            if (textureContainer != null) {
                double mouseX = getInput().getMouseXWorld();
                double playerCenterX = entity.getX() + GameConfig.PLAYER_SIZE / 2.0;
                if (mouseX < playerCenterX) {
                    textureContainer.setScaleX(-1);
                } else {
                    textureContainer.setScaleX(1);
                }
            }
        } catch (RuntimeException e) {
            System.err.println("Could not update player facing direction: " + e.getMessage());
        }

        updateAnimation(tpf);
    }

    private void checkPlatformCollisions(double dy) {
        for (Entity platform : getGameWorld().getEntitiesByType(EntityType.PLATFORM)) {
            double pLeft  = platform.getX();
            double pRight = pLeft + platform.getWidth();
            double pTop   = platform.getY();

            double eLeft   = entity.getX();
            double eRight  = eLeft + GameConfig.PLAYER_SIZE;
            double eBottom = entity.getY() + GameConfig.PLAYER_SIZE;

            boolean hOverlap = eRight > pLeft + 2 && eLeft < pRight - 2;

            // Land on top only (moving downward, bottom just crossed platform top)
            if (hOverlap && vy >= 0
                    && eBottom >= pTop
                    && eBottom <= pTop + Math.abs(dy) + GameConfig.PLAYER_SIZE * 0.5) {
                entity.setY(pTop - GameConfig.PLAYER_SIZE);
                vy = 0;
                onGround = true;
            }
        }
    }

    private boolean checkObstacleCollision() {
        for (Entity obstacle : getGameWorld().getEntitiesByType(EntityType.OBSTACLE)) {
            double oLeft   = obstacle.getX();
            double oRight  = oLeft + obstacle.getWidth();
            double oTop    = obstacle.getY();
            double oBottom = oTop + obstacle.getHeight();

            // Slight inset to avoid 1-pixel edge triggers
            double eLeft   = entity.getX() + 4;
            double eRight  = entity.getX() + GameConfig.PLAYER_SIZE - 4;
            double eTop    = entity.getY() + 4;
            double eBottom = entity.getY() + GameConfig.PLAYER_SIZE - 4;

            if (eRight > oLeft && eLeft < oRight && eBottom > oTop && eTop < oBottom) {
                return true;
            }
        }
        return false;
    }

    private void die() {
        if (dead) return;
        dead = true;
        if (onDeath != null) onDeath.run();
    }

    private void clampX() {
        if (entity.getX() < 0) {
            entity.setX(0);
        }
        if (entity.getX() + GameConfig.PLAYER_SIZE > GameConfig.WORLD_WIDTH) {
            entity.setX(GameConfig.WORLD_WIDTH - GameConfig.PLAYER_SIZE);
        }
    }

    private void updateAnimation(double tpf) {
        String currentState;
        if (!onGround) {
            currentState = "jump";
        } else if (movingLeft || movingRight) {
            currentState = "run";
        } else {
            currentState = "idle";
        }

        if (!currentState.equals(lastAnimationState)) {
            animationTimer = 0;
            currentRunFrame = 0;
            currentIdleFrame = 0;
            lastAnimationState = currentState;
            if (currentState.equals("jump")) {
                updatePlayerTexture("sprites/jump.png");
                return;
            }
        }

        animationTimer += tpf;

        if (currentState.equals("run")) {
            if (animationTimer >= animationFrameDuration) {
                animationTimer -= animationFrameDuration;
                currentRunFrame = (currentRunFrame + 1) % 4;
                updatePlayerTexture("sprites/player_run" + (currentRunFrame + 1) + ".png");
            }
        } else if (currentState.equals("idle")) {
            if (animationTimer >= animationFrameDuration) {
                animationTimer -= animationFrameDuration;
                currentIdleFrame = (currentIdleFrame + 1) % 2;
                updatePlayerTexture("sprites/player_idle" + (currentIdleFrame + 1) + ".png");
            }
        }
    }

    private void updatePlayerTexture(String textureName) {
        if (textureContainer == null) {
            return;
        }
        try {
            textureContainer.getChildren().clear();
            Texture newTexture = texture(textureName, GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE);
            textureContainer.getChildren().add(newTexture);
        } catch (RuntimeException e) {
            System.err.println("Could not update player texture: " + textureName + " - " + e.getMessage());
        }
    }
}
