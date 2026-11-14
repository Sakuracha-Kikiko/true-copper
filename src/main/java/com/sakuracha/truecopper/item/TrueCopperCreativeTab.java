package com.sakuracha.truecopper.item;

import com.sakuracha.truecopper.TrueCopper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TrueCopperCreativeTab {

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TrueCopper.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TRUE_COPPER = TABS.register(
            "true_copper",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.true_copper"))
                    .icon(() -> new ItemStack(TrueCopperItems.TRUE_COPPER_SHORTSWORD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(TrueCopperItems.TRUE_COPPER_SHORTSWORD.get());
                        output.accept(TrueCopperItems.TRUE_WOODEN_SWORD.get());
                    })
                    .build()
    );

    private TrueCopperCreativeTab() {
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
