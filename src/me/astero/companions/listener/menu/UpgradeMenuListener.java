package me.astero.companions.listener.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerCache;
import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.util.MessageUtil;

public class UpgradeMenuListener implements Listener {

    private final CompanionsPlugin main;

    public UpgradeMenuListener(CompanionsPlugin main) {
        this.main = main;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        try {
            Component title = event.getView().title();
            boolean upgradeMenu = title.equals(MessageUtil.parse(main.getFileHandler().getUpgradeAbilitiesTitle()));
            if (!upgradeMenu || event.getCurrentItem() == null) {
                return;
            }

            event.setCancelled(true);
            Component clickedName = event.getCurrentItem().getItemMeta().displayName();
            if (clickedName == null) return;

            if (clickedName.equals(MessageUtil.parse(main.getFileHandler().getGoBackUDName()))) {
                Bukkit.dispatchCommand(player, main.getFileHandler().getUpgradeGoBackCommand());
                return;
            }

            if (!PlayerData.instanceOf(player).hasActiveCompanionSelected()) {
                noCompanionMessage(player);
                return;
            }

            if (clickedName.equals(MessageUtil.parse(main.getFileHandler().getAbilityLevelName()))) {
                handleAbilityLevelClick(player, event.getClick());
                return;
            }

            if (clickedName.equals(MessageUtil.parse(main.getFileHandler().getAbilityLevelMName()))
                    && event.getClick() == ClickType.RIGHT) {
                main.getCompanionUtil().buyUpgradeAbility(player, false);
                Bukkit.dispatchCommand(player, "companions upgrade");
                return;
            }

            if (clickedName.equals(MessageUtil.parse(main.getFileHandler().getRenameCompanionName()))) {
                main.getCompanionUtil().buyUpgradeRename(player);
            } else if (clickedName.equals(MessageUtil.parse(main.getFileHandler().getHideCompanionName()))) {
                main.getCompanionUtil().buyUpgradeHideName(player);
            } else if (clickedName.equals(MessageUtil.parse(main.getFileHandler().getChangeWeaponName()))) {
                main.getCompanionUtil().buyUpgradeChangeWeapon(player);
            }
        } catch (NullPointerException ignored) {
        }
    }

    private void handleAbilityLevelClick(Player player, ClickType clickType) {
        if (clickType == ClickType.LEFT) {
            main.getCompanionUtil().buyUpgradeAbility(player, true);
        } else if (clickType == ClickType.RIGHT) {
            String active = PlayerData.instanceOf(player).getActiveCompanionName().toLowerCase();
            int level = PlayerCache.instanceOf(player.getUniqueId()).getOwnedCache().get(active).getAbilityLevel();

            if (level != 1) {
                main.getCompanionUtil().buyUpgradeAbility(player, false);
            } else {
                MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(),
                        main.getFileHandler().getAbilityDowngradedMaxedMessage());
            }
        }

        Bukkit.dispatchCommand(player, "companions upgrade");
    }

    private void noCompanionMessage(Player player) {
        player.closeInventory();
        MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(),
                main.getFileHandler().getNoActiveCompanionMessage());
    }
}
