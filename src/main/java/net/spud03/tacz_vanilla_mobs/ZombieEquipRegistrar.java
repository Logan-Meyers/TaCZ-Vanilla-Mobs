package net.spud03.tacz_vanilla_mobs;

import cn.sh1rocu.tacz.api.event.EntityJoinLevelEvent;
import com.google.common.eventbus.Subscribe;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.item.ModernKineticGunItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombieEquipRegistrar {
    private static final Random RNG = new Random();
    private static final double CHANCE = 0.25;  // 25%
    private static final Identifier GUN_ITEM_ID = new Identifier("tacz", "modern_kinetic_gun");
    private static final List<ItemStack> TEMPLATES = new ArrayList<>();

    public static void register() {
        // once server starts, build templates
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            buildTemplates();
            TaCZVanillaMobs.LOGGER.info("Built templates after server started");
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(world instanceof ServerWorld)) return;
            if (!(entity instanceof ZombieEntity zombie)) return;
//            if (RNG.nextDouble() > CHANCE) return;
            if (TEMPLATES.isEmpty()) {
                TaCZVanillaMobs.LOGGER.warn("Templates are empty!");
            }

            ItemStack template = TEMPLATES.get(RNG.nextInt(TEMPLATES.size()));
            ItemStack give = template.copy();

            zombie.equipStack(EquipmentSlot.MAINHAND, give);
            zombie.setCanPickUpLoot(true);
            zombie.setEquipmentDropChance(EquipmentSlot.MAINHAND, 2.0f);  // should be pretty rare

            // add shooting goals and AI
        });
    }

//    @Shadow @Final
//    protected GoalSelector goalSelector;
//
//    @Subscribe
//    public static void EntityJoined(EntityJoinLevelEvent event) {
//        TaCZVanillaMobs.LOGGER.info("Entity joined world!");
//        Entity entity = event.getEntity();
//        if (entity instanceof ZombieEntity) {
//            ZombieEntity zombie = (ZombieEntity) entity;
//
//        }
//    }

    private static void buildTemplates() {
        TEMPLATES.clear();
        TEMPLATES.add(makeGunStack(true, FireMode.AUTO, new Identifier("tacz", "cz75"), 16));
        TEMPLATES.add(makeGunStack(true, FireMode.SEMI, new Identifier("tacz", "p320"), 10));
    }

    private static ItemStack makeGunStack(boolean hasBullet,
                                          FireMode fireMode,
                                          Identifier gunId,
                                          int ammoCount) {

        // try to get item for modern_kinetic_gun
        Item item = Registries.ITEM.getOrEmpty(GUN_ITEM_ID).orElse(Items.AIR);

        // make sure item exists
        if (item == Items.AIR) {
            TaCZVanillaMobs.LOGGER.warn("modern_kinetic_gun not present");
            return ItemStack.EMPTY;  // item not present, so return empty list
        }

        // return if for some reason the item is not actually an instance of the class we want
        if (!(item instanceof ModernKineticGunItem gun)) {
            TaCZVanillaMobs.LOGGER.warn("Registered item is not ModernKineticGunItem: " +
                    item.getClass().getName());
            return ItemStack.EMPTY;  // invalid item for gun, so return empty list
        }

        // create the stack for the given gun
        ItemStack stack = new ItemStack(gun);
        gun.setBulletInBarrel(stack, hasBullet);
        gun.setCurrentAmmoCount(stack, ammoCount);
        gun.setFireMode(stack, fireMode);
        gun.setGunId(stack, gunId);

        // success!
        TaCZVanillaMobs.LOGGER.info("Created template for " + gunId);
        return stack;
    }
}
