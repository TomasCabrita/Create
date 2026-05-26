package com.simibubi.create.infrastructure.gametest.tests;

import static com.simibubi.create.infrastructure.gametest.CreateGameTestHelper.TICKS_PER_SECOND;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;

@GameTestGroup(path = "goggles_tooltip")
public class TestGogglesTooltip {
    // ===== Test Variables =====

    // Blaze Burner variables
    private static final BlockPos BURNER_POS = new BlockPos(1, 2, 1);
    private static final int FUEL_DECAY_CHECK_SECONDS = 3;


    // ===== Test Methods =====

    @GameTest(template = "blaze_burner_empty")
    public static void noFuelTooltip(CreateGameTestHelper helper) {
        BlazeBurnerBlockEntity burner = getBurner(helper);

        // Check if the blaze burner is in creative mode (it should not be)
        if (burner.isCreative())
            helper.fail("Blaze Burner Empty: Should not be in creative mode for this test");
        // Check if the blaze burner has no fuel
        if (burner.getHeatLevelFromBlock() != HeatLevel.SMOULDERING)
            helper.fail("Blaze Burner Empty: Should not have fuel for this test. Expected heat level: SMOULDERING, got: " + burner.getHeatLevelFromBlock());
        // Check if active fuel is NONE
        if (burner.getActiveFuel() != FuelType.NONE)
            helper.fail("Blaze Burner Empty: Should not have fuel for this test. Expected fuel type: NONE, got: " + burner.getActiveFuel());

        helper.succeed();
    }

    @GameTest(template = "blaze_burner_infinite")
    public static void infiniteFuelTooltip(CreateGameTestHelper helper) {
        BlazeBurnerBlockEntity burner = getBurner(helper);

        // Check if the blaze burner is in creative mode
        if (!burner.isCreative())
            helper.fail("Blaze Burner Infinite: Should be in creative mode for this test");
        // Check if the remaining burn time is 0 for infinite fuel
        if (burner.getRemainingBurnTime() != 0)
            helper.fail("Blaze Burner Infinite: Remaining burn time should be 0 for this test. Got: " + burner.getRemainingBurnTime());

        helper.succeed();
    }

    @GameTest(template = "blaze_burner_normal", timeoutTicks = (FUEL_DECAY_CHECK_SECONDS + 2) * TICKS_PER_SECOND)
    public static void normalFuelTooltipAndDecay(CreateGameTestHelper helper) {
        BlazeBurnerBlockEntity burner = getBurner(helper);

        // Check if the blaze burner is in creative mode (it should not be)
        if (burner.isCreative())
            helper.fail("Blaze Burner Normal: Should not be in creative mode for this test");
        // Check if the blaze burner has normal fuel
        if (burner.getActiveFuel() != FuelType.NORMAL)
            helper.fail("Blaze Burner Normal: Should have normal fuel for this test. Expected fuel type: NORMAL, got: " + burner.getActiveFuel());
        if (burner.getRemainingBurnTime() <= 0)
            helper.fail("Blaze Burner Normal: Remaining burn time should be greater than 0 for this test");

        int initialBurnTime = burner.getRemainingBurnTime();

        // Wait for a few seconds and check if the burn time has decayed appropriately
        helper.whenSecondsPassed(FUEL_DECAY_CHECK_SECONDS, () -> {
            int decayed = initialBurnTime - burner.getRemainingBurnTime();
            // We expect the burn time to have decayed by at least FUEL_DECAY_CHECK_SECONDS
            // Subtracting 1 tick to account for any potential timing discrepancies in the test environment
            // Note: Burn time decays at 20 ticks per second, but this can be affected by the game's tick rate
            int minExpected = FUEL_DECAY_CHECK_SECONDS * TICKS_PER_SECOND - 1;

            if (decayed < minExpected)
                helper.fail("Blaze Burner Normal: Burn time decayed only " + decayed
                    + " ticks, when at least " + minExpected + " ticks were expected");

            helper.succeed();
        });
    }


    // ===== Helper Methods =====

    private static BlazeBurnerBlockEntity getBurner(CreateGameTestHelper helper) {
        return helper.getBlockEntity(AllBlockEntityTypes.HEATER.get(), BURNER_POS);
    }
}