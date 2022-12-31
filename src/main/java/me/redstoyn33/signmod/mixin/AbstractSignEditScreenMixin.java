package me.redstoyn33.signmod.mixin;

import me.redstoyn33.signmod.SignCode;
import me.redstoyn33.signmod.SignMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Mixin(GuiEditSign.class)
public abstract class AbstractSignEditScreenMixin extends GuiScreen {

    private static boolean signMod_encode = false;
    private static boolean signMod_stableC = true;
    private static boolean signMod_signPos = true;

    public GuiTextField signMod_textInput;
    public GuiTextField signMod_keyInput;

    @Shadow
    @Final
    private TileEntitySign tileSign;

    @Shadow
    private GuiButton doneBtn;

    @Inject(at = @At("TAIL"), method = "initGui")
    private void init(CallbackInfo ci) {
        doneBtn.y = height / 4 + 90;

        signMod_textInput = new GuiTextField(1, this.fontRenderer, 10, height / 4 + 120, width - 80, 20);
        signMod_textInput.setMaxStringLength(SignMod.MAX_LINE_TEXT * 8);
        signMod_keyInput = new GuiTextField(2, this.fontRenderer, 10, height / 4 + 150, width - 80, 20);
        signMod_keyInput.setMaxStringLength(100);
        signMod_keyInput.setText(SignMod.key);
        this.addButton(new GuiButton(3, width - 60, height / 4 + 120, 50, 20, "Send"));
        this.addButton(new GuiButton(4, width - 60, height / 4 + 150, 50, 20, signMod_encode ? "Encoded" : "Uncoded"));
        this.addButton(new GuiButton(5, width - 60, height / 4 + 90, 50, 20, signMod_stableC ? "Stable" : "Сompress"));
        this.addButton(new GuiButton(6, width - 60, height / 4 + 60, 50, 20, signMod_signPos ? "SignPos" : "IgnorePos"));
    }

    @Inject(at = @At("TAIL"), method = "drawScreen")
    private void render(int p_73863_1_, int p_73863_2_, float p_73863_3_, CallbackInfo ci) {
        signMod_textInput.drawTextBox();
        signMod_keyInput.drawTextBox();
    }

    @Inject(at = @At("HEAD"), method = "updateScreen")
    private void tick(CallbackInfo ci) {
        signMod_textInput.updateCursorCounter();
        signMod_keyInput.updateCursorCounter();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX,mouseY,mouseButton);
        signMod_textInput.mouseClicked(mouseX, mouseY, mouseButton);
        signMod_keyInput.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Inject(at = @At("TAIL"), method = "keyTyped")
    private void charTyped(char p_73869_1_, int p_73869_2_, CallbackInfo ci) {
        signMod_textInput.textboxKeyTyped(p_73869_1_, p_73869_2_);
        signMod_keyInput.textboxKeyTyped(p_73869_1_, p_73869_2_);
        SignMod.key = signMod_keyInput.getText();
    }

    @Inject(at = @At("HEAD"),method = "actionPerformed")
    private void actionPerformed(GuiButton p_146284_1_, CallbackInfo ci){
        if (p_146284_1_.id == 3){
            if (signMod_textInput.getText().isEmpty()) return;
            if (SignMod.key.isEmpty()) return;
            String s;
            if (signMod_encode) {
                if (signMod_stableC) {
                    s = Base64.getEncoder().encodeToString(SignMod.xor(signMod_textInput.getText().getBytes(StandardCharsets.UTF_8), SignMod.key.getBytes(StandardCharsets.UTF_8)));
                } else {
                    s = SignMod.byte2s(SignMod.xor(signMod_textInput.getText().getBytes(StandardCharsets.UTF_8), SignMod.key.getBytes(StandardCharsets.UTF_8)));
                }
            } else {
                s = signMod_textInput.getText();
            }
            byte[] t = s.getBytes(StandardCharsets.UTF_8);
            if (signMod_signPos)
                t = ArrayUtils.addAll(t, SignMod.pos2s(tileSign.getPos()).getBytes(StandardCharsets.UTF_8));
            s += Base64.getEncoder().encodeToString(SignMod.HMAC_SHA256(SignMod.key.getBytes(StandardCharsets.UTF_8), t));
            s += SignCode.newCode(signMod_encode, signMod_stableC, signMod_signPos);
            if (s.length() > SignMod.MAX_LINE_TEXT * 4) {
                p_146284_1_.displayString = "Oversize";
                return;
            }
            int row = ((s.length() - 1) / SignMod.MAX_LINE_TEXT) + 1;
            for (int i = 0; i < row; i++) {
                String r = (i == row - 1) ? s.substring(i * SignMod.MAX_LINE_TEXT) : s.substring(i * SignMod.MAX_LINE_TEXT, SignMod.MAX_LINE_TEXT + i * SignMod.MAX_LINE_TEXT);
                tileSign.signText[i] = new TextComponentString(r);

            }
            this.tileSign.markDirty();
            this.mc.displayGuiScreen((GuiScreen)null);
        } else if (p_146284_1_.id == 4){
            signMod_encode = !signMod_encode;
            p_146284_1_.displayString = signMod_encode ? "Encoded" : "Uncoded";
        } else if (p_146284_1_.id == 5){
            signMod_stableC = !signMod_stableC;
            p_146284_1_.displayString = signMod_stableC ? "Stable" : "Сompress";
        } else if (p_146284_1_.id == 6){
            signMod_signPos = !signMod_signPos;
            p_146284_1_.displayString = signMod_signPos ? "SignPos" : "IgnorePos";
        }
    }
}
