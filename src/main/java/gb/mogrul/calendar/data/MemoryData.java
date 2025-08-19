package gb.mogrul.calendar.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryData {
    public static Map<UUID, Integer> wakeTicks = new HashMap<>();
    public static Integer daysPassed = 0;
}
