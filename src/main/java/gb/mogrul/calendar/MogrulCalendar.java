package gb.mogrul.calendar;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

import static gb.mogrul.calendar.Config.COMMON_CONFIG;

@Mod(MogrulCalendar.MODID)
public class MogrulCalendar {
    public static final String MODID = "mogrulcalendar";
    public static final String LOGNAME = "MogrulCalendar";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static MinecraftServer server;

    public MogrulCalendar(ModContainer modContainer) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return;
        }

        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
    }
}
