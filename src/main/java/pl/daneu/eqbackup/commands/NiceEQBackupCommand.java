package pl.daneu.eqbackup.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import pl.daneu.eqbackup.data.Config;
import pl.daneu.eqbackup.NiceEQBackup;
import pl.daneu.eqbackup.commands.customcommand.CommandInfo;
import pl.daneu.eqbackup.commands.customcommand.CustomCommand;
import pl.daneu.eqbackup.guis.PanelGUI;
import pl.daneu.eqbackup.handlers.UserHandler;
import pl.daneu.eqbackup.objects.Backup;
import pl.daneu.eqbackup.objects.User;
import pl.daneu.eqbackup.utils.ChatUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CommandInfo(name="niceeqbackup", permission="niceeqbackup.use", onlyForPlayer=true)
public class NiceEQBackupCommand extends CustomCommand implements TabCompleter {

    public static Set<String> PLAYERS_NAMES;

    private final NiceEQBackup plugin;

    public NiceEQBackupCommand(NiceEQBackup plugin) {
        super(plugin);

        this.plugin = plugin;
        PLAYERS_NAMES = plugin.getDatabase().getPlayersNames();
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        if(args.length == 0){
            ChatUtil.message(sender, Config.MESSAGES_COMMAND_ARGUMENTS);

            return;
        }

        if(args[0].equals("reload")){
            plugin.reloadConfig();
            plugin.saveConfig();

            plugin.reloadDatabase();
            Config.init(plugin);
            plugin.reloadBackupInventoryTask();

            ChatUtil.message(sender, "&a&lNiceEQBackups &7&l| &aReload complete!");

            return;
        }
        else if(args[0].equals("*")){
            Bukkit.getOnlinePlayers().forEach(p -> {
                User user = UserHandler.getUser(p.getUniqueId());

                if(user == null) return;

                user.addBackup(p, Backup.BackupType.ALIVE);

                if(Config.SAVE_ON_COMMAND_SAVE_ALL)
                    plugin.getDatabase().saveOrPut(user);
            });

            ChatUtil.message(sender, Config.MESSAGES_COMMAND_BACKUP_FOR_ALL);

            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        User user;

        if(target == null) {
            user = plugin.getDatabase().getOfflineUser(args[0]);

            if(user == null){
                ChatUtil.message(sender, ChatUtil.getReplacedPlayer(Config.MESSAGES_COMMAND_NO_PLAYER_LIKE, args[0]));

                return;
            }
        }
        else
            user = UserHandler.getUser(args[0]);

        new PanelGUI(user, plugin.getDatabase()).open((Player) sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return null;

        List<String> list = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).distinct().collect(Collectors.toList());
        list.add("*");
        list.add("reload");
        list.addAll(PLAYERS_NAMES);

        return list.stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
    }
}
