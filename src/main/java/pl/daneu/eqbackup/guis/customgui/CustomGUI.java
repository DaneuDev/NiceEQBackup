package pl.daneu.eqbackup.guis.customgui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Consumer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class CustomGUI implements InventoryHolder {

    private final String name;
    private final int size;
    private final InventoryType inventoryType;

    private final HashMap<Integer, ItemGUI> items = new HashMap<>();
    private final HashMap<Integer, BukkitTask> tasks = new HashMap<>();

    private Inventory inventory;
    private Consumer<InventoryCloseEvent> onCloseConsumer;

    public CustomGUI(String name, int size) {
        this.name = name;
        this.size = size;
        inventoryType = InventoryType.CHEST;
    }

    public CustomGUI(String name, InventoryType inventoryType) {
        this.name = name;
        this.inventoryType = inventoryType;
        size = 0;
    }

    public CustomGUI getCustomGUI(){ return this; }
    public String getName(){ return name; }
    public HashMap<Integer, ItemGUI> getItems(){ return items; }
    public HashMap<Integer, BukkitTask> getTasks(){ return tasks; }
    public Consumer<InventoryCloseEvent> getOnCloseConsumer(){ return onCloseConsumer; }

    public ItemGUI getItem(int column, int row) {
        int slot = column - 1 + (row - 1) * 9;

        return getItems().get(slot);
    }
    public ItemGUI getItem(int slot){ return getItems().get(slot); }

    public CustomGUI setItem(int column, int row, ItemGUI itemGUI) {
        int slot = column - 1 + (row - 1) * 9;

        items.put(slot, itemGUI);

        return this;
    }

    public CustomGUI setItem(int column, int row, ItemStack item, boolean isClickable) {
        int slot = column - 1 + (row - 1) * 9;

        ItemGUI itemGUI = new ItemGUI(item, isClickable);
        items.put(slot, itemGUI);

        return this;
    }

    public CustomGUI setItem(int slot, ItemGUI itemGUI) {
        items.put(slot, itemGUI);

        return this;
    }

    public CustomGUI setItem(int slot, ItemStack item, boolean isClickable) {
        ItemGUI itemGUI = new ItemGUI(item, isClickable);
        items.put(slot, itemGUI);

        return this;
    }

    public CustomGUI removeItem(int column, int row) {
        int slot = column - 1 + (row - 1) * 9;

        items.remove(slot);
        putOrRefreshSlot(column, row);

        return this;
    }

    public CustomGUI removeItem(int slot) {
        items.remove(slot);
        putOrRefreshSlot(slot);

        return this;
    }

    public void putOrRefreshSlot(int column, int row) {
        int slot = column - 1 + (row - 1) * 9;

        ItemGUI itemGUI = items.get(slot);
        ItemStack item = itemGUI == null ? null : itemGUI.getItem();
        ItemStack currentItem = inventory.getItem(slot);

        if (currentItem == null && itemGUI != null && item != null)
            inventory.setItem(slot, item);
        else if (currentItem != null) {
            if (itemGUI != null && item != null && !currentItem.equals(item))
                inventory.setItem(slot, item);
            else if (itemGUI == null)
                inventory.clear(slot);
        }

        if (itemGUI != null && itemGUI.isClickable() && itemGUI.getConsumer() == null)
            items.remove(slot);
    }

    public void putOrRefreshSlot(int slot) {
        putOrRefreshSlot(slot % 9 + 1, slot / 9 + 1);
    }

    public void putItemsOrRefreshAllSlots() {
        if (Arrays.stream(inventory.getContents()).filter(Objects::nonNull).collect(Collectors.toSet()).size() != 0) {
            int size = inventoryType == InventoryType.CHEST ? this.size * 9 : inventoryType.getDefaultSize();

            for (int slot = 0; slot < size; slot++) {
                ItemGUI itemGUI = items.get(slot);
                ItemStack item = itemGUI == null ? null : itemGUI.getItem();
                ItemStack currentItem = inventory.getItem(slot);

                if (currentItem == null && itemGUI != null)
                    inventory.setItem(slot, item);
                else if (currentItem != null) {
                    if (itemGUI != null && !currentItem.equals(item))
                        inventory.setItem(slot, item);
                    else if (itemGUI == null)
                        inventory.clear(slot);
                }

                if (itemGUI != null && itemGUI.isClickable() && itemGUI.getConsumer() == null)
                    items.remove(slot);
            }

            return;
        }

        items.forEach((slot, item) -> inventory.setItem(slot, item.getItem()));
    }

    public CustomGUI addAnimationTask(BukkitTask bukkitTask) {
        tasks.put(bukkitTask.getTaskId(), bukkitTask);

        return this;
    }

    public CustomGUI addOnClose(Consumer<InventoryCloseEvent> onCloseConsumer){
        this.onCloseConsumer = onCloseConsumer;

        return this;
    }

    @Override
    public Inventory getInventory() {
        if (inventory != null) return inventory;

        if (size == 0)
            inventory = Bukkit.createInventory(this, inventoryType, name);
        else
            inventory = Bukkit.createInventory(this, size * 9, name);

        return inventory;
    }

    public void open(Player p) {
        UUID uuid = p.getUniqueId();
        CustomGUI customGUI = ManagerGUI.getManagerGUI().getGUI(uuid);

        if (customGUI != null) {
            HashMap<Integer, BukkitTask> tasks = customGUI.getTasks();
            if (!tasks.isEmpty())
                tasks.forEach((id, task) -> {
                    if (task.isCancelled() || (!Bukkit.getScheduler().isCurrentlyRunning(id) && !Bukkit.getScheduler().isQueued(id)))
                        return;

                    task.cancel();
                });

            new BukkitRunnable() {
                private final CustomGUI customGUI = getCustomGUI();

                @Override
                public void run() {
                    createNew(p, customGUI);
                }
            }.runTaskLater(ManagerGUI.getManagerGUI().getJavaPlugin(), 2);
        }
        else
            createNew(p, this);
    }

    private void createNew(Player p, CustomGUI customGUI){
        if(customGUI instanceof OnCloseGUI)
            onCloseConsumer = ((OnCloseGUI) customGUI).addOnClose();

        if (customGUI instanceof ContentGUI)
            ((ContentGUI) customGUI).setContent();

        getInventory();
        putItemsOrRefreshAllSlots();

        p.openInventory(getInventory());

        ManagerGUI.getManagerGUI().addCustomGUI(p.getUniqueId(), customGUI);
    }
}
