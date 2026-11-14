package com.sakuracha.truecopper.recipe;

import com.sakuracha.truecopper.TrueCopper;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TrueCopperRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, TrueCopper.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShimmeringTransmutationRecipe>> SHIMMERING_TRANSMUTATION =
            SERIALIZERS.register("shimmering_transmutation",
                    () -> new SimpleCraftingRecipeSerializer<>(ShimmeringTransmutationRecipe::new));

    private TrueCopperRecipes() {
    }

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }
}
