package pl.daneu.niceeqbackup.commands;

import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import pl.daneu.daneutools.customcommand.CustomCommand;
import pl.daneu.daneutools.utils.ChatUtil;
import pl.daneu.niceeqbackup.data.Config;
import pl.daneu.niceeqbackup.NiceEQBackup;
import pl.daneu.niceeqbackup.guis.PanelGUI;
import pl.daneu.niceeqbackup.handlers.UserHandler;
import pl.daneu.niceeqbackup.objects.Backup;
import pl.daneu.niceeqbackup.objects.User;
import pl.daneu.niceeqbackup.tasks.BackupInventoriesTask;
import pl.daneu.niceeqbackup.utils.LoggerUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NiceEQBackupCommand extends CustomCommand implements TabCompleter {

    private final NiceEQBackup plugin;

    public NiceEQBackupCommand(NiceEQBackup plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    protected String getName() {
        return "niceeqbackup";
    }

    @Override
    public String getPermission() {
        return "niceeqbackup.use";
    }

    @Override
    public String getPermissionMessage() {
        return Config.MESSAGES_COMMAND_PERMISSION;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        if(args.length == 0){
            ChatUtil.message(sender, "&cNiceEQBackup &7&l| &c/niceeqbackup reload/open/saveAll");
            return;
        }

        if(args[0].equalsIgnoreCase("reload")){
            reloadUsers();
            plugin.reloadConfig();
            Config.init(plugin);
            reloadDatabase();
            reloadBackupInventoryTask();

            LoggerUtil.sendInfoLog("Reload complete");
            ChatUtil.message(sender, "&aNiceEQBackup &7&l| &aReload complete!");

            return;
        }
        else if(args[0].equalsIgnoreCase("saveAll")){
            Bukkit.getOnlinePlayers().forEach(p -> {
                User user = UserHandler.getUser(p.getUniqueId());

                if(user == null)
                    return;

                user.addBackup(p, Backup.SaveType.COMMAND);
            });

            ChatUtil.message(sender, "&aNiceEQBackup &7&l| &aYou did successful backups for &lall players!");

            return;
        }
        else if(!args[0].equalsIgnoreCase("open"))
            return;

        if(args.length == 1){
            ChatUtil.message(sender, "&cNiceEQBackup &7&l| &c/niceeqbackup open <player-name>");

            return;
        }

        User user = UserHandler.getUser(args[1]);
        if(user == null) {
            user = plugin.getDatabase().getUser(args[1]);

            if(user == null){
                ChatUtil.message(sender, "&cNiceEQBackup &7&l| &cThere is no player like &e" + args[1]);

                return;
            }
        }

        new PanelGUI(user).open((Player) sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player))
            return null;

        Set<String> set;
        if(args.length == 1)
            set = Sets.newHashSet("saveAll", "open", "reload");
        else if(args[0].equalsIgnoreCase("open") && args.length == 2){
            set = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toSet());
            set.addAll(Config.PLAYERS_NAMES);
        }
        else
            set = new HashSet<>();

        return set.stream()
                .filter(s -> s.startsWith(args[args.length - 1]))
                .collect(Collectors.toList());
    }

    private void reloadUsers(){
        if(plugin.getDatabase().getConnection() != null)
            plugin.getUserHandler().getUsers().forEach(u -> {
                plugin.getDatabase().saveOrPut(u);
                plugin.getUserHandler().removeUser(u.getUUID());
            });
    }

    private void reloadDatabase(){
        if(!plugin.initDatabase()) {
            Bukkit.getPluginManager().disablePlugin(plugin);

            return;
        }

        Bukkit.getOnlinePlayers().forEach(p -> {
            User user = plugin.getDatabase().getUser(p.getUniqueId());
            plugin.getUserHandler().addUser(user);
        });

        Config.PLAYERS_NAMES.clear();
        Config.PLAYERS_NAMES.addAll(plugin.getDatabase().getPlayersNames());
    }

    private void reloadBackupInventoryTask(){
        plugin.getBackupInventoriesTask().cancel();

        if(!Config.SAVE_ON_TASK)
            return;

        int time = plugin.getConfig().getInt("options.save-task-interval") * 20;
        plugin.setBackupInventoriesTask(new BackupInventoriesTask(plugin.getDatabase(), plugin.getUserHandler()));
        plugin.getBackupInventoriesTask().runTaskTimer(plugin, time, time);
    }
}
