package org.example.entity.pickup;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import org.example.config.GameConfig;
import org.example.entity.EntityType;
import org.example.entity.player.PlayerComponent;
import org.example.audio.AudioManager;
import org.example.ui.AssetManager;

import java.util.function.IntConsumer;

import static com.almasb.fxgl.dsl.FXGL.getGameWorld;

public class PickupComponent extends Component {

    private final PickupType type;
    private final IntConsumer scoreCallback;

    public PickupComponent(PickupType type, IntConsumer scoreCallback) {
        this.type = type;
        this.scoreCallback = scoreCallback;
    }

    @Override
    public void onUpdate(double tpf) {
        for (Entity player : getGameWorld().getEntitiesByType(EntityType.PLAYER)) {
            if (!intersects(player)) {
                continue;
            }

            PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
            if (playerComponent == null || playerComponent.isDead()) {
                return;
            }

            collect(playerComponent);
            entity.removeFromWorld();
            return;
        }
    }

    private void collect(PlayerComponent playerComponent) {
        if (type == PickupType.COIN) {
            AudioManager.playSound(AssetManager.SFX_COIN);
            if (scoreCallback != null) {
                scoreCallback.accept(GameConfig.COIN_SCORE);
            }
        } else {
            playerComponent.heal(GameConfig.HEART_HEAL);
        }
    }

    private boolean intersects(Entity target) {
        double left = entity.getX();
        double right = left + GameConfig.PICKUP_SIZE;
        double top = entity.getY();
        double bottom = top + GameConfig.PICKUP_SIZE;

        double targetLeft = target.getX();
        double targetRight = targetLeft + GameConfig.PLAYER_SIZE;
        double targetTop = target.getY();
        double targetBottom = targetTop + GameConfig.PLAYER_SIZE;

        return right > targetLeft && left < targetRight && bottom > targetTop && top < targetBottom;
    }
}
