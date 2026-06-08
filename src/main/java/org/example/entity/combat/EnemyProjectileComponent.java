package org.example.entity.combat;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import org.example.config.GameConfig;
import org.example.entity.EntityType;
import org.example.entity.player.PlayerComponent;
import org.example.util.TimeUtil;

import static com.almasb.fxgl.dsl.FXGL.getGameWorld;

public class EnemyProjectileComponent extends Component {

    private final double directionX;
    private final double directionY;
    private final int damage;
    private final double speed;

    public EnemyProjectileComponent(double directionX, double directionY, int damage, double speed) {
        double len = Math.sqrt(directionX * directionX + directionY * directionY);
        if (len == 0) {
            this.directionX = -1;
            this.directionY = 0;
        } else {
            this.directionX = directionX / len;
            this.directionY = directionY / len;
        }
        this.damage = damage;
        this.speed = speed;
    }

    @Override
    public void onUpdate(double tpf) {
        double dt = TimeUtil.stableDelta(tpf);
        entity.translateX(directionX * speed * dt);
        entity.translateY(directionY * speed * dt);

        if (entity.getX() < -100 || entity.getX() > GameConfig.WORLD_WIDTH + 100
                || entity.getY() < -100 || entity.getY() > GameConfig.WORLD_HEIGHT + 100) {
            entity.removeFromWorld();
            return;
        }

        for (Entity player : getGameWorld().getEntitiesByType(EntityType.PLAYER)) {
            PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
            if (playerComponent != null && !playerComponent.isDead() && intersects(player)) {
                playerComponent.takeDamage(damage, directionX);
                entity.removeFromWorld();
                return;
            }
        }

        if (hitsWorldGeometry()) {
            entity.removeFromWorld();
        }
    }

    private boolean hitsWorldGeometry() {
        return entity.getY() + entity.getHeight() >= GameConfig.GROUND_Y
                || intersectsAny(EntityType.OBSTACLE)
                || intersectsAny(EntityType.PLATFORM)
                || intersectsAny(EntityType.GROUND);
    }

    private boolean intersectsAny(EntityType type) {
        for (Entity target : getGameWorld().getEntitiesByType(type)) {
            if (intersects(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean intersects(Entity target) {
        double left = entity.getX();
        double right = left + entity.getWidth();
        double top = entity.getY();
        double bottom = top + entity.getHeight();

        double targetLeft = target.getX();
        double targetRight = targetLeft + target.getWidth();
        double targetTop = target.getY();
        double targetBottom = targetTop + target.getHeight();

        return right > targetLeft && left < targetRight && bottom > targetTop && top < targetBottom;
    }
}