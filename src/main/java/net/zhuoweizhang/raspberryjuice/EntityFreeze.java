package net.zhuoweizhang.raspberryjuice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Goblom
 * See https://bukkit.org/threads/resource-entity-freeze.189284/
 */
public class EntityFreeze implements Listener {

    public static Map<Integer, Location> frozenEntities = new HashMap();
    private Set<BukkitTask> tasks = new HashSet<>();

    private BukkitScheduler scheduler = Bukkit.getScheduler();
    private PluginManager pluginManager = Bukkit.getPluginManager();
    private RaspberryJuicePlugin plugin;

    public EntityFreeze(RaspberryJuicePlugin plugin) {
        this.plugin = plugin;
        tasks.add(scheduler.runTaskTimer(this.plugin, new Freeze(), 0, 20L)); //plugin represents your JavaPlugin
        pluginManager.registerEvents(this, this.plugin);
    }

    public void freezeEntity(LivingEntity entity) {
        frozenEntities.put(entity.getEntityId(), entity.getLocation());
    }

    public boolean isFrozen(LivingEntity entity) {
        return frozenEntities.containsKey(entity.getEntityId());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        for (LivingEntity ent : event.getWorld().getLivingEntities()) {
            UUID id = ent.getUniqueId();
            if (frozenEntities.containsKey(id)) frozenEntities.remove(id);
        }
    }

    private class Freeze implements Runnable {
        @Override
        public void run() {
            for (Map.Entry<Integer, Location> entry : frozenEntities.entrySet()) {
                LivingEntity entity = (LivingEntity) plugin.getEntity(entry.getKey());
                entity.teleport(entry.getValue());
            }
        }
    }
}