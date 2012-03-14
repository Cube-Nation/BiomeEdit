package me.derflash.plugins.cnbiomeedit;

import me.derflash.plugins.cnbiomeedit.BiomeBrushSettings.BiomeMode;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

	private CNBiomeEdit plugin;
	
	public PlayerListener(CNBiomeEdit plugin) {
		this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	
	@EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
    	if (player.getItemInHand().getType().equals(Material.DEAD_BUSH) && plugin.isBrushActive(player)) {
    		event.setCancelled(true);

    		Block targetBlock = null;
    		try {
        		targetBlock = player.getTargetBlock(null, 300);
    		} catch (Exception e) {
    		}
    		
    		if (targetBlock != null) {
        		final Location targetLocation = targetBlock.getLocation();
        		final BiomeBrushSettings bbs = plugin.currentBrushers.get(player);
        		
        		if (bbs.getMode().equals(BiomeMode.ROUND)) {
    				if (CNBiomeEdit.plugin.threaded) {
        				Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, new Runnable() {
        					public void run() {
        	        			BiomeEditor.makeAndMarkCylinderBiome(player, targetLocation, bbs.getBiome(), bbs.getSize(), targetLocation.getBlockY());
        					}});
    				} else {
            			BiomeEditor.makeAndMarkCylinderBiome(player, targetLocation, bbs.getBiome(), bbs.getSize(), targetLocation.getBlockY());
    				}
        			
        		} else if (bbs.getMode().equals(BiomeMode.SQUARE)) {
    				if (CNBiomeEdit.plugin.threaded) {
        				Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, new Runnable() {
        					public void run() {
        	        			BiomeEditor.makeAndMarkSquareBiome(player, targetLocation, bbs.getBiome(), bbs.getSize(), targetLocation.getBlockY());
        					}});
    				} else {
            			BiomeEditor.makeAndMarkSquareBiome(player, targetLocation, bbs.getBiome(), bbs.getSize(), targetLocation.getBlockY());
    				}
        			
        		} else if (bbs.getMode().equals(BiomeMode.REPLACE)) {
    				if (CNBiomeEdit.plugin.threaded) {
        				Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, new Runnable() {
        					public void run() {
        	        			BiomeEditor.replaceAndMarkCompleteBiome(player, targetLocation, bbs.getBiome(), targetLocation.getBlockY());
        					}});
    				} else {
            			BiomeEditor.replaceAndMarkCompleteBiome(player, targetLocation, bbs.getBiome(), targetLocation.getBlockY());
    				}

        		}    			
    		}
    	}
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		plugin.currentBrushers.remove(player);
    }
	
}

