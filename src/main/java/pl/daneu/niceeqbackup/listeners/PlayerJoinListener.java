package pl.daneu.niceeqbackup.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.daneu.niceeqbackup.data.IDatabase;
import pl.daneu.niceeqbackup.handlers.UserHandler;
import pl.daneu.niceeqbackup.objects.User;

public class PlayerJoinListener implements Listener {

    private final IDatabase database;
    private final UserHandler userHandler;

    public PlayerJoinListener(IDatabase database, UserHandler userHandler){
        this.database = database;
        this.userHandler = userHandler;
    }

    @EventHandler
    public void onPlayerQuit(PlayerJoinEvent e){
        Player p = e.getPlayer();
        User user = database.getUser(p.getUniqueId());

        if(user == null)
            user = new User(p);

        userHandler.addUser(user);
    }
}
