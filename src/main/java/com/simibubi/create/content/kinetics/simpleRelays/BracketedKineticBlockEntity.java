package com.simibubi.create.content.kinetics.simpleRelays;

import java.util.List;

import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BracketedKineticBlockEntity extends SimpleKineticBlockEntity implements TransformableBlockEntity {

	public BracketedKineticBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours
			.add(new BracketedBlockEntityBehaviour(this, state -> state.getBlock() instanceof AbstractSimpleShaftBlock));
		super.addBehaviours(behaviours);
	}

	@Override
	public void transform(BlockEntity be, StructureTransform transform) {
		BracketedBlockEntityBehaviour bracketBehaviour = getBehaviour(BracketedBlockEntityBehaviour.TYPE);
		if (bracketBehaviour != null) {
			bracketBehaviour.transformBracket(transform);
		}
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		Block block = getBlockState().getBlock();
		boolean isCogwheel = ICogWheel.isSmallCog(block) || ICogWheel.isLargeCog(block);

		if (isCogwheel) {
			CreateLang.translate("tooltip.cogwheel.header")
				.forGoggles(tooltip);
		} else {
			CreateLang.translate("tooltip.shaft.header")
				.forGoggles(tooltip);
		}
		addToGoggleRotationDirectionTooltip(tooltip);
		return getSpeed() != 0;
	}
}
