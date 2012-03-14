package me.derflash.plugins.cnbiomeedit;

import java.util.Iterator;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.Polygonal2DRegionSelector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardFunctions {

	public static boolean makeWGBiome(Player player, String regionID, Biome biome) {
		RegionManager rm = CNBiomeEdit.plugin.wgPlugin().getGlobalRegionManager().get(player.getWorld());
		ProtectedRegion pRegion = rm.getRegion(regionID);
		if (pRegion == null) return false;
		
		RegionSelector rs = null;
		if (pRegion.getTypeName().equalsIgnoreCase("polygon")) {
			rs = new Polygonal2DRegionSelector(new BukkitWorld(player.getWorld()), pRegion.getPoints(), pRegion.getMinimumPoint().getBlockY(), pRegion.getMaximumPoint().getBlockY());
		} else {
			rs = new CuboidRegionSelector(new BukkitWorld(player.getWorld()));
			rs.selectPrimary(pRegion.getMinimumPoint());
			rs.selectSecondary(pRegion.getMaximumPoint());
		}
		
		Region region = null;
		try {
			region = rs.getRegion();
		} catch (IncompleteRegionException e1) {
			e1.printStackTrace();
		}
		if (region == null) return false;

		Iterator<BlockVector> it = region.iterator();
		while (it.hasNext()) {
			BlockVector nextVector = it.next();
			Functions.setBiomeAt(player.getWorld(), nextVector.getBlockX(), nextVector.getBlockZ(), biome);
		}
		
		return true;
	}
	
}
