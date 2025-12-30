package net.spud03.tacz_vanilla_mobs.goals;

import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.util.TacHitResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (mob.getWorld().isClient) return false;

        target = mob.getWorld().getClosestPlayer(mob, 32.0f);
        if (target == null || !target.isAlive()) return false;

        ItemStack stack = mob.getMainHandStack();
        if (stack == null) return false;
        if (!(stack.getItem() instanceof ModernKineticGunItem)) return false;

        // ensure data exists
        if (data == null) {
            data = new ShooterDataHolder();
            data.baseTimestamp = System.currentTimeMillis();
            data.currentGunItem = mob::getMainHandStack; // supplier used in LivingEntityShoot
            data.shootTimestamp = 0L;
            data.lastShootTimestamp = 0L;
            // init other fields conservatively if referenced by LivingEntityShoot:
            data.reloadStateType = ReloadState.StateType.NOT_RELOADING;
            // ... set other fields if necessary by inspecting ShooterDataHolder class
        }

        if (draw == null) {
            draw = new LivingEntityDrawGun(mob, data); // constructor args per actual class
            TaCZVanillaMobs.LOGGER.info("Created draw data");
        }

        if (shooterLogic == null) {
            shooterLogic = new LivingEntityShoot(mob, data, draw);
            TaCZVanillaMobs.LOGGER.info("Created shooter logic data");
        }

        return true;
    }

    @Override
    public boolean shouldContinue() {
        if (mob.getWorld().getClosestPlayer(mob, 64.0f) == null) {
            TaCZVanillaMobs.LOGGER.info("Zombie cannot continue GunShootGoal - player too far");
            return false;
        }
        return true;
    }

    @Override
    public void tick() {
        if (mob.getWorld().isClient) return;

        // face the target
        mob.getLookControl().lookAt(target, 30.0F, 30.0F);

        // pitch/yaw suppliers as expected by LivingEntityShoot (pitch, yaw)
        Supplier<Float> pitchSupplier = () -> {
            double dy = target.getY() - mob.getY();
            float pitch = (float)(dy * (180.0 / Math.PI)) - 90.0F;
            return pitch;
        };
        Supplier<Float> yawSupplier = () -> {
            double dx = target.getX() - mob.getX();
            double dz = target.getZ() - mob.getZ();
            float yaw = (float)(Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            return yaw;
        };

        long timestamp = System.currentTimeMillis() - data.baseTimestamp;

        // call tacz shooting routine
        com.tacz.guns.api.entity.ShootResult result = shooterLogic.shoot(pitchSupplier, yawSupplier, timestamp);

        TaCZVanillaMobs.LOGGER.info("Shooting result: " + result);
        net.minecraft.nbt.NbtCompound nbt = mob.getMainHandStack().getNbt();
        byte ammo = -1;
        if (nbt != null)
            ammo = nbt.getByte("GunCurrentAmmoCount");

//        TaCZVanillaMobs.LOGGER.info("Ammo left: " + ammo);

        // optionally handle different results (reload, no ammo) by triggering reload logic or other actions
        if (result == com.tacz.guns.api.entity.ShootResult.NO_AMMO) {
            // maybe call reload logic (if tacz exposes it) or try to find ammo
            System.out.println("Zombie out of ammo!");
        }

        TaCZVanillaMobs.LOGGER.info("Shooter entity: {}", mob.getType());
        TaCZVanillaMobs.LOGGER.info("Gun stack: {}", mob.getMainHandStack());
        TaCZVanillaMobs.LOGGER.info("Gun NBT: {}", mob.getMainHandStack().getNbt());
    }
}
