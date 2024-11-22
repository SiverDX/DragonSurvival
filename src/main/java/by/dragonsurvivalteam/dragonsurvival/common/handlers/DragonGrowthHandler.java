package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncGrowthState;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncSize;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = DragonSurvival.MODID)
public class DragonGrowthHandler {
    @Translation(type = Translation.Type.MISC, comments = "You have reached the largest size")
    private static final String REACHED_LARGEST = Translation.Type.GUI.wrap("system.reached_largest");

    @Translation(type = Translation.Type.MISC, comments = "You have reached the smallest size")
    private static final String REACHED_SMALLEST = Translation.Type.GUI.wrap("system.reached_smallest");

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon()) {
            return;
        }

        double growth = getGrowth(data.getLevel(), event.getItemStack().getItem());

        if (growth == 0) {
            return;
        }

        double newSize = DragonLevel.getBoundedSize(data.getSize() + growth);

        if (data.getSize() == newSize) {
            player.sendSystemMessage(Component.translatable(growth > 0 ? REACHED_LARGEST : REACHED_SMALLEST).withStyle(ChatFormatting.RED));
            return;
        }

        data.setSize(newSize, player);

        if (!player.isCreative()) {
            event.getItemStack().shrink(1);
        }

        if (player.level().isClientSide()) {
            return;
        }

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncSize.Data(player.getId(), data.getSize()));
        DSAdvancementTriggers.BE_DRAGON.get().trigger((ServerPlayer) player, data.getSize(), data.getTypeName());
    }

    public static double getGrowth(final Holder<DragonLevel> dragonLevel, final Item item) {
        int growth = 0;

        for (MiscCodecs.GrowthItem growthItem : dragonLevel.value().growthItems()) {
            // Select the largest number (independent on positive / negative)
            if ((growth == 0 || Math.abs(growthItem.growthInTicks()) > Math.abs(growth)) && growthItem.items().contains(item.builtInRegistryHolder())) {
                growth = growthItem.growthInTicks();
            }
        }

        return dragonLevel.value().ticksToSize(growth);
    }

    @SubscribeEvent
    public static void onPlayerUpdate(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(serverPlayer);

        if (!data.isDragon()) {
            return;
        }

        DragonLevel dragonLevel = data.getLevel().value();
        int increment = Functions.secondsToTicks(60);
        double newSize = DragonLevel.getBoundedSize(data.getSize() + dragonLevel.ticksToSize(increment));

        if (newSize == data.getSize() || !DragonLevel.getBounds().matches(data.getSize()) || !dragonLevel.canNaturallyGrow().matches(serverPlayer.serverLevel(), serverPlayer.position(), serverPlayer)) {
            if (data.isGrowing) {
                data.isGrowing = false;
                PacketDistributor.sendToPlayer(serverPlayer, new SyncGrowthState.Data(false));
            }

            return;
        } else if (!data.isGrowing) {
            data.isGrowing = true;
            PacketDistributor.sendToPlayer(serverPlayer, new SyncGrowthState.Data(true));
        }

        if (serverPlayer.tickCount % increment == 0) {
            data.setSize(newSize, serverPlayer);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncSize.Data(serverPlayer.getId(), data.getSize()));
            DSAdvancementTriggers.BE_DRAGON.get().trigger(serverPlayer, data.getSize(), data.getTypeName());
            serverPlayer.refreshDimensions();
        }
    }
}