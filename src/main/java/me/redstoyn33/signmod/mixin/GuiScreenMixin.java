package me.redstoyn33.signmod.mixin;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.IOException;
import java.util.List;

@Mixin(GuiScreen.class)
public abstract class GuiScreenMixin extends Gui {
    @Shadow public int width;

    @Shadow public int height;

    @Shadow
    protected FontRenderer fontRenderer;

    @Shadow
    public Minecraft mc;

    @Shadow
    protected <T extends GuiButton> T addButton(T buttonIn) {
        return null;
    }

    @Shadow
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {}
}
