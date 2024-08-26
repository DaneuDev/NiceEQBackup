package pl.daneu.niceeqbackup.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import pl.daneu.daneutools.customgui.ContentGUI;
import pl.daneu.daneutools.customgui.CustomGUI;
import pl.daneu.daneutools.customgui.ItemGUI;
import pl.daneu.daneutools.customgui.ManagerGUI;
import pl.daneu.daneutools.utils.ChatUtil;
import pl.daneu.daneutools.utils.ItemBuilder;
import pl.daneu.daneutools.utils.TextUtil;
import pl.daneu.niceeqbackup.objects.Backup;
import pl.daneu.niceeqbackup.objects.User;

public class PanelGUI extends CustomGUI implements ContentGUI {

    private final User user;

    public PanelGUI(User user) {
        super(TextUtil.getColoredText("&7Panel of player &6" + user.getPlayerName()), InventoryType.HOPPER);

        this.user = user;
    }

    @Override
    public void setContent() {
        ItemStack glass = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE, 1, " ").create();
        for(int i = 0; i < 5; i += 2)
            setItem(i, glass).setClickable(false);

        ItemStack backupsListItem = new ItemBuilder(Material.BARREL, 1, TextUtil.getColoredText("&7List of backups")).create();
        setItem(1, new ItemGUI(backupsListItem, false, e -> {
            Player p = (Player) e.getWhoClicked();

            new BackupsGUI(user, 0).open(p);
        }));

        ItemStack saveBackupItem = new ItemBuilder(Material.WRITABLE_BOOK, 1, TextUtil.getColoredText("&7Save player")).create();
        setItem(3, new ItemGUI(saveBackupItem, false, e -> {
            Player p = (Player) e.getWhoClicked(),
                    target = Bukkit.getPlayer(user.getUUID());

            if(target == null){
                ChatUtil.message(p, "&cNiceEQBackup &7&l| &cPlayer is offline");

                return;
            }

            user.addBackup(target, Backup.SaveType.CLICK);

            ChatUtil.message(p, "&aNiceEQBackup &7&l| &aYou created backup for player &l" + user.getPlayerName());

            Bukkit.getOnlinePlayers().stream()
                    .filter(lP -> lP.hasPermission("niceeqbackups.use") &&
                            lP.getOpenInventory().getTopInventory().getHolder() instanceof BackupsGUI)
                    .forEach(lP -> {
                        BackupsGUI backupsShowcaseGUI = (BackupsGUI) ManagerGUI.getManagerGUI().getGUI(lP.getUniqueId());
                        new BackupsGUI(backupsShowcaseGUI.getUser(), 0).open(lP);
                    });
        }));
    }
}
