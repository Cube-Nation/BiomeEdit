package me.derflash.plugins.cnbiomeedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class UIStuff {
	public static void markBiome(Location location, Player player, int yLoc) {
		HashSet<int[]> outerPoints = BiomeEditor.findBiomeArea(location, false);
		
		ArrayList<int[]> sorted = Functions.sortAreaPoints(outerPoints);
		
		markAreaWithPoints(sorted, player, yLoc);
    }
	

	
	public static void markAreaWithPoints(Collection<int []> sorted, Player player, int yLoc) {
		if (yLoc == -1) yLoc = player.getLocation().getBlockY();
		
		player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75s|polygon2d");
    	int counter = 0;
		for (int[] point : sorted) {
			player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75p2|" + counter + "|" + point[0] + "|"  + point[1] +"|0");
			counter++;
		}
       	player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75mm|"+yLoc+"|" + yLoc);
       	
       	
       	/*
    	int counter = 0;
    	player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75s|polygon2d");
        for (String _message : weCUIMessages) {
        	player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75p2|" + counter + "|" + _message+"|0");
        	counter++;
        }
       	player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75mm|"+pos.getBlockY()+"|" + pos.getBlockY());
        */
	}
}
