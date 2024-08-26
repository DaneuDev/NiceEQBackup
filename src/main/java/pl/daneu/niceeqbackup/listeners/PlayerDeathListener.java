package pl.daneu.niceeqbackup.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import pl.daneu.daneutools.utils.ChatUtil;
import pl.daneu.niceeqbackup.data.Config;
import pl.daneu.niceeqbackup.handlers.UserHandler;
import pl.daneu.niceeqbackup.objects.Backup;
import pl.daneu.niceeqbackup.objects.User;
import pl.daneu.niceeqbackup.utils.LoggerUtil;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerDeathEvent e){
        if(!Config.SAVE_ON_DEATH)
            return;

        Player p = e.getEntity();
        User user = UserHandler.getUser(p.getUniqueId());

        if(user == null){
            LoggerUtil.sendSevereLog("No user found on PlayerDeathEvent");
            return;
        }

        user.addBackup(p, Backup.SaveType.DEATH);
    }
}
