package pl.daneu.eqbackup;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pl.daneu.eqbackup.commands.NiceEQBackupCommand;
import pl.daneu.eqbackup.data.Config;
import pl.daneu.eqbackup.data.IDatabase;
import pl.daneu.eqbackup.data.MySQL;
import pl.daneu.eqbackup.data.SQLite;
import pl.daneu.eqbackup.guis.customgui.ManagerGUI;
import pl.daneu.eqbackup.handlers.UserHandler;
import pl.daneu.eqbackup.listeners.Listeners;
import pl.daneu.eqbackup.tasks.BackupInventoriesTask;

public class NiceEQBackup extends JavaPlugin {

    private IDatabase database;
    private UserHandler userHandler;
    private BackupInventoriesTask backupInventoriesTask;

    public IDatabase getDatabase(){ return database; }
    public UserHandler getUserHandler(){ return userHandler; }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginManager pluginManager = Bukkit.getPluginManager();

        if(!initDatabase() || !Config.init(this)){
            pluginManager.disablePlugin(this);
            return;
        }

        userHandler = new UserHandler();

        pluginManager.registerEvents(new Listeners(this, database, userHandler), this);

        new ManagerGUI(this);
        new NiceEQBackupCommand(this);

        int time = getConfig().getInt("tasks.backup-period-time") * 20;
        backupInventoriesTask = new BackupInventoriesTask(database, userHandler);
        backupInventoriesTask.runTaskTimer(this, time, time);
    }

    @Override
    public void onDisable() {
        if(database != null && database.getConnection() != null)
            userHandler.getUsers().values().forEach(database::saveOrPutSync);
    }

    public void reloadBackupInventoryTask(){
        backupInventoriesTask.cancel();

        int time = getConfig().getInt("tasks.backup-period-time") * 20;
        backupInventoriesTask = new BackupInventoriesTask(database, userHandler);
        backupInventoriesTask.runTaskTimer(this, time, time);
    }

    public void reloadDatabase(){
        if(database != null && database.getConnection() != null)
            userHandler.getUsers().values().forEach(database::saveOrPutSync);
        userHandler.getUsers().clear();

        if(!initDatabase()) {
            getPluginLoader().disablePlugin(this);

            return;
        }

        Bukkit.getOnlinePlayers().forEach(database::insertUser);

        NiceEQBackupCommand.PLAYERS_NAMES = database.getPlayersNames();
    }

    private boolean initDatabase(){
        String databaseType = getConfig().getString("database.type");
        if(databaseType == null || databaseType.isEmpty()){
            Bukkit.getLogger().severe("NiceEQBackup | You have to specify a type of database");

            return false;
        }
        switch (databaseType){
            case "mysql":
                database = new MySQL(this);
                break;
            case "sqlite":
                database = new SQLite(this);
                break;
            default:
                Bukkit.getLogger().severe("NiceEQBackup | You have to specify a correct type of database (mysql/sqlite)");

                return false;
        }

        if(database.getConnection() == null) {
            Bukkit.getLogger().severe("NiceEQBackup | No connection with the database");

            return false;
        }

        return database.createTable();
    }
}
