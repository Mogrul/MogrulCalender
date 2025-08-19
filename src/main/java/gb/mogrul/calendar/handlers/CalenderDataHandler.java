package gb.mogrul.calendar.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gb.mogrul.calendar.Config;
import gb.mogrul.calendar.data.DayData;
import gb.mogrul.calendar.data.MemoryData;
import gb.mogrul.calendar.data.MonthData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.io.IOException;
import java.nio.file.Files;

import static gb.mogrul.calendar.MogrulCalendar.*;
import static gb.mogrul.calendar.handlers.MainDataHandler.daysPassedFile;

@EventBusSubscriber(modid = MODID, value = Dist.DEDICATED_SERVER)
public class CalenderDataHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onServerStarted(ServerStartedEvent event) {
        try {
            String json = new String(Files.readAllBytes(daysPassedFile));
            DayData dayData = GSON.fromJson(json, DayData.class);

            MemoryData.daysPassed = dayData.daysPassed;

            LOGGER.info("[{}] Loaded daysPassed to memory!", LOGNAME);

        } catch (IOException e) {
            LOGGER.info("[{}] Failed to load daysPassedFile! {}", LOGNAME, e.getMessage());
        }
    }

    public static void saveDaysPassed(DayData dayData) {
        try {
            String json = GSON.toJson(dayData);
            Files.writeString(daysPassedFile, json);
        } catch (IOException e) {
            LOGGER.info("[{}] Failed to save daysPassedFile! {}", LOGNAME, e.getMessage());
        }
    }

    public static class DateResult {
        public final int year;
        public final String month;
        public final int day;

        public DateResult(int year, String month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
    }

    public static DateResult getDate() {
        int totalDays = MemoryData.daysPassed + (Config.startingDays - 1);
        int daysPerYear = Config.calenderMonths.values().stream().mapToInt(m -> m.dayCount).sum();
        int year = Config.startingYear + (totalDays / daysPerYear);

        int dayOfYear = totalDays % daysPerYear;

        MonthData currentMonth = null;
        int dayOfMonth = 0;

        for (MonthData month : Config.calenderMonths.values()) {
            if (dayOfYear < month.dayCount) {
                dayOfMonth = dayOfYear;
                currentMonth = month;
                break;
            } else {
                dayOfYear -= month.dayCount;
            }
        }

        // Fallback in case map iteration order is wrong
        if (currentMonth == null) {
            currentMonth = Config.calenderMonths.values().iterator().next();
            dayOfMonth = 1;
        }

        return new DateResult(year, currentMonth.name, dayOfMonth);
    }
}
