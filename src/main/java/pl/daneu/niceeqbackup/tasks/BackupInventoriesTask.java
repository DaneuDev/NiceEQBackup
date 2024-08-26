package pl.daneu.niceeqbackup.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.daneu.daneutools.customgui.ManagerGUI;
import pl.daneu.niceeqbackup.data.Config;
import pl.daneu.niceeqbackup.data.IDatabase;
import pl.daneu.niceeqbackup.guis.BackupsGUI;
import pl.daneu.niceeqbackup.handlers.UserHandler;
import pl.daneu.niceeqbackup.objects.Backup;

public class BackupInventoriesTask extends BukkitRunnable {

    private final IDatabase database;
    private final UserHandler userHandler;

    public BackupInventoriesTask(IDatabase database, UserHandler userHandler){
        this.database = database;
        this.userHandler = userHandler;
    }

    @Override
    public void run() {
        userHandler.getUsers().forEach(u -> {
            Player p = Bukkit.getPlayer(u.getUUID());

            if(p == null){
                database.saveOrPutAsync(u);
                userHandler.removeUser(u.getUUID());
                return;
            }

            u.addBackup(p, Backup.SaveType.TASK);

            if(Config.SAVE_ON_TASK)
                database.saveOrPutAsync(u);
        });

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("niceeqbackups.use") &&
                        p.getOpenInventory().getTopInventory().getHolder() instanceof BackupsGUI)
                .forEach(p -> {
                    BackupsGUI backupsShowcaseGUI = (BackupsGUI) (ManagerGUI.getManagerGUI().getGUI(p.getUniqueId()));
                    new BackupsGUI(backupsShowcaseGUI.getUser(), 0).open(p);
                });
    }
}
