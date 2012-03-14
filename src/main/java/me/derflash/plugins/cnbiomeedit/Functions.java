package me.derflash.plugins.cnbiomeedit;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.World;
import org.bukkit.block.Biome;

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

    public static final double lengthSq(double x, double z) {
        return (x * x) + (z * z);
    }

	public static void setBiomeAt(final World world, final int x,final int z, final Biome biome) {
    	world.setBiome(x, z, biome);
    }		
	
	
    public static void smartRegenChunkAt(int x, int z, World world, ArrayList<String> reggedChunks) {
    	try {
    		world.regenerateChunk(x, z);
    	} catch (Exception e) {
    		world.refreshChunk(x, z);
    	}
		/*
		Chunk toReg = world.getChunkAt(x, z);
		
		if (!reggedChunks.contains(toReg.toString())) {
			System.out.println("SmartChunkRegAt: " + x + "," + z);
			reggedChunks.add(toReg.toString());
            try {
                world.regenerateChunk(toReg.getX(), toReg.getZ());
            } catch (Throwable t) {
                t.printStackTrace();
            }
		}
		*/
	}
}
