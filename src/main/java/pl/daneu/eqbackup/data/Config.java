package pl.daneu.eqbackup.data;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import pl.daneu.eqbackup.NiceEQBackup;
import pl.daneu.eqbackup.utils.ChatUtil;
import pl.daneu.eqbackup.utils.ItemBuilder;
import pl.daneu.eqbackup.utils.ItemUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Config {

    public static String
            MESSAGES_COMMAND_PERMISSION,
            MESSAGES_COMMAND_ARGUMENTS,
            MESSAGES_COMMAND_NO_PLAYER_LIKE,
            MESSAGES_COMMAND_BACKUP_FOR_ALL,
            PANEL_TITLE,
            MESSAGES_PANEL_PLAYER_OFFLINE,
            MESSAGES_PANEL_SUCCEED,
            BACKUPSLIST_TITLE,
            BACKUPSLIST_DEATH_TITLE,
            MESSAGES_BACKUPSLIST_GIVE_EQ_PLAYER_OFFLINE,
            MESSAGES_BACKUPSLIST_GIVE_EC_PLAYER_OFFLINE,
            MESSAGES_BACKUPSLIST_GIVE_EQ_SUCCEED,
            MESSAGES_BACKUPSLIST_GIVE_EC_SUCCEED,
            MESSAGES_BACKUPSLIST_DELETE,
            SHOWCASE_EQ_TITLE,
            SHOWCASE_EC_TITLE;
    public static ItemStack
            PANEL_BACKUPSLIST,
            PANEL_DO_BACKUP,
            BACKUPSLIST_BACKUP,
            BACKUPSLIST_GO_BACK,
            SHOWCASE_GO_BACK,
            SHOWCASE_EXP;
    public static int MAX_BACKUPS_AMOUNT, MAX_DEATH_BACKUPS_AMOUNT;
    public static boolean SAVE_ON_TASK, SAVE_ON_DEATH, SAVE_ON_PANEL_SAVE, SAVE_ON_COMMAND_SAVE_ALL;

    public static boolean init(NiceEQBackup plugin){
        FileConfiguration config = plugin.getConfig();

        MAX_BACKUPS_AMOUNT = config.getInt("options.max-backups-number");
        MAX_DEATH_BACKUPS_AMOUNT = config.getInt("options.max-death-backups-number");

        if(MAX_BACKUPS_AMOUNT > 45 || MAX_DEATH_BACKUPS_AMOUNT > 45){
            Bukkit.getLogger().severe("NiceEQBackup | max-backup-amount and max-death-backup-amount value can't be greater than 45");

            return false;
        }

        SAVE_ON_TASK = config.getBoolean("options.save-on-task");
        SAVE_ON_DEATH = config.getBoolean("options.save-on-death");
        SAVE_ON_PANEL_SAVE = config.getBoolean("options.save-on-panel-save");
        SAVE_ON_COMMAND_SAVE_ALL = config.getBoolean("options.save-on-command-save-all");

        MESSAGES_COMMAND_PERMISSION = ChatUtil.getColoredText(config, "command.no-permission");
        MESSAGES_COMMAND_ARGUMENTS = ChatUtil.getColoredText(config, ".command.missing-arguments");
        MESSAGES_COMMAND_NO_PLAYER_LIKE = ChatUtil.getColoredText(config, "command.no-player-like");
        MESSAGES_COMMAND_BACKUP_FOR_ALL = ChatUtil.getColoredText(config, "command.backup-for-all");
        MESSAGES_PANEL_PLAYER_OFFLINE = ChatUtil.getColoredText(config, "guis.panel.messages.player-offline");
        MESSAGES_PANEL_SUCCEED = ChatUtil.getColoredText(config, "guis.panel.messages.succeed");
        MESSAGES_BACKUPSLIST_GIVE_EQ_PLAYER_OFFLINE = ChatUtil.getColoredText(config, "guis.backups-list.messages.give-eq-player-offline");
        MESSAGES_BACKUPSLIST_GIVE_EC_PLAYER_OFFLINE = ChatUtil.getColoredText(config, "guis.backups-list.messages.give-ec-player-offline");
        MESSAGES_BACKUPSLIST_GIVE_EQ_SUCCEED = ChatUtil.getColoredText(config, "guis.backups-list.messages.give-eq-succeed");
        MESSAGES_BACKUPSLIST_GIVE_EC_SUCCEED = ChatUtil.getColoredText(config, "guis.backups-list.messages.give-ec-succeed");
        MESSAGES_BACKUPSLIST_DELETE = ChatUtil.getColoredText(config, "guis.backups-list.messages.delete");

        PANEL_TITLE = ChatUtil.getColoredText(config, "guis.panel.title");
        BACKUPSLIST_TITLE = ChatUtil.getColoredText(config, "guis.backups-list.title");
        BACKUPSLIST_DEATH_TITLE = ChatUtil.getColoredText(config, "guis.backups-list.death-title");
        SHOWCASE_EQ_TITLE = ChatUtil.getColoredText(config, "guis.backup-showcase.eq-title");
        SHOWCASE_EC_TITLE = ChatUtil.getColoredText(config, "guis.backup-showcase.ec-title");

        PANEL_BACKUPSLIST = getItem(config.getConfigurationSection("guis.panel.items.backups-list"), Material.WRITABLE_BOOK);
        PANEL_DO_BACKUP = getItem(config.getConfigurationSection("guis.panel.items.do-backup"), Material.PAPER);
        BACKUPSLIST_BACKUP = getItem(config.getConfigurationSection("guis.backups-list.items.backup"), Material.BOOK);
        BACKUPSLIST_GO_BACK = getItem(config.getConfigurationSection("guis.backups-list.items.go-back"), Material.BARRIER);
        SHOWCASE_GO_BACK = getItem(config.getConfigurationSection("guis.backup-showcase.items.go-back"), Material.BARRIER);
        SHOWCASE_EXP = getItem(config.getConfigurationSection("guis.backup-showcase.items.exp"), Material.EXPERIENCE_BOTTLE);

        return true;
    }

    private static ItemStack getItem(ConfigurationSection config, Material material){
        String name = config.getString("name");
        List<String> lore = config.getStringList("lore");

        ItemBuilder itemBuilder = new ItemBuilder(material);
        if(name != null)
            itemBuilder.setName(ChatUtil.getColoredText(name));
        if(!lore.isEmpty())
            itemBuilder.setLore(ChatUtil.getColoredList(lore));

        return itemBuilder.create();
    }

    public static ItemStack fixItem(ItemStack item, Map<String, String> replacing){
        ItemBuilder itemBuilder = new ItemBuilder(item);

        String name = itemBuilder.getName();
        if(name != null){
            for(String toReplace : replacing.keySet()) {
                String replaceBy = replacing.get(toReplace);

                name = name.replaceAll(toReplace, replaceBy);
            }

            itemBuilder.setName(name);
        }

        List<String> lore = itemBuilder.getLore();
        if(!lore.isEmpty()){
            lore = lore.stream()
                    .map(c -> {
                        for(String toReplace : replacing.keySet()) {
                            String replaceBy = replacing.get(toReplace);

                            c = c.replaceAll(toReplace, replaceBy);
                        }

                        return c;
                    })
                    .collect(Collectors.toList());
            itemBuilder.setLore(lore);
        }

        return itemBuilder.create();
    }

}
