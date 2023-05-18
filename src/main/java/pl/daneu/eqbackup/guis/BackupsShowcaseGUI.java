package pl.daneu.eqbackup.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import pl.daneu.eqbackup.data.Config;
import pl.daneu.eqbackup.data.IDatabase;
import pl.daneu.eqbackup.guis.customgui.ContentGUI;
import pl.daneu.eqbackup.guis.customgui.CustomGUI;
import pl.daneu.eqbackup.guis.customgui.ItemGUI;
import pl.daneu.eqbackup.guis.showcases.EQShowcaseGUI;
import pl.daneu.eqbackup.guis.showcases.ECShowcaseGUI;
import pl.daneu.eqbackup.objects.Backup;
import pl.daneu.eqbackup.objects.User;
import pl.daneu.eqbackup.utils.ChatUtil;
import pl.daneu.eqbackup.utils.ItemBuilder;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class BackupsShowcaseGUI extends CustomGUI implements ContentGUI {

    private final User user;
    private final Backup.BackupType backupType;
    private final LinkedList<Backup> backups;
    private final int invSize;
    private final IDatabase database;

    public BackupsShowcaseGUI(User user, Backup.BackupType backupType, IDatabase database) {
        super(backupType == Backup.BackupType.ALIVE ? Config.BACKUPSLIST_TITLE : Config.BACKUPSLIST_DEATH_TITLE,
                (user.getBackups(backupType).size() / 9) + (user.getBackups(backupType).size() % 9 == 0 ? 0 : 1) + 1);

        this.user = user;
        this.backups = user.getBackups(backupType);
        this.backupType = backupType;
        invSize = (backups.size() / 9) * 9 + (backups.size() % 9 == 0 ? 0 : 9) + 9;
        this.database = database;
    }

    public User getUser(){ return user; }
    public Backup.BackupType getBackupType(){ return backupType; }

    @Override
    public void setContent() {
        ItemStack glass = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE, 1, " ").create();
        for(int i = invSize - 9; i < invSize; i++)
            setItem(i, glass, false);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss, dd.MM.yyyy");
        for(int i = 0; i < backups.size(); i++){
            Backup backup = backups.get(i);
            Timestamp time = backup.getCreateTime();

            Map<String, String> replacing = new HashMap<>();
            replacing.put("%number%", i+1 + " ");
            replacing.put("%time%", format.format(time));
            ItemStack backupItem = Config.fixItem(Config.BACKUPSLIST_BACKUP, replacing);

            setItem(i, new ItemGUI(backupItem)
                    .execute(e -> {
                        Player p = (Player) e.getWhoClicked();
                        ClickType clickType = e.getClick();

                        switch(clickType){
                            case LEFT:
                                new EQShowcaseGUI(user, backup, backupType, database).open(p);
                                break;

                            case RIGHT:
                                new ECShowcaseGUI(user, backup, backupType, database).open(p);
                                break;

                            case SHIFT_LEFT: {
                                Player target = Bukkit.getPlayer(user.getUUID());

                                if (target == null) {
                                    String message = Config.MESSAGES_BACKUPSLIST_GIVE_EQ_PLAYER_OFFLINE.replaceAll("%player%", user.getPlayerName());
                                    ChatUtil.message(p, message);

                                    return;
                                }

                                PlayerInventory inv = target.getInventory();
                                inv.clear();
                                backup.getItems().forEach(inv::setItem);

                                p.setLevel(backup.getLevel());
                                p.setExp(backup.getExpToLevel());

                                String message = Config.MESSAGES_BACKUPSLIST_GIVE_EQ_SUCCEED.replaceAll("%player%", target.getName()).replaceAll("%time%", format.format(time));
                                ChatUtil.message(p, message);

                                break;
                            }

                            case SHIFT_RIGHT: {
                                Player target = Bukkit.getPlayer(user.getUUID());

                                if (target == null) {
                                    String message = Config.MESSAGES_BACKUPSLIST_GIVE_EC_PLAYER_OFFLINE.replaceAll("%player%", user.getPlayerName());
                                    ChatUtil.message(p, message);

                                    return;
                                }

                                Inventory enderChest = target.getEnderChest();
                                enderChest.clear();
                                backup.getEnderChestItems().forEach(enderChest::setItem);

                                String message = Config.MESSAGES_BACKUPSLIST_GIVE_EC_SUCCEED.replaceAll("%player%", target.getName()).replaceAll("%time%", format.format(time));
                                ChatUtil.message(p, message);

                                break;
                            }

                            case MIDDLE: {
                                backups.remove(backup);

                                if (Bukkit.getPlayer(user.getUUID()) == null)
                                    database.saveOrPut(user);

                                new BackupsShowcaseGUI(user, backupType, database).open(p);

                                String message = Config.MESSAGES_BACKUPSLIST_DELETE.replaceAll("%player%", user.getPlayerName()).replaceAll("%time%", format.format(time));
                                ChatUtil.message(p, message);
                            }
                        }
                    }));
        }

        ItemStack exit = new ItemBuilder(Material.BARRIER, 1, ChatUtil.getColoredText("&cGo back to panel")).create();
        setItem(invSize - 5, new ItemGUI(exit)
                .execute(e -> {
                    Player p = (Player) e.getWhoClicked();

                    new PanelGUI(user, database).open(p);
                }));
    }
}
