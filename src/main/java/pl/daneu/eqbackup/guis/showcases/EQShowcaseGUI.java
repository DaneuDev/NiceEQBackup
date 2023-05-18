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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class EQShowcaseGUI extends CustomGUI implements ContentGUI, OnCloseGUI {

    private final User user;
    private final Backup backup;
    private final Backup.BackupType backupType;
    private final IDatabase database;
    private boolean isChanged = false;

    public EQShowcaseGUI(User user, Backup backup, Backup.BackupType backupType, IDatabase database) {
        super(Config.SHOWCASE_EQ_TITLE.replaceAll("%time%", new SimpleDateFormat("HH:mm:ss, dd.MM.yyyy").format(backup.getCreateTime())), 5);

        this.user = user;
        this.backup = backup;
        this.backupType = backupType;
        this.database = database;
    }

    public Backup getBackup(){ return backup; }
    public boolean isChanged(){ return isChanged; }

    @Override
    public void setContent() {
        for(int i = 0; i < 4*9 + 5; i++)
            setEQItem(i, null);

        backup.getItems().forEach((slot, item) -> {
            if(slot < 9)
                setEQItem(slot + 3*9, item);
            else if(slot == 40)
                setEQItem(4*9 + 4, item);
            else if(slot > 35)
                setEQItem(slot, item);
            else
                setEQItem(slot - 9, item);
        });

        ItemStack glass = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE, 1, " ").create();
        for(int i = 0; i < 3; i++)
            setItem(6 + i, 5, glass, false);

        Map<String, String> replacing = new HashMap<>();
        replacing.put("%level%", backup.getLevel() + "");
        replacing.put("%exp-to-level%", new DecimalFormat("##.##").format(backup.getExpToLevel()*10));
        ItemStack exp = Config.fixItem(Config.SHOWCASE_EXP, replacing);
        setItem(7, 5, exp, false);

        ItemStack exit = Config.SHOWCASE_GO_BACK;
        setItem(9, 5, new ItemGUI(exit)
                .execute(e -> {
                    Player p = (Player) e.getWhoClicked();

                    if(isChanged)
                        backup.updateEQItems(getInventory());

                    new BackupsShowcaseGUI(user, backupType, database).open(p);
                }));
    }

    @Override
    public Consumer<InventoryCloseEvent> addOnClose() {
        EQShowcaseGUI gui = this;

        return inventoryCloseEvent -> {
            if(gui.isChanged())
                gui.getBackup().updateEQItems(gui.getInventory());
        };
    }

    private void setEQItem(int slot, ItemStack item){
        setItem(slot, new ItemGUI(item, true).execute(e -> {
            if(!isChanged)
                isChanged = true;
        }));
    }
}
