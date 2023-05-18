package pl.daneu.eqbackup.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import pl.daneu.eqbackup.NiceEQBackup;
import pl.daneu.eqbackup.data.Config;
import pl.daneu.eqbackup.data.IDatabase;
import pl.daneu.eqbackup.guis.customgui.CustomGUI;
import pl.daneu.eqbackup.guis.customgui.ManagerGUI;
import pl.daneu.eqbackup.guis.showcases.ECShowcaseGUI;
import pl.daneu.eqbackup.guis.showcases.EQShowcaseGUI;
import pl.daneu.eqbackup.handlers.UserHandler;
import pl.daneu.eqbackup.objects.Backup;
import pl.daneu.eqbackup.objects.User;

import java.util.UUID;

public class Listeners implements Listener {

    private final NiceEQBackup plugin;
    private final IDatabase database;
    private final UserHandler userHandler;

    public Listeners(NiceEQBackup plugin, IDatabase database, UserHandler userHandler){
        this.plugin = plugin;
        this.database = database;
        this.userHandler = userHandler;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();

        database.insertUser(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        User user = UserHandler.getUser(p.getUniqueId());

        if(user == null)
            return;

        database.saveOrPut(user);

        new BukkitRunnable() {
            @Override
            public void run() {
                userHandler.removeUser(p.getUniqueId());
            }
        }.runTaskLater(plugin, 10);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent e){
        Player p = e.getEntity();
        User user = UserHandler.getUser(p.getUniqueId());

        if(user == null)
            return;

        user.addBackup(p, Backup.BackupType.DEATH);

        if(Config.SAVE_ON_DEATH)
           database.saveOrPut(user);
    }
}
