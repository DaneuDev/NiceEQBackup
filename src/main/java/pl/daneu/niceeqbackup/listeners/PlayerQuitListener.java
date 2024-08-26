package pl.daneu.niceeqbackup.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.daneu.niceeqbackup.data.Config;
import pl.daneu.niceeqbackup.data.IDatabase;
import pl.daneu.niceeqbackup.handlers.UserHandler;
import pl.daneu.niceeqbackup.objects.Backup;
import pl.daneu.niceeqbackup.objects.User;
import pl.daneu.niceeqbackup.utils.LoggerUtil;

public class PlayerQuitListener implements Listener {

    private final UserHandler userHandler;
    private final IDatabase database;

    public PlayerQuitListener(UserHandler userHandler, IDatabase database){
        this.userHandler = userHandler;
        this.database = database;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        User user = UserHandler.getUser(p.getUniqueId());

        if(user == null){
            LoggerUtil.sendSevereLog("No user found on PlayerQuitEvent");
            return;
        }

        userHandler.removeUser(user.getUUID());

        if(Config.SAVE_ON_QUIT)
            user.addBackup(p, Backup.SaveType.QUIT);

        database.saveOrPutAsync(user);
    }

}
