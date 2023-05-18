package pl.daneu.eqbackup.objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import pl.daneu.eqbackup.data.Config;

import java.sql.Timestamp;
import java.util.*;

public class User {

    private final UUID uuid;
    private final String playerName;
    private final LinkedList<Backup> backups;
    private final LinkedList<Backup> deathBackups;

    public User(UUID uuid, String playerName){
        this.uuid = uuid;
        this.playerName = playerName;
        this.backups = new LinkedList<>();
        this.deathBackups = new LinkedList<>();
    }

    public User(UUID uuid, String playerName, LinkedList<Backup> backups, LinkedList<Backup> deathBackups){
        this.uuid = uuid;
        this.playerName = playerName;
        this.backups = backups;
        this.deathBackups = deathBackups;
    }

    public UUID getUUID(){ return uuid; }
    public String getPlayerName(){ return playerName; }
    public LinkedList<Backup> getBackups(Backup.BackupType backupType){
        return backupType == Backup.BackupType.ALIVE ? backups : deathBackups;
    }

    public void addBackup(Player p, Backup.BackupType backupType){
        LinkedList<Backup> backupsToAddTo = backupType == Backup.BackupType.ALIVE ? backups : deathBackups;

        Map<Integer, ItemStack> items = new HashMap<>();
        Map<Integer, ItemStack> enderChestItems = new HashMap<>();
        PlayerInventory inv = p.getInventory();
        Inventory enderChest = p.getEnderChest();

        if(inv.getSize() != 0)
            for(int slot = 40; slot >= 0; slot--){
                ItemStack item = inv.getItem(slot);

                if(item == null || item.getType() == Material.AIR)
                    continue;

                items.put(slot, item.clone());
            }

        if(enderChest.getSize() != 0)
            for(int slot = 0; slot < 6*9; slot++){
                ItemStack item = enderChest.getItem(slot);

                if(item == null || item.getType() == Material.AIR)
                    continue;

                enderChestItems.put(slot, item.clone());
            }

        Timestamp createTime = new Timestamp(System.currentTimeMillis());

        if(backupsToAddTo.size() + 1 > Config.MAX_BACKUPS_AMOUNT)
            backupsToAddTo.removeFirst();

        backupsToAddTo.add(new Backup(createTime, p.getLevel(), p.getExp(), items, enderChestItems));
    }
}
