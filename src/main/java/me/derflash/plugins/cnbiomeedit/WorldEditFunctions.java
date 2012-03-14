package me.derflash.plugins.cnbiomeedit;

import java.util.Iterator;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
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
					region.contract(new com.sk89q.worldedit.Vector(0,height / 2, 0));
					if (height > 2) region.contract(new com.sk89q.worldedit.Vector(0,-1 * (height / 2 - 1), 0));
				} catch (RegionOperationException e) {
					e.printStackTrace();
				}
			}

			Iterator<BlockVector> it = region.iterator();
			while (it.hasNext()) {
				BlockVector nextVector = it.next();
				Functions.setBiomeAt(player.getWorld(), nextVector.getBlockX(), nextVector.getBlockZ(), biome);
			}
		
		} catch (IncompleteRegionException e) {
			return false;
		}

		return true;

	}

}
