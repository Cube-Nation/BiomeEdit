package me.derflash.plugins.cnbiomeedit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;


import me.derflash.plugins.cnbiomeedit.CNBiomeEdit.Verbose;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BiomeEditor {
	
	private static Thread replacer;
	
    public static BiomeArea findBiomeArea(Location fromPos) {
		CNBiomeEdit.logIt("findBiomeArea", Verbose.ALL);
		
		BiomeCheck bc = new BiomeCheck(); 
		
        World world = fromPos.getWorld();
		Biome biome = world.getBiome(fromPos.getBlockX(), fromPos.getBlockZ());

		bc.checkPoints.add(new int[] {fromPos.getBlockX(), fromPos.getBlockZ()});
		
		while (!bc.checkPoints.isEmpty()) {
			bc.currentCheck = bc.checkPoints.pop();
			int x = bc.currentCheck[0];
			int z = bc.currentCheck[1];
			
			int[][] locsAroundMe = new int[][] {new int[] {x+1,z}, new int[] {x-1,z}, new int[] {x,z+1}, new int[] {x,z-1}};
			for (int i = 0; i<locsAroundMe.length; i++) {				
				bc.locToCheck = locsAroundMe[i];
				String checkString = bc.locToCheck[0] + "|" + bc.locToCheck[1];
				if (!bc.checkedPoints.contains(checkString)) {
					if (!BiomeEditor.parseChunkOnMainThread(world, biome, bc)) {
						CNBiomeEdit.logIt("findBiomeArea failed checking biome at: " + bc.locToCheck, Verbose.ERROR);
						return null;
					}
					
				}
				bc.checkedPoints.add(checkString);
			}
		}
		CNBiomeEdit.logIt("findBiomeArea done", Verbose.ALL);

		return new BiomeArea(bc.foundPoints, bc.outerPoints);
		
    }
    
    
    private static boolean parseChunkOnMainThread(final World world, final Biome biome, final BiomeCheck bc) {

    	/*
    	int response = 0;
		final Chunk chunk = world.getChunkAt(bc.locToCheck[0] >> 4, bc.locToCheck[1] >> 4);

		CNBiomeEdit.logIt("check chunk loaded: " + chunk.toString(), Verbose.ALL);
		if (!chunk.isLoaded()) {
			CNBiomeEdit.logIt("Loading unloaded chunk at " + chunk.toString(), Verbose.ERROR);
			
			try {
				Future<Integer> go = Bukkit.getScheduler().callSyncMethod(CNBiomeEdit.plugin, new Callable<Integer>() {
					public Integer call() {
						return chunk.load() ? 1 : -1;
					}});
				while (!go.isDone()) {}
				response = go.get();
				
			} catch (InterruptedException e) {} catch (ExecutionException e) {}				
			
		}
		
		if (response == -1) {
			CNBiomeEdit.logIt("Could not load chunk at " + chunk.toString(), Verbose.ERROR);
			return false;
		}
		
		while (!chunk.isLoaded()) {
			// do nothing but wait
		}
		*/
		
		if (world.getBiome(bc.locToCheck[0], bc.locToCheck[1]).equals(biome)) {
			bc.foundPoints.add(bc.locToCheck);
			bc.checkPoints.push(bc.locToCheck);
			return true;

		} else {
			if (!bc.outerPoints.contains(bc.currentCheck)) bc.outerPoints.add(bc.currentCheck);
			return true;
		}
		
    }
    
	// ########################################
    
    
    public static BiomeArea makeSquareArea(Location location, Biome _biome, int _biomeSize) {
    	Vector pos1 = new Vector(location.getBlockX() - _biomeSize, location.getBlockY(), location.getBlockZ() - _biomeSize);
    	
    	HashSet<int[]> weCUIMessages = new HashSet<int []>();
        HashSet<int[]> foundPoints = new HashSet<int[]>();

        int xMax = _biomeSize * 2;
        int zMax = _biomeSize * 2;
        
    	for (int x = 0; x < xMax; x++) {
        	for (int z = 0; z < zMax; z++) {
        		int _x = pos1.getBlockX()+x;
        		int _z = pos1.getBlockZ()+z;
        		
        		int[] p = new int[] {_x,_z};
        		        		
        		foundPoints.add(p);
        		
        		if ( (x == 0 || x == xMax - 1) || (z == 0 || z == zMax - 1) ) {
        			weCUIMessages.add(new int [] {_x,_z});
        		}
        	}
    	}
    	
		return new BiomeArea(foundPoints, weCUIMessages);
	}
    
    public static BiomeArea makeCylinderArea(Location fromPos, Biome biome, double radius) {
    	Vector pos = fromPos.toVector();
    	
    	double radiusX = radius;
    	double radiusZ = radius;
    	
        radiusX += 0.5;
        radiusZ += 0.5;
       
        if (pos.getBlockY() < 0) {
            pos = pos.setY(0);
        }

        double invRadiusX = 1 / radiusX;
        double invRadiusZ = 1 / radiusZ;

        int ceilRadiusX = (int) Math.ceil(radiusX);
        int ceilRadiusZ = (int) Math.ceil(radiusZ);
        
        HashSet<int[]> weCUIMessages = new HashSet<int []>();
        HashSet<int[]> foundPoints = new HashSet<int[]>();

        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextZn = 0;
            forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                double zn = nextZn;
                nextZn = (z + 1) * invRadiusZ;

                double distanceSq = Functions.lengthSq(xn, zn);
                if (distanceSq > 1) {
                    if (z == 0) {
                        break forX;
                    }
                    break forZ;
                }
                
                int x1 = pos.getBlockX() + x;
                int x2 = pos.getBlockX() - x;
                int z1 = pos.getBlockZ() + z;
                int z2 = pos.getBlockZ() - z;
                
                int[] p1 = new int[] {x1,z1};
                int[] p2 = new int[] {x2,z1};
                int[] p3 = new int[] {x1,z2};
                int[] p4 = new int[] {x2,z2};
               
        		foundPoints.add(p1);
        		foundPoints.add(p2);
        		foundPoints.add(p3);
        		foundPoints.add(p4);

                if (! (Functions.lengthSq(nextXn, zn) <= 1 && Functions.lengthSq(xn, nextZn) <= 1)) {                	
                	weCUIMessages.add(p1);
                	weCUIMessages.add(p2);
                	weCUIMessages.add(p3);
                	weCUIMessages.add(p4);
                }
            }
        }
        
		return new BiomeArea(foundPoints, weCUIMessages);
        
    }
    

	public static void makeWGBiome(final Player player, final String regionID, final Biome biome) {
		Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, new Runnable() { public void run() {
	    	try{
	    		if (WorldGuardFunctions.makeWGBiome(player, regionID, biome)) {
	    			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "WorldGuard region was replaced to: " + biome.toString());
	    			return;
	    		}
	    	}
	    	catch (Exception e) {}
	    	catch (Error er) {}
			
			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Could not find any WorldGuard region with that ID.");
		}});
	}

	public static void makeWEBiome(final Player player, final Biome biome) {
		Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, new Runnable() { public void run() {
	    	try{
	    		if (WorldEditFunctions.makeWEBiome(player, biome)) {
	    			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "WorldEdit selection was replaced to: " + biome.toString());
	    			return;
	    		}
	    	}
	    	catch (Exception e) {}
	    	catch (Error er) {}
	    	
			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Could not find any WorldEdit selection. Please use your wand to define one first.");
		}});
	}
	
	
	// ########################################
	

	public static void makeAndMarkSquareBiome(Player player, Biome biome, int size, int yLoc) {
		makeAndMarkSquareBiome(player, player.getLocation(), biome, size, yLoc);
	}
	public static void makeAndMarkSquareBiome(final Player player, final Location location, final Biome biome, final int size, final int yLoc) {
		Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, new Runnable() { public void run() {
			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Calculating biome area. This may take a while...");
			BiomeArea bArea = BiomeEditor.makeSquareArea(location, biome, size);
			
			if (Thread.interrupted()) {return;}
	   		
			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Done. Changing " + ChatColor.AQUA + bArea.getPoints().size() + ChatColor.WHITE + " blocks to new biome now ...");
			changeAndMarkBiome(bArea, biome, player.getWorld(), player, yLoc);
			
			if (Thread.interrupted()) {return;}

			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Square " + biome.toString() + " biome with radius "+ size +" created at " + location.getBlockX() + "," + location.getBlockZ());
		}});
	}

	public static void replaceAndMarkCompleteBiome(Player player, Biome biome, int yLoc) {
		replaceAndMarkCompleteBiome(player, player.getLocation(), biome, yLoc);
	}
	public static void replaceAndMarkCompleteBiome(final Player player, final Location location, final Biome biome, final int yLoc) {
		if (replacer != null) {
			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "BiomeEdit replacement job still active.");
			return;
		}
		
		replacer = new Thread() { public void run() {
			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Calculating biome boundaries. This may take a while...");
			BiomeArea bArea = BiomeEditor.findBiomeArea(location);
			
			if (Thread.interrupted()) {return;}

			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Done. Changing " + ChatColor.AQUA + bArea.getPoints().size() + ChatColor.WHITE + " blocks to new biome now ...");
			changeAndMarkBiome(bArea, biome, player.getWorld(), player, yLoc);

			if (Thread.interrupted()) {return;}

			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Biome was replaced to: " + biome.toString());
			replacer = null;
		}};
		
		Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, replacer);
	}


	public static void makeAndMarkCylinderBiome(Player player, Biome biome, int size, int yLoc) {
		makeAndMarkCylinderBiome(player, player.getLocation(), biome, size, yLoc);
	}
	public static void makeAndMarkCylinderBiome(final Player player, final Location location, final Biome biome, final int size, final int yLoc) {
		Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, new Runnable() { public void run() {
			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Calculating biome area. This may take a while...");
			BiomeArea bArea = BiomeEditor.makeCylinderArea(location, biome, size);

			if (Thread.interrupted()) {return;}

			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Done. Changing " + ChatColor.AQUA + bArea.getPoints().size() + ChatColor.WHITE + " blocks to new biome now ...");
			changeAndMarkBiome(bArea, biome, player.getWorld(), player, yLoc);

			if (Thread.interrupted()) {return;}

			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Round " + biome.toString() + " biome with radius "+ size +" created at " + location.getBlockX() + "," + location.getBlockZ());
		}});

	}
	
	public static void cancel(Player player) {
		Bukkit.getScheduler().cancelTasks(CNBiomeEdit.plugin);
		if (replacer != null) replacer.interrupt();
		replacer = null;

		player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Trying to cancel all BiomeEdit jobs.");
	}


	
	private static void changeAndMarkBiome(BiomeArea bArea, Biome biome, World world, Player player, int yLoc) {
		replaceBiomePoints(bArea.getPoints(), world, biome, player);

		if (Thread.interrupted()) {return;}

   		if (UIStuff.hasCUISupport(player)) {
			
			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Done. Sorting outer points for better visuability.");
   			ArrayList<int[]> sorted = Functions.sortAreaPoints(bArea.getOuterPoints());
   			
			if (Thread.interrupted()) {return;}
			
			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Done. Visualizing area using weCUI.");
   			UIStuff.markAreaWithPoints(sorted, player, yLoc);

   			if (Thread.interrupted()) {return;}
   		}

		player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Done. Reloading chunks.");
		Functions.smartReloadChunks(bArea.getPoints(), player.getWorld());

	}
	
	public static void replaceBiomePoints(HashSet<int[]> points, World world, Biome biome, Player player) {
		CNBiomeEdit.logIt("replaceBiomePoints", Verbose.ALL);
		
		int counter = 0;
		int jump = 0;
		int sweep = CNBiomeEdit.plugin.perSweep;
		
		int maxCount = points.size();

		int jumpAt = 10;
		if (maxCount > 1000 * sweep) jumpAt = 1;
		else if (maxCount > 350 * sweep) jumpAt = 2;
		else if (maxCount > 100 * sweep) jumpAt = 5;
		
		HashSet<int []> cache = new HashSet<int []>();
		HashSet<String> loadedChunks = new HashSet<String>();

		for (int[] point : points) {
			BiomeEditor.setBiomeAt(world, point[0], point[1], biome, cache, loadedChunks);
			
			if (((counter * 100 / maxCount)  % jumpAt) == 0) {
				int percent = counter * 100 / maxCount;
				if (jump != percent) {
					if (Thread.interrupted()) {
						player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "... BiomeEdit job cancelled!");
						return;
					}
					player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "... " + percent + "%");
					jump = percent;
				}
			}
			counter++;
			
		}
		
		if (Thread.interrupted()) {return;}

		BiomeEditor.flushBiomeSetHunks(world, biome, cache, loadedChunks);

		CNBiomeEdit.logIt("replaceBiomePoints done", Verbose.ALL);
	}
    
	static void setBiomeAt(World world, int x, int z, Biome biome, HashSet<int []> blockHunk, HashSet<String> loadedChunks) {
		CNBiomeEdit.logIt("setBiomeAt", Verbose.ALL);
		
		if (blockHunk.size() < CNBiomeEdit.plugin.perSweep) {
			blockHunk.add(new int[] {x, z});
			CNBiomeEdit.logIt("Added to blockHunk", Verbose.ALL);
			return;
		}
		blockHunk.add(new int[] {x,z});
	
		BiomeEditor.flushBiomeSetHunks(world, biome, blockHunk, loadedChunks);
	
		CNBiomeEdit.logIt("setBiomeAt done", Verbose.ALL);
	}


	static void flushBiomeSetHunks(World world, Biome biome, HashSet<int []> blockHunk, HashSet<String> loadedChunks) {
		CNBiomeEdit.logIt("flushing blockHunk", Verbose.ALL);
		BiomeEditor.setBiomeOnMainThread(world, blockHunk, biome, loadedChunks);
		
		CNBiomeEdit.logIt("clearing blockHunk", Verbose.ALL);
		blockHunk.clear();
	}


	static void setBiomeOnMainThread(final World world, final HashSet<int []> points, final Biome biome, final HashSet<String> loadedChunks) {
		Future<String> returnFuture = Bukkit.getScheduler().callSyncMethod(CNBiomeEdit.plugin, new Callable<String>() { public String call() {

			/*
			for (int [] blockLoc : points) {
				int x = blockLoc[0];
				int z = blockLoc[1];
				
				String checkString = (x >> 4) + "|" +  (z >> 4);

				if (!loadedChunks.contains(checkString)) {
					Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
					
					if (!chunk.isLoaded()) {
						CNBiomeEdit.logIt("Loading unloaded chunk on setBiomeOnMainThread(): " + chunk.getX() + "/" + chunk.getZ(), Verbose.ERROR);
						
						if (!chunk.load()) {
							CNBiomeEdit.logIt("Could not load chunk on setBiomeOnMainThread(): " + chunk.getX() + "/" + chunk.getZ(), Verbose.ERROR);
							return null;
		
						}
						
					}
					
					loadedChunks.add(checkString);
				}
						
			}
			*/
			
			for (int [] blockLoc : points) {
				world.setBiome(blockLoc[0], blockLoc[1], biome);
			}
	
			return null;
		}});
		
		while (!returnFuture.isDone()) {};
		
	}

}
