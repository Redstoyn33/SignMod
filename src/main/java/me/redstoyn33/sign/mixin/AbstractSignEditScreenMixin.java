package me.redstoyn33.sign.mixin;

import me.redstoyn33.sign.SignModInfo;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen {

    private static boolean signMod_encrypt = false;
    private static boolean signMod_stableC = true;

    public TextFieldWidget signMod_textInput;
    public TextFieldWidget signMod_keyInput;

    @Shadow
    @Final
    protected String[] text;

    protected AbstractSignEditScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract void finishEditing();

    @Shadow
    @Final
    protected SignBlockEntity blockEntity;

    @Shadow
    public abstract void tick();

    @Inject(at = @At("TAIL"), method = "init")

    private void init(CallbackInfo ci) {
        if (children().get(0) instanceof ButtonWidget) {
            ((ButtonWidget) children().get(0)).setY(this.height / 4 + 90);
        }
        signMod_textInput = new TextFieldWidget(textRenderer, 10, height / 4 + 120, width - 80, 20, Text.literal("Text"));
        signMod_textInput.setMaxLength(SignModInfo.MAX_LINE_TEXT * 8);
        signMod_textInput.setPlaceholder(Text.literal("Text"));
        signMod_textInput.setTooltip(Tooltip.of(Text.literal("0")));
        signMod_textInput.setChangedListener(s -> signMod_textInput.setTooltip(Tooltip.of(Text.literal(String.valueOf(s.length())))));
        signMod_keyInput = new TextFieldWidget(textRenderer, 10, height / 4 + 150, width - 80, 20, Text.literal("Key"));
        signMod_keyInput.setMaxLength(100);
        signMod_keyInput.setPlaceholder(Text.literal("Key"));
        signMod_keyInput.setText(SignModInfo.key);
        signMod_keyInput.setChangedListener(s -> SignModInfo.key = s);
        addSelectableChild(signMod_textInput);
        addSelectableChild(signMod_keyInput);
        addDrawableChild(ButtonWidget.builder(signMod_encrypt ? Text.literal("Encoded") : Text.literal("Uncoded"), button -> {
                    signMod_encrypt = !signMod_encrypt;
                    button.setMessage(signMod_encrypt ? Text.literal("Encoded") : Text.literal("Uncoded"));
                })
                .dimensions(width - 60, height / 4 + 150, 50, 20).build());
        addDrawableChild(ButtonWidget.builder(signMod_stableC ? Text.literal("Stable") : Text.literal("Сompress"), button -> {
                    signMod_stableC = !signMod_stableC;
                    button.setMessage(signMod_stableC ? Text.literal("Stable") : Text.literal("Сompress"));
                })
                .dimensions(width - 60, height / 4 + 60, 50, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Send"), button -> {
                    //
                    if (signMod_textInput.getText().isEmpty()) return;
                    if (SignModInfo.key.isEmpty()) return;
                    String s;
                    if (signMod_encrypt) {
                        if (signMod_stableC) {
                            s = Base64.getEncoder().encodeToString(SignModInfo.xor(signMod_textInput.getText().getBytes(StandardCharsets.UTF_8), SignModInfo.key.getBytes(StandardCharsets.UTF_8)));
                        } else {
                            s = SignModInfo.byte2s(SignModInfo.xor(signMod_textInput.getText().getBytes(StandardCharsets.UTF_8), SignModInfo.key.getBytes(StandardCharsets.UTF_8)));
                        }
                    } else {
                        s = signMod_textInput.getText();
                    }
                    SignModInfo.sha256.update(s.getBytes(StandardCharsets.UTF_8));
                    SignModInfo.sha256.update(SignModInfo.key.getBytes(StandardCharsets.UTF_8));
                    s += Base64.getEncoder().encodeToString(SignModInfo.sha256.digest());
                    s += signMod_encrypt ? (signMod_stableC ? "+" : "*") : "-";
                    if (s.length() > SignModInfo.MAX_LINE_TEXT * 4) {
                        button.setMessage(Text.literal("Oversize"));
                        button.setTooltip(Tooltip.of(Text.literal(s.length() + "/" + (SignModInfo.MAX_LINE_TEXT * 4))));
                        return;
                    }
                    int row = ((s.length() - 1) / SignModInfo.MAX_LINE_TEXT) + 1;
                    for (int i = 0; i < row; i++) {
                        String r = (i == row - 1) ? s.substring(i * SignModInfo.MAX_LINE_TEXT) : s.substring(i * SignModInfo.MAX_LINE_TEXT, SignModInfo.MAX_LINE_TEXT + i * SignModInfo.MAX_LINE_TEXT);
                        text[i] = r;
                        blockEntity.setTextOnRow(i, Text.literal(r));
                    }
                    finishEditing();
                    //
                })
                .dimensions(width - 60, height / 4 + 120, 50, 20).build());
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        signMod_textInput.render(matrices, mouseX, mouseY, delta);
        signMod_keyInput.render(matrices, mouseX, mouseY, delta);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void tick(CallbackInfo ci) {
        signMod_textInput.tick();
        signMod_keyInput.tick();
    }

    @Inject(at = @At("HEAD"), method = "keyPressed")
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        signMod_textInput.keyPressed(keyCode, scanCode, modifiers);
        signMod_keyInput.keyPressed(keyCode, scanCode, modifiers);
    }

    @Inject(at = @At("HEAD"), method = "charTyped")
    private void charTyped(char chr, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        signMod_textInput.charTyped(chr, modifiers);
        signMod_keyInput.charTyped(chr, modifiers);
    }

}
