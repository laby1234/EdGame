package org.example.entity.player;

import com.almasb.fxgl.entity.component.Component;
import org.example.config.GameConfig;

public class PlayerComponent extends Component {

    private double vy = 0;
    private boolean onGround = false;
    private boolean jumpReq = false;

    public void moveLeft() {
        entity.translateX(-GameConfig.PLAYER_SPEED);
        clampX();
    }

    public void moveRight() {
        entity.translateX(GameConfig.PLAYER_SPEED);
        clampX();
    }

    private void clampX() {
        if (entity.getX() < 0) {
            entity.setX(0);
        }
        if (entity.getX() + GameConfig.PLAYER_SIZE > GameConfig.WINDOW_WIDTH) {
            entity.setX(GameConfig.WINDOW_WIDTH - GameConfig.PLAYER_SIZE);
        }
    }

    public void requestJump() {
        jumpReq = true;
    }

    @Override
    public void onUpdate(double tpf) {
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
    }
}

