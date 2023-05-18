package pl.daneu.eqbackup.guis.showcases;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;
import pl.daneu.eqbackup.data.Config;
import pl.daneu.eqbackup.data.IDatabase;
import pl.daneu.eqbackup.guis.BackupsShowcaseGUI;
import pl.daneu.eqbackup.guis.customgui.ContentGUI;
import pl.daneu.eqbackup.guis.customgui.CustomGUI;
import pl.daneu.eqbackup.guis.customgui.ItemGUI;
import pl.daneu.eqbackup.guis.customgui.OnCloseGUI;
import pl.daneu.eqbackup.objects.Backup;
import pl.daneu.eqbackup.objects.User;
import pl.daneu.eqbackup.utils.ItemBuilder;

import java.text.SimpleDateFormat;

public class ECShowcaseGUI extends CustomGUI implements ContentGUI, OnCloseGUI {

    private final User user;
    private final Backup backup;
    private final Backup.BackupType backupType;
    private final IDatabase database;
    private boolean isChanged = false;

    public ECShowcaseGUI(User user, Backup backup, Backup.BackupType backupType, IDatabase database) {
        super(Config.SHOWCASE_EC_TITLE.replaceAll("%time%", new SimpleDateFormat("HH:mm:ss, dd.MM.yyyy").format(backup.getCreateTime())), 4);

        this.user = user;
        this.backup = backup;
        this.backupType = backupType;
        this.database = database;
    }

    public Backup getBackup(){ return backup; }
    public boolean isChanged(){ return isChanged; }

    @Override
    public void setContent() {
        for(int i = 0; i < 3*9; i++)
            setECItem(i, null);

        backup.getEnderChestItems().forEach(this::setECItem);

        ItemStack glass = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE, 1, " ").create();
        for(int i = 1; i < 10; i++)
            setItem(i, 4, glass, false);

        ItemStack exit = Config.SHOWCASE_GO_BACK;
        setItem(5, 4, new ItemGUI(exit)
                .execute(e -> {
                    Player p = (Player) e.getWhoClicked();

                    if(isChanged)
                        backup.updateECItems(getInventory());

                    new BackupsShowcaseGUI(user, backupType, database).open(p);
                }));
    }

    @Override
    public Consumer<InventoryCloseEvent> addOnClose() {
        ECShowcaseGUI gui = this;

        return inventoryCloseEvent -> {
            if(gui.isChanged())
                gui.getBackup().updateECItems(gui.getInventory());
        };
    }

    private void setECItem(int slot, ItemStack item){
        setItem(slot, new ItemGUI(item, true).execute(e -> {
            if(!isChanged)
                isChanged = true;
        }));
    }
}
