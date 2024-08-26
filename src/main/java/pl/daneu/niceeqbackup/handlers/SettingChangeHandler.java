package pl.daneu.niceeqbackup.handlers;

import pl.daneu.niceeqbackup.objects.Backup;
import pl.daneu.niceeqbackup.objects.SettingChange;
import pl.daneu.niceeqbackup.objects.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingChangeHandler {

    private static final Map<UUID, SettingChange> settingsChanges = new HashMap<>();

    public static SettingChange getPlayerSettingChange(UUID uuid){
        return settingsChanges.get(uuid);
    }

    public void add(UUID whoChange, User user, Backup backup, SettingChange.Type type, int page, boolean fromBackupList){
        settingsChanges.put(whoChange, new SettingChange(user, backup, type, page, fromBackupList));
    }

    public void remove(UUID uuid){
        settingsChanges.remove(uuid);
    }

}
