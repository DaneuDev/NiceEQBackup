package pl.daneu.niceeqbackup.guis;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import pl.daneu.niceeqbackup.objects.SettingChange;
import pl.daneu.niceeqbackup.objects.User;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class ConfigurableGiveGUI extends CustomGUI implements ContentGUI {

    private final Map<SettingChange.Type, Boolean> enabledSettings = new HashMap<>();

    private final User user;
    private final Backup backup;
    private final int page;
    private final boolean fromBackupList;

    public ConfigurableGiveGUI(User user, Backup backup, int page, boolean fromBackupList){
        super(TextUtil.getColoredText("&7Give backup"),
                3);

        this.user = user;
        this.backup = backup;
        this.page = page;
        this.fromBackupList = fromBackupList;

        for(SettingChange.Type value : SettingChange.Type.values())
            enabledSettings.put(value, true);
    }

    @Override
    public void setContent() {
        for(int i = 0; i < 5; i++){
            ItemStack item = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE, 1, " ").create();

            setItem(i + 1, 1, item).setClickable(false);
            setItem(i + 1, 3, item).setClickable(false);
        }

        SettingChange.Type[] types = SettingChange.Type.values();
        for(int i = 0; i < 5; i++){
            SettingChange.Type type = types[i];
            int constant = i;

            setItem(i + 1, 2, new ItemGUI(getSettingItem(type, true), false, e -> {
                if(e.getClick() == ClickType.RIGHT && type != SettingChange.Type.ITEMS){
                    if(type == SettingChange.Type.LOCATION){
                        backup.setLocation(e.getWhoClicked().getLocation());

                        if (Bukkit.getPlayer(user.getUUID()) == null)
                            NiceEQBackup.getInstance().getDatabase().saveOrPut(user);

                        setItem(constant + 1, 2, getSettingItem(type, enabledSettings.get(type)));

                        ChatUtil.message(e.getWhoClicked(), "&aNiceEQBackup &7&l| &aLocation has changed");
                    }
                    else{
                        NiceEQBackup.getInstance().getSettingChangeHandler().add(e.getWhoClicked().getUniqueId(), user, backup, type, page, fromBackupList);
                        getInventory().close();

                        ChatUtil.message(e.getWhoClicked(), "&aNiceEQBackup &7&l| &aType new value for " + type.getLabel() + ". Current &l" + getCurrentValue(type));
                    }
                }
                else{
                    boolean isEnabled = !enabledSettings.get(type);
                    enabledSettings.put(type, isEnabled);

                    setItem(constant + 1, 1, getGlassItem(isEnabled));
                    setItem(constant + 1, 2, getSettingItem(type, isEnabled));
                    setItem(constant + 1, 3, getGlassItem(isEnabled));
                }
            }));
        }

        ItemStack give = new ItemBuilder(Material.GREEN_CONCRETE, 1, TextUtil.getColoredText("&aGive")).create();
        setItem(7, 2, new ItemGUI(give, false, e -> {
            Player target = Bukkit.getPlayer(user.getUUID());
            Player p = (Player) e.getWhoClicked();

            if(target == null){
                ChatUtil.message(p, "&cNiceEQBackup &7&l| &cPlayer is offline");
                return;
            }

            backup.applyBackup(target, enabledSettings);
            getInventory().close();

            ChatUtil.message(p, "&aNiceEQBackup &7&l| &aYou gave backup to player");
        }));

        ItemStack exit = new ItemBuilder(Material.RED_CONCRETE, 1, TextUtil.getColoredText("&cGo back")).create();
        setItem(8, 2, new ItemGUI(exit, false, e -> {
            Player p = (Player) e.getWhoClicked();

            if(fromBackupList)
                new BackupsGUI(user, page).open(p);
            else
                new BackupGUI(user, backup, page).open(p);
        }));
    }

    private Number getCurrentValue(SettingChange.Type type){
        switch (type){
            case EXPERIENCE -> { return backup.getExperience(); }
            case HEALTH -> { return backup.getHealth(); }
            case FOOD -> { return backup.getFood(); }
        }

        return 0;
    }

    private ItemStack getGlassItem(boolean isEnabled){
        return isEnabled ?
                new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE, 1, " ").create() :
                new ItemBuilder(Material.RED_STAINED_GLASS_PANE, 1, " ").create();
    }

    private ItemStack getSettingItem(SettingChange.Type type, boolean isEnabled){
        String status = isEnabled ? "&aENABLED" : "&cDISABLED";
        String value = switch (type){
            case ITEMS -> null;
            case EXPERIENCE -> String.valueOf(backup.getExperience());
            case HEALTH -> new DecimalFormat("##.##").format(backup.getHealth());
            case FOOD -> String.valueOf(backup.getFood());
            case LOCATION -> getLocationValue(backup.getLocation());
        };

        ItemBuilder item = new ItemBuilder(type.getMaterial(), 1, TextUtil.getColoredText("&7" + type.getLabel()));

        if(value == null)
            item.setLore(TextUtil.getColoredText("&7Status " + status));
        else
            item.setLore(TextUtil.getColoredList(
                    "&7Value &8" + value,
                    "&7Status " + status
            ));

        if(type != SettingChange.Type.ITEMS){
            item.getLore().add("");
            item.getLore().add(TextUtil.getColoredText("&8Click RMB to change"));
        }

        return item.create();
    }

    private String getLocationValue(Location loc){
        DecimalFormat decimalFormat = new DecimalFormat("##.##");

        return decimalFormat.format(loc.getX()) + "/" + decimalFormat.format(loc.getY()) + "/" + decimalFormat.format(loc.getZ());
    }
}
