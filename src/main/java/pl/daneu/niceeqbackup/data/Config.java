package pl.daneu.niceeqbackup.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import pl.daneu.daneutools.utils.TextUtil;
import pl.daneu.niceeqbackup.NiceEQBackup;
import pl.daneu.niceeqbackup.utils.LoggerUtil;

import java.util.HashSet;
import java.util.Set;

public class Config {

    public static final Set<String> PLAYERS_NAMES = new HashSet<>();

    public static String
            MESSAGES_COMMAND_PERMISSION;

    public static int MAX_BACKUPS_AMOUNT;

    public static boolean SAVE_ON_TASK,
            SAVE_ON_DEATH,
            SAVE_ON_QUIT;

    public static boolean init(NiceEQBackup plugin){
        FileConfiguration config = plugin.getConfig();

        MAX_BACKUPS_AMOUNT = config.getInt("options.max-backups-number");

        SAVE_ON_TASK = config.getBoolean("options.save-on-task");
        SAVE_ON_DEATH = config.getBoolean("options.save-on-death");
        SAVE_ON_QUIT = config.getBoolean("options.save-on-quit");

        MESSAGES_COMMAND_PERMISSION = TextUtil.getColoredText(config, "messages.command-no-permission");

        return true;
    }

    public static void initPlayersNames(NiceEQBackup plugin){
        PLAYERS_NAMES.addAll(plugin.getDatabase().getPlayersNames());
    }

}
