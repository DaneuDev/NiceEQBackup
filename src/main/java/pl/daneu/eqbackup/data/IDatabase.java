package pl.daneu.eqbackup.data;

import org.bukkit.entity.Player;
import pl.daneu.eqbackup.objects.User;

import java.sql.Connection;
import java.util.Set;

public interface IDatabase {

    Connection getConnection();

    boolean createTable();

    Set<String> getPlayersNames();

    void insertUser(Player p);

    User getOfflineUser(String name);

    void saveOrPut(User user);

    void saveOrPutSync(User user);
}
