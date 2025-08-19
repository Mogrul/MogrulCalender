package gb.mogrul.calendar;

import gb.mogrul.calendar.data.MonthData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

import static gb.mogrul.calendar.MogrulCalendar.*;

@EventBusSubscriber(modid = MODID)
public class Config {
    // Config values.
    public static Integer wakeDisplayTimeoutSeconds;
    public static Integer startingYear;
    public static Integer startingMonth;
    public static Integer startingDays;
    public static Map<String, MonthData> calenderMonths = new LinkedHashMap<>();

    // ConfigSpec Values.
    public static ModConfigSpec.IntValue configSpecWakeDisplayTimeoutSeconds;

    // Calender Values.
    public static final ModConfigSpec.IntValue configSpecStartingYear;
    public static final ModConfigSpec.IntValue configSpecStartingMonth;
    public static final ModConfigSpec.IntValue configSpecStartingDays;

    public static final ModConfigSpec.ConfigValue<List<String>> configSpecCalenderMonths;

    public static final ModConfigSpec COMMON_CONFIG;
    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("Display");

        configSpecWakeDisplayTimeoutSeconds = builder
                .comment("In seconds, how long should the text display after waking up.")
                .defineInRange("wakeDisplayTimeoutSeconds", 5, 0, Integer.MAX_VALUE);

        builder.pop();

        builder.push("Calender");

        configSpecStartingYear = builder
                .comment("Year the server starts at.")
                .defineInRange("startingYear", 2025, 1, Integer.MAX_VALUE);

        configSpecStartingMonth = builder
                .comment("Months the server starts at.")
                .defineInRange("startingMonths", 1, 1, Integer.MAX_VALUE);

        configSpecStartingDays = builder
                .comment("Days the server starts at.")
                .defineInRange("startingDays", 1, 1, Integer.MAX_VALUE);

        configSpecCalenderMonths = builder
                .comment("List of month followed by days in months for the calender display.")
                .define(
                        "calenderMonths",
                        List.of(
                                "January=31",
                                "February=28",
                                "March=31",
                                "April=30",
                                "May=31",
                                "June=30",
                                "July=31",
                                "August=31",
                                "September=30",
                                "October=31",
                                "November=30",
                                "December=31"
                        ),
                        o -> o instanceof String
                );

        builder.pop();

        COMMON_CONFIG = builder.build();
    }

    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (!event.getConfig().getModId().equals(MODID)) return;

        if (event.getConfig().getSpec() == COMMON_CONFIG) {
            LOGGER.info("[{}] Config loading!", LOGNAME);
            setConfig();
        }
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (!event.getConfig().getModId().equals(MODID)) return;
        if (event.getConfig().getSpec() == COMMON_CONFIG) {
            LOGGER.info("[{}] Config reloaded!", LOGNAME);
            setConfig();
        }
    }

    private static void setConfig() {
        // Display settings.
        wakeDisplayTimeoutSeconds = configSpecWakeDisplayTimeoutSeconds.get();

        // Calender settings.
        startingYear = configSpecStartingYear.get();
        startingMonth = configSpecStartingMonth.get();
        startingDays = configSpecStartingDays.get();
        configSpecCalenderMonths.get().forEach(entry -> {
            String[] parts = entry.split("=", 2);
            if (parts.length != 2) return;

            String monthName = parts[0].trim();
            Integer dayCount = Integer.valueOf(parts[1].trim());

            calenderMonths.put(monthName, new MonthData(monthName, dayCount));
        });
    }
}
