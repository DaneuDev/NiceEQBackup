package pl.daneu.eqbackup.guis.customgui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManagerGUI implements Listener {

    private static ManagerGUI managerGUI;

    private final JavaPlugin javaPlugin;
    private final Map<UUID, CustomGUI> customGUIs;

    public ManagerGUI(JavaPlugin javaPlugin){
        this.javaPlugin = javaPlugin;
        customGUIs = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, javaPlugin);

        managerGUI = this;
    }

    public static ManagerGUI getManagerGUI(){ return managerGUI; }

    public JavaPlugin getJavaPlugin(){ return javaPlugin; }
    public CustomGUI getGUI(UUID uuid){ return customGUIs.get(uuid); }

    public void addCustomGUI(UUID uuid, CustomGUI customGUI){ customGUIs.put(uuid, customGUI); }

    @EventHandler
    private void dragEvent(InventoryDragEvent e){
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if(!(inventoryHolder instanceof CustomGUI)) return;

        Player p = (Player) e.getWhoClicked();
        CustomGUI gui = getGUI(p.getUniqueId());

        if(gui == null) return;

        e.setCancelled(true);
    }

    @EventHandler
    private void clickEvent(InventoryClickEvent e){
        Inventory inv = e.getClickedInventory();
        if(inv == null) return;

        InventoryHolder inventoryHolder = e.getView().getTopInventory().getHolder();
        ClickType clickType = e.getClick();

        if(!(inventoryHolder instanceof CustomGUI)) return;
        else if(inv.equals(e.getView().getBottomInventory())){
            if(e.isShiftClick() || clickType == ClickType.DOUBLE_CLICK)
                e.setCancelled(true);
            else if(clickType == ClickType.SWAP_OFFHAND)
                preventOffHandBug(e);

            return;
        }

        Player p = (Player) e.getWhoClicked();
        CustomGUI gui = getGUI(p.getUniqueId());

        if(gui == null) return;

        int slot = e.getSlot();
        ItemGUI item = gui.getItems().get(slot);

        if(item == null) {
            ItemStack cursorItem = e.getCursor();

            if (cursorItem != null)
                e.setCancelled(true);

            return;
        }

        if(item.hasCooldown()){
            if(!item.checkIfCanClick()){
                e.setCancelled(true);
                return;
            }
            else
                item.setLastClick();
        }

        if(clickType == ClickType.SWAP_OFFHAND && !item.isClickable())
            preventOffHandBug(e);
        else
            e.setCancelled(!item.isClickable());

        if(item.getConsumer() != null)
            item.getConsumer().accept(e);
    }

    private void preventOffHandBug(InventoryClickEvent e){
        e.setCancelled(true);

        PlayerInventory playerInventory = e.getWhoClicked().getInventory();
        ItemStack offHandItem = playerInventory.getItemInOffHand();

        playerInventory.setItemInOffHand(null);

        if(offHandItem.getType() != Material.AIR)
            new BukkitRunnable() {
                @Override
                public void run() {
                    playerInventory.setItemInOffHand(offHandItem);
                }
            }.runTask(javaPlugin);
    }

    @EventHandler
    private void closeEvent(InventoryCloseEvent e){
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if(!(inventoryHolder instanceof CustomGUI))return;

        Player p = (Player) e.getPlayer();
        UUID uuid = p.getUniqueId();
        CustomGUI customGUI = getGUI(uuid);

        if(customGUI == null) return;

        if(customGUI.getOnCloseConsumer() != null)
            customGUI.getOnCloseConsumer().accept(e);

        customGUIs.remove(uuid);

        HashMap<Integer, BukkitTask> tasks = customGUI.getTasks();
        if(!tasks.isEmpty()) {
            tasks.forEach((id, task) -> {
                if (task.isCancelled() ||
                        (!Bukkit.getScheduler().isCurrentlyRunning(id) && !Bukkit.getScheduler().isQueued(id))) return;

                task.cancel();
            });
        }
    }
}
