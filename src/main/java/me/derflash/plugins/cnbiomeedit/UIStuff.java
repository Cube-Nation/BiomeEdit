package me.derflash.plugins.cnbiomeedit;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class UIStuff {
	public static BiomeArea markBiome(Location location, Player player, int yLoc) {
		BiomeArea bArea = BiomeEditor.findBiomeArea(location);
		
		ArrayList<int[]> sorted = Functions.sortAreaPoints(bArea.getOuterPoints());
		
		markAreaWithPoints(sorted, player, yLoc);
		
		return bArea;
    }

	public static void markAreaWithPoints(Collection<int []> sorted, Player player, int yLoc) {
		sorted = Functions.thinOut(sorted);
		
		if (yLoc == -1) yLoc = player.getLocation().getBlockY();
		WorldEditFunctions.markPolyRegionWithPoints(player, sorted, yLoc);
	}
	
	public static boolean hasCUISupport(Player player) {
		try {
			return WorldEditFunctions.hasCUISupport(player);
		}
    	catch (Exception e) {}
    	catch (Error er) {}
		
	    return false;
	}
	

}
