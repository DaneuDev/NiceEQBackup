package pl.daneu.niceeqbackup.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import pl.daneu.daneutools.utils.ChatUtil;
import pl.daneu.niceeqbackup.NiceEQBackup;
import pl.daneu.niceeqbackup.guis.ConfigurableGiveGUI;
import pl.daneu.niceeqbackup.handlers.SettingChangeHandler;
import pl.daneu.niceeqbackup.objects.SettingChange;
import pl.daneu.niceeqbackup.objects.User;

public class PlayerSendMessageListener implements Listener {

    @EventHandler
    public void onChatMessage(AsyncChatEvent e){
        Player p = e.getPlayer();
        SettingChange settingChange = SettingChangeHandler.getPlayerSettingChange(p.getUniqueId());

        if(settingChange == null)
            return;

        e.setCancelled(true);

        String message = e.signedMessage().message();
        Number value = -1;

        switch (settingChange.type()){
            case EXPERIENCE -> {
                try{
                    value = Integer.parseInt(message);
                }
                catch (NumberFormatException exc){
                    ChatUtil.message(p, "&cNiceEQBackup &7&l| &cInvalid format. Only numbers");
                    return;
                }

                if((int) value < 0)
                    value = 0;

                settingChange.backup().setExperience((int) value);
            }
            case HEALTH -> {
                try{
                    value = Double.parseDouble(message);
                }
                catch (NumberFormatException exc){
                    ChatUtil.message(p, "&cNiceEQBackup &7&l| &cInvalid format. Only numbers");
                    return;
                }

                if((double) value > 20)
                    value = 20;
                else if((double) value < 0)
                    value = 0.5;

                settingChange.backup().setHealth((double) value);
            }
            case FOOD -> {
                try{
                    value = Integer.parseInt(message);
                }
                catch (NumberFormatException exc){
                    ChatUtil.message(p, "&cNiceEQBackup &7&l| &cInvalid format. Only numbers");
                    return;
                }

                if((int) value > 20)
                    value = 20;
                else if((int) value < 0)
                    value = 0;

                settingChange.backup().setFood((int) value);
            }
        }

        NiceEQBackup.getInstance().getSettingChangeHandler().remove(p.getUniqueId());

        User user = settingChange.user();

        if (Bukkit.getPlayer(user.getUUID()) == null)
            NiceEQBackup.getInstance().getDatabase().saveOrPut(user);

        ChatUtil.message(p, "&aNiceEQBackup &7&l| &aNew value of " + settingChange.type().getLabel() + " is " + value);

        new BukkitRunnable() {
            @Override
            public void run() {
                new ConfigurableGiveGUI(user, settingChange.backup(), settingChange.page(), settingChange.fromBackupList()).open(p);
            }
        }.runTask(NiceEQBackup.getInstance());
    }

}
