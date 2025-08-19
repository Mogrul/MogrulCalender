package gb.mogrul.calendar.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gb.mogrul.calendar.Config;
import gb.mogrul.calendar.data.DayData;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static gb.mogrul.calendar.MogrulCalendar.*;

@EventBusSubscriber(modid = MODID, value = Dist.DEDICATED_SERVER)
public class MainDataHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String DATAPATH = "Mogrul";
    private static final String MODPATH = "Calender";
    private static final String DAYSPASSEDFILENAME = "days_passed.json";

    public static Path daysPassedFile;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onServerStarting(ServerStartedEvent event) {
        MinecraftServer minecraftServer = event.getServer();
        Path serverRoot = minecraftServer.getServerDirectory();
        Path dataPath = serverRoot.resolve(DATAPATH);
        Path modPath = dataPath.resolve(MODPATH);

        daysPassedFile = modPath.resolve(DAYSPASSEDFILENAME);

        // Create mod path.
        try {
            if (!Files.exists(modPath)) {
                Files.createDirectories(modPath);
            }
        } catch (IOException e) {
            LOGGER.error("[{}] Failed to create mod path: {}", LOGNAME, e.getMessage());
        }

        // Create data path.
        try {
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        } catch (IOException e) {
            LOGGER.error("[{}] Failed to create data path: {}", LOGNAME, e.getMessage());
        }

        // Create daysPassed File.
        try {
            if (!Files.exists(daysPassedFile)) {
                DayData defaultData = new DayData(Config.startingDays);
                String json = GSON.toJson(defaultData);
                Files.writeString(daysPassedFile, json, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                LOGGER.info("[{}] Created days passed file!", LOGNAME);
            }
        } catch (IOException e) {
            LOGGER.error("[{}] Failed to create days passed file! {}", LOGNAME, e.getMessage());
        }
    }
}
