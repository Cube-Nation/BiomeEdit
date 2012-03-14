package me.derflash.plugins.cnbiomeedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet50PreChunk;
import net.minecraft.server.Packet51MapChunk;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

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

    
	public static void _setBiomeAt(final World world, int x, int z, Biome biome) {
		Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
		if (!chunk.isLoaded()) chunk.load();
        
    	world.setBiome(x, z, biome);
	}

	public static void _setBiomeAt(final World world, HashSet<int []> blockHunk, Biome biome) {
		for (int [] blockLoc : blockHunk) {
	    	world.setBiome(blockLoc[0], blockLoc[1], biome);
		}
	}

	private static HashSet<int []> blockHunk;
	public static void setBiomeAt(final World world, final int x,final int z, final Biome biome) {
		if (!CNBiomeEdit.plugin.threaded) {
			Functions._setBiomeAt(world, x, z, biome);
		} else {
			if (blockHunk == null) blockHunk = new HashSet<int []>();

			if (blockHunk.size() < CNBiomeEdit.plugin.perSweep) {
				blockHunk.add(new int[] {x, z});
				return;
			}
			blockHunk.add(new int[] {x,z});
			
			Future<Object> go = Bukkit.getScheduler().callSyncMethod(CNBiomeEdit.plugin, new Callable<Object>() {
				public Object call() throws Exception {
					Functions._setBiomeAt(world, blockHunk, biome);
					return null;
				}});
			while (!go.isDone()) {}
			
			blockHunk.clear();
		}
    }		
	

	public static void smartReloadChunks(HashSet<int []> points, World world) {
    	
		// reload chunks
        HashSet<Chunk> refreshedChunks = new HashSet<Chunk>();
		for (int[] point : points) {
			Chunk chunk = world.getChunkAt(point[0] >> 4, point[1] >> 4);
			if (!refreshedChunks.contains(chunk)) {
	            try {
					System.out.println("Refreshing: " + chunk.toString());
					
			        // doesn't do anything at all
	            	world.refreshChunk(chunk.getX(), chunk.getZ());
	            	
	            } catch (Throwable t) {
	                t.printStackTrace();
	            }
				refreshedChunks.add(chunk);
			}
		}
		
		for (Player player : world.getPlayers()) {
			Location loc = player.getLocation();
			if (refreshedChunks.contains(world.getChunkAt(loc.getBlockX() >> 4, loc.getBlockZ() >> 4))) {

				System.out.println("Resetting chunk cache for " + player.getName());

				// player needs refresh
				EntityPlayer entityplayer = ((CraftPlayer)player).getHandle();

		        // boots me from the server with "moved too quickly, hacking?"
//		        byte actualDimension = (byte) (world.getEnvironment().getId());
//		        WorldServer worldserver = ((CraftWorld)world).getHandle();
//		        entityplayer.netServerHandler.sendPacket(new Packet9Respawn((byte) (actualDimension >= 0 ? -1 : 0), (byte) worldserver.difficulty, worldserver.getWorldData().getType(), worldserver.getHeight(), entityplayer.itemInWorldManager.getGameMode()));
//		        entityplayer.netServerHandler.sendPacket(new Packet9Respawn(actualDimension, (byte) worldserver.difficulty, worldserver.getWorldData().getType(), worldserver.getHeight(), entityplayer.itemInWorldManager.getGameMode()));
		        
				// resend him all changed chunks
				for (Chunk chunk : refreshedChunks) {
					if (chunk.isLoaded()) {

						// doesn't do anything at all
						entityplayer.netServerHandler.sendPacket(new Packet50PreChunk(chunk.getX(), chunk.getZ(), false));

						if (chunk.unload()) {
							System.out.println("Resending: " + chunk.toString());
							CraftChunk cChunk = (CraftChunk)chunk;
							Packet51MapChunk mapChunk = new Packet51MapChunk(cChunk.getHandle(), false, 0);
							
					        // doesn't do anything at all
							entityplayer.netServerHandler.sendPacket(mapChunk);
							
						} else {
							System.out.println("Could not unload: " + chunk.toString());
						}
					}
				}
			}
		}
	}
}
