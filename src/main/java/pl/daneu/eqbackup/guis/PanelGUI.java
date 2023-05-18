package pl.daneu.eqbackup.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import pl.daneu.eqbackup.data.Config;
import pl.daneu.eqbackup.data.IDatabase;
import pl.daneu.eqbackup.guis.customgui.ContentGUI;
import pl.daneu.eqbackup.guis.customgui.CustomGUI;
import pl.daneu.eqbackup.guis.customgui.ItemGUI;
import pl.daneu.eqbackup.guis.customgui.ManagerGUI;
import pl.daneu.eqbackup.objects.Backup;
import pl.daneu.eqbackup.objects.User;
import pl.daneu.eqbackup.utils.ChatUtil;
import pl.daneu.eqbackup.utils.ItemBuilder;

import java.util.HashMap;
import java.util.Map;

public class PanelGUI extends CustomGUI implements ContentGUI {

    private final User user;
    private final IDatabase database;

    public PanelGUI(User user, IDatabase database) {
        super(Config.PANEL_TITLE.replaceAll("%player%", user.getPlayerName()), InventoryType.HOPPER);

        this.user = user;
        this.database = database;
    }

    @Override
    public void setContent() {
        ItemStack glass = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE, 1, " ").create();
        for(int i = 0; i < 5; i += 2)
            setItem(i, glass, false);

        Map<String, String> replacing = new HashMap<>();
        replacing.put("%backups%", user.getBackups(Backup.BackupType.ALIVE).size() + "");
        replacing.put("%death-backups%", user.getBackups(Backup.BackupType.DEATH).size() + "");

        ItemStack backupsList = Config.fixItem(Config.PANEL_BACKUPSLIST, replacing);

        setItem(1, new ItemGUI(backupsList)
                .execute(e -> {
                    Player p = (Player) e.getWhoClicked();
                    ClickType clickType = e.getClick();

                    switch(clickType){
                        case LEFT:
                            new BackupsShowcaseGUI(user, Backup.BackupType.ALIVE, database).open(p);
                            break;
                        case RIGHT:
                            new BackupsShowcaseGUI(user, Backup.BackupType.DEATH, database).open(p);
                            break;
                    }
                }));

        ItemStack saveEQ = Config.PANEL_DO_BACKUP;

        setItem(3, new ItemGUI(saveEQ)
                .execute(e -> {
                    Player p = (Player) e.getWhoClicked(),
                            target = Bukkit.getPlayer(user.getUUID());

                    if(target == null){
                        String message = Config.MESSAGES_PANEL_PLAYER_OFFLINE.replaceAll("%player%", user.getPlayerName());
                        ChatUtil.message(p, message);

                        return;
                    }

                    user.addBackup(target, Backup.BackupType.ALIVE);

                    if(Config.SAVE_ON_PANEL_SAVE)
                        database.saveOrPut(user);

                    String message = Config.MESSAGES_PANEL_SUCCEED.replaceAll("%player%", user.getPlayerName());
                    ChatUtil.message(p, message);

                    Bukkit.getOnlinePlayers().stream()
                            .filter(lP -> lP.hasPermission("niceeqbackups.use") && lP.getOpenInventory().getTopInventory().getHolder() instanceof BackupsShowcaseGUI)
                            .forEach(lP -> {
                                BackupsShowcaseGUI backupsShowcaseGUI = (BackupsShowcaseGUI) (ManagerGUI.getManagerGUI().getGUI(lP.getUniqueId()));
                                new BackupsShowcaseGUI(backupsShowcaseGUI.getUser(), backupsShowcaseGUI.getBackupType(), database).open(lP);
                            });
                }));
    }
}
