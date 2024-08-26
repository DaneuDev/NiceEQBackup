package pl.daneu.niceeqbackup.guis;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import pl.daneu.daneutools.customgui.ContentGUI;
import pl.daneu.daneutools.customgui.CustomGUI;
import pl.daneu.daneutools.customgui.ItemGUI;
import pl.daneu.daneutools.customgui.OnCloseGUI;
import pl.daneu.daneutools.utils.ChatUtil;
import pl.daneu.daneutools.utils.ItemBuilder;
import pl.daneu.daneutools.utils.TextUtil;
import pl.daneu.niceeqbackup.NiceEQBackup;
import pl.daneu.niceeqbackup.objects.Backup;
import pl.daneu.niceeqbackup.objects.User;
import pl.daneu.niceeqbackup.utils.MathUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BackupGUI extends CustomGUI implements ContentGUI, OnCloseGUI {

    private final User user;
    private final Backup backup;
    private final int page;
    private boolean isChanged = false;

    public BackupGUI(User user, Backup backup, int page) {
        super(TextUtil.getColoredText("&7Backup &8" + new SimpleDateFormat("HH:mm:ss, dd.MM.yyyy").format(backup.getCreateTime())),
                5);

        this.user = user;
        this.backup = backup;
        this.page = page;
    }

    @Override
    public void setContent() {
        for(int i = 0; i < 3*9; i++){
            ItemStack item = backup.getInventory().getContest().get(i);

            setEQItem(i, item);
        }

        for(int i = 3*9; i < 4*9; i++){
            ItemStack item = backup.getInventory().getHotbar().get(i - 3*9);

            setEQItem(i, item);
        }

        setEQItem(4*9, backup.getInventory().getHelmet());
        setEQItem(4*9 + 1, backup.getInventory().getChestplate());
        setEQItem(4*9 + 2, backup.getInventory().getLeggings());
        setEQItem(4*9 + 3, backup.getInventory().getBoots());

        setEQItem(4*9 + 4, backup.getInventory().getOffHand());

        ItemStack settings = new ItemBuilder(Material.COMPARATOR, 1, TextUtil.getColoredText("&7Settings"))
                .setLore(TextUtil.getColoredList("",
                        "&7Total experience &8" + backup.getExperience(),
                        "&7Level &8" + MathUtil.getLevelFromTotalExperience(backup.getExperience()),
                        "&7Health &8" + new DecimalFormat("##.##").format(backup.getHealth()),
                        "&7Food &8" + backup.getFood(),
                        "&7Location &8" + getLocationValue(backup.getLocation())
                ))
                .create();
        setItem(7, 5, settings).setClickable(false);

        ItemStack giveItem = new ItemBuilder(Material.GREEN_CONCRETE, 1, TextUtil.getColoredText("&7Give backup")).create();
        setItem(8, 5, new ItemGUI(giveItem, false, e -> {
            Player p = (Player) e.getWhoClicked();

            new ConfigurableGiveGUI(user, backup, page, false).open(p);
        }));

        ItemStack exitItem = new ItemBuilder(Material.BARRIER, 1, TextUtil.getColoredText("&cGo back to backups")).create();
        setItem(9, 5, new ItemGUI(exitItem, false, e -> {
            Player p = (Player) e.getWhoClicked();

            new BackupsGUI(user, page).open(p);
        }));
    }

    private void setEQItem(int slot, ItemStack item){
        setItem(slot, new ItemGUI(item, true, e -> isChanged = true));
    }

    private String getLocationValue(Location loc){
        DecimalFormat decimalFormat = new DecimalFormat("##.##");

        return decimalFormat.format(loc.getX()) + "/" + decimalFormat.format(loc.getY()) + "/" + decimalFormat.format(loc.getZ());
    }

    @Override
    public Consumer<InventoryCloseEvent> onClose() {
        return inventoryCloseEvent -> {
            if(!isChanged)
                return;

            List<ItemStack> items = new ArrayList<>();

            for(int i = 3*9; i < 4*9; i++){
                ItemGUI itemGUI = getItem(i);
                ItemStack item = itemGUI == null ? null : itemGUI.getItem();

                items.add(item);
            }

            for(int i = 0; i < 3*9; i++) {
                ItemGUI itemGUI = getItem(i);
                ItemStack item = itemGUI == null ? null : itemGUI.getItem();

                items.add(item);
            }

            ItemGUI boots = getItem(4*9);
            items.add(boots == null ? null : boots.getItem());

            ItemGUI leggings = getItem(4*9 + 1);
            items.add(leggings == null ? null : leggings.getItem());

            ItemGUI chestplate = getItem(4*9 + 2);
            items.add(chestplate == null ? null : chestplate.getItem());

            ItemGUI helmet = getItem(4*9 + 3);
            items.add(helmet == null ? null : helmet.getItem());

            ItemGUI offHand = getItem(4*9 + 5);
            items.add(offHand == null ? null : offHand.getItem());

            backup.getInventory().setItems(items);

            if (Bukkit.getPlayer(user.getUUID()) == null)
                NiceEQBackup.getInstance().getDatabase().saveOrPut(user);
        };
    }
}
