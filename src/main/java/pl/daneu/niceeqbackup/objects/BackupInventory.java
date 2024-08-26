package pl.daneu.niceeqbackup.objects;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import pl.daneu.niceeqbackup.utils.SerializationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class BackupInventory {

    private final List<ItemStack> items;

    public BackupInventory(PlayerInventory inv){
        items = new ArrayList<>();

        IntStream.range(0, 41)
                .forEach(n -> items.add(inv.getItem(n)));
    }

    public BackupInventory(List<ItemStack> items){
        this.items = items;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public List<ItemStack> getContest() {
        return items.subList(9, 36);
    }

    public List<ItemStack> getHotbar(){
        return items.subList(0, 9);
    }

    public ItemStack getHelmet(){
        return items.get(39);
    }

    public ItemStack getChestplate() {
        return items.get(38);
    }

    public ItemStack getLeggings() {
        return items.get(37);
    }

    public ItemStack getBoots() {
        return items.get(36);
    }

    public ItemStack getOffHand() {
        return items.get(40);
    }

    public void setItems(List<ItemStack> items){
        this.items.clear();
        this.items.addAll(items);
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < 41; i++) {
            ItemStack item = items.get(i);
            String serializedItem = item == null ? "n" : SerializationUtil.getItemStackString(item);

            builder.append(serializedItem).append(",");
        }

        return builder.toString();
    }

    public static BackupInventory valueOf(String s){
        List<ItemStack> items = new ArrayList<>();
        String[] options = s.split(",");

        for(int i = 0; i < 41; i++){
            ItemStack item = options[i].equals("n") ?
                    null :
                    SerializationUtil.getItemStack(options[i]);

            items.add(item);
        }
        
        return new BackupInventory(items);
    }

}
