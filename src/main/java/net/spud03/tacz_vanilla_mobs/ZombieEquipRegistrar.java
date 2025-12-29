package net.spud03.tacz_vanilla_mobs;

import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.item.ModernKineticGunItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombieEquipRegistrar {
    private static final Random RNG = new Random();
    private static final double CHANCE = 0.25;  // 25%
    private static final Identifier GUN_ITEM_ID = new Identifier("tacz", "modern_kinetic_gun");
    private static final List<ItemStack> TEMPLATES = new ArrayList<>();

    public static void register() {
        buildTemplates();  // commenting this fixes null

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(world instanceof ServerWorld)) return;
            if (!(entity instanceof ZombieEntity zombie)) return;
//            if (RNG.nextDouble() > CHANCE) return;
            if (TEMPLATES.isEmpty()) return;

            ItemStack template = TEMPLATES.get(RNG.nextInt(TEMPLATES.size()));
            ItemStack give = template.copy();

            zombie.equipStack(EquipmentSlot.MAINHAND, give);
            zombie.setCanPickUpLoot(true);
            zombie.setEquipmentDropChance(EquipmentSlot.MAINHAND, 2.0f);  // should be pretty rare

            // add shooting
        });
    }

    private static void buildTemplates() {
        TEMPLATES.clear();
        TEMPLATES.add(makeGunStack(true, FireMode.AUTO, new Identifier("tacz", "cz75"), 16));
        TEMPLATES.add(makeGunStack(true, FireMode.SEMI, new Identifier("tacz", "p320"), 10));
    }

    private static ItemStack makeGunStack(boolean hasBullet,
                                          FireMode fireMode,
                                          Identifier gunId,
                                          int ammoCount) {
        Item item = Registries.ITEM.get(GUN_ITEM_ID);
        if (item == Items.AIR) {
            TaCZVanillaMobs.LOGGER.warn("modern_kinetic_gun not present");
            return ItemStack.EMPTY;
        }
        if (!(item instanceof ModernKineticGunItem gun)) {
            TaCZVanillaMobs.LOGGER.warn("Registered item is not ModernKineticGunItem: " +
                    item.getClass().getName());
            return new ItemStack(item); // fallback, but won't have setters
        }

        ItemStack stack = new ItemStack(gun);
        gun.setBulletInBarrel(stack, hasBullet);
        gun.setCurrentAmmoCount(stack, ammoCount);
        gun.setFireMode(stack, fireMode);
        gun.setGunId(stack, gunId);

        TaCZVanillaMobs.LOGGER.info("Created template for " + gunId);
        return stack;
    }
}
