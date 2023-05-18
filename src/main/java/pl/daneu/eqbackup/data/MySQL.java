package pl.daneu.eqbackup.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.daneu.eqbackup.NiceEQBackup;
import pl.daneu.eqbackup.commands.NiceEQBackupCommand;
import pl.daneu.eqbackup.objects.Backup;
import pl.daneu.eqbackup.objects.User;

import java.sql.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

public class MySQL implements IDatabase{

    private final NiceEQBackup plugin;
    private final HikariDataSource hikariDataSource;

    public MySQL(NiceEQBackup plugin) {
        this.plugin = plugin;

        FileConfiguration fileConfiguration = plugin.getConfig();

        String host = fileConfiguration.getString("database.host");
        String port = fileConfiguration.getString("database.port");
        String database = fileConfiguration.getString("database.database-name");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";

        String username = fileConfiguration.getString("database.username");
        String password = fileConfiguration.getString("database.password");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);

        if(username != null && !username.isEmpty())
            hikariConfig.setUsername(username);

        if(password != null && !password.isEmpty())
            hikariConfig.setPassword(password);

        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        hikariConfig.setMaximumPoolSize((Runtime.getRuntime().availableProcessors() * 2) + 1);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() {
        if(!hikariDataSource.isRunning()) {
            Bukkit.getLogger().severe("NiceEQBackup | There is a problem with a connection to database");
            return null;
        }

        try {
            return hikariDataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("NiceEQBackup | There is a problem with a connection to database");

            return null;
        }
    }

    @Override
    public boolean createTable() {
        StringBuilder backups = new StringBuilder();
        for(int i = 0; i < 45; i++)

            backups.append("backup").append(i).append(" TEXT, ");
        for(int i = 0; i < 45; i++)
            backups.append("deathBackup").append(i).append(" TEXT, ");

        backups.delete(backups.length() - 2, backups.length());
        backups.append(")");

        String statement = "CREATE TABLE IF NOT EXISTS " +
                "nice_eq_backups(uuid VARCHAR(36) PRIMARY KEY, " +
                "player_name TEXT, " + backups;

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.execute();

            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("NiceEQBackup | There is a problem with creating a table");

            return false;
        }
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
            e.printStackTrace();
            Bukkit.getLogger().severe("NiceEQBackup | There is a problem with getting users nicknames");

            return null;
        }
    }

    @Override
    public void insertUser(Player p) {
        String statement = "SELECT * FROM nice_eq_backups WHERE uuid=?";

        new BukkitRunnable() {
            @Override
            public void run() {
                User user;

                try (Connection connection = plugin.getDatabase().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                    preparedStatement.setString(1, p.getUniqueId().toString());

                    try(ResultSet resultSet = preparedStatement.executeQuery()){
                        if(!resultSet.next()) {
                            user = new User(p.getUniqueId(), p.getName());
                            saveOrPut(user);

                            NiceEQBackupCommand.PLAYERS_NAMES = getPlayersNames();
                        }
                        else{
                            LinkedList<Backup> backups = new LinkedList<>();
                            LinkedList<Backup> deathBackups = new LinkedList<>();

                            for(int i = 0; i < Config.MAX_BACKUPS_AMOUNT; i++) {
                                String backupS = resultSet.getString("backup" + i);

                                if(backupS == null || backupS.isEmpty()) continue;

                                backups.add(Backup.valueOf(backupS));
                            }

                            for(int i = 0; i < Config.MAX_DEATH_BACKUPS_AMOUNT; i++) {
                                String backupS = resultSet.getString("deathBackup" + i);

                                if(backupS == null || backupS.isEmpty()) continue;

                                deathBackups.add(Backup.valueOf(backupS));
                            }

                            user = new User(p.getUniqueId(), p.getName(), backups, deathBackups);
                        }
                    }
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    Bukkit.getLogger().severe("NiceEQBackup | There is a problem with getting the user online");

                    return;
                }

                plugin.getUserHandler().addUser(user);
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    public User getOfflineUser(String name) {
        String statement = "SELECT * FROM nice_eq_backups WHERE player_name=?";

        try (Connection connection = plugin.getDatabase().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setString(1, name);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(!resultSet.next())
                    return null;
                else{
                    LinkedList<Backup> backups = new LinkedList<>();
                    LinkedList<Backup> deathBackups = new LinkedList<>();

                    for(int i = 0; i < Config.MAX_BACKUPS_AMOUNT; i++) {
                        String backupS = resultSet.getString("backup" + i);

                        if(backupS == null || backupS.isEmpty()) continue;

                        backups.add(Backup.valueOf(backupS));
                    }

                    for(int i = 0; i < Config.MAX_DEATH_BACKUPS_AMOUNT; i++) {
                        String backupS = resultSet.getString("deathBackup" + i);

                        if(backupS == null || backupS.isEmpty()) continue;

                        deathBackups.add(Backup.valueOf(backupS));
                    }

                    return new User(UUID.fromString(resultSet.getString("uuid")), name, backups, deathBackups);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("NiceEQBackup | There is a problem with getting the user offline");

            return null;
        }
    }

    @Override
    public void saveOrPut(User user) {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveOrPutSync(user);
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    public void saveOrPutSync(User user) {
        StringBuilder backupsValues = new StringBuilder();
        for(int i = 0; i < 90; i++)
            backupsValues.append("''").append(",");
        backupsValues.deleteCharAt(backupsValues.length() - 1);

        StringBuilder backupsDK = new StringBuilder();
        for(int i = 0; i < Config.MAX_BACKUPS_AMOUNT; i++)
            backupsDK.append("backup").append(i).append("=?, ");
        for(int i = 0; i < Config.MAX_DEATH_BACKUPS_AMOUNT; i++)
            backupsDK.append("deathBackup").append(i).append("=?, ");
        backupsDK.delete(backupsDK.length() - 2, backupsDK.length());

        String statement = "INSERT INTO nice_eq_backups VALUES(?,?," + backupsValues + ") " +
                "ON DUPLICATE KEY UPDATE " + backupsDK;

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setString(1, user.getUUID().toString());
            preparedStatement.setString(2, user.getPlayerName());

            for(int i = 0; i < Config.MAX_BACKUPS_AMOUNT; i++) {
                LinkedList<Backup> backupsList = user.getBackups(Backup.BackupType.ALIVE);
                String backup;
                if(backupsList.size() > i)
                    backup = backupsList.get(i).toString();
                else
                    backup = "";

                if(backup == null) continue;

                preparedStatement.setString(i + 3, backup);
            }

            for(int i = 0; i < Config.MAX_DEATH_BACKUPS_AMOUNT; i++) {
                LinkedList<Backup> backupsList = user.getBackups(Backup.BackupType.DEATH);
                String backup;
                if(backupsList.size() > i)
                    backup = backupsList.get(i).toString();
                else
                    backup = "";

                if(backup == null) continue;

                preparedStatement.setString(1 + 2 + Config.MAX_BACKUPS_AMOUNT + i, backup);
            }

            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("NiceEQBackup | There is a problem with saving the user");
        }
    }
}
