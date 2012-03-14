package me.derflash.plugins.cnbiomeedit;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class UIStuff {
	public static BiomeArea markBiome(Location location, Player player, int yLoc) {
		BiomeArea bArea = BiomeEditor.findBiomeArea(location);
		ArrayList<int[]> sorted = Functions.sortAreaPoints(bArea.getOuterPoints());
		
		markAreaWithPoints(sorted, player, yLoc);
		
		return bArea;
    }
	
	public static void markAreaWithPoints(Collection<int []> sorted, Player player, int yLoc) {
		if (yLoc == -1) yLoc = player.getLocation().getBlockY();

		player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75s|polygon2d");
    	int counter = 0;
		for (int[] point : sorted) {
			player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75p2|" + counter + "|" + point[0] + "|"  + point[1] +"|0");
			counter++;
		}
		UIStuff.flattenWECUI(player, yLoc);
	}
	
	public static void flattenWECUI(Player player, int yLoc) {
		if (yLoc == -1) yLoc = player.getLocation().getBlockY();
       	player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75mm|"+yLoc+"|" + yLoc);

	}

	public static boolean hasCUISupport(Player player) {
		WorldEditPlugin wePlugin = CNBiomeEdit.plugin.wePlugin();
		if (wePlugin == null) return false;		// we just assume yes

		return wePlugin.getSession(player).hasCUISupport();
	}
}
