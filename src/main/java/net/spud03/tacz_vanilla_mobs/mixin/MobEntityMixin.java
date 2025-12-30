package net.spud03.tacz_vanilla_mobs.mixin;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.spud03.tacz_vanilla_mobs.goals.GunShootGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class MobEntityMixin {

    @Shadow @Final
    protected GoalSelector goalSelector;

    @Inject(method="initGoals", at = @At("Tail"))
    protected void addCustomGoal(CallbackInfo ci) {
        if ((Object) this instanceof ZombieEntity zombie) {
            this.goalSelector.add(1, new GunShootGoal(zombie));

            System.out.println("Added goal to zombies");
        }
    }
}
