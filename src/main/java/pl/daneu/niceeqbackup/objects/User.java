package pl.daneu.niceeqbackup.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import pl.daneu.niceeqbackup.data.Config;

import java.sql.Timestamp;
import java.util.*;

public class User {

    private final UUID uuid;
    private final String playerName;
    private final LinkedList<Backup> backups;

    public User(Player p){
        this.uuid = p.getUniqueId();
        this.playerName = p.getName();
        this.backups = new LinkedList<>();
    }

    public User(UUID uuid, String playerName, LinkedList<Backup> backups){
        this.uuid = uuid;
        this.playerName = playerName;
        this.backups = backups;
    }

    public UUID getUUID(){
        return uuid;
    }

    public String getPlayerName(){
        return playerName;
    }

    public LinkedList<Backup> getBackups(){
        return backups;
    }

    public void addBackup(Player p, Backup.SaveType saveType){
        PlayerInventory inv = p.getInventory();

        Timestamp createTime = new Timestamp(System.currentTimeMillis());
        BackupInventory backupInventory = new BackupInventory(inv);

        if(backups.size() == Config.MAX_BACKUPS_AMOUNT)
            backups.removeLast();

        Backup backup = new Backup(saveType, createTime, backupInventory, p.getHealth(), p.getFoodLevel(), p.getTotalExperience(), p.getLocation());

        if(saveType == Backup.SaveType.DEATH)
            backup.setHealth(20);

        backups.addFirst(backup);
    }
}
