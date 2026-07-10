package com.softcore.addon.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.softcore.addon.arguments.EnumArgumentType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.slot.SlotActionType;

public class ClickSlotCommand extends Command {

    public ClickSlotCommand() {
        super("clickslot", "Clicks a slot!", "cs", "cslot");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("slot", IntegerArgumentType.integer())
               .then(argument("button", IntegerArgumentType.integer())
                       .then(argument("action", EnumArgumentType.enumArgument(SlotActionType.PICKUP))
               .executes((context) -> {
                   assert mc.interactionManager != null;
                   int slot = context.getArgument("slot", Integer.class);
                   int button = context.getArgument("button", Integer.class);
                   SlotActionType action = context.getArgument("action", SlotActionType.class);
                   int syncId = mc.currentScreen instanceof HandledScreen<?> screen ? screen.getScreenHandler().syncId : 0;
                   mc.interactionManager.clickSlot(syncId, slot, button, action, mc.player);
                   return SINGLE_SUCCESS;
               }))

                       .then(argument("times", IntegerArgumentType.integer())
                           .then(argument("type", IntegerArgumentType.integer(0, SlotActionType.values().length - 1))
                               .executes(context -> {
                                   assert mc.interactionManager != null;
                                   int slot = context.getArgument("slot", Integer.class);
                                   int button = context.getArgument("button", Integer.class);
                                   SlotActionType action = SlotActionType.values()[context.getArgument("type", Integer.class)];
                                   int times = context.getArgument("times", Integer.class);
                                   for (int i = 0; i < times; i++) {
                                       int syncId = mc.currentScreen instanceof HandledScreen<?> screen ? screen.getScreenHandler().syncId : 0;
                                       mc.interactionManager.clickSlot(syncId, slot, button, action, mc.player);
                                   }

                                   return SINGLE_SUCCESS;
                               })
                           ))


               ));
    }
}
