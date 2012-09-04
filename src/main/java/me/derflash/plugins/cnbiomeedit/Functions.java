package me.derflash.plugins.cnbiomeedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import me.derflash.plugins.cnbiomeedit.CNBiomeEdit.Verbose;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class Functions {

	public static ArrayList<int []> sortAreaPoints(Collection<int []> outerPoints) {
		// sort em
		ArrayList<int[]> sorted = new ArrayList<int[]>();
		
		
		int[] first = outerPoints.iterator().next();
		sorted.add(first);
		outerPoints.remove(first);

		int maxCount = outerPoints.size();
		for (int i = 0; i < maxCount; i++) {			
			double d = -1;
			int[] next = null;
			for (int[] pointCheck : outerPoints) {
				double checkD = Math.pow(pointCheck[0] - first[0], 2) + Math.pow(pointCheck[1] - first[1], 2);
				checkD = Math.sqrt(checkD);				
				if (d == -1 || checkD < d) {
					next = pointCheck;
					d = checkD;
				}
			}
			sorted.add(next);
			outerPoints.remove(next);
			first = next;
		}
		
		if (!outerPoints.isEmpty()) {
			sorted.addAll(outerPoints);
		}
		
		return sorted;
	}

    public static double lengthSq(double x, double z) {
        return (x * x) + (z * z);
    }
    
    
	public static void smartReloadChunks(HashSet<int []> points, final World world) {
    	
		// reload chunks
        HashSet<String> refreshedChunks = new HashSet<String>();
		for (final int[] point : points) {
			
			String checkString = (point[0] >> 4) + "|" +  (point[1] >> 4);

			if (!refreshedChunks.contains(checkString)) {
				CNBiomeEdit.logIt("Refreshing: " + checkString, Verbose.ALL);
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(CNBiomeEdit.plugin, new Runnable() { public void run() {
					Chunk chunk = world.getChunkAt(point[0] >> 4, point[1] >> 4);
	            	world.refreshChunk(chunk.getX(), chunk.getZ());
				}});

				refreshedChunks.add(checkString);
			}
		}

	}

	public static Collection<int[]> thinOut(Collection<int[]> sorted) {
		if (sorted.size() <= CNBiomeEdit.plugin.cuiMaxPoints) return sorted;
		
		// try to reduce the amount of CUI points
		while (sorted.size() > CNBiomeEdit.plugin.cuiMaxPoints) {
			CNBiomeEdit.logIt("Found more than " + sorted.size() + " (max: " + CNBiomeEdit.plugin.cuiMaxPoints + ") weCUI points, diving in half.", Verbose.ERROR);
			
			ArrayList<int[]> sortedNew = new ArrayList<int[]>();
			boolean odd = true;
			for (int[] pt : sorted) {
				if (odd) {
					sortedNew.add(pt);
				}
				odd = !odd;
			}
			sorted = sortedNew;
		}
		return sorted;
	}
}
