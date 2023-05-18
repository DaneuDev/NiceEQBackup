package pl.daneu.eqbackup.guis.customgui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

public class ItemGUI {

    private ItemStack item;
    private boolean isClickable;
    private double cooldown;
    private long lastClick = 0;
    private Consumer<InventoryClickEvent> consumer;

    public ItemGUI(ItemStack item){
        this.item = item;

        isClickable = false;
        cooldown = 0;
    }

    public ItemGUI(ItemStack item, boolean isClickable){
        this.item = item;
        this.isClickable = isClickable;
        cooldown = 0;
    }

    public ItemGUI(ItemStack item, boolean isClickable, double cooldown){
        this.item = item;
        this.isClickable = isClickable;
        this.cooldown = cooldown;
    }

    public ItemStack getItem(){ return item; }
    public boolean isClickable(){ return isClickable; }
    public Consumer<InventoryClickEvent> getConsumer(){ return consumer; }
    public boolean hasCooldown(){ return cooldown != 0; }

    public ItemGUI setClickable(boolean isClickable){
        this.isClickable = isClickable;

        return this;
    }

    public ItemGUI setItem(ItemStack item){
        this.item = item;

        return this;
    }

    public ItemGUI setCooldown(double cooldown){
        this.cooldown = cooldown;

        return this;
    }

    public void setLastClick(){ lastClick = System.currentTimeMillis(); }

    public boolean checkIfCanClick(){ return lastClick == 0 || lastClick + cooldown*1000L <= System.currentTimeMillis(); }

    public ItemGUI execute(Consumer<InventoryClickEvent> consumer){
        this.consumer = consumer;

        return this;
    }
}
