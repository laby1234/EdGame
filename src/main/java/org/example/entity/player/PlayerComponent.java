package org.example.entity.player;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.layout.StackPane;
import org.example.audio.AudioManager;
import org.example.config.GameConfig;
import org.example.entity.EntityFactory;
import org.example.entity.EntityType;
import org.example.entity.enemy.EnemyComponent;
import org.example.ui.AssetManager;
import org.example.util.TimeUtil;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static com.almasb.fxgl.dsl.FXGL.play;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.getInput;
import static com.almasb.fxgl.dsl.FXGL.texture;

public class PlayerComponent extends Component {

    private enum Weapon {
        SWORD, BOW
    }

    private double vy = 0;
    private int health = GameConfig.PLAYER_MAX_HEALTH;
    private boolean onGround = false;
    private boolean jumpReq = false;
    private boolean dead = false;
    private Runnable onDeath;
    private IntConsumer onHealthChanged;
    private Consumer<String> onWeaponChanged;

    private double animationTimer = 0;
    private final double animationFrameDuration = 0.15;
    private int currentRunFrame = 0;
    private int currentIdleFrame = 0;
    private String lastAnimationState = "idle";

    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean facingRight = true;
    private double swordCooldown = 0;
    private double arrowCooldown = 0;
    private double damageCooldown = 0;
    private double swordVisibleTimer = 0;
    private double bowChargeTime = 0;
    private double knockbackVelocityX = 0;
    private double flashTimer = 0;
    private boolean chargingBow = false;
    private Weapon activeWeapon = Weapon.SWORD;
    private boolean carryingChest = false;
    private boolean inputEnabled = true;

    private StackPane textureContainer;
    private Texture playerView;
    private final Texture swordView = createSwordView();
    private final Texture bowView = createBowView();
    private final Texture chestCarryView = createChestCarryView();

    public void setTextureContainer(StackPane container) {
        this.textureContainer = container;

        if (!container.getChildren().isEmpty() && container.getChildren().get(0) instanceof Texture texture) {
            this.playerView = texture;
        }
    }

    public void setOnDeath(Runnable onDeath) {
        this.onDeath = onDeath;
    }

    public void setOnHealthChanged(IntConsumer onHealthChanged) {
        this.onHealthChanged = onHealthChanged;
        notifyHealthChanged();
    }

    public void setOnWeaponChanged(Consumer<String> onWeaponChanged) {
        this.onWeaponChanged = onWeaponChanged;
        notifyWeaponChanged();
    }

    public void setMovingLeft(boolean moving) {
        if (!inputEnabled) {
            this.movingLeft = false;
            return;
        }
        this.movingLeft = moving;
    }

    public void setMovingRight(boolean moving) {
        if (!inputEnabled) {
            this.movingRight = false;
            return;
        }
        this.movingRight = moving;
    }

    public void requestJump() {
        if (!inputEnabled) {
            return;
        }
        jumpReq = true;
    }

    public boolean isDead() {
        return dead;
    }

    public int getHealth() {
        return health;
    }

    public void selectSword() {
        activeWeapon = Weapon.SWORD;
        chargingBow = false;
        bowChargeTime = 0;
        notifyWeaponChanged();
    }

    public void selectBow() {
        activeWeapon = Weapon.BOW;
        chargingBow = false;
        bowChargeTime = 0;
        notifyWeaponChanged();
    }

    public void switchWeaponByScroll(double deltaY) {
        if (dead || deltaY == 0 || carryingChest) {
            return;
        }

        if (deltaY > 0) {
            selectSword();
        } else {
            selectBow();
        }
    }

    public void startWeaponAction() {
        if (dead || carryingChest|| !inputEnabled) {
            return;
        }

        try {
            updateFacingFromMouse();
        } catch (RuntimeException e) {
            System.err.println("Could not update facing before weapon action: " + e.getMessage());
        }

        if (activeWeapon == Weapon.SWORD) {
            attackSword();
        } else {
            startBowCharge();
        }
    }

    public void releaseWeaponAction() {
        if (!inputEnabled) {
            return;
        }

        if (carryingChest) {
            chargingBow = false;
            bowChargeTime = 0;
            updateBowView();
            return;
        }

        try {
            updateFacingFromMouse();
        } catch (RuntimeException e) {
            System.err.println("Could not update facing before weapon release: " + e.getMessage());
        }

        if (activeWeapon == Weapon.BOW) {
            releaseBowShot();
        }
    }

    private void attackSword() {
        if (dead || swordCooldown > 0) {
            return;
        }

        AudioManager.playSound(AssetManager.SFX_SWORD_HIT);
        swordCooldown = GameConfig.SWORD_COOLDOWN;
        swordVisibleTimer = 0.12;
        updateSwordView();

        double attackLeft = facingRight
                ? entity.getX() + GameConfig.PLAYER_SIZE
                : entity.getX() - GameConfig.SWORD_RANGE;
        double attackRight = attackLeft + GameConfig.SWORD_RANGE;
        double attackTop = entity.getY() + 4;
        double attackBottom = entity.getY() + GameConfig.PLAYER_SIZE + 8;

        for (Entity enemy : new ArrayList<>(getGameWorld().getEntitiesByType(EntityType.ENEMY))) {
            EnemyComponent enemyComponent = enemy.getComponent(EnemyComponent.class);
            if (enemyComponent != null && enemyComponent.isAlive()
                    && intersects(enemy, attackLeft, attackRight, attackTop, attackBottom)) {
                enemyComponent.takeDamage(GameConfig.SWORD_DAMAGE, facingRight ? 1 : -1);
            }
        }
    }

    private void startBowCharge() {
        if (dead || arrowCooldown > 0) {
            return;
        }

        chargingBow = true;
        bowChargeTime = 0;
        updateBowView();
    }

    private void releaseBowShot() {
        if (!chargingBow) {
            return;
        }

        AudioManager.playSound(AssetManager.SFX_BOW_SHOOT);
        chargingBow = false;
        arrowCooldown = GameConfig.ARROW_COOLDOWN;
        updateBowView();

        double arrowX = facingRight ? entity.getX() + GameConfig.PLAYER_SIZE : entity.getX() - 34;
        double arrowY = entity.getY() + GameConfig.PLAYER_SIZE * 0.45;
        double directionX = facingRight ? 1 : -1;
        double directionY = 0;

        try {
            double targetX = getInput().getMouseXWorld();
            double targetY = getInput().getMouseYWorld();
            directionX = targetX - arrowX;
            directionY = targetY - arrowY;
        } catch (RuntimeException e) {
            System.err.println("Could not aim bow shot: " + e.getMessage());
        }

        double chargePercent = Math.min(1, bowChargeTime / GameConfig.BOW_MAX_CHARGE_TIME);
        int damage = (int) Math.round(GameConfig.ARROW_DAMAGE * (0.55 + chargePercent * 0.45));
        double speed = GameConfig.ARROW_SPEED * (0.65 + chargePercent * 0.35);

        EntityFactory.createArrow(arrowX, arrowY, directionX, directionY, damage, speed);
        bowChargeTime = 0;
    }

    public void takeDamage(int damage, double hitDirection) {
        if (dead || damageCooldown > 0) {
            return;
        }

        AudioManager.playSound(AssetManager.SFX_HURT);
        health = Math.max(0, health - damage);
        damageCooldown = GameConfig.PLAYER_DAMAGE_COOLDOWN;
        knockbackVelocityX = (hitDirection < 0 ? -1 : 1) * GameConfig.PLAYER_KNOCKBACK_X;
        vy = GameConfig.PLAYER_KNOCKBACK_Y;
        flashTimer = 0.18;
        notifyHealthChanged();

        if (health == 0) {
            die();
        }
    }

    public void heal(int amount) {
        if (dead || health >= GameConfig.PLAYER_MAX_HEALTH) {
            return;
        }

        health = Math.min(GameConfig.PLAYER_MAX_HEALTH, health + amount);
        notifyHealthChanged();
    }

    public void setCarryingChest(boolean carryingChest) {
        this.carryingChest = carryingChest;

        if (carryingChest) {
            chargingBow = false;
            bowChargeTime = 0;
            swordVisibleTimer = 0;
        }

        updateSwordView();
        updateBowView();
        updateChestCarryView();
        notifyWeaponChanged();
    }

    @Override
    public void onUpdate(double tpf) {
        if (dead) {
            return;
        }

        double dt = TimeUtil.stableDelta(tpf);

        swordCooldown = Math.max(0, swordCooldown - dt);
        arrowCooldown = Math.max(0, arrowCooldown - dt);
        damageCooldown = Math.max(0, damageCooldown - dt);
        swordVisibleTimer = Math.max(0, swordVisibleTimer - dt);
        flashTimer = Math.max(0, flashTimer - dt);

        if (chargingBow) {
            bowChargeTime = Math.min(GameConfig.BOW_MAX_CHARGE_TIME, bowChargeTime + dt);
        }

        if (Math.abs(knockbackVelocityX) > 1) {
            entity.translateX(knockbackVelocityX * dt);
            clampX();
            knockbackVelocityX *= Math.pow(0.08, dt);
        }

        if (movingLeft) {
            facingRight = false;
            entity.translateX(-GameConfig.PLAYER_SPEED * dt);
            clampX();
        }

        if (movingRight) {
            facingRight = true;
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

        if (entity.getY() > GameConfig.WORLD_HEIGHT + 200) {
            die();
            return;
        }

        double obstacleHitDirection = getObstacleHitDirection();
        if (!Double.isNaN(obstacleHitDirection)) {
            takeDamage(GameConfig.SPIKE_DAMAGE, obstacleHitDirection);
            return;
        }

        if (jumpReq) {
            if (onGround) {
                vy = GameConfig.PLAYER_JUMP_STRENGTH;
            }
            jumpReq = false;
        }

        try {
            updateFacingFromMouse();
        } catch (RuntimeException e) {
            System.err.println("Could not update player facing direction: " + e.getMessage());
        }

        updateAnimation(tpf);
        updateDamageFlash();
        updateSwordView();
        updateBowView();
        updateChestCarryView();
    }

    private void checkPlatformCollisions(double dy) {
        for (Entity platform : getGameWorld().getEntitiesByType(EntityType.PLATFORM)) {
            double pLeft = platform.getX();
            double pRight = pLeft + platform.getWidth();
            double pTop = platform.getY();

            double eLeft = entity.getX();
            double eRight = eLeft + GameConfig.PLAYER_SIZE;
            double eBottom = entity.getY() + GameConfig.PLAYER_SIZE;

            boolean hOverlap = eRight > pLeft + 2 && eLeft < pRight - 2;

            if (hOverlap && vy >= 0
                    && eBottom >= pTop
                    && eBottom <= pTop + Math.abs(dy) + GameConfig.PLAYER_SIZE * 0.5) {
                entity.setY(pTop - GameConfig.PLAYER_SIZE);
                vy = 0;
                onGround = true;
            }
        }
    }

    private double getObstacleHitDirection() {
        for (Entity obstacle : getGameWorld().getEntitiesByType(EntityType.OBSTACLE)) {
            double oLeft = obstacle.getX();
            double oRight = oLeft + obstacle.getWidth();
            double oTop = obstacle.getY();
            double oBottom = oTop + obstacle.getHeight();

            double eLeft = entity.getX() + 4;
            double eRight = entity.getX() + GameConfig.PLAYER_SIZE - 4;
            double eTop = entity.getY() + 4;
            double eBottom = entity.getY() + GameConfig.PLAYER_SIZE - 4;

            if (eRight > oLeft && eLeft < oRight && eBottom > oTop && eTop < oBottom) {
                double playerCenterX = entity.getX() + GameConfig.PLAYER_SIZE / 2.0;
                double obstacleCenterX = obstacle.getX() + obstacle.getWidth() / 2.0;
                return playerCenterX < obstacleCenterX ? -1 : 1;
            }
        }
        return Double.NaN;
    }

    private void die() {
        if (dead) {
            return;
        }

        dead = true;
        health = 0;
        notifyHealthChanged();

        if (onDeath != null) {
            onDeath.run();
        }
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
            Texture newTexture = texture(textureName, GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE);

            if (playerView != null) {
                int index = textureContainer.getChildren().indexOf(playerView);
                if (index >= 0) {
                    textureContainer.getChildren().set(index, newTexture);
                } else {
                    textureContainer.getChildren().add(0, newTexture);
                }
            } else {
                textureContainer.getChildren().add(0, newTexture);
            }

            playerView = newTexture;

            updateSwordView();
            updateBowView();
        } catch (RuntimeException e) {
            System.err.println("Could not update player texture: " + textureName + " - " + e.getMessage());
        }
    }

    private boolean intersects(Entity target, double left, double right, double top, double bottom) {
        double targetLeft = target.getX();
        double targetRight = targetLeft + target.getWidth();
        double targetTop = target.getY();
        double targetBottom = targetTop + target.getHeight();

        return right > targetLeft && left < targetRight && bottom > targetTop && top < targetBottom;
    }

    private void notifyHealthChanged() {
        if (onHealthChanged != null) {
            onHealthChanged.accept(health);
        }
    }

    private void notifyWeaponChanged() {
        if (onWeaponChanged != null) {
            if (carryingChest) {
                onWeaponChanged.accept("Chest");
            } else {
                onWeaponChanged.accept(activeWeapon == Weapon.SWORD ? "Sword" : "Bow");
            }
        }
    }

    private void updateSwordView() {
        if (carryingChest) {
            textureContainer.getChildren().remove(swordView);
            return;
        }

        if (textureContainer == null) {
            return;
        }

        boolean shouldShow = swordVisibleTimer > 0;

        if (shouldShow && !textureContainer.getChildren().contains(swordView)) {
            textureContainer.getChildren().add(swordView);
        } else if (!shouldShow) {
            textureContainer.getChildren().remove(swordView);
        }

        swordView.setTranslateX(26);
        swordView.setRotate(-18);
    }

    private void updateBowView() {
        if (carryingChest) {
            textureContainer.getChildren().remove(bowView);
            return;
        }

        if (textureContainer == null) {
            return;
        }

        boolean shouldShow = chargingBow;

        if (shouldShow && !textureContainer.getChildren().contains(bowView)) {
            textureContainer.getChildren().add(bowView);
        } else if (!shouldShow) {
            textureContainer.getChildren().remove(bowView);
        }

        bowView.setTranslateX(5);
        bowView.setRotate(0);
    }

    private void updateChestCarryView() {
        if (textureContainer == null) {
            return;
        }

        boolean shouldShow = carryingChest;

        if (shouldShow && !textureContainer.getChildren().contains(chestCarryView)) {
            textureContainer.getChildren().add(chestCarryView);
        } else if (!shouldShow) {
            textureContainer.getChildren().remove(chestCarryView);
        }

        chestCarryView.setTranslateX(0);
        chestCarryView.setTranslateY(-28);
        chestCarryView.setRotate(0);
    }

    private static Texture createSwordView() {
        return texture("blocks/sword.png", 48, 16);
    }

    private static Texture createBowView() {
        return texture("blocks/bow.png", 16, 36);
    }
    private static Texture createChestCarryView() {
        return texture("blocks/chest2.png", 34, 34);
    }

    private void updateFacingFromMouse() {
        if (textureContainer == null) {
            return;
        }

        double mouseX = getInput().getMouseXWorld();
        double playerCenterX = entity.getX() + GameConfig.PLAYER_SIZE / 2.0;
        facingRight = mouseX >= playerCenterX;
        textureContainer.setScaleX(facingRight ? 1 : -1);
    }

    private void updateDamageFlash() {
        if (textureContainer == null) {
            return;
        }

        textureContainer.setOpacity(flashTimer > 0 ? 0.45 : 1.0);
    }

    public void setInputEnabled(boolean inputEnabled) {
        this.inputEnabled = inputEnabled;
    }

    public void resetInputState() {
        movingLeft = false;
        movingRight = false;
        jumpReq = false;
        chargingBow = false;
        bowChargeTime = 0;
    }

    public void killInstantly() {
        if (dead) {
            return;
        }
        health = 0;
        die();
    }
}
