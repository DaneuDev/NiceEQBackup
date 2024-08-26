package pl.daneu.niceeqbackup.data;

import pl.daneu.niceeqbackup.objects.User;

import java.sql.Connection;
import java.util.Set;
import java.util.UUID;

public interface IDatabase {

    Connection getConnection();

    boolean createAndFixTable();

    Set<String> getPlayersNames();

    User getUser(UUID uniqueId);

    User getUser(String name);

    User getUser(String type, String... values);

    void saveOrPutAsync(User user);

    void saveOrPut(User user);

}
