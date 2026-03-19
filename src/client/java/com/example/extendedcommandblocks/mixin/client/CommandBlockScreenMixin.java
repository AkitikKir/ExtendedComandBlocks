package com.example.extendedcommandblocks.mixin.client;

import com.example.extendedcommandblocks.network.ClientWhitelistState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CommandBlockScreen.class)
public abstract class CommandBlockScreenMixin extends Screen {

    protected CommandBlockScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void extendedcommandblocks$renderWhitelistPanel(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.textRenderer == null) {
            return;
        }

        int panelX = 8;
        int panelY = 8;
        int panelWidth = Math.min(240, this.width - 16);
        int panelHeight = 52;

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xAA10131A);
        context.drawBorder(panelX, panelY, panelWidth, panelHeight, 0xFF3A4659);

        context.drawTextWithShadow(this.textRenderer, Text.translatable("gui.extendedcommandblocks.panel_title"), panelX + 6, panelY + 6, 0xFFE9F0FF);

        List<String> commands = ClientWhitelistState.INSTANCE.getAllowedCommands();
        String commandPreview;
        if (commands.isEmpty()) {
            commandPreview = Text.translatable("gui.extendedcommandblocks.panel_empty").getString();
        } else {
            int maxEntries = Math.min(4, commands.size());
            commandPreview = "/" + String.join(", /", commands.subList(0, maxEntries));
            if (commands.size() > maxEntries) {
                commandPreview += " ...";
            }
        }
        context.drawText(this.textRenderer, commandPreview, panelX + 6, panelY + 20, 0xFFC8D7F4, false);

        String deniedMessage = ClientWhitelistState.INSTANCE.currentDeniedMessage();
        if (deniedMessage != null) {
            context.drawText(this.textRenderer, deniedMessage, panelX + 6, panelY + 34, 0xFFFF8888, false);
        } else {
            context.drawText(this.textRenderer, Text.translatable("gui.extendedcommandblocks.panel_hint"), panelX + 6, panelY + 34, 0xFF95A8CB, false);
        }
    }
}
