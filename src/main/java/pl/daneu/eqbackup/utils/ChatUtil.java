package pl.daneu.eqbackup.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatUtil {

    public static String getColoredText(String text){ return ChatColor.translateAlternateColorCodes('&', text); }

    public static String getColoredText(FileConfiguration config, String path){ return getColoredText(config.getString(path)); }

    public static List<String> getColoredList(List<String> list){
        ArrayList<String> newList = new ArrayList<>();
        list.forEach(s -> newList.add(getColoredText(s)));

        return newList;
    }

    public static List<String> getColoredList(String... text){
        ArrayList<String> list = new ArrayList<>();
        Arrays.stream(text).forEach(s -> list.add(getColoredText(s)));

        return list;
    }

    public static String getReplacedText(String text, String replace, String replaceWith){
        return getColoredText(text.replace(replace, replaceWith));
    }

    public static String getReplacedPlayer(String text, String replaceWith){
        return getColoredText(text.replace("%player%", replaceWith));
    }

    public static void message(CommandSender sender, String text){
        sender.sendMessage(getColoredText(text));
    }

    public static void broadcastMessage(String text){
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(getColoredText(text)));
    }
}