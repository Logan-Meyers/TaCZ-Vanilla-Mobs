package net.spud03.tacz_vanilla_mobs.mixin;

import com.tacz.guns.entity.shooter.LivingEntityAim;
import com.tacz.guns.entity.shooter.LivingEntityShoot;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import net.spud03.tacz_vanilla_mobs.goals.GunShootGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieEntity.class)
public class ZombieEntityMixin extends HostileEntity {
    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method="initGoals", at = @At("TAIL"))
    protected void addCustomGoal(CallbackInfo ci) {
        if ((Object) this instanceof ZombieEntity zombie) {
//            this.goalSelector.add(3, new GunShootGoal(zombie));
            this.goalSelector.add(3, new GunShootGoal(zombie));

            System.out.println("Added goal to zombie");
        }
    }
}
