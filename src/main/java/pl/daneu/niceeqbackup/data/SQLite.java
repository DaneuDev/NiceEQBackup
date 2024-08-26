package pl.daneu.niceeqbackup.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.daneu.niceeqbackup.NiceEQBackup;
import pl.daneu.niceeqbackup.commands.NiceEQBackupCommand;
import pl.daneu.niceeqbackup.objects.Backup;
import pl.daneu.niceeqbackup.objects.User;
import pl.daneu.niceeqbackup.utils.LoggerUtil;

import java.sql.*;
import java.util.*;

public class SQLite implements IDatabase{

    private final NiceEQBackup plugin;
    private final String url;

    public SQLite(NiceEQBackup plugin){
        this.plugin = plugin;
        url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/backups.db";
    }

    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            LoggerUtil.sendSevereLog("There is a problem with a connection to database");
            e.printStackTrace();

            return null;
        }
    }

    //I could do it in one single statement, but who cares
    @Override
    public boolean createAndFixTable() {
        StringBuilder backups = new StringBuilder();
        for(int i = 0; i < Config.MAX_BACKUPS_AMOUNT; i++)
            backups.append("backup").append(i).append(" TEXT, ");

        backups.delete(backups.length() - 2, backups.length());
        backups.append(")");

        StringBuilder statement = new StringBuilder("CREATE TABLE IF NOT EXISTS " +
                "nice_eq_backups(uuid VARCHAR(36) PRIMARY KEY, " +
                "player_name TEXT NOT NULL, " + backups);

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement.toString())) {
            preparedStatement.execute();
        }
        catch (SQLException e) {
            LoggerUtil.sendSevereLog("There is a problem with creating a table");
            e.printStackTrace();

            return false;
        }

        statement = new StringBuilder("SELECT COUNT(*) FROM pragma_table_info('nice_eq_backups') WHERE name LIKE 'backup%'");
        int amount;

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement.toString())) {
            amount = preparedStatement.executeQuery().getInt(1);
        }
        catch (SQLException e) {
            LoggerUtil.sendSevereLog("There is a problem with checking a table");
            e.printStackTrace();

            return false;
        }

        if(amount == Config.MAX_BACKUPS_AMOUNT)
            return true;
        else if(amount < Config.MAX_BACKUPS_AMOUNT){
            for(int i = amount; i < Config.MAX_BACKUPS_AMOUNT; i++){
                statement = new StringBuilder().append("ALTER TABLE nice_eq_backups ADD COLUMN backup").append(i).append(" TEXT;");

                try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement.toString())) {
                    preparedStatement.execute();
                }
                catch (SQLException e) {
                    LoggerUtil.sendSevereLog("There is a problem with updating a table");
                    e.printStackTrace();

                    return false;
                }
            }

            return true;
        }

        for(int i = amount; i > Config.MAX_BACKUPS_AMOUNT; i--){
            statement = new StringBuilder().append("ALTER TABLE nice_eq_backups DROP COLUMN backup").append(i - 1).append(";");

            try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement.toString())) {
                preparedStatement.execute();
            }
            catch (SQLException e) {
                LoggerUtil.sendSevereLog("There is a problem with updating a table");
                e.printStackTrace();

                return false;
            }
        }

        return true;
    }

    @Override
    public Set<String> getPlayersNames() {
        String statement = "SELECT player_name FROM nice_eq_backups";
        Set<String> playersNames = new HashSet<>();

        try (Connection connection = plugin.getDatabase().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement); ResultSet resultSet = preparedStatement.executeQuery()) {
            while(resultSet.next())
                playersNames.add(resultSet.getString("player_name"));

            return playersNames;
        }
        catch (SQLException e) {
            LoggerUtil.sendSevereLog("There is a problem with getting users nicknames");
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public User getUser(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);

        if(p == null)
            return null;

        return getUser("uuid", uuid.toString(), p.getName());
    }

    @Override
    public User getUser(String name){
        return getUser("name", name);
    }

    @Override
    public User getUser(String type, String... values) {
        String statement = type.equals("uuid") ?
                "SELECT * FROM nice_eq_backups WHERE uuid=?" :
                "SELECT * FROM nice_eq_backups WHERE player_name=?";

        User user = null;

        try (Connection connection = plugin.getDatabase().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setString(1, values[0]);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    LinkedList<Backup> backups = new LinkedList<>();

                    for(int i = 0; i < Config.MAX_BACKUPS_AMOUNT; i++) {
                        String backupContent = resultSet.getString("backup" + i);

                        if(backupContent == null || backupContent.trim().isEmpty())
                            continue;

                        Backup backup = Backup.valueOf(backupContent);
                        backups.add(backup);
                    }

                    if(type.equals("name"))
                        user = new User(null, values[0], backups);
                    else
                        user = new User(UUID.fromString(values[0]), values[1], backups);
                }
            }
        }
        catch (SQLException e) {
            LoggerUtil.sendSevereLog("There is a problem with getting the user online");
            e.printStackTrace();

            return null;
        }

        return user;
    }

    @Override
    public void saveOrPutAsync(User user) {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveOrPut(user);
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    public void saveOrPut(User user) {
        StringBuilder backupsValues = new StringBuilder();
        for(int i = 0; i < Config.MAX_BACKUPS_AMOUNT; i++){
            String toInsert = "''";

            if(user.getBackups().size() >= i + 1){
                Backup backup = user.getBackups().get(i);
                toInsert = "'" + backup.toString() + "'";
            }

            backupsValues.append(toInsert).append(",");
        }
        backupsValues.deleteCharAt(backupsValues.length() - 1);

        StringBuilder backupsUpdate = new StringBuilder();
        for(int i = 0; i < Config.MAX_BACKUPS_AMOUNT; i++)
            backupsUpdate.append("backup").append(i).append("=?, ");
        backupsUpdate.delete(backupsUpdate.length() - 2, backupsUpdate.length());

        String statement = "INSERT INTO nice_eq_backups VALUES(?,?," + backupsValues + ") " +
                "ON CONFLICT(uuid) DO UPDATE SET " + backupsUpdate;

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setString(1, user.getUUID().toString());
            preparedStatement.setString(2, user.getPlayerName());

            for(int i = 0; i < Config.MAX_BACKUPS_AMOUNT; i++) {
                List<Backup> backupsList = user.getBackups();
                Backup backup = backupsList.size() > i ? backupsList.get(i) : null;

                if(backup == null)
                    continue;

                preparedStatement.setString(i + 3, backup.toString());
            }

            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            LoggerUtil.sendSevereLog("There is a problem with saving the user");
            e.printStackTrace();
        }
    }
}
