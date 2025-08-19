package gb.mogrul.calendar.handlers;

import gb.mogrul.calendar.Config;
import gb.mogrul.calendar.data.DayData;
import gb.mogrul.calendar.data.MemoryData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

import static gb.mogrul.calendar.MogrulCalendar.*;

@EventBusSubscriber(modid = MODID, value = Dist.DEDICATED_SERVER)
public class CalenderEventHandler {
    private static final ServerBossEvent DATE_TEXT = new ServerBossEvent(
            Component.literal("Zzzzz"),
            BossEvent.BossBarColor.WHITE,
            BossEvent.BossBarOverlay.PROGRESS
    );

    @SubscribeEvent
    public static void onServerTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        // Only run in Overworld
        if (!serverLevel.dimension().equals(ServerLevel.OVERWORLD)) return;

        long dayTime = serverLevel.getDayTime();

        if (dayTime % 24000 == 0) { // New day at sunrise
            onNewDay();
        }
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(ServerLevel.OVERWORLD)) return;

        onNewDay();

        CalenderDataHandler.DateResult dateResult = CalenderDataHandler.getDate();
        DATE_TEXT.setName(Component.literal(getDayWithSuffix(dateResult.day) + " " +  dateResult.month + ", " +dateResult.year));
    }

    private static void onNewDay() {
        MemoryData.daysPassed++;
        CalenderDataHandler.saveDaysPassed(new DayData(MemoryData.daysPassed));
        CalenderDataHandler.DateResult dateResult = CalenderDataHandler.getDate();

        LOGGER.info("[{}] A new day has happened! Current date: {} {} {}",
                LOGNAME,
                dateResult.day,
                dateResult.month,
                dateResult.year
        );
    }

    @SubscribeEvent
    public static void onCanSleep(CanPlayerSleepEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            DATE_TEXT.addPlayer(serverPlayer);
            DATE_TEXT.setProgress(1.0f);
            DATE_TEXT.setVisible(true);

            CalenderDataHandler.DateResult dateResult = CalenderDataHandler.getDate();

            DATE_TEXT.setName(Component.literal(getDayWithSuffix(dateResult.day) + " " +  dateResult.month + ", " +dateResult.year));

            MemoryData.wakeTicks.remove(serverPlayer.getUUID()); // Remove them from map if they're in there.
        }
    }

    @SubscribeEvent
    public static void onPlayerTickEvent(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.isSleeping()) return;

        UUID playerUUID = serverPlayer.getUUID();

        if (DATE_TEXT.getPlayers().contains(serverPlayer) && !MemoryData.wakeTicks.containsKey(playerUUID)) {
            MemoryData.wakeTicks.put(playerUUID, serverPlayer.server.getTickCount());
        }

        if (MemoryData.wakeTicks.containsKey(playerUUID)) {
            int wokeAt = MemoryData.wakeTicks.get(playerUUID);
            int now = serverPlayer.server.getTickCount();

            if (now - wokeAt >= (Config.wakeDisplayTimeoutSeconds * 20)) {
                DATE_TEXT.removePlayer(serverPlayer);
                MemoryData.wakeTicks.remove(playerUUID);
            }
        }
    }

    private static String getDayWithSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return day + "th"; // special case for 11th, 12th, 13th
        }
        switch (day % 10) {
            case 1: return day + "st";
            case 2: return day + "nd";
            case 3: return day + "rd";
            default: return day + "th";
        }
    }
}
