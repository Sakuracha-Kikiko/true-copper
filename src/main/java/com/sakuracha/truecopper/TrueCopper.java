package com.sakuracha.truecopper;

import com.sakuracha.truecopper.client.ClientSetup;
import com.sakuracha.truecopper.item.TrueCopperCreativeTab;
import com.sakuracha.truecopper.item.TrueCopperItems;
import com.sakuracha.truecopper.network.TrueCopperNetwork;
import com.sakuracha.truecopper.recipe.TrueCopperRecipes;
import com.sakuracha.truecopper.sound.ModSounds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * True Copper - quality of life features and compatibilities for The Zenith Sword.
 */
@Mod(TrueCopper.MOD_ID)
public final class TrueCopper {

    public static final String MOD_ID = "true_copper";

    public TrueCopper(ModContainer container, IEventBus modEventBus) {
        ModSounds.SOUNDS.register(modEventBus);
        TrueCopperItems.register(modEventBus);
        TrueCopperCreativeTab.register(modEventBus);
        TrueCopperRecipes.register(modEventBus);
        TrueCopperNetwork.register(modEventBus);
        container.registerConfig(ModConfig.Type.COMMON, TrueCopperConfig.SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.registerConfigScreen(container);
        }
    }
}
