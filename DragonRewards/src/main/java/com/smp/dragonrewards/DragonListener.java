package com.smp.dragonrewards;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DragonListener implements Listener {

    private final DragonRewards plugin;
    private static final double REWARD_RADIUS = 300.0;

    public DragonListener(DragonRewards plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;

        Location deathLoc = dragon.getLocation();
        World world = deathLoc.getWorld();

        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distance(deathLoc) <= REWARD_RADIUS) {
                nearbyPlayers.add(p);
            }
        }

        startEpicAnimation(deathLoc, world, nearbyPlayers);
    }

    private void startEpicAnimation(Location loc, World world, List<Player> players) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("\u00a75\u00a7l\u00a7k|\u00a7r \u00a7d\u00a7l\u2620 L'ENDER DRAGON EST VAINCU \u00a7l\u2620 \u00a75\u00a7l\u00a7k|");
        Bukkit.broadcastMessage("\u00a77Le ciel de l'End se dechire...");
        Bukkit.broadcastMessage("");

        new BukkitRunnable() {
            int ticks = 0;
            double angle = 0;

            @Override
            public void run() {
                ticks++;
                angle += 8;

                for (int i = 0; i < 3; i++) {
                    double rad = Math.toRadians(angle + (i * 120));
                    double height = (ticks / 10.0);
                    double x = Math.cos(rad) * 5;
                    double z = Math.sin(rad) * 5;
                    world.spawnParticle(Particle.DRAGON_BREATH, loc.clone().add(x, height % 15, z), 3, 0.1, 0.1, 0.1, 0.02);
                    world.spawnParticle(Particle.END_ROD, loc.clone().add(x, height % 15, z), 2, 0.1, 0.1, 0.1, 0.01);
                }

                if (ticks % 20 == 0) {
                    world.spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 3, 0), 5, 3, 3, 3, 0);
                    world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 50, 5, 5, 5, 0.3);
                    world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
                }

                if (ticks % 30 == 0) {
                    world.strikeLightningEffect(loc.clone().add(
                        (Math.random() - 0.5) * 10, 0, (Math.random() - 0.5) * 10
                    ));
                }

                for (Player p : players) {
                    for (int i = 0; i < 6; i++) {
                        double rad = Math.toRadians((angle * 2) + (i * 60));
                        double px = Math.cos(rad) * 1.5;
                        double pz = Math.sin(rad) * 1.5;
                        p.getWorld().spawnParticle(Particle.DRAGON_BREATH,
                            p.getLocation().clone().add(px, 1, pz), 1, 0, 0, 0, 0);
                    }
                }

                if (ticks % 15 == 0) {
                    world.playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.8f + (ticks * 0.01f));
                }

                if (ticks >= 300) {
                    cancel();
                    giveRewards(loc, world, players);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void giveRewards(Location loc, World world, List<Player> players) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("\u00a75\u00a7l================================================");
        Bukkit.broadcastMessage("\u00a7d\u00a7l          \u2605 GUERRIER TRANSCENDANT \u2605");
        Bukkit.broadcastMessage("\u00a77     Les braves suivants ont transcende");
        Bukkit.broadcastMessage("\u00a77          les limites du possible :");
        Bukkit.broadcastMessage("");

        for (Player p : players) {
            Bukkit.broadcastMessage("\u00a7d    \u2605 \u00a7l" + p.getName() + "\u00a7r\u00a7d \u2605");
        }

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("\u00a77  Leur legende sera gravee pour l'eternite.");
        Bukkit.broadcastMessage("\u00a75\u00a7l================================================");
        Bukkit.broadcastMessage("");

        for (Player p : players) {
            Location eggLoc = p.getLocation().clone().add(
                p.getLocation().getDirection().getX() * 2,
                0.5,
                p.getLocation().getDirection().getZ() * 2
            );
            world.dropItem(eggLoc, new ItemStack(Material.DRAGON_EGG));

            if (!plugin.hasBeenRewarded(p.getUniqueId())) {
                plugin.markRewarded(p.getUniqueId());

                p.addPotionEffect(new PotionEffect(
                    PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 0, false, false, true
                ));

                AttributeInstance maxHealth = p.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth != null) {
                    maxHealth.setBaseValue(maxHealth.getBaseValue() + 4.0);
                    p.setHealth(Math.min(p.getHealth() + 4.0, maxHealth.getBaseValue()));
                }

                p.sendMessage("\u00a75\u00a7l\u2605 GUERRIER TRANSCENDANT \u00a7r\u00a75\u00a7l\u2605");
                p.sendMessage("\u00a77Vous avez recu :");
                p.sendMessage("\u00a7d  \u25b8 +2 coeurs violets permanents");
                p.sendMessage("\u00a7d  \u25b8 Un oeuf de dragon");
            } else {
                p.sendMessage("\u00a75Vous avez deja recu les recompenses du Dragon !");
                p.sendMessage("\u00a77Vous recevez quand meme un oeuf de dragon !");
            }

            p.getWorld().spawnParticle(Particle.DRAGON_BREATH, p.getLocation().clone().add(0, 1, 0), 100, 1, 1, 1, 0.1);
            p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().clone().add(0, 1, 0), 50, 0.5, 1, 0.5, 0.05);
            p.getWorld().strikeLightningEffect(p.getLocation());
            p.getWorld().playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.8f);
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.0f);
        }
    }
}
