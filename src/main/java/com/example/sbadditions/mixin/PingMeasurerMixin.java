package com.example.sbadditions.mixin;

import com.example.sbadditions.PingTracker;
import net.minecraft.client.network.PingMeasurer;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PingMeasurer.class)
public class PingMeasurerMixin {

    @Inject(method = "onPingResult", at = @At("HEAD"))
    private void captureRtt(PingResultS2CPacket packet, CallbackInfo ci) {
        PingTracker.set((int)(net.minecraft.util.Util.getMeasuringTimeMs() - packet.startTime()));
    }
}
