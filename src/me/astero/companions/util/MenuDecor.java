package me.astero.companions.util;

import me.astero.companions.CompanionsPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Decoration des menus : cadre de vitres, coins et bouton "Fermer".
 *
 * Ces objets sont reconnus par un marqueur dans leurs donnees persistantes, jamais par
 * leur nom : ils ne peuvent donc pas etre confondus avec un compagnon ou un bouton.
 *
 * Reglages dans config.yml, section GUI.decor. Toutes les cles sont facultatives :
 * sans elles, les valeurs par defaut ci-dessous s'appliquent, ce qui evite qu'une
 * ancienne config ne plante au chargement.
 */
public final class MenuDecor {

    private static final String PANE = "pane";
    private static final String CLOSE = "close";

    private final CompanionsPlugin main;
    private final NamespacedKey decorKey;

    public MenuDecor(CompanionsPlugin main) {
        this.main = main;
        this.decorKey = new NamespacedKey(main, "menu_decor");
    }

    /** Nombre de cases du centre d'un menu encadre : 7 colonnes par rangee interieure. */
    public static int innerSlots(int size) {
        int rows = size / 9;
        return rows >= 3 ? (rows - 2) * 7 : size;
    }

    /** Case "Fermer" : milieu de la derniere rangee, sauf reglage contraire. */
    public int closeSlot(int size) {
        return main.getConfig().getInt("GUI.decor.close.slot", size - 5);
    }

    /** Pose des vitres sur toutes les cases libres du pourtour ; le centre reste vide. */
    public void fillBorder(Inventory inventory) {
        int size = inventory.getSize();
        int rows = size / 9;
        for (int slot = 0; slot < size; slot++) {
            int row = slot / 9;
            int col = slot % 9;
            boolean border = row == 0 || row == rows - 1 || col == 0 || col == 8;
            if (border && inventory.getItem(slot) == null) {
                inventory.setItem(slot, pane(isCorner(slot, size)));
            }
        }
    }

    /** Pose des vitres sur toutes les cases libres du menu. */
    public void fillEmpty(Inventory inventory) {
        int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, pane(isCorner(slot, size)));
            }
        }
    }

    public ItemStack closeButton() {
        FileConfiguration config = main.getConfig();
        ItemStack item = new ItemStack(material(config.getString("GUI.decor.close.type"), Material.BARRIER));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(config.getString("GUI.decor.close.name", "<!italic><#ff8a8a><b>Fermer</b>")));
        List<String> lore = config.getStringList("GUI.decor.close.description");
        if (!lore.isEmpty()) {
            meta.lore(lore.stream().map(MessageUtil::parse).toList());
        }
        meta.getPersistentDataContainer().set(decorKey, PersistentDataType.STRING, CLOSE);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isDecor(ItemStack item) {
        return PANE.equals(tag(item));
    }

    public boolean isClose(ItemStack item) {
        return CLOSE.equals(tag(item));
    }

    private ItemStack pane(boolean corner) {
        FileConfiguration config = main.getConfig();
        Material material = corner
                ? material(config.getString("GUI.decor.corner"), Material.LIGHT_BLUE_STAINED_GLASS_PANE)
                : material(config.getString("GUI.decor.border"), Material.GRAY_STAINED_GLASS_PANE);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        // Pas d'infobulle au survol : une vitre ne doit rien afficher.
        meta.setHideTooltip(true);
        meta.getPersistentDataContainer().set(decorKey, PersistentDataType.STRING, PANE);
        item.setItemMeta(meta);
        return item;
    }

    private static boolean isCorner(int slot, int size) {
        return slot == 0 || slot == 8 || slot == size - 9 || slot == size - 1;
    }

    private String tag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(decorKey, PersistentDataType.STRING);
    }

    private Material material(String name, Material fallback) {
        if (name == null) {
            return fallback;
        }
        Material material = Material.matchMaterial(name);
        if (material == null) {
            main.getLogger().warning("Materiau inconnu dans GUI.decor : " + name + " - repli sur " + fallback);
            return fallback;
        }
        return material;
    }
}
