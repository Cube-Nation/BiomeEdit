package me.derflash.plugins.cnbiomeedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;


public class BiomeEditor {
	
    public static HashSet<int[]> findBiomeArea(Location fromPos, boolean filled) {
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
						if (!filled && !outerPoints.contains(currentCheck)) outerPoints.add(currentCheck);
					}
				}
				checkedPoints.add(checkString);
			}
		}
		
		return filled ? foundPoints : outerPoints;
		
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
    
	public static Collection<int[]> replaceBiome(Location location, Biome biome) {
		HashSet<int[]> points = BiomeEditor.findBiomeArea(location, true);
		
		World world = location.getWorld();
		for (int[] point : points) {
			Functions.setBiomeAt(world, point[0], point[1], biome);
		}

		return BiomeEditor.findBiomeArea(location, false);
	}

	public static boolean makeWGBiome(Player player, String regionID, Biome biome, CNBiomeEdit plugin) {
    	try{ return WorldGuardFunctions.makeWGBiome(player, regionID, biome, plugin); }
    	catch (Exception e) {}
    	catch (Error er) {}
		return false;
	}


	public static boolean makeWEBiome(Player player, Biome biome, CNBiomeEdit plugin) {
    	try{ return WorldEditFunctions.makeWEBiome(player, biome, plugin); }
    	catch (Exception e) {}
    	catch (Error er) {}
		return false;
	}
	
	// ########################################
	

	public static void makeAndMarkSquareBiome(Player player, Biome biome, int size, int yLoc) {
		makeAndMarkSquareBiome(player, player.getLocation(), biome, size, yLoc);
	}
	public static void makeAndMarkSquareBiome(Player player, Location location, Biome _biome, int _biomeSize, int yLoc) {
   		ArrayList<int[]> borderPoints = BiomeEditor.makeSquareBiome(location, _biome, _biomeSize);
		ArrayList<int[]> sorted = Functions.sortAreaPoints(borderPoints);
		UIStuff.markAreaWithPoints(sorted, player, yLoc);
	}


	public static void replaceAndMarkBiome(Player player, Biome _biome, int yLoc) {
		replaceAndMarkBiome(player, player.getLocation(), _biome, yLoc);
	}
	public static void replaceAndMarkBiome(Player player, Location location, Biome _biome, int yLoc) {
		Collection<int[]> borderPoints = BiomeEditor.replaceBiome(location, _biome);
		ArrayList<int[]> sorted = Functions.sortAreaPoints(borderPoints);
		UIStuff.markAreaWithPoints(sorted, player, yLoc);		
	}


	public static void makeAndMarkCylinderBiome(Player player, Biome biome, int size, int yLoc) {
		makeAndMarkCylinderBiome(player, player.getLocation(), biome, size, yLoc);
	}
	public static void makeAndMarkCylinderBiome(Player player, Location targetLocation, Biome biome, int size, int yLoc) {
		ArrayList<int[]> borderPoints = BiomeEditor.makeCylinderBiome(targetLocation, biome, size);
		ArrayList<int[]> sorted = Functions.sortAreaPoints(borderPoints);
		UIStuff.markAreaWithPoints(sorted, player, yLoc);		
	}





}
