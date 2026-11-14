package com.sakuracha.truecopper.recipe;

import com.oceanscenery.zenith.registry.ZenithItems;
import com.sakuracha.truecopper.TrueCopperConfig;
import com.sakuracha.truecopper.item.TrueCopperItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Transmutes one sword into the next form in a crafting grid, keeping every data component
 * (enchantments, custom name, damage, ...) of the input.
 */
public final class ShimmeringTransmutationRecipe extends CustomRecipe {

    public ShimmeringTransmutationRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!TrueCopperConfig.enableShimmeringTransmutation()) {
            return false;
        }
        ItemStack ingredient = singleIngredient(input);
        return !ingredient.isEmpty() && !nextForm(ingredient).isEmpty();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack ingredient = singleIngredient(input);
        ItemStack result = nextForm(ingredient);
        return result.isEmpty() ? ItemStack.EMPTY : ingredient.transmuteCopy(result.getItem(), 1);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TrueCopperRecipes.SHIMMERING_TRANSMUTATION.get();
    }

    private static ItemStack singleIngredient(CraftingInput input) {
        if (input.ingredientCount() != 1) {
            return ItemStack.EMPTY;
        }
        // The single ingredient can sit in any slot of the grid, not just the first one.
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack nextForm(ItemStack stack) {
        if (stack.is(ZenithItems.ZENITH.get())) {
            return new ItemStack(TrueCopperItems.TRUE_COPPER_SHORTSWORD.get());
        }
        if (stack.is(TrueCopperItems.TRUE_COPPER_SHORTSWORD.get())) {
            return new ItemStack(TrueCopperItems.TRUE_WOODEN_SWORD.get());
        }
        if (stack.is(TrueCopperItems.TRUE_WOODEN_SWORD.get())) {
            return new ItemStack(ZenithItems.ZENITH.get());
        }
        return ItemStack.EMPTY;
    }
}
