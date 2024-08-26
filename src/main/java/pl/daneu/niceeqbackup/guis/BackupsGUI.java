package pl.daneu.niceeqbackup.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import pl.daneu.daneutools.customgui.ContentGUI;
import pl.daneu.daneutools.customgui.CustomGUI;
import pl.daneu.daneutools.customgui.ItemGUI;
import pl.daneu.daneutools.utils.ChatUtil;
import pl.daneu.daneutools.utils.ItemBuilder;
import pl.daneu.daneutools.utils.TextUtil;
import pl.daneu.niceeqbackup.NiceEQBackup;
import pl.daneu.niceeqbackup.objects.Backup;
import pl.daneu.niceeqbackup.objects.User;
import pl.daneu.niceeqbackup.utils.LoggerUtil;
import pl.daneu.niceeqbackup.utils.MathUtil;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

public class BackupsGUI extends CustomGUI implements ContentGUI {

    private final User user;
    private final LinkedList<Backup> backups;
    private final int toSet, invSize, page;

    public BackupsGUI(User user, int page) {
        super(TextUtil.getColoredText("&7Backups &8" + (page + 1) + "/" + ((user.getBackups().size() - 1)/45 + 1)),
                MathUtil.calculateInventorySize(user.getBackups().size(), page));

        this.user = user;
        this.backups = user.getBackups();
        this.page = page;
        this.toSet = MathUtil.calculateItemsCount(user.getBackups().size(), page);
        this.invSize = MathUtil.calculateInventorySize(user.getBackups().size(), page);
    }

    public User getUser(){
        return user;
    }

    @Override
    public void setContent() {
        ItemStack glass = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE, 1, " ").create();
        for(int i = 0; i < 9; i++)
            setItem(i + 1, invSize, glass).setClickable(false);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss, dd.MM.yyyy");
        for(int i = page*5*9; i < toSet + page*5*9; i++){
            Backup backup = backups.get(i);
            Timestamp time = backup.getCreateTime();

            ItemStack backupItem = new ItemBuilder(Material.PAPER, 1, TextUtil.getColoredText("&7Backup &8" + (i + 1)))
                    .setLore(TextUtil.getColoredList(
                            "",
                            "&7Create type &8" + backup.getSaveType().name(),
                            "&7Time created &8" + format.format(time),
                            "",
                            "&7Left click &8&l| &8inspect",
                            "&7Right click &8&l| &8give",
                            "&7Middle click (cursor) &8&l| &8delete"
                    ))
                    .create();

            setItem(i - page*5*9, new ItemGUI(backupItem, false, e -> {
                Player p = (Player) e.getWhoClicked();
                ClickType clickType = e.getClick();

                switch(clickType){
                    case LEFT -> new BackupGUI(user, backup, page).open(p);
                    case RIGHT -> new ConfigurableGiveGUI(user, backup, page, true).open(p);
                    case MIDDLE -> {
                        backups.remove(backup);

                        if (Bukkit.getPlayer(user.getUUID()) == null)
                            NiceEQBackup.getInstance().getDatabase().saveOrPut(user);

                        new BackupsGUI(user, user.getBackups().size()/(1 + page*5*9) > 0 ? page : page == 1 ? 1 : page - 1).open(p);
                        ChatUtil.message(p, "&aNiceEQBackup &7&l| &aBackup successfully deleted");
                    }
                }
            }));
        }

        if(user.getBackups().size()/(1 + ((page + 1)*5*9)) > 0){
            ItemStack nextPage = new ItemBuilder(Material.GREEN_CONCRETE, 1, TextUtil.getColoredText("&7Go to next page")).create();
            setItem(2, invSize, new ItemGUI(nextPage, false, e ->{
                Player p = (Player) e.getWhoClicked();
                new BackupsGUI(user, page + 1).open(p);
            }));
        }

        if(page > 1){
            ItemStack previousPage = new ItemBuilder(Material.YELLOW_CONCRETE, 1, TextUtil.getColoredText("&7Go to previous page")).create();
            setItem(1, invSize, new ItemGUI(previousPage, false, e ->{
                Player p = (Player) e.getWhoClicked();
                new BackupsGUI(user, page - 1).open(p);
            }));
        }

        ItemStack exitItem = new ItemBuilder(Material.BARRIER, 1, TextUtil.getColoredText("&7Go back to panel")).create();
        setItem(9, invSize, new ItemGUI(exitItem, false, e -> {
            Player p = (Player) e.getWhoClicked();
            new PanelGUI(user).open(p);
        }));
    }
}
