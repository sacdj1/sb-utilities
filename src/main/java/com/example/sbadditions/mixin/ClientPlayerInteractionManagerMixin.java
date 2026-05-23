package com.example.sbadditions.mixin;

import com.example.sbadditions.modules.ToolSwapper;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void cancelForWeaponSwap(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (ToolSwapper.consumeBlockNextAttack()) {
            ci.cancel();
        }
    }
}
