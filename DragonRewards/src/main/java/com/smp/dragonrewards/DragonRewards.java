package com.smp.dragonrewards;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DragonRewards extends JavaPlugin {

    private static DragonRewards instance;
    private final Set<UUID> rewardedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadRewardedPlayers();
        getServer().getPluginManager().registerEvents(new DragonListener(this), this);
        getLogger().info("DragonRewards active ! Bonne chance contre le Dragon...");
    }

    @Override
    public void onDisable() {
        saveRewardedPlayers();
        getLogger().info("DragonRewards desactive.");
    }

    public static DragonRewards getInstance() { return instance; }

    public boolean hasBeenRewarded(UUID uuid) {
        return rewardedPlayers.contains(uuid);
    }

    public void markRewarded(UUID uuid) {
        rewardedPlayers.add(uuid);
        saveRewardedPlayers();
    }

    private void saveRewardedPlayers() {
        java.util.List<String> list = new java.util.ArrayList<>();
        for (UUID uuid : rewardedPlayers) list.add(uuid.toString());
        getConfig().set("rewarded_players", list);
        saveConfig();
    }

    private void loadRewardedPlayers() {
        for (String s : getConfig().getStringList("rewarded_players")) {
            try { rewardedPlayers.add(UUID.fromString(s)); } catch (Exception ignored) {}
        }
        getLogger().info("Charge " + rewardedPlayers.size() + " joueur(s) deja recompense(s).");
    }
}
