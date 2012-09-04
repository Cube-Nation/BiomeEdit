package me.derflash.plugins.cnbiomeedit;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.cui.SelectionMinMaxEvent;
import com.sk89q.worldedit.cui.SelectionPoint2DEvent;
import com.sk89q.worldedit.cui.SelectionShapeEvent;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;


public class WorldEditFunctions {

	private static WorldEditPlugin _wePlugin;
	
	public static WorldEditPlugin wePlugin() {
		if (_wePlugin == null) {
			_wePlugin = (WorldEditPlugin) CNBiomeEdit.plugin.getServer().getPluginManager().getPlugin("WorldEdit");
			if (_wePlugin == null) {
				CNBiomeEdit.plugin.getLogger().info("[BiomeEdit] WEPlugin not found");
				return null;
			}
		}
		return _wePlugin;
	}
	
	public static boolean hasCUISupport(Player player) {
		WorldEditPlugin wePlugin = WorldEditFunctions.wePlugin();
		if (wePlugin == null) return false;		// we just assume yes

		return wePlugin.getSession(player).hasCUISupport();
	}
	
	public static boolean makeWEBiome(Player player, Biome biome) {
		LocalSession lSession = WorldEditFunctions.wePlugin().getSession(player);
		try {
			Region region = lSession.getSelection(new BukkitWorld(player.getWorld()));
			if (region == null) return false;
			
			// flattn region for more speed
			int height = region.getHeight();
			if (height > 1) {
				try {
					region.contract(new com.sk89q.worldedit.Vector(0,height / 2, 0), null);
					if (height > 2) region.contract(new com.sk89q.worldedit.Vector(0,-1 * (height / 2 - 1), 0), null);
				} catch (RegionOperationException e) {
					e.printStackTrace();
				}
			}
			

			Iterator<BlockVector> it = region.iterator();
			HashSet<int []> cache = new HashSet<int []>();
			while (it.hasNext()) {
				BlockVector nextVector = it.next();
				cache.add(new int[] {nextVector.getBlockX(), nextVector.getBlockZ()});
			}
			
			BiomeEditor.replaceBiomePoints(cache, player.getWorld(), biome, player);

		
		} catch (IncompleteRegionException e) {
			return false;
		}

		return true;

	}


	public static void markPolyRegionWithPoints(final Player player, final Collection<int []> sortedPoints, final int yLoc) {
		WorldEditPlugin wePlugin = WorldEditFunctions.wePlugin();
		LocalSession session = wePlugin.getSession(player);
		com.sk89q.worldedit.bukkit.BukkitPlayer bPlayer = new com.sk89q.worldedit.bukkit.BukkitPlayer(wePlugin, wePlugin.getServerInterface(), player);
		
        session.dispatchCUIEvent(bPlayer, new SelectionShapeEvent("polygon2d"));
        
		int counter = 0;
		for (int[] point : sortedPoints) {
			session.dispatchCUIEvent(bPlayer, new SelectionPoint2DEvent(counter, new Vector(point[0], yLoc, point[1]), 0));
	        counter++;
		}
		
        session.dispatchCUIEvent(bPlayer, new SelectionMinMaxEvent(yLoc, yLoc));
		
	}
}
