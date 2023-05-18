package pl.daneu.eqbackup.objects;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.daneu.eqbackup.utils.ItemUtil;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Backup {

    private final Timestamp createTime;
    private final int level;
    private final float expToLevel;
    private Map<Integer, ItemStack> items, enderChestItems;

    public Backup(Timestamp createTime, int level, float expToLevel, Map<Integer, ItemStack> items, Map<Integer, ItemStack> enderChestItems){
        this.createTime = createTime;
        this.items = items;
        this.enderChestItems = enderChestItems;
        this.level = level;
        this.expToLevel = expToLevel;
    }

    public Timestamp getCreateTime(){ return createTime; }
    public Map<Integer, ItemStack> getItems(){ return items; }
    public Map<Integer, ItemStack> getEnderChestItems(){ return enderChestItems; }
    public int getLevel(){ return level; }
    public float getExpToLevel(){ return expToLevel; }

    public void updateEQItems(Inventory inventory){
        HashMap<Integer, ItemStack> updatedItems = new HashMap<>();

        for(int i = 0; i < 4*9 + 5; i++){
            ItemStack item = inventory.getItem(i);
            if(item == null || item.getType() == Material.AIR) continue;

            item = item.clone();

            if(i < 3*9)
                updatedItems.put(i + 9, item);
            else if(i < 4*9)
                updatedItems.put(i - 3*9, item);
            else if(i < 4*9 + 4)
                updatedItems.put(i, item);
            else
                updatedItems.put(40, item);
        }

        items = updatedItems;
    }

    public void updateECItems(Inventory inventory){
        HashMap<Integer, ItemStack> updatedItems = new HashMap<>();

        for(int i = 0; i < 4*9; i++){
            ItemStack item = inventory.getItem(i);
            if(item == null || item.getType() == Material.AIR) continue;

            updatedItems.put(i, item.clone());
        }

        enderChestItems = updatedItems;
    }

    @Override
    public String toString() {
        StringBuilder eqItems = new StringBuilder();
        items.forEach((slot, item) -> {
            if(item.getType() == Material.AIR) return;

            String itemS = ItemUtil.toBase64(item);

            eqItems.append(slot).append(":").append(itemS).append(",");
        });
        if(eqItems.length() == 0)
            eqItems.append("e");

        StringBuilder enderItems = new StringBuilder();
        enderChestItems.forEach((slot, item) -> {
            if(item.getType() == Material.AIR) return;

            String itemS = ItemUtil.toBase64(item);

            enderItems.append(slot).append(":").append(itemS).append(",");
        });
        if(enderItems.length() == 0)
            enderItems.append("e");

        return createTime + "|" + level + "," + expToLevel + "|" + eqItems + "::" + enderItems;
    }

    public static Backup valueOf(String str){
        String[] options = str.split("\\|");

        Timestamp createTime = Timestamp.valueOf(options[0]);

        String[] levelOptions = options[1].split(",");
        int level = Integer.parseInt(levelOptions[0]);
        float expToLevel = Float.parseFloat(levelOptions[1]);

        Map<Integer, ItemStack> items = new HashMap<>();
        Map<Integer, ItemStack> enderChestItems = new HashMap<>();

        String[] typeOptions = options[2].split("::");

        if(!typeOptions[0].equals("e"))
            for(String itemS : typeOptions[0].split(",")) {
                String[] itemOptions = itemS.split(":");

                int slot = Integer.parseInt(itemOptions[0]);
                ItemStack item = ItemUtil.fromBase64(itemOptions[1]);

                items.put(slot, item);
            }

        if(!typeOptions[1].equals("e"))
            for(String itemS : typeOptions[1].split(",")) {
                String[] itemOptions = itemS.split(":");

                int slot = Integer.parseInt(itemOptions[0]);
                ItemStack item = ItemUtil.fromBase64(itemOptions[1]);

                enderChestItems.put(slot, item);
        }

        return new Backup(createTime, level, expToLevel, items, enderChestItems);
    }

    public enum BackupType{
        ALIVE,
        DEATH;
    }
}
