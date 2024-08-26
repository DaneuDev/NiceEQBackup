package pl.daneu.niceeqbackup;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pl.daneu.daneutools.customgui.ManagerGUI;
import pl.daneu.niceeqbackup.commands.NiceEQBackupCommand;
import pl.daneu.niceeqbackup.data.Config;
import pl.daneu.niceeqbackup.data.IDatabase;
import pl.daneu.niceeqbackup.data.MySQL;
import pl.daneu.niceeqbackup.data.SQLite;
import pl.daneu.niceeqbackup.handlers.SettingChangeHandler;
import pl.daneu.niceeqbackup.handlers.UserHandler;
import pl.daneu.niceeqbackup.listeners.PlayerDeathListener;
import pl.daneu.niceeqbackup.listeners.PlayerJoinListener;
import pl.daneu.niceeqbackup.listeners.PlayerQuitListener;
import pl.daneu.niceeqbackup.listeners.PlayerSendMessageListener;
import pl.daneu.niceeqbackup.objects.SettingChange;
import pl.daneu.niceeqbackup.objects.User;
import pl.daneu.niceeqbackup.tasks.BackupInventoriesTask;
import pl.daneu.niceeqbackup.utils.LoggerUtil;

import java.util.stream.Stream;

public class NiceEQBackup extends JavaPlugin {

    private static NiceEQBackup instance;

    private IDatabase database;
    private UserHandler userHandler;
    private SettingChangeHandler settingChangeHandler;
    private BackupInventoriesTask backupInventoriesTask;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        PluginManager pluginManager = Bukkit.getPluginManager();

        if(!Config.init(this) || !initDatabase()){
            pluginManager.disablePlugin(this);
            return;
        }
        Config.initPlayersNames(this);

        userHandler = new UserHandler();
        settingChangeHandler = new SettingChangeHandler();

        Stream.of(
                new PlayerJoinListener(database, userHandler),
                new PlayerQuitListener(userHandler, database),
                new PlayerDeathListener(),
                new PlayerSendMessageListener()
        ).forEach(e -> pluginManager.registerEvents(e, this));

        new ManagerGUI(this);
        new NiceEQBackupCommand(this);

        if(Config.SAVE_ON_TASK) {
            int time = getConfig().getInt("options.save-task-interval") * 20;
            backupInventoriesTask = new BackupInventoriesTask(database, userHandler);
            backupInventoriesTask.runTaskTimer(this, time, time);
        }

        LoggerUtil.sendInfoLog("Plugin enabled");
    }

    @Override
    public void onDisable() {
        if(database != null && database.getConnection() != null)
            userHandler.getUsers().forEach(database::saveOrPut);

        LoggerUtil.sendInfoLog("Plugin disabled");
    }

    public IDatabase getDatabase(){
        return database;
    }

    public SettingChangeHandler getSettingChangeHandler() {
        return settingChangeHandler;
    }

    public UserHandler getUserHandler() {
        return userHandler;
    }

    public BackupInventoriesTask getBackupInventoriesTask() {
        return backupInventoriesTask;
    }

    public void setBackupInventoriesTask(BackupInventoriesTask backupInventoriesTask) {
        this.backupInventoriesTask = backupInventoriesTask;
    }

    public boolean initDatabase(){
        String databaseType = getConfig().getString("database.type");

        if(databaseType == null || databaseType.trim().isEmpty()){
            LoggerUtil.sendSevereLog("You have to specify a correct type of database (mysql/sqlite)");

            return false;
        }

        switch (databaseType){
            case "mysql" -> database = new MySQL(this);
            case "sqlite" -> database = new SQLite(this);
            default -> {
                LoggerUtil.sendSevereLog("You have to specify a correct type of database (mysql/sqlite)");

                return false;
            }
        }

        if(database.getConnection() == null) {
            LoggerUtil.sendSevereLog("No connection with the database");

            return false;
        }

        return database.createAndFixTable();
    }

    public static NiceEQBackup getInstance(){
        return instance;
    }
}
