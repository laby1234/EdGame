package org.example.entity;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Point2D;
import org.example.config.GameConfig;
import org.example.entity.combat.ArrowComponent;
import org.example.entity.enemy.EnemyComponent;
import org.example.entity.pickup.PickupComponent;
import org.example.entity.pickup.PickupType;
import org.example.entity.player.PlayerComponent;

import java.util.function.IntConsumer;
import java.util.function.BiConsumer;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.texture;

public class EntityFactory {

    public static Entity createGround() {
        int cols = (int) Math.ceil((double) GameConfig.WORLD_WIDTH / GameConfig.TILE_SIZE);
        int rows = (int) Math.ceil((double) GameConfig.GROUND_HEIGHT / GameConfig.TILE_SIZE);

        Pane groundView = new Pane();
        groundView.setPrefSize(cols * GameConfig.TILE_SIZE, rows * GameConfig.TILE_SIZE);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String tileName = row == 0 ? "blocks/grass.png" : "blocks/dirt.png";
                Texture tile = texture(tileName, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                tile.setTranslateX(col * GameConfig.TILE_SIZE);
                tile.setTranslateY(row * GameConfig.TILE_SIZE);
                groundView.getChildren().add(tile);
            }
        }

        return entityBuilder()
                .type(EntityType.GROUND)
                .at(0, GameConfig.GROUND_Y)
                .view(groundView)
                .buildAndAttach();
    }

    public static Entity createPlayer() {
        PlayerComponent playerComponent = new PlayerComponent();
        StackPane container = new StackPane();
        container.setPrefSize(GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE);
        container.getChildren().add(texture("sprites/player_idle1.png", GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE));
        playerComponent.setTextureContainer(container);

        return entityBuilder()
                .type(EntityType.PLAYER)
                .at(GameConfig.PLAYER_START_X, GameConfig.PLAYER_START_Y)
                .view(container)
                .with(playerComponent)
                .buildAndAttach();
    }

    public static Entity createPlatform(double x, double y, int widthInTiles) {
        double w = widthInTiles * GameConfig.TILE_SIZE;
        double h = GameConfig.TILE_SIZE;

        Pane view = new Pane();
        view.setPrefSize(w, h);

        for (int i = 0; i < widthInTiles; i++) {
            Texture tile = texture("blocks/grass.png", GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
            tile.setTranslateX(i * GameConfig.TILE_SIZE);
            view.getChildren().add(tile);
        }

        return entityBuilder()
                .type(EntityType.PLATFORM)
                .at(x, y)
                .view(view)
                .bbox(new HitBox(BoundingShape.box(w, h)))
                .buildAndAttach();
    }

    public static Entity createObstacle(double x, double y) {
        double size = GameConfig.TILE_SIZE;
        Texture spikes = texture("blocks/spikes.png", size, size);

        double hitboxInsetX = 6;
        double hitboxInsetY = 10;
        double hitboxW = size - hitboxInsetX * 2;
        double hitboxH = size - hitboxInsetY;

        return entityBuilder()
                .type(EntityType.OBSTACLE)
                .at(x, y)
                .view(spikes)
                .bbox(new HitBox(new Point2D(hitboxInsetX, hitboxInsetY), BoundingShape.box(hitboxW, hitboxH)))
                .buildAndAttach();
    }

    public static Entity createEnemy(double x, double y, double patrolDistance, Entity player, PlayerComponent playerComponent,
                                     IntConsumer scoreCallback, BiConsumer<Integer, Entity> feedbackCallback) {
        double leftBound = Math.max(0, x - patrolDistance);
        double rightBound = Math.min(GameConfig.WORLD_WIDTH - GameConfig.ENEMY_WIDTH, x + patrolDistance);
        return createEnemy(x, y, leftBound, rightBound, player, playerComponent, scoreCallback, feedbackCallback);
    }

    public static Entity createEnemy(double x, double y, double leftBound, double rightBound, Entity player, PlayerComponent playerComponent,
                                     IntConsumer scoreCallback, BiConsumer<Integer, Entity> feedbackCallback) {
        Pane enemyView = new Pane();
        enemyView.setPrefSize(GameConfig.ENEMY_WIDTH, GameConfig.ENEMY_HEIGHT);

        Texture bodyTexture = texture("sprites/skeleton.png", GameConfig.ENEMY_WIDTH, GameConfig.ENEMY_HEIGHT);

        Rectangle healthBack = new Rectangle(4, -9, 36, 5);
        healthBack.setFill(Color.web("#2C1810"));
        healthBack.setStroke(Color.web("#1C0F08"));
        healthBack.setStrokeWidth(1);

        Rectangle healthFill = new Rectangle(4, -9, 36, 5);
        healthFill.setFill(Color.web("#4FBF5F"));

        enemyView.getChildren().addAll(bodyTexture, healthBack, healthFill);

        double hitboxInsetX = 8;
        double hitboxInsetY = 6;
        double hitboxW = GameConfig.ENEMY_WIDTH - hitboxInsetX * 2;
        double hitboxH = GameConfig.ENEMY_HEIGHT - hitboxInsetY * 2;

        return entityBuilder()
                .type(EntityType.ENEMY)
                .at(x, y)
                .view(enemyView)
                .bbox(new HitBox(new Point2D(hitboxInsetX, hitboxInsetY), BoundingShape.box(hitboxW, hitboxH)))
                .with(new EnemyComponent(player, playerComponent, leftBound, rightBound, healthFill, scoreCallback, feedbackCallback))
                .buildAndAttach();
    }

    public static Entity createArrow(double x, double y, double directionX, double directionY, int damage, double speed) {
        Texture arrowView = texture("blocks/arrow.png", 34, 8);
        arrowView.setRotate(Math.toDegrees(Math.atan2(directionY, directionX)));

        return entityBuilder()
                .type(EntityType.PLAYER_ARROW)
                .at(x, y)
                .view(arrowView)
                .bbox(new HitBox(BoundingShape.box(34, 8)))
                .with(new ArrowComponent(directionX, directionY, damage, speed))
                .buildAndAttach();
    }

    public static Entity createCoin(double x, double y, IntConsumer scoreCallback) {
        Texture coin = texture("blocks/coin.png", GameConfig.PICKUP_SIZE, GameConfig.PICKUP_SIZE);

        return entityBuilder()
                .type(EntityType.PICKUP)
                .at(x, y)
                .view(coin)
                .bbox(new HitBox(BoundingShape.box(GameConfig.PICKUP_SIZE, GameConfig.PICKUP_SIZE)))
                .with(new PickupComponent(PickupType.COIN, scoreCallback))
                .buildAndAttach();
    }

    public static Entity createHeart(double x, double y) {
        Texture heart = texture("blocks/heart.png", GameConfig.PICKUP_SIZE, GameConfig.PICKUP_SIZE);

        return entityBuilder()
                .type(EntityType.PICKUP)
                .at(x, y)
                .view(heart)
                .bbox(new HitBox(BoundingShape.box(GameConfig.PICKUP_SIZE, GameConfig.PICKUP_SIZE)))
                .with(new PickupComponent(PickupType.HEART, null))
                .buildAndAttach();
    }

    public static PlayerComponent getPlayerComponent(Entity player) {
        return player.getComponent(PlayerComponent.class);
    }
}
