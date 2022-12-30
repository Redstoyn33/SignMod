package me.redstoyn33.sign.mixin;

import me.redstoyn33.sign.SignCode;
import me.redstoyn33.sign.SignModInfo;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Mixin(AbstractSignBlock.class)
public class AbstractSignBlockMixin {

    @Inject(method = "onUse", at = @At("HEAD"))
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (hand == Hand.OFF_HAND) return;
        if (!world.isClient) return;
        if (SignModInfo.key.isEmpty()) return;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SignBlockEntity) {
            Text[] texts = ((SignBlockEntityMixin) blockEntity).getTexts();
            StringBuilder sb = new StringBuilder();
            for (Text text : texts) {
                if (text.getString().isEmpty()) break;
                sb.append(text.getString());
            }
            if (!sb.isEmpty() && sb.length() > SignModInfo.SIGN_SIZE + 1) {
                SignCode endCode = SignCode.newCode(sb.substring(sb.length() - 1));
                if (endCode!=null) {
                    String text = sb.substring(0, sb.length() - SignModInfo.SIGN_SIZE - 1);
                    String sign = sb.substring(sb.length() - SignModInfo.SIGN_SIZE - 1, sb.length() - 1);
                    boolean encode = endCode.encode;
                    boolean stableC = endCode.stable;
                    boolean singPos = endCode.pos;
                    byte[] t = text.getBytes(StandardCharsets.UTF_8);
                    if (singPos) t = ArrayUtils.addAll(t.clone(),pos.toShortString().getBytes(StandardCharsets.UTF_8));
                    String testSign = Base64.getEncoder().encodeToString(SignModInfo.HMAC_SHA256(SignModInfo.key.getBytes(StandardCharsets.UTF_8),t));
                    if (testSign.equals(sign)) {
                        if (encode) {
                            if (stableC){
                                player.sendMessage(Text.literal("Sign is correct, encoded message:"));
                                player.sendMessage(Text.literal(new String(SignModInfo.xor(Base64.getDecoder().decode(text), SignModInfo.key.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
                            } else {
                                byte[] bytes = SignModInfo.s2byte(text);
                                if (bytes != null) {
                                    player.sendMessage(Text.literal("Sign is correct, encoded message:"));
                                    player.sendMessage(Text.literal(new String(SignModInfo.xor(bytes, SignModInfo.key.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
                                } else {
                                    player.sendMessage(Text.literal("Sign is correct, decode fail"));
                                }
                            }
                        } else {
                            player.sendMessage(Text.literal("Sign is correct, uncoded message:"));
                            player.sendMessage(Text.literal(text));
                        }
                    } else {
                        player.sendMessage(Text.literal("Corrupted"));
                    }
                }
            }
        }
    }
}
