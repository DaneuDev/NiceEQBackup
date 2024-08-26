package pl.daneu.niceeqbackup.objects;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.daneu.niceeqbackup.utils.MathUtil;
import pl.daneu.niceeqbackup.utils.SerializationUtil;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

public class Backup {

    private final SaveType saveType;
    private final Timestamp createTime;
    private final BackupInventory inventory;
    private int experience, food;
    private double health;
    private Location location;

    public Backup(SaveType saveType, Timestamp createTime, BackupInventory inventory, double health, int food, int experience, Location location){
        this.saveType = saveType;
        this.createTime = createTime;
        this.inventory = inventory;
        this.health = health;
        this.food = food;
        this.experience = experience;
        this.location = location;
    }

    public SaveType getSaveType() {
        return saveType;
    }

    public Timestamp getCreateTime(){
        return createTime;
    }

    public BackupInventory getInventory(){
        return inventory;
    }

    public double getHealth() {
        return health;
    }

    public int getFood() {
        return food;
    }

    public int getExperience() {
        return experience;
    }

    public Location getLocation() {
        return location;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void applyBackup(Player p, Map<SettingChange.Type, Boolean> enabledSettings){
        if(enabledSettings.get(SettingChange.Type.ITEMS)){
            PlayerInventory inv = p.getInventory();

            for(int i = 0; i < 41; i++)
                inv.setItem(i, inventory.getItems().get(i));
        }

        if(enabledSettings.get(SettingChange.Type.EXPERIENCE)){
            p.setLevel(MathUtil.getLevelFromTotalExperience(experience));
            p.setExp(experience/MathUtil.getTotalExperienceForLevelFromLevel(experience));
        }

        if(enabledSettings.get(SettingChange.Type.HEALTH))
            p.setHealth(health);

        if(enabledSettings.get(SettingChange.Type.FOOD))
            p.setFoodLevel(food);

        if(enabledSettings.get(SettingChange.Type.LOCATION))
            p.teleport(location);
    }

    @Override
    public String toString() {
        return saveType.toString() + "|" +
                createTime.toString() + "|" +
                inventory.toString() + "|" +
                experience + "|" +
                food + "|" +
                health + "|" +
                SerializationUtil.getLocationString(location);
    }

    public static Backup valueOf(String str){
        String[] options = str.split("\\|");

        SaveType saveType = SaveType.valueOf(options[0]);
        Timestamp createTime = Timestamp.valueOf(options[1]);
        BackupInventory inventory = BackupInventory.valueOf(options[2]);
        int experience = Integer.parseInt(options[3]);
        int food = Integer.parseInt(options[4]);
        double health = Double.parseDouble(options[5]);
        Location location = SerializationUtil.getLocation(options[6]);

        return new Backup(saveType, createTime, inventory, health, food, experience, location);
    }

    public enum SaveType {
        CLICK, TASK, DEATH, QUIT, COMMAND;
    }
}
