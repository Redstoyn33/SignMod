package me.redstoyn33.signmod.mixin;

import me.redstoyn33.signmod.SignCode;
import me.redstoyn33.signmod.SignMod;
import net.minecraft.block.BlockSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Mixin(BlockSign.class)
public class BlockSignMixin {

    @Inject(method = "onBlockActivated", at = @At("HEAD"))
    public void onUse(World p_180639_1_, BlockPos p_180639_2_, IBlockState p_180639_3_, EntityPlayer p_180639_4_, EnumHand p_180639_5_, EnumFacing p_180639_6_, float p_180639_7_, float p_180639_8_, float p_180639_9_, CallbackInfoReturnable<Boolean> cir) {
        if (p_180639_5_ == EnumHand.OFF_HAND) return;
        if (!p_180639_1_.isRemote) return;
        if (SignMod.key.isEmpty()) return;
        TileEntity blockEntity = p_180639_1_.getTileEntity(p_180639_2_);
        if (blockEntity instanceof TileEntitySign) {
            ITextComponent[] texts = ((TileEntitySign)blockEntity).signText;
            StringBuilder sb = new StringBuilder();
            for (ITextComponent text : texts) {
                if (text.getUnformattedComponentText().isEmpty()) break;
                sb.append(text.getUnformattedComponentText());
            }
            if (!sb.toString().isEmpty() && sb.length() > SignMod.SIGN_SIZE + 1) {
                SignCode endCode = SignCode.newCode(sb.substring(sb.length() - 1));
                if (endCode!=null) {
                    String text = sb.substring(0, sb.length() - SignMod.SIGN_SIZE - 1);
                    String sign = sb.substring(sb.length() - SignMod.SIGN_SIZE - 1, sb.length() - 1);
                    boolean encode = endCode.encode;
                    boolean stableC = endCode.stable;
                    boolean singPos = endCode.pos;
                    byte[] t = text.getBytes(StandardCharsets.UTF_8);
                    if (singPos) t = ArrayUtils.addAll(t,SignMod.pos2s(p_180639_2_).getBytes(StandardCharsets.UTF_8));
                    String testSign = Base64.getEncoder().encodeToString(SignMod.HMAC_SHA256(SignMod.key.getBytes(StandardCharsets.UTF_8),t));
                    if (testSign.equals(sign)) {
                        if (encode) {
                            if (stableC){
                                p_180639_4_.sendMessage(new TextComponentString("Sign is correct, encoded message:"));
                                p_180639_4_.sendMessage(new TextComponentString(new String(SignMod.xor(Base64.getDecoder().decode(text), SignMod.key.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
                            } else {
                                byte[] bytes = SignMod.s2byte(text);
                                if (bytes != null) {
                                    p_180639_4_.sendMessage(new TextComponentString("Sign is correct, encoded message:"));
                                    p_180639_4_.sendMessage(new TextComponentString(new String(SignMod.xor(bytes, SignMod.key.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
                                } else {
                                    p_180639_4_.sendMessage(new TextComponentString("Sign is correct, decode fail"));
                                }
                            }
                        } else {
                            p_180639_4_.sendMessage(new TextComponentString("Sign is correct, uncoded message:"));
                            p_180639_4_.sendMessage(new TextComponentString(text));
                        }
                    } else {
                        p_180639_4_.sendMessage(new TextComponentString("Corrupted"));
                    }
                }
            }
        }
    }
}
