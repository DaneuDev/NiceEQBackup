package pl.daneu.eqbackup.guis.customgui;

import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.util.Consumer;

public interface OnCloseGUI {

    Consumer<InventoryCloseEvent> addOnClose();

}
