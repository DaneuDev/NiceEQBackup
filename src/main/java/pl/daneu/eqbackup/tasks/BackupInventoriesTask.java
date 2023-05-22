package pl.daneu.eqbackup.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.daneu.eqbackup.data.Config;
import pl.daneu.eqbackup.data.IDatabase;
import pl.daneu.eqbackup.guis.BackupsShowcaseGUI;
import pl.daneu.eqbackup.guis.customgui.ManagerGUI;
import pl.daneu.eqbackup.handlers.UserHandler;
import pl.daneu.eqbackup.objects.Backup;

public class BackupInventoriesTask extends BukkitRunnable {

    private final IDatabase database;
    private final UserHandler userHandler;

    public BackupInventoriesTask(IDatabase database, UserHandler userHandler){
        this.database = database;
        this.userHandler = userHandler;
    }

    @Override
    public void run() {
        userHandler.getUsers().forEach((uuid, u) -> {
            Player p = Bukkit.getPlayer(uuid);

            if(p == null){
                database.saveOrPut(u);
                userHandler.removeUser(uuid);
                return;
            }

            u.addBackup(p, Backup.BackupType.ALIVE);

            if(Config.SAVE_ON_TASK)
                database.saveOrPut(u);
        });

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("niceeqbackups.use") && p.getOpenInventory().getTopInventory().getHolder() instanceof BackupsShowcaseGUI)
                .forEach(p -> {
                    BackupsShowcaseGUI backupsShowcaseGUI = (BackupsShowcaseGUI) (ManagerGUI.getManagerGUI().getGUI(p.getUniqueId()));
                    new BackupsShowcaseGUI(backupsShowcaseGUI.getUser(), backupsShowcaseGUI.getBackupType(), database).open(p);
                });
    }
}
