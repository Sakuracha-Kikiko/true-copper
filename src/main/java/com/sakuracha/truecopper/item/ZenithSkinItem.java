package com.sakuracha.truecopper.item;

import com.oceanscenery.zenith.mod_class.data_component.LastUseTime;
import com.oceanscenery.zenith.mod_class.item.ZenithItem;
import com.oceanscenery.zenith.registry.ZenithDataComponents;
import com.oceanscenery.zenith.registry.ZenithItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A Zenith Sword with a different item texture and projectile skin. All combat behaviour
 * (damage, reach, cooldown, enchantments, blacklist, deferred damage) is inherited from
 * {@link ZenithItem}.
 */
public class ZenithSkinItem extends ZenithItem {

    private final ZenithSkin skin;

    public ZenithSkinItem(Properties properties, ZenithSkin skin) {
        super(properties);
        this.skin = skin;
    }

    public ZenithSkin skin() {
        return this.skin;
    }

    @Override
    public void attack(ItemStack usedItem, LivingEntity attacker, Level level, InteractionHand hand) {
        if (level.isClientSide()) {
            return;
        }
        // The Zenith damage filter only accepts the real Zenith item, so run the swing on a
        // ZENITH stack that carries over every component of this stack (enchantments, name,
        // attack mode, distance and the cooldown timestamp). Everything else is Zenith code.
        ItemStack zenithStack = new ItemStack(ZenithItems.ZENITH.get());
        zenithStack.applyComponents(usedItem.getComponents());
        super.attack(zenithStack, attacker, level, hand);

        LastUseTime lastUseTime = zenithStack.get(ZenithDataComponents.LAST_USE_TIME);
        if (lastUseTime != null) {
            usedItem.set(ZenithDataComponents.LAST_USE_TIME, lastUseTime);
        }
    }

    @Override
    public void addEntity(Level level, LivingEntity owner, ItemStack stack, double distance, int type) {
        // ZenithItem.attack calls this for the first blade and for the two delayed blades,
        // so mapping the type here gives every blade this skin.
        super.addEntity(level, owner, stack, distance, this.skin.projectileType());
    }
}
