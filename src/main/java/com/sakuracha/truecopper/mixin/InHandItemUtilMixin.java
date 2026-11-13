package com.sakuracha.truecopper.mixin;

import com.oceanscenery.zenith.mod_class.item.ZenithItem;
import com.oceanscenery.zenith.util.InHandItemUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Zenith client only sends its attack packet while the held stack is the exact Zenith
 * item. The cosmetic variants extend ZenithItem, so accept any ZenithItem subclass as well.
 */
@Mixin(value = InHandItemUtil.class, remap = false)
public abstract class InHandItemUtilMixin {

    @Inject(method = "matchHoldZenith", at = @At("RETURN"), cancellable = true)
    private static void truecopper$matchCustomZenithItems(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (mainHand.getItem() instanceof ZenithItem || offHand.getItem() instanceof ZenithItem) {
            cir.setReturnValue(true);
        }
    }
}
