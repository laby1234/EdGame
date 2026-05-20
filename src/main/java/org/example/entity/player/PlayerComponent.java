package org.example.entity.player;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.layout.StackPane;
import org.example.config.GameConfig;

import static com.almasb.fxgl.dsl.FXGL.getInput;
import static com.almasb.fxgl.dsl.FXGL.texture;

public class PlayerComponent extends Component {

    private double vy = 0;
    private boolean onGround = false;
    private boolean jumpReq = false;

    // Animation properties
    private double animationTimer = 0;
    private double animationFrameDuration = 0.15; // Duration per frame in seconds
    private int currentRunFrame = 0;
    private int currentIdleFrame = 0;
    private String lastAnimationState = "idle";
    
    // Key state tracking
    private boolean movingLeft = false;
    private boolean movingRight = false;

    private StackPane textureContainer;

    public void setTextureContainer(StackPane container) {
        this.textureContainer = container;
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

    @Override
    public void onUpdate(double tpf) {
        // Update position based on current key states
        if (movingLeft) {
            entity.translateX(-GameConfig.PLAYER_SPEED);
            clampX();
        }
        if (movingRight) {
            entity.translateX(GameConfig.PLAYER_SPEED);
            clampX();
        }
        
        vy += GameConfig.PLAYER_GRAVITY;
        entity.translateY(vy);

        if (entity.getY() < 0) {
            entity.setY(0);
            vy = 0;
        }

        if (entity.getY() + GameConfig.PLAYER_SIZE >= GameConfig.GROUND_Y) {
            entity.setY(GameConfig.GROUND_Y - GameConfig.PLAYER_SIZE);
            vy = 0;
            onGround = true;
        } else {
            onGround = false;
        }

        if (jumpReq) {
            if (onGround) {
                vy = GameConfig.PLAYER_JUMP_STRENGTH;
            }
            jumpReq = false;
        }

        // Face player toward mouse cursor
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
        } catch (Exception ignored) {
        }

        // Handle animation
        updateAnimation(tpf);
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
        // Priority: jump (in air) > run > idle
        String currentState;

        if (!onGround) {
            currentState = "jump";
        } else if (movingLeft || movingRight) {
            currentState = "run";
        } else {
            currentState = "idle";
        }

        // Reset animation if state changed
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
            // Run animation: 3 frames
            if (animationTimer >= animationFrameDuration) {
                animationTimer -= animationFrameDuration;
                currentRunFrame = (currentRunFrame + 1) % 4;
                updatePlayerTexture("sprites/player_run" + (currentRunFrame + 1) + ".png");
            }
        } else if (currentState.equals("idle")) {
            // Idle animation: 2 frames (blink)
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
        } catch (Exception e) {
            // If texture loading fails, keep current texture
        }
    }
}