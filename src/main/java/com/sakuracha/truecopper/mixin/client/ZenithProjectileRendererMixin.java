package com.sakuracha.truecopper.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.oceanscenery.zenith.client.ZenithProjectileRenderer;
import com.oceanscenery.zenith.mod_class.entity.ZenithProjectile;
import com.oceanscenery.zenith.util.PosUtil;
import com.oceanscenery.zenith.util.Quaternion;
import com.oceanscenery.zenith.util.Vector3;
import com.sakuracha.truecopper.item.TrueCopperItems;
import com.sakuracha.truecopper.item.ZenithSkin;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Swaps the Zenith projectile's sword model and trail colour for the cosmetic skins.
 * The projectile's synced sword type carries the skin (21 = copper, 22 = wooden).
 */
@Mixin(value = ZenithProjectileRenderer.class, remap = false)
public abstract class ZenithProjectileRendererMixin {

    @Shadow
    private ItemRenderer itemRenderer;

    @Shadow
    private PoseStack swordPoses;

    @Shadow
    private static Quaternionf Q1;

    @Shadow
    private static Quaternionf Q2;

    /** Real sword type of the projectile currently being rendered. */
    private static int truecopper$currentType;

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE",
                    target = "Lcom/oceanscenery/zenith/mod_class/entity/ZenithProjectile;getSwordType()I")
    )
    private int truecopper$mapSwordType(ZenithProjectile entity) {
        int type = entity.getSwordType();
        truecopper$currentType = type;
        // Types 21/22 are outside the Zenith colour table. Map them onto valid indices so the
        // bounds check and the COLORS lookup keep working; the real type is restored below.
        return type == ZenithSkin.COPPER.projectileType() || type == ZenithSkin.WOODEN.projectileType()
                ? type - ZenithSkin.COPPER.projectileType()
                : type;
    }

    @Inject(method = "renderSword", at = @At("HEAD"), cancellable = true)
    private void truecopper$renderSkinSword(ZenithProjectile entity, int swordType, double progressAngle,
                                            double distance, Quaternion[] fixed, Vector3 normal,
                                            double x, double y, double z, PoseStack.Pose pose,
                                            MultiBufferSource buffers, CallbackInfo ci) {
        ZenithSkin skin = ZenithSkin.byProjectileType(entity.getSwordType());
        if (skin == null) {
            return;
        }
        ItemStack stack = switch (skin) {
            case COPPER -> TrueCopperItems.TRUE_COPPER_SHORTSWORD.get().getDefaultInstance();
            case WOODEN -> TrueCopperItems.TRUE_WOODEN_SWORD.get().getDefaultInstance();
        };

        this.swordPoses.setIdentity();
        this.swordPoses.mulPose(pose.pose());
        this.swordPoses.last().normal().set(pose.normal());
        this.swordPoses.translate(x, y, z);
        Quaternion rotation = Quaternion.rotate(normal, -PosUtil.getPolar(progressAngle, distance));
        this.swordPoses.mulPose(rotation.toQuaternionf());
        this.swordPoses.mulPose(fixed[2].toQuaternionf());
        this.swordPoses.mulPose(fixed[1].toQuaternionf());
        this.swordPoses.mulPose(fixed[0].toQuaternionf());
        this.swordPoses.mulPose(Q2);
        this.swordPoses.mulPose(Q1);

        BakedModel model = this.itemRenderer.getModel(stack, entity.level(), null, entity.getId());
        this.itemRenderer.render(stack, ItemDisplayContext.NONE, false, this.swordPoses, buffers,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
        ci.cancel();
    }

    @ModifyVariable(method = "renderTrail", at = @At("HEAD"), argsOnly = true)
    private int[] truecopper$trailColor(int[] color) {
        ZenithSkin skin = ZenithSkin.byProjectileType(truecopper$currentType);
        return skin == null ? color : skin.trailColor();
    }
}
