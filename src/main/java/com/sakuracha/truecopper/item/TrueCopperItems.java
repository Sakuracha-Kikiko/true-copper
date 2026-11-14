package com.sakuracha.truecopper.item;

import com.oceanscenery.zenith.mod_class.data_component.AttackMode;
import com.oceanscenery.zenith.mod_class.data_component.Distance;
import com.oceanscenery.zenith.mod_class.data_component.LastUseTime;
import com.oceanscenery.zenith.mod_class.item.ZenithItem;
import com.oceanscenery.zenith.registry.ZenithDataComponents;
import com.sakuracha.truecopper.TrueCopper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TrueCopperItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TrueCopper.MOD_ID);

    public static final DeferredItem<ZenithSkinItem> TRUE_COPPER_SHORTSWORD = ITEMS.registerItem(
            "true_copper_shortsword",
            properties -> new ZenithSkinItem(swordProperties(properties), ZenithSkin.COPPER)
    );

    public static final DeferredItem<ZenithSkinItem> TRUE_WOODEN_SWORD = ITEMS.registerItem(
            "true_wooden_sword",
            properties -> new ZenithSkinItem(swordProperties(properties), ZenithSkin.WOODEN)
    );

    private TrueCopperItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    private static Item.Properties swordProperties(Item.Properties properties) {
        // Same components as the real Zenith Sword, so the items are identical in combat.
        return properties.stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant()
                .setNoRepair()
                .component(DataComponents.TOOL, ZenithItem.createToolProperties())
                .component(ZenithDataComponents.LAST_USE_TIME.get(), new LastUseTime(0))
                .component(ZenithDataComponents.DISTANCE.get(), new Distance(20))
                .component(ZenithDataComponents.ATTACK_MODE.get(), AttackMode.LIVING_ENTITY);
    }

}
