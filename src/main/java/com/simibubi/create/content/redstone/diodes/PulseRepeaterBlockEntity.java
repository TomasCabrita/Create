package com.simibubi.create.content.redstone.diodes;

import java.util.List;

import static com.simibubi.create.content.redstone.diodes.BrassDiodeBlock.POWERING;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PulseRepeaterBlockEntity extends BrassDiodeBlockEntity implements IHaveGoggleInformation {

	public PulseRepeaterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin) {
		if (atMin && !powered)
			return;
		if (state > maxState.getValue() + 1) {
			if (!powered && !powering)
				state = 0;
			return;
		}

		state++;
		if (level.isClientSide)
			return;

		if (state == maxState.getValue() - 1 && !powering)
			level.setBlockAndUpdate(worldPosition, getBlockState().cycle(POWERING));
		if (state == maxState.getValue() + 1 && powering)
			level.setBlockAndUpdate(worldPosition, getBlockState().cycle(POWERING));
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		int maxTicks = maxState.getValue();

		CreateLang.translate("tooltip.pulse_repeater.header")
			.forGoggles(tooltip);

		CreateLang.translate("tooltip.pulse.until_next_pulse")
			.style(ChatFormatting.GRAY)
			.forGoggles(tooltip);

		CreateLang.text(formatGoggleTooltip(state, maxTicks))
			.style(ChatFormatting.AQUA)
			.text(ChatFormatting.GRAY, " / ")
			.add(CreateLang.text(formatGoggleTooltip(maxTicks, maxTicks))
				.style(ChatFormatting.DARK_GRAY))
			.forGoggles(tooltip, 1);

		return true;
	}
}
