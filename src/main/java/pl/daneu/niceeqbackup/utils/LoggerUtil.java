package pl.daneu.niceeqbackup.utils;

import org.bukkit.Bukkit;

public class LoggerUtil {

    public static void sendSevereLog(String s){
        Bukkit.getLogger().severe("NiceEQBackup | " + s);
    }

    public static void sendInfoLog(String s){
        Bukkit.getLogger().info("NiceEQBackup | " + s);
    }

}
