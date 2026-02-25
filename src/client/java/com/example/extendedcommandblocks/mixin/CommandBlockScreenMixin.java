package com.example.extendedcommandblocks.mixin;

import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.world.CommandBlockExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractCommandBlockScreen.class)
public abstract class CommandBlockScreenMixin extends net.minecraft.client.gui.screen.Screen {

    @Shadow
    protected ButtonWidget doneButton;

    @Shadow
    protected abstract void syncSettingsToServer(CommandBlockExecutor commandExecutor);

    @Shadow
    protected CommandBlockExecutor commandExecutor;

    protected CommandBlockScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void extendedcommandblocks$replaceDoneButton(CallbackInfo ci) {
        int x = this.doneButton.getX();
        int y = this.doneButton.getY();
        int width = this.doneButton.getWidth();
        int height = this.doneButton.getHeight();

        this.remove(this.doneButton);

        this.doneButton = ButtonWidget.builder(Text.literal("Применить"), button ->
                this.syncSettingsToServer(this.commandExecutor)
        ).dimensions(x, y, width, height).build();

        this.addDrawableChild(this.doneButton);
    }
}
