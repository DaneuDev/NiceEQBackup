package pl.daneu.eqbackup.commands.customcommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.daneu.eqbackup.data.Config;
import pl.daneu.eqbackup.utils.ChatUtil;

public abstract class CustomCommand implements CommandExecutor {

    private final CommandInfo commandInfo;

    public CustomCommand(JavaPlugin plugin){
        commandInfo = getClass().getDeclaredAnnotation(CommandInfo.class);

        plugin.getCommand(commandInfo.name()).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getName().equalsIgnoreCase(commandInfo.name())) return false;
        if(!(sender instanceof Player) && commandInfo.onlyForPlayer()){
            ChatUtil.message(sender, "&cOnly players can use that command");
            return false;
        }
        if(!commandInfo.permission().isEmpty() && !sender.hasPermission(commandInfo.permission())){
            ChatUtil.message(sender, Config.MESSAGES_COMMAND_PERMISSION);
            return false;
        }

        execute(sender, args);
        return true;
    }

    protected abstract void execute(CommandSender sender, String[] args);
}
