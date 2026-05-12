package org.example;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.onKey;
import static com.almasb.fxgl.dsl.FXGL.onKeyDown;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.texture;


public class Main extends GameApplication {

    private PlayerComponent playerComponent;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("EdGame");
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.A,     () -> playerComponent.moveLeft());
        onKey(KeyCode.LEFT,  () -> playerComponent.moveLeft());
        onKey(KeyCode.D,     () -> playerComponent.moveRight());
        onKey(KeyCode.RIGHT, () -> playerComponent.moveRight());

        onKeyDown(KeyCode.W,     () -> playerComponent.requestJump());
        onKeyDown(KeyCode.UP,    () -> playerComponent.requestJump());
        onKeyDown(KeyCode.SPACE, () -> playerComponent.requestJump());
    }

    @Override
    protected void initGame() {
        getGameScene().setBackgroundColor(Color.WHITE);

        entityBuilder()
                .at(0, 500)
                .view(new Rectangle(800, 100, Color.DARKGREEN))
                .buildAndAttach();

        playerComponent = new PlayerComponent();
        entityBuilder()
                .at(100, 300)
                .view(texture("player.png", 40, 40))
                .with(playerComponent)
                .buildAndAttach();
    }

    public static class PlayerComponent extends Component {

        private static final double SPEED    = 3;
        private static final double GRAVITY  = 0.3;
        private static final double JUMP_STR = -12;
        private static final double GROUND_Y = 500;
        private static final double SIZE     = 40;

        private double vy        = 0;
        private boolean onGround = false;
        private boolean jumpReq  = false;

        public void moveLeft() {
            entity.translateX(-SPEED);
            clampX();
        }

        public void moveRight() {
            entity.translateX(SPEED);
            clampX();
        }

        private void clampX() {
            if (entity.getX() < 0)          entity.setX(0);
            if (entity.getX() + SIZE > 800)  entity.setX(800 - SIZE);
        }

        public void requestJump() {
            jumpReq = true;
        }

        @Override
        public void onUpdate(double tpf) {
            vy += GRAVITY;
            entity.translateY(vy);

            if (entity.getY() < 0) {
                entity.setY(0);
                vy = 0;
            }

            if (entity.getY() + SIZE >= GROUND_Y) {
                entity.setY(GROUND_Y - SIZE);
                vy       = 0;
                onGround = true;
            } else {
                onGround = false;
            }

            if (jumpReq) {
                if (onGround) vy = JUMP_STR;
                jumpReq = false;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}