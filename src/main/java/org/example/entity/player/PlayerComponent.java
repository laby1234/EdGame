package org.example.entity.player;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.config.GameConfig;
import org.example.entity.EntityFactory;
import org.example.entity.EntityType;
import org.example.entity.enemy.EnemyComponent;
import org.example.util.TimeUtil;

import java.util.ArrayList;
import java.util.function.IntConsumer;
import java.util.function.Consumer;

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
    private double animationFrameDuration = 0.15; // Duration per frame in seconds
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

    private StackPane textureContainer;
    private final Rectangle swordView = createSwordView();

    public void setTextureContainer(StackPane container) {
        this.textureContainer = container;
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
        notifyWeaponChanged();
    }

    public void startWeaponAction() {
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
            if (enemyComponent != null && enemyComponent.isAlive() && intersects(enemy, attackLeft, attackRight, attackTop, attackBottom)) {
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
    }

    private void releaseBowShot() {
        if (!chargingBow) {
            return;
        }

        chargingBow = false;
        arrowCooldown = GameConfig.ARROW_COOLDOWN;
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

    @Override
    public void onUpdate(double tpf) {
        if (dead) return;
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

        // Update position based on current key states
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
            updateFacingFromMouse();
        } catch (RuntimeException e) {
            System.err.println("Could not update player facing direction: " + e.getMessage());
        }

        updateAnimation(tpf);
        updateDamageFlash();
        updateSwordView();
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
        health = 0;
        notifyHealthChanged();
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
            updateSwordView();
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
            onWeaponChanged.accept(activeWeapon == Weapon.SWORD ? "Sword" : "Bow");
        }
    }

    private void updateSwordView() {
        if (textureContainer == null) {
            return;
        }

        boolean shouldShow = swordVisibleTimer > 0;
        if (shouldShow && !textureContainer.getChildren().contains(swordView)) {
            textureContainer.getChildren().add(swordView);
        } else if (!shouldShow) {
            textureContainer.getChildren().remove(swordView);
        }

        swordView.setTranslateX(facingRight ? 28 : -28);
        swordView.setTranslateY(13);
        swordView.setRotate(facingRight ? -18 : 18);
    }

    private void updateDamageFlash() {
        if (textureContainer == null) {
            return;
        }
        textureContainer.setOpacity(flashTimer > 0 ? 0.45 : 1.0);
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

    private static Rectangle createSwordView() {
        Rectangle sword = new Rectangle(34, 6);
        sword.setFill(Color.web("#DDE6F0"));
        sword.setStroke(Color.web("#FFFFFF"));
        sword.setStrokeWidth(1);
        sword.setArcWidth(3);
        sword.setArcHeight(3);
        return sword;
    }
}
