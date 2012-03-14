package me.derflash.plugins.cnbiomeedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BiomeEditor {
	
    public static BiomeArea findBiomeArea(Location fromPos) {
    	Stack<int[]> checkPoints = new Stack<int[]>();
        HashSet<int[]> foundPoints = new HashSet<int[]>();
        HashSet<int[]> outerPoints = new HashSet<int[]>();
        HashSet<String> checkedPoints = new HashSet<String>();
        
		String checkString;
		
        World world = fromPos.getWorld();
		Biome biome = world.getBiome(fromPos.getBlockX(), fromPos.getBlockZ());

		checkPoints.add(new int[] {fromPos.getBlockX(), fromPos.getBlockZ()});
		
		while (!checkPoints.isEmpty()) {
			int[] currentCheck = checkPoints.pop();
			int x = currentCheck[0];
			int z = currentCheck[1];
			
			int[][] locsAroundMe = new int[][] {new int[] {x+1,z}, new int[] {x-1,z}, new int[] {x,z+1}, new int[] {x,z-1}};
			for (int i = 0; i<locsAroundMe.length; i++) {
				int[] locToCheck = locsAroundMe[i];
				checkString = locToCheck[0] + "|" + locToCheck[1];
				if (!checkedPoints.contains(checkString)) {
					if (world.getBiome(locToCheck[0], locToCheck[1]).equals(biome)) {
						foundPoints.add(locToCheck);
						checkPoints.push(locToCheck);
					} else {
						if (!outerPoints.contains(currentCheck)) outerPoints.add(currentCheck);
					}
				}
				checkedPoints.add(checkString);
			}
		}
		
		return new BiomeArea(foundPoints, outerPoints);
		
    }
    
    
	// ########################################
    
    
    public static ArrayList<int[]> makeSquareBiome(Location location, Biome _biome, int _biomeSize) {
    	World world = location.getWorld();
    	Vector pos1 = new Vector(location.getBlockX() - _biomeSize, location.getBlockY(), location.getBlockZ() - _biomeSize);
        ArrayList<int []> weCUIMessages = new ArrayList<int []>();

        int xMax = _biomeSize * 2;
        int zMax = _biomeSize * 2;
        
    	for (int x = 0; x < xMax; x++) {
        	for (int z = 0; z < zMax; z++) {
        		int _x = pos1.getBlockX()+x;
        		int _z = pos1.getBlockZ()+z;
        		
                Functions.setBiomeAt(world, _x, _z, _biome);
        		if ( (x == 0 || x == xMax - 1) || (z == 0 || z == zMax - 1) ) {
        			weCUIMessages.add(new int [] {_x,_z});
        		}
        	}
    	}
    	
		return weCUIMessages;
	}
    
    public static ArrayList<int[]> makeCylinderBiome(Location fromPos, Biome biome, double radius) {
    	World world = fromPos.getWorld();
    	Vector pos = fromPos.toVector();
    	
    	double radiusX = radius;
    	double radiusZ = radius;
    	
        radiusX += 0.5;
        radiusZ += 0.5;
       
        if (pos.getBlockY() < 0) {
            pos = pos.setY(0);
        }

        final double invRadiusX = 1 / radiusX;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);
        
        ArrayList<int []> weCUIMessages = new ArrayList<int []>();
        
        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextZn = 0;
            forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                final double zn = nextZn;
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
                
                Functions.setBiomeAt(world, x1, z1, biome);
                Functions.setBiomeAt(world, x2, z1, biome);
                Functions.setBiomeAt(world, x1, z2, biome);
                Functions.setBiomeAt(world, x2, z2, biome);

                // TODO
                /*
                if (regen) { 
                	smartRegenChunkAt(x1, z1, world, reggedChunks);
                	smartRegenChunkAt(x2, z1, world, reggedChunks);
                	smartRegenChunkAt(x1, z2, world, reggedChunks);
                	smartRegenChunkAt(x2, z2, world, reggedChunks);
                }
                */

                if (! (Functions.lengthSq(nextXn, zn) <= 1 && Functions.lengthSq(xn, nextZn) <= 1)) {
                	// we got an outter point => weGUI update
                	
                	weCUIMessages.add(new int[] {x1,z1});
                	weCUIMessages.add(new int[] {x2,z2});
                	weCUIMessages.add(new int[] {x2,z1});
                	weCUIMessages.add(new int[] {x1,z2});
                }
            }
        }
        
        return weCUIMessages;
        
    }
    
	public static Collection<int[]> replaceBiome(Location location, final Biome biome, Player player) {
		player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Calculating biome boundaries...");
		BiomeArea bArea = BiomeEditor.findBiomeArea(location);
		
		int counter = 0;
		int maxCount = bArea.getPoints().size();
		
		player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Done. Changing " + ChatColor.AQUA + maxCount + ChatColor.WHITE + " blocks to new biome now ...");
		final World world = location.getWorld();
		
		int jump = 0;
		int jumpAt = 10;
		if (maxCount > 10000) jumpAt = 1;
		else if (maxCount > 6000) jumpAt = 2;
		else if (maxCount > 2000) jumpAt = 5;
		
		for (final int[] point : bArea.getPoints()) {
			
			if (!CNBiomeEdit.plugin.settings.getBoolean("threaded")) {
				Functions.setBiomeAt(world, point[0], point[1], biome);
				
			} else {
				Future<Object> go = Bukkit.getScheduler().callSyncMethod(CNBiomeEdit.plugin, new Callable<Object>() {
					public Object call() throws Exception {
						Functions.setBiomeAt(world, point[0], point[1], biome);
						return null;
					}});
				while (!go.isDone()) {}
				
				if (((counter * 100 / maxCount)  % jumpAt) == 0) {
					int percent = counter * 100 / maxCount;
					if (jump != percent) {
						player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "... " + percent + "%");
						jump = percent;
					}
				}
				counter++;

			}
			
		}

		return bArea.getOuterPoints();
	}

	public static void makeWGBiome(Player player, String regionID, Biome biome) {
    	try{
    		if (WorldGuardFunctions.makeWGBiome(player, regionID, biome)) {
    			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "WorldGuard region was replaced to: " + biome.toString());
    			return;
    		}
    	}
    	catch (Exception e) {}
    	catch (Error er) {}
		
		player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Could not find any WorldGuard region with that ID.");
	}

	public static void makeWEBiome(Player player, Biome biome) {
    	try{
    		if (WorldEditFunctions.makeWEBiome(player, biome)) {
    			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "WorldEdit selection was replaced to: " + biome.toString());
    			return;
    		}
    	}
    	catch (Exception e) {}
    	catch (Error er) {}
    	
		player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Could not find any WorldEdit selection. Please use your wand to define one first.");
	}
	
	
	// ########################################
	

	public static void makeAndMarkSquareBiome(Player player, Biome biome, int size, int yLoc) {
		makeAndMarkSquareBiome(player, player.getLocation(), biome, size, yLoc);
	}
	public static void makeAndMarkSquareBiome(Player player, Location location, Biome biome, int size, int yLoc) {
   		ArrayList<int[]> borderPoints = BiomeEditor.makeSquareBiome(location, biome, size);
   		
   		if (UIStuff.hasCUISupport(player)) {
   			ArrayList<int[]> sorted = Functions.sortAreaPoints(borderPoints);
   			UIStuff.markAreaWithPoints(sorted, player, yLoc);
   		}

		player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Square biome with radius "+ size +" created: " + biome.toString());
}


	public static void replaceAndMarkBiome(Player player, Biome biome, int yLoc) {
		replaceAndMarkBiome(player, player.getLocation(), biome, yLoc);
	}
	public static void replaceAndMarkBiome(Player player, Location location, Biome biome, int yLoc) {
		Collection<int[]> borderPoints = BiomeEditor.replaceBiome(location, biome, player);
		
   		if (UIStuff.hasCUISupport(player)) {
   			ArrayList<int[]> sorted = Functions.sortAreaPoints(borderPoints);
   			UIStuff.markAreaWithPoints(sorted, player, yLoc);		
   		}

		player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Biome was replaced to: " + biome.toString());
	}


	public static void makeAndMarkCylinderBiome(Player player, Biome biome, int size, int yLoc) {
		makeAndMarkCylinderBiome(player, player.getLocation(), biome, size, yLoc);
	}
	public static void makeAndMarkCylinderBiome(Player player, Location targetLocation, Biome biome, int size, int yLoc) {
		ArrayList<int[]> borderPoints = BiomeEditor.makeCylinderBiome(targetLocation, biome, size);

   		if (UIStuff.hasCUISupport(player)) {
   			ArrayList<int[]> sorted = Functions.sortAreaPoints(borderPoints);
   			UIStuff.markAreaWithPoints(sorted, player, yLoc);
   		}
   		
		player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Round biome with radius "+ size +" created: " + biome.toString());

	}

}
