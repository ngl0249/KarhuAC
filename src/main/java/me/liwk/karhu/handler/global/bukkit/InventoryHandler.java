package me.liwk.karhu.handler.global.bukkit;

import me.liwk.karhu.util.gui.Button;
import me.liwk.karhu.util.gui.Gui;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InventoryHandler implements Listener {

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        // Validate clicked inventory and current item
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;

        // Safely cast to Player
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        // Retrieve the Gui instance for the player
        Gui gui = Gui.getGui(player);

        // Get the inventory title in a version-compatible way
        String inventoryTitle = getInventoryTitle(e);
        if (inventoryTitle != null && inventoryTitle.contains("§r") && gui != null) {
            e.setCancelled(true);
        }

        // Handle button actions in the GUI
        if (gui != null) {
            for (Button b : gui.getButtons()) {
                if (b.item.clone().equals(e.getCurrentItem())) {
                    e.setCancelled(true);
                    // Play the appropriate sound based on server version
                    Sound sound = getCompatibleSound("ENTITY_CHICKEN_EGG", "CHICKEN_EGG_POP");
                    player.playSound(player.getLocation(), sound, 0.5f, 1f);
                    b.onClick(player, e.getClick());
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClose(InventoryCloseEvent e) {
        // Safely cast the player
        if (e.getPlayer() instanceof Player) {
            Player player = (Player) e.getPlayer();

            // Retrieve the inventory title, using reflection for broader compatibility
            String inventoryTitle = getInventoryTitle(e);
            if (inventoryTitle != null && inventoryTitle.contains("§r")) {
                Gui gui = Gui.getGui(player); // Retrieve the Gui object
                if (gui != null) {
                    gui.close(player); // Close the GUI
                }
            }
        }
    }

    public static String getInventoryTitle(InventoryEvent event) {
        try {
            Object view = event.getView();
            Method getTitle = view.getClass().getMethod("getTitle");
            getTitle.setAccessible(true);
            return (String) getTitle.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Sound getCompatibleSound(String modernKey, String legacyKey) {
        try {
            // Attempt to use modern key via reflection
            Method valueOfMethod = Sound.class.getMethod("valueOf", String.class);
            return (Sound) valueOfMethod.invoke(null, modernKey);
        } catch (Exception modernException) {
            try {
                // Fallback to legacy key
                Method valueOfMethod = Sound.class.getMethod("valueOf", String.class);
                return (Sound) valueOfMethod.invoke(null, legacyKey);
            } catch (Exception legacyException) {
                // Absolute fallback
                try {
                    // Find a generic sound
                    Field[] soundFields = Sound.class.getFields();
                    return (Sound) soundFields[0].get(null);
                } catch (Exception finalException) {
                    throw new RuntimeException("Unable to find compatible sound", finalException);
                }
            }
        }
    }

}
