package com.sk89q.craftbook.sponge.mechanics.area;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.Location;

import com.sk89q.craftbook.sponge.util.SignUtil;

public class Bridge extends SimpleArea {

    public Location getOtherEnd(Location block) {

        Direction back = SignUtil.getBack(block);

        for (int i = 0; i < 16; i++) {

            block = block.getRelative(back);

            if (SignUtil.isSign(block)) {
                Sign sign = (Sign) block.getTileEntity().get();

                if (SignUtil.getTextRaw(sign, 1).equals("[Bridge]") || SignUtil.getTextRaw(sign, 1).equals("[Bridge End]")) {
                    return block;
                }
            }
        }

        return null;
    }

    @Override
    public boolean triggerMechanic(Location block, Sign sign, Human human, Boolean forceState) {

        if (!SignUtil.getTextRaw(sign, 1).equals("[Bridge]")) {

            Direction back = SignUtil.getBack(block);

            Location baseBlock = block.getRelative(Direction.DOWN);

            Location left = baseBlock.getRelative(SignUtil.getLeft(block));
            Location right = baseBlock.getRelative(SignUtil.getRight(block));

            Location otherSide = getOtherEnd(block);
            if (otherSide == null) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Texts.builder("Missing other end!").build());
                return true;
            }

            baseBlock = baseBlock.getRelative(back);

            left = baseBlock.getRelative(SignUtil.getLeft(block));
            right = baseBlock.getRelative(SignUtil.getRight(block));

            BlockState type = block.getRelative(Direction.DOWN).getState();
            if (baseBlock.getState().equals(type) && (forceState == null || forceState == false)) type = BlockTypes.AIR.getDefaultState();

            while (baseBlock.getX() != otherSide.getX() || baseBlock.getZ() != otherSide.getZ()) {

                baseBlock.replaceWith(type);
                left.replaceWith(type);
                right.replaceWith(type);

                baseBlock = baseBlock.getRelative(back);

                left = baseBlock.getRelative(SignUtil.getLeft(block));
                right = baseBlock.getRelative(SignUtil.getRight(block));
            }
        } else {
            if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Texts.builder("Bridge not activatable from here!").build());
            return false;
        }

        return true;
    }

    @Override
    public boolean isMechanicSign(Sign sign) {
        return SignUtil.getTextRaw(sign, 1).equalsIgnoreCase("[Bridge]") || SignUtil.getTextRaw(sign, 1).equalsIgnoreCase("[Bridge End]");
    }
}
