package org.example.entity.enemy;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.config.GameConfig;
import org.example.entity.EntityType;
import org.example.entity.player.PlayerComponent;
import org.example.util.TimeUtil;

import java.util.function.IntConsumer;
import java.util.function.BiConsumer;

import static com.almasb.fxgl.dsl.FXGL.getGameWorld;

public class EnemyComponent extends Component {

    private final Entity player;
    private final PlayerComponent playerComponent;
    private final double leftBound;
    private final double rightBound;
    private final Rectangle healthFill;
    private final IntConsumer scoreCallback;
    private final BiConsumer<Integer, Entity> feedbackCallback;

    private int health = GameConfig.ENEMY_MAX_HEALTH;
    private double direction = -1;
    private double attackTimer = 0;
    private double knockbackVelocity = 0;
    private double flashTimer = 0;

    public EnemyComponent(Entity player, PlayerComponent playerComponent, double leftBound, double rightBound,
                          Rectangle healthFill, IntConsumer scoreCallback, BiConsumer<Integer, Entity> feedbackCallback) {
        this.player = player;
        this.playerComponent = playerComponent;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.healthFill = healthFill;
        this.scoreCallback = scoreCallback;
        this.feedbackCallback = feedbackCallback;
    }

    @Override
    public void onUpdate(double tpf) {
        double dt = TimeUtil.stableDelta(tpf);
        attackTimer = Math.max(0, attackTimer - dt);
        flashTimer = Math.max(0, flashTimer - dt);
        updateHealthBar();
        resolveObstacleOverlap();

        if (Math.abs(knockbackVelocity) > 1) {
            moveByKnockback(dt);
            return;
        }

        if (player == null || playerComponent == null || playerComponent.isDead()) {
            patrol(dt);
            updateFacing();
            return;
        }

        double playerCenterX = player.getX() + GameConfig.PLAYER_SIZE / 2.0;
        double enemyCenterX = entity.getX() + GameConfig.ENEMY_WIDTH / 2.0;
        double distanceX = playerCenterX - enemyCenterX;
        double distanceY = Math.abs(player.getY() - entity.getY());
        double attackRange = Math.max(18, GameConfig.ENEMY_ATTACK_RANGE - 10);

        if (Math.abs(distanceX) <= GameConfig.ENEMY_AGGRO_RANGE && distanceY < GameConfig.TILE_SIZE * 2.0) {
            direction = Math.signum(distanceX);
            if (direction == 0) {
                direction = 1;
            }

            if (Math.abs(distanceX) > attackRange) {
                move(direction, dt);
            } else if (attackTimer <= 0) {
                playerComponent.takeDamage(GameConfig.PLAYER_CONTACT_DAMAGE, Math.signum(distanceX));
                attackTimer = GameConfig.ENEMY_ATTACK_COOLDOWN;
            }
        } else {
            patrol(dt);
        }

        updateFacing();
    }

    private void updateFacing() {
        double scaleX = direction >= 0 ? 1 : -1;
        entity.getViewComponent().getChildren().forEach(node -> node.setScaleX(scaleX));
    }

    public void takeDamage(int damage, double hitDirection) {
        health = Math.max(0, health - damage);
        knockbackVelocity = (hitDirection < 0 ? -1 : 1) * GameConfig.ENEMY_KNOCKBACK;
        flashHit();
        if (feedbackCallback != null) {
            feedbackCallback.accept(damage, entity);
        }
        updateHealthBar();
        if (health == 0) {
            if (scoreCallback != null) {
                scoreCallback.accept(GameConfig.ENEMY_SCORE);
            }
            entity.removeFromWorld();
        }
    }

    public boolean isAlive() {
        return health > 0;
    }

    private void patrol(double dt) {
        move(direction, dt);
        if (entity.getX() <= leftBound) {
            entity.setX(leftBound);
            direction = 1;
        } else if (entity.getX() >= rightBound) {
            entity.setX(rightBound);
            direction = -1;
        }
    }

    private void move(double moveDirection, double dt) {
        double dx = moveDirection * GameConfig.ENEMY_SPEED * dt;
        if (wouldLeavePatrolBounds(dx)) {
            direction = -direction;
            return;
        }
        if (wouldHitObstacle(dx)) {
            direction = -direction;
            return;
        }
        entity.translateX(dx);
    }

    private void moveByKnockback(double dt) {
        double dx = knockbackVelocity * dt;
        if (!wouldLeavePatrolBounds(dx) && !wouldHitObstacle(dx)) {
            entity.translateX(dx);
        }
        knockbackVelocity *= Math.pow(0.08, dt);
    }

    private boolean wouldLeavePatrolBounds(double dx) {
        double nextX = entity.getX() + dx;
        if (nextX < leftBound) {
            entity.setX(leftBound);
            return true;
        }
        if (nextX > rightBound) {
            entity.setX(rightBound);
            return true;
        }
        return false;
    }

    private boolean wouldHitObstacle(double dx) {
        double nextLeft = entity.getX() + dx + 2;
        double nextRight = entity.getX() + dx + GameConfig.ENEMY_WIDTH - 2;
        double nextTop = entity.getY() + 4;
        double nextBottom = entity.getY() + GameConfig.ENEMY_HEIGHT - 2;

        for (Entity obstacle : getGameWorld().getEntitiesByType(EntityType.OBSTACLE)) {
            double obstacleLeft = obstacle.getX();
            double obstacleRight = obstacleLeft + obstacle.getWidth();
            double obstacleTop = obstacle.getY();
            double obstacleBottom = obstacleTop + obstacle.getHeight();

            if (nextRight > obstacleLeft && nextLeft < obstacleRight && nextBottom > obstacleTop && nextTop < obstacleBottom) {
                return true;
            }
        }
        return false;
    }

    private void resolveObstacleOverlap() {
        for (Entity obstacle : getGameWorld().getEntitiesByType(EntityType.OBSTACLE)) {
            double obstacleLeft = obstacle.getX();
            double obstacleRight = obstacleLeft + obstacle.getWidth();
            double obstacleTop = obstacle.getY();
            double obstacleBottom = obstacleTop + obstacle.getHeight();

            double enemyLeft = entity.getX() + 2;
            double enemyRight = entity.getX() + GameConfig.ENEMY_WIDTH - 2;
            double enemyTop = entity.getY() + 4;
            double enemyBottom = entity.getY() + GameConfig.ENEMY_HEIGHT - 2;

            boolean overlaps = enemyRight > obstacleLeft
                    && enemyLeft < obstacleRight
                    && enemyBottom > obstacleTop
                    && enemyTop < obstacleBottom;

            if (!overlaps) {
                continue;
            }

            double enemyCenterX = entity.getX() + GameConfig.ENEMY_WIDTH / 2.0;
            double obstacleCenterX = obstacleLeft + obstacle.getWidth() / 2.0;
            if (enemyCenterX < obstacleCenterX) {
                entity.setX(Math.max(leftBound, obstacleLeft - GameConfig.ENEMY_WIDTH));
                direction = -1;
            } else {
                entity.setX(Math.min(rightBound, obstacleRight));
                direction = 1;
            }
        }
    }

    private void updateHealthBar() {
        if (flashTimer > 0) {
            healthFill.setFill(Color.web("#FFFFFF"));
            return;
        }

        double healthPercent = (double) health / GameConfig.ENEMY_MAX_HEALTH;
        healthFill.setWidth(36 * healthPercent);
        if (healthPercent <= 0.35) {
            healthFill.setFill(Color.web("#D94A38"));
        } else if (healthPercent <= 0.65) {
            healthFill.setFill(Color.web("#E0A82E"));
        } else {
            healthFill.setFill(Color.web("#4FBF5F"));
        }
    }

    private void flashHit() {
        flashTimer = 0.12;
    }
}
