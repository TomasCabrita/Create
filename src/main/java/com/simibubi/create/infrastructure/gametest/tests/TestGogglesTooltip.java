package com.simibubi.create.infrastructure.gametest.tests;

import java.util.List;

import static com.simibubi.create.infrastructure.gametest.CreateGameTestHelper.TICKS_PER_SECOND;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.content.redstone.diodes.PulseTimerBlockEntity;
import com.simibubi.create.content.redstone.diodes.PulseRepeaterBlockEntity;
import com.simibubi.create.content.redstone.diodes.PulseExtenderBlockEntity;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;

@GameTestGroup(path = "goggles_tooltip")
public class TestGogglesTooltip {
    // ===== Test Variables =====

    private static final int PROGRESSION_CHECK_SECONDS = 3;
    private static final BlockPos BLOCK_POS = new BlockPos(1, 2, 1);


    // ===== Test Methods =====

    // Blaze Burner Tooltip Tests
    @GameTest(template = "blaze_burner_empty")
    public static void noFuelTooltip(CreateGameTestHelper helper) {
        BlazeBurnerBlockEntity burner = getBurner(helper);
        String testType = "Blaze Burner Empty";

        // Check if the blaze burner is in creative mode (it should not be)
        if (burner.isCreative())
            helper.fail(testType + ": Should not be in creative mode for this test");
        // Check if the blaze burner has no fuel
        if (burner.getHeatLevelFromBlock() != HeatLevel.SMOULDERING)
            helper.fail(testType + ": Should not have fuel for this test. Expected heat level: SMOULDERING, got: " + burner.getHeatLevelFromBlock());
        // Check if active fuel is NONE
        if (burner.getActiveFuel() != FuelType.NONE)
            helper.fail(testType + ": Should not have fuel for this test. Expected fuel type: NONE, got: " + burner.getActiveFuel());

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = burner.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for an empty blaze burner
        assertTooltipContains(helper, tooltip, result, testType,
        "create.tooltip.blaze_burner.header",
                    "create.tooltip.blaze_burner.fuel_capacity",
                    "create.tooltip.blaze_burner.empty");

        // Assert that the tooltip does not contain the "Remaining Burn Time" text
        String fullText = collectTooltipText(tooltip);
        String remaining = Component.translatable("create.tooltip.blaze_burner.remaining").getString();
        if (fullText.contains(remaining))
            helper.fail(testType + ": Tooltip should not contain \"" + remaining + "\"");

        helper.succeed();
    }

    @GameTest(template = "blaze_burner_infinite")
    public static void infiniteFuelTooltip(CreateGameTestHelper helper) {
        BlazeBurnerBlockEntity burner = getBurner(helper);
        String testType = "Blaze Burner Infinite";

        // Check if the blaze burner is in creative mode
        if (!burner.isCreative())
            helper.fail(testType + ": Should be in creative mode for this test");
        // Check if the remaining burn time is 0 for infinite fuel
        if (burner.getRemainingBurnTime() != 0)
            helper.fail(testType + ": Remaining burn time should be 0 for this test. Got: " + burner.getRemainingBurnTime());

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = burner.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for an infinite fuel blaze burner
        assertTooltipContains(helper, tooltip, result, testType,
                    "create.tooltip.blaze_burner.header",
                    "create.tooltip.blaze_burner.fuel_capacity",
                    "create.tooltip.blaze_burner.infinite");

            // Assert that the tooltip does not contain the "Remaining Burn Time" text
        String fullText = collectTooltipText(tooltip);
        String remaining = Component.translatable("create.tooltip.blaze_burner.remaining").getString();
        if (fullText.contains(remaining))
            helper.fail(testType + ": Tooltip should not contain \"" + remaining + "\"");

        helper.succeed();
    }

    @GameTest(template = "blaze_burner_normal", timeoutTicks = (PROGRESSION_CHECK_SECONDS + 2) * TICKS_PER_SECOND)
    public static void normalFuelTooltipAndDecay(CreateGameTestHelper helper) {
        BlazeBurnerBlockEntity burner = getBurner(helper);
        String testType = "Blaze Burner Normal";

        // Check if the blaze burner is in creative mode (it should not be)
        if (burner.isCreative())
            helper.fail(testType + ": Should not be in creative mode for this test");
        // Check if the blaze burner has normal fuel
        if (burner.getActiveFuel() != FuelType.NORMAL)
            helper.fail(testType + ": Should have normal fuel for this test. Expected fuel type: NORMAL, got: " + burner.getActiveFuel());
        if (burner.getRemainingBurnTime() <= 0)
            helper.fail(testType + ": Remaining burn time should be greater than 0 for this test");

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = burner.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for a normal fuel blaze burner
        assertTooltipContains(helper, tooltip, result, testType,
            "create.tooltip.blaze_burner.header",
            "create.tooltip.blaze_burner.fuel_capacity",
            "create.tooltip.blaze_burner.remaining");

        // Collect the initial burn time and tooltip text for later comparison
        String initialTooltipText = collectTooltipText(tooltip);
        int initialBurnTime = burner.getRemainingBurnTime();

        // Wait for a few seconds and check if the burn time has decayed appropriately
        helper.whenSecondsPassed(PROGRESSION_CHECK_SECONDS, () -> {
            int decayed = initialBurnTime - burner.getRemainingBurnTime();
            // We expect the burn time to have decayed by at least PROGRESSION_CHECK_SECONDS
            // Subtracting 1 tick to account for any potential timing discrepancies in the test environment
            // Note: Burn time decays at 20 ticks per second, but this can be affected by the game's tick rate
            int minExpected = PROGRESSION_CHECK_SECONDS * TICKS_PER_SECOND - 1;

            if (decayed < minExpected)
                helper.fail(testType + ": Burn time decayed only " + decayed
                    + " ticks, when at least " + minExpected + " ticks were expected");

            // Create a new tooltip after burn time decay and check if the text has updated accordingly
            List<Component> updatedTooltip = new java.util.ArrayList<>();
            burner.addToGoggleTooltip(updatedTooltip, false);
            String updatedTooltipText = collectTooltipText(updatedTooltip);

            if (initialTooltipText.equals(updatedTooltipText))
                helper.fail(testType + ": Tooltip text did not update after burn time decay. Initial: \""
                    + initialTooltipText + "\", Updated: \"" + updatedTooltipText + "\"");

            helper.succeed();
        });
    }

    // Pulse Mechanism Tooltip Tests
    @GameTest(template = "pulse_timer", timeoutTicks = (PROGRESSION_CHECK_SECONDS + 2) * TICKS_PER_SECOND)
    public static void pulseTimerTooltip(CreateGameTestHelper helper) {
        PulseTimerBlockEntity pulseTimer = helper.getBlockEntity(AllBlockEntityTypes.PULSE_TIMER.get(), BLOCK_POS);
        String testType = "Pulse Timer";

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = pulseTimer.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for a pulse timer
        assertTooltipContains(helper, tooltip, result, testType,
            "create.tooltip.pulse_timer.header",
            "create.tooltip.pulse.until_next_pulse");

        // Collect the initial tooltip text for later comparison
        String initialTooltipText = collectTooltipText(tooltip);

        // Wait for a few seconds and check if the tooltip has updated to reflect the pulse timer's progression
        helper.whenSecondsPassed(PROGRESSION_CHECK_SECONDS, () -> {
            // Create a new tooltip after time has passed and check if the text has updated accordingly
            List<Component> updatedTooltip = new java.util.ArrayList<>();
            pulseTimer.addToGoggleTooltip(updatedTooltip, false);
            String updatedTooltipText = collectTooltipText(updatedTooltip);

            if (initialTooltipText.equals(updatedTooltipText))
                helper.fail(testType + ": Tooltip text did not update after pulse timer progression. Initial: \""
                    + initialTooltipText + "\", Updated: \"" + updatedTooltipText + "\"");

            helper.succeed();
        });
    }

    @GameTest(template = "pulse_repeater", timeoutTicks = (PROGRESSION_CHECK_SECONDS + 2) * TICKS_PER_SECOND)
    public static void pulseRepeaterTooltip(CreateGameTestHelper helper) {
        PulseRepeaterBlockEntity pulseRepeater = helper.getBlockEntity(AllBlockEntityTypes.PULSE_REPEATER.get(), BLOCK_POS);
        String testType = "Pulse Repeater";

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = pulseRepeater.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for a pulse repeater
        assertTooltipContains(helper, tooltip, result, testType,
            "create.tooltip.pulse_repeater.header",
            "create.tooltip.pulse.until_next_pulse");

        // Collect the initial tooltip text for later comparison
        String initialTooltipText = collectTooltipText(tooltip);

        // Wait for a few seconds and check if the tooltip has updated to reflect the pulse repeater's progression
        helper.whenSecondsPassed(PROGRESSION_CHECK_SECONDS, () -> {
            // Create a new tooltip after time has passed and check if the text has updated accordingly
            List<Component> updatedTooltip = new java.util.ArrayList<>();
            pulseRepeater.addToGoggleTooltip(updatedTooltip, false);
            String updatedTooltipText = collectTooltipText(updatedTooltip);

            if (initialTooltipText.equals(updatedTooltipText))
                helper.fail(testType + ": Tooltip text did not update after pulse repeater progression. Initial: \""
                    + initialTooltipText + "\", Updated: \"" + updatedTooltipText + "\"");

            helper.succeed();
        });
    }

    @GameTest(template = "pulse_extender", timeoutTicks = (PROGRESSION_CHECK_SECONDS + 2) * TICKS_PER_SECOND)
    public static void pulseExtenderTooltip(CreateGameTestHelper helper) {
        PulseExtenderBlockEntity pulseExtender = helper.getBlockEntity(AllBlockEntityTypes.PULSE_EXTENDER.get(), BLOCK_POS);
        String testType = "Pulse Extender";

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = pulseExtender.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for a pulse extender
        assertTooltipContains(helper, tooltip, result, testType,
            "create.tooltip.pulse_extender.header",
            "create.tooltip.pulse_extender.remaining");

        // Collect the initial tooltip text for later comparison
        String initialTooltipText = collectTooltipText(tooltip);

        // Wait for a few seconds and check if the tooltip has updated to reflect the pulse extender's progression
        helper.whenSecondsPassed(PROGRESSION_CHECK_SECONDS, () -> {
            // Create a new tooltip after time has passed and check if the text has updated accordingly
            List<Component> updatedTooltip = new java.util.ArrayList<>();
            pulseExtender.addToGoggleTooltip(updatedTooltip, false);
            String updatedTooltipText = collectTooltipText(updatedTooltip);

            if (initialTooltipText.equals(updatedTooltipText))
                helper.fail(testType + ": Tooltip text did not update after pulse extender progression. Initial: \""
                    + initialTooltipText + "\", Updated: \"" + updatedTooltipText + "\"");

            helper.succeed();
        });
    }


    // ===== Helper Methods =====

    // Helper method to collect tooltip text into a single string for easier searching
    private static String collectTooltipText(List<Component> tooltip) {
        StringBuilder sb = new StringBuilder();
        // Concatenate all tooltip components into a single string
        for (Component component : tooltip)
            sb.append(component.getString()).append(" ");
        return sb.toString();
    }

    // Helper method to assert that the tooltip contains the expected text based on the translation key
    private static void assertTooltipContains(CreateGameTestHelper helper, List<Component> tooltip,
        boolean result, String testType, String... translationKeys) {

        // Check if addToGoggleTooltip returned true and that the tooltip is not empty
        if (!result)
            helper.fail(testType + ": addToGoggleTooltip() returned false");
        if (tooltip.isEmpty())
            helper.fail(testType + ": tooltip is empty");

        // Collect the full tooltip text for easier searching
        String fullText = collectTooltipText(tooltip);

        // Check that each expected message is present in the tooltip
        // Note: Used Component.translatable to get the expected text based on the translation key
        for (String key : translationKeys) {
            String expected = Component.translatable(key).getString();

            if (!fullText.contains(expected))
                helper.fail(testType + ": tooltip does not contain \"" + expected
                    + "\". Content: " + fullText);
        }
    }

    private static BlazeBurnerBlockEntity getBurner(CreateGameTestHelper helper) {
        return helper.getBlockEntity(AllBlockEntityTypes.HEATER.get(), BLOCK_POS);
    }
}