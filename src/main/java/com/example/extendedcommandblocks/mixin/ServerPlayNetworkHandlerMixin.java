package com.example.extendedcommandblocks.mixin;

import com.example.extendedcommandblocks.PermissionGate;
import com.example.extendedcommandblocks.config.CommandWhitelist;
import com.example.extendedcommandblocks.network.ModNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
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
        if (PermissionGate.INSTANCE.hasCommandBlockAccess(this.player)) {
            return;
        }

        if (!PermissionGate.INSTANCE.hasPotionAccess(this.player)) {
            this.player.sendMessage(Text.translatable("message.extendedcommandblocks.need_effect_edit"), false);
            ci.cancel();
            return;
        }

        if (!CommandWhitelist.INSTANCE.checkCommand(packet.getCommand())) {
            Text deniedText = Text.translatable("message.extendedcommandblocks.command_denied");
            this.player.sendMessage(deniedText, false);
            ModNetworking.INSTANCE.sendAccessDenied(this.player, packet.getPos(), deniedText.getString());

            BlockEntity blockEntity = this.player.getServerWorld().getBlockEntity(packet.getPos());
            if (blockEntity instanceof CommandBlockBlockEntity commandBlockBlockEntity) {
                this.player.openCommandBlockScreen(commandBlockBlockEntity);
            }

            ci.cancel();
        }
    }

    @Redirect(
            method = "onUpdateCommandBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isCreativeLevelTwoOp()Z")
    )
    private boolean extendedcommandblocks$allowPotionEditors(ServerPlayerEntity player) {
        return PermissionGate.INSTANCE.hasCommandBlockAccess(player);
    }
}
