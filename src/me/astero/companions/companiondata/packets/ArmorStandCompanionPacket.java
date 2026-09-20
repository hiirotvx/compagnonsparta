package me.astero.companions.companiondata.packets;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.filemanager.CompanionDetails;
import me.astero.companions.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Simple implementation that uses Bukkit ArmorStands to render companions. This removes
 * the dependency on server-internal (NMS) classes so the plugin can run on modern versions.
 */
public class ArmorStandCompanionPacket implements CompanionPacket {

    private final CompanionsPlugin main;

    public ArmorStandCompanionPacket(CompanionsPlugin main) {
        this.main = main;
    }

    @Override
    public void loadCompanion(Player player) {
        main.getCompanions().summonCompanion(player);
    }

    @Override
    public void companionFollow(Player player) {
        PlayerData data = PlayerData.instanceOf(player);
        ArmorStand companion = data.getActiveCompanion();

        if (companion != null && companion.isValid()) {
            Location target = calculateFollowLocation(player);
            if (target != null) {
                companion.teleport(target);
            }
        }

        if (data.isRespawned() || data.isTeleport()) {
            data.setStepsCount(data.getStepsCount() + 1);
            if (data.getStepsCount() >= 5) {
                data.setRespawned(false);
                data.setTeleport(false);
                loadCompanion(player);
                data.setStepsCount(0);
            }
        }

        if (data.isSpeedBoosted()) {
            player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation(), 1, 0, 0, 0, 0.01);
        }
    }

    private Location calculateFollowLocation(Player player) {
        PlayerData data = PlayerData.instanceOf(player);
        String activeName = data.getActiveCompanionName();
        if (activeName == null || activeName.equalsIgnoreCase("NONE")) {
            return null;
        }

        CompanionDetails details = main.getFileHandler().getCompanionDetails()
                .get(activeName.toLowerCase());

        double yawRadians = Math.toRadians(player.getLocation().getYaw() - 180);
        double offsetX = Math.cos(yawRadians + (details != null ? details.getX() : 0));
        double offsetZ = Math.sin(yawRadians + (details != null ? details.getZ() : 0));
        double offsetY = details != null ? details.getY() : 0.6D;

        return player.getLocation().add(offsetX, offsetY, offsetZ);
    }

    @Override
    public void despawnCompanion(Player player, Player packetPlayer) {
        despawnCompanion(player);
    }

    @Override
    public void despawnCompanion(Player player) {
        PlayerData data = PlayerData.instanceOf(player);
        ArmorStand companion = data.getActiveCompanion();
        if (companion != null) {
            companion.remove();
            data.setActiveCompanion(null);
        }

        main.getCompanionUtil().removeParticles(player);
        main.getAnimation().removeAnimation(player);
        main.getCompanionUtil().stopCommandAbility(player);
        main.getPotionEffectAbility().remove(player);
    }

    @Override
    public void toggleCompanion(Player player) {
        despawnCompanion(player);
    }

    @Override
    public void setCustomName(Player player, String newName) {
        ArmorStand companion = PlayerData.instanceOf(player).getActiveCompanion();
        if (companion == null) {
            return;
        }

        companion.customName(MessageUtil.parse(newName));
        companion.setCustomNameVisible(true);
    }

    @Override
    public void setCustomNameVisible(Player player, boolean visible) {
        ArmorStand companion = PlayerData.instanceOf(player).getActiveCompanion();
        if (companion != null) {
            companion.setCustomNameVisible(visible);
        }
    }

    @Override
    public void setCustomWeapon(Player player, ItemStack itemStack) {
        ArmorStand companion = PlayerData.instanceOf(player).getActiveCompanion();
        if (companion == null || companion.getEquipment() == null) {
            return;
        }

        ItemStack clone = itemStack == null ? null : itemStack.clone();
        if (clone != null) {
            ItemMeta meta = clone.getItemMeta();
            if (meta != null) {
                meta.setUnbreakable(true);
                clone.setItemMeta(meta);
            }
        }

        companion.getEquipment().setItemInMainHand(clone);
    }
}
