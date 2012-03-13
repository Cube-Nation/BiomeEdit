package me.derflash.plugins.cnbiomeedit;

import java.util.Iterator;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;

public class WorldEditFunctions {

	
	public static boolean makeWEBiome(Player player, Biome biome, CNBiomeEdit plugin) {
		LocalSession lSession = plugin.wePlugin().getSession(player);
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
				player.getWorld().setBiome(nextVector.getBlockX(), nextVector.getBlockZ(), biome);
			}
		
		} catch (IncompleteRegionException e) {
			return false;
		}

		return true;

	}

}
