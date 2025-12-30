package net.spud03.tacz_vanilla_mobs.goals;

import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.util.TacHitResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import com.tacz.guns.entity.shooter.LivingEntityShoot;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.entity.shooter.LivingEntityDrawGun;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import net.spud03.tacz_vanilla_mobs.TaCZVanillaMobs;

import java.util.EnumSet;
import java.util.function.Supplier;

public class GunShootGoal extends Goal {
    private final ZombieEntity mob;
    private LivingEntity target;
    private ShooterDataHolder data;
    private LivingEntityDrawGun draw;
    private LivingEntityShoot shooterLogic;

    public GunShootGoal(ZombieEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (mob.getWorld().isClient) return false;
        target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;

        ItemStack stack = mob.getMainHandStack();
        if (stack == null) return false;
        if (!(stack.getItem() instanceof IGun)) return false;

        // ensure data exists
        if (data == null) {
            data = new ShooterDataHolder();
            data.baseTimestamp = System.currentTimeMillis();
            data.currentGunItem = () -> mob.getMainHandStack(); // supplier used in LivingEntityShoot
            data.shootTimestamp = 0L;
            data.lastShootTimestamp = 0L;
            // init other fields conservatively if referenced by LivingEntityShoot:
            data.reloadStateType = ReloadState.StateType.NOT_RELOADING;
            // ... set other fields if necessary by inspecting ShooterDataHolder class
        }

        if (draw == null) {
            draw = new LivingEntityDrawGun(mob, data); // constructor args per actual class
        }

        if (shooterLogic == null) {
            shooterLogic = new LivingEntityShoot(mob, data, draw);
        }

        return true;
    }

    @Override
    public boolean shouldContinue() {
        return canStart(); // or more refined check (target distance/visibility)
    }

    @Override
    public void tick() {
        if (mob.getWorld().isClient) return;
        // face the target
        mob.getLookControl().lookAt(target, 30.0F, 30.0F);

        // pitch/yaw suppliers as expected by LivingEntityShoot (pitch, yaw)
        Supplier<Float> pitchSupplier = () -> 0f;// target.getPitch(mob.getX(), mob.getY(), mob.getZ()); // placeholder
        Supplier<Float> yawSupplier = () -> {
            double dx = target.getX() - mob.getX();
            double dz = target.getZ() - mob.getZ();
            float yaw = (float)(Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            return yaw;
        };

        long timestamp = System.currentTimeMillis() - data.baseTimestamp;

        // call tacz shooting routine
        com.tacz.guns.api.entity.ShootResult result = shooterLogic.shoot(pitchSupplier, yawSupplier, timestamp);

        // optionally handle different results (reload, no ammo) by triggering reload logic or other actions
        if (result == com.tacz.guns.api.entity.ShootResult.NO_AMMO) {
            // maybe call reload logic (if tacz exposes it) or try to find ammo
            System.out.println("Zombie out of ammo!");
        }
    }
}
