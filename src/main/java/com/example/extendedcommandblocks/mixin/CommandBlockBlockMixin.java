package com.example.extendedcommandblocks.mixin;

import com.example.extendedcommandblocks.PermissionGate;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandBlock.class)
public class CommandBlockBlockMixin {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void extendedcommandblocks$onUse(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }

        if (serverPlayer.isCreativeLevelTwoOp()) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof CommandBlockBlockEntity commandBlockEntity)) {
            return;
        }

        if (!PermissionGate.INSTANCE.hasPotionAccess(serverPlayer)) {
            serverPlayer.sendMessage(Text.literal("Нужен специальный эффект для доступа к командному блоку"), true);
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }

        serverPlayer.openCommandBlockScreen(commandBlockEntity);
        cir.setReturnValue(ActionResult.success(world.isClient));
    }
}
