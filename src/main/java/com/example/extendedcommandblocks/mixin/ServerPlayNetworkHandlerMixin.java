package com.example.extendedcommandblocks.mixin;

import com.example.extendedcommandblocks.PermissionGate;
import com.example.extendedcommandblocks.config.CommandWhitelist;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onUpdateCommandBlock", at = @At("HEAD"), cancellable = true)
    private void extendedcommandblocks$validateWhitelist(UpdateCommandBlockC2SPacket packet, CallbackInfo ci) {
        if (this.player.isCreativeLevelTwoOp()) {
            return;
        }

        if (!PermissionGate.INSTANCE.hasPotionAccess(this.player)) {
            this.player.sendMessage(Text.literal("Нужен специальный эффект для редактирования командного блока"), true);
            ci.cancel();
            return;
        }

        if (!CommandWhitelist.INSTANCE.checkCommand(packet.getCommand())) {
            this.player.sendMessage(Text.literal("Команда не существует или у вас нет доступа"), true);
            ci.cancel();
        }
    }

    @Redirect(
            method = "onUpdateCommandBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isCreativeLevelTwoOp()Z")
    )
    private boolean extendedcommandblocks$allowPotionEditors(ServerPlayerEntity player) {
        return player.isCreativeLevelTwoOp() || PermissionGate.INSTANCE.hasPotionAccess(player);
    }
}
