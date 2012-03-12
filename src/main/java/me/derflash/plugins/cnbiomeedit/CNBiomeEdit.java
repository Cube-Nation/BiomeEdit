package me.derflash.plugins.cnbiomeedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class CNBiomeEdit extends JavaPlugin implements Listener {
	HashSet<Byte> transparentBlocks = null;
	private HashMap<Player, BiomeBrushSettings> currentBrushers = new HashMap<Player, BiomeBrushSettings>();

	
	
    public void onDisable() {
    }

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

		transparentBlocks = new HashSet<Byte>();
		transparentBlocks.add((byte) 0);
		transparentBlocks.add((byte) 20);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
    	if (isBrushActive(player)) {
    		event.setCancelled(true);

    		Block targetBlock = player.getTargetBlock(null, 200);
    		Location targetLocation = targetBlock.getLocation();
    		
    		BiomeBrushSettings bbs = currentBrushers.get(player);

       		makeCylinderBiome(player, targetLocation.toVector(), bbs.getBiome(), player.getWorld(), bbs.getSize());
    	}
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		currentBrushers.remove(player);
    }

    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	// player is the warned player; player1 is the sender
    	// permissions cubewarn.staff & cubewarn.admin
    	Player player = (Player) sender;
    	
		if (!player.hasPermission("cnbiome.admin")) return true;
    	
    	if (label.equalsIgnoreCase("biome")) {
    		
        		if(args.length > 2 && args[0].equalsIgnoreCase("set") ) {
        			Biome _biome = BiomeBrushSettings.getBiomeFromString(args[1]);
        			int _biomeSize = Integer.parseInt(args[2]);
        			
        			if (_biome == null) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "There's no such biome. See " + ChatColor.AQUA + "/" + label + " list");
        				return true;
        			}
        			
    				if (!BiomeBrushSettings.isValidBiomeSize(_biomeSize)) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "This is no valid size. See " + ChatColor.AQUA + "/" + label + " help");
        				return true;
    				}
        			
               		makeCylinderBiome(player, null, _biome, player.getWorld(), _biomeSize);
        			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Biome with radius "+ _biomeSize +" set to: " + _biome.toString());

        		} else if(args.length > 0 && args[0].equalsIgnoreCase("brush") ) {
        			if (args.length == 1 || args[1].equalsIgnoreCase("off")) {
        				deactivateBrush(player);
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Biome brush mode deactived.");
        				return true;
        			}
        			
        			BiomeBrushSettings _settings = new BiomeBrushSettings();
        			
        			if (args.length > 1) {
            			if (!_settings.setBiome(args[1])) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "There's no such biome. See " + ChatColor.AQUA + "/" + label + " list");
            				return true;
            			}
        			}
        			
        			if (args.length > 2) {
            			if (!_settings.setSize(Integer.parseInt(args[2]))) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "This is no valid size. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}
        			}
        			        			        			
        			if (activateBrush(player, _settings)) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Biome brush mode activated. [Biome: " + _settings.getBiome().toString() + " | Size: " + _settings.getSize() + "]");
        			} else {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Biome brush mode could not be activated. :-(");
        				
        			}

        			
        		} else if(args.length > 0 && args[0].equalsIgnoreCase("info") ) {
        			Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
        			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "You are currently standing in: " + ChatColor.AQUA + biome.toString());
        			
        		} else if(args.length > 0 && args[0].equalsIgnoreCase("list") ) {
        			String biomes = null;
        			for (Biome biome : Biome.values()) {
        				if (biomes == null) biomes = biome.toString();
        				else biomes += ", " + biome.toString();
        			}
        			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Available biomes: " + ChatColor.AQUA + biomes);
        			
        		} else {
        			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "- Command overview -");
        			player.sendMessage(ChatColor.AQUA + "* " + ChatColor.WHITE + "/"+label+" set [biome] [radius] - Sets the biome with this radius on the current player location");
        			player.sendMessage(ChatColor.AQUA + "* " + ChatColor.WHITE + "/"+label+" brush [biome] [radius] - Activate biome brush");
        			player.sendMessage(ChatColor.AQUA + "* " + ChatColor.WHITE + "/"+label+" brush off - Deactivate biome brush");
        			player.sendMessage(ChatColor.AQUA + "* " + ChatColor.WHITE + "/"+label+" info - Prints out the biome you're currently standing in");

        		}
        		
    	}
    	
    	return true;
    }






	public boolean isBrushActive(Player player) {
		return currentBrushers.containsKey(player);
	}
	
	public boolean deactivateBrush(Player player) {
		currentBrushers.remove(player);
		return true;
	}

	public boolean activateBrush(Player player, BiomeBrushSettings _settings) {
		// do some checks?!
		
		currentBrushers.put(player, _settings);
		return true;
	}
    
	
    public void makeCylinderBiome(Player player, Vector pos, Biome biome, World world, double radius) {
    	if (pos == null) pos = player.getLocation().toVector();
    	
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
        
//        ArrayList<String> reggedChunks = new ArrayList<String>();

        ArrayList<String> weCUIMessages1 = new ArrayList<String>();
        ArrayList<String> weCUIMessages2 = new ArrayList<String>();
        ArrayList<String> weCUIMessages3 = new ArrayList<String>();
        ArrayList<String> weCUIMessages4 = new ArrayList<String>();
        

        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextZn = 0;
            forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                final double zn = nextZn;
                nextZn = (z + 1) * invRadiusZ;

                double distanceSq = lengthSq(xn, zn);
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
                
                setBiomeAt(world, x1, z1, biome);
                setBiomeAt(world, x2, z1, biome);
                setBiomeAt(world, x1, z2, biome);
                setBiomeAt(world, x2, z2, biome);

                // TODO
                /*
                if (regen) { 
                	smartRegenChunkAt(x1, z1, world, reggedChunks);
                	smartRegenChunkAt(x2, z1, world, reggedChunks);
                	smartRegenChunkAt(x1, z2, world, reggedChunks);
                	smartRegenChunkAt(x2, z2, world, reggedChunks);
                }
                */

                if (! (lengthSq(nextXn, zn) <= 1 && lengthSq(xn, nextZn) <= 1)) {
                	// we got an outter point => weGUI update
                	
                	weCUIMessages1.add(x1+"|"+z1);
                	weCUIMessages2.add(x2+"|"+z2);
                	weCUIMessages3.add(x2+"|"+z1);
                	weCUIMessages4.add(x1+"|"+z2);
                }
            }
        }
        
        // join guiMessages now
        ArrayList<String> weCUIMessages = new ArrayList<String>();
        weCUIMessages.addAll(weCUIMessages1);
        weCUIMessages.addAll(weCUIMessages2);
        weCUIMessages.addAll(weCUIMessages3);
        weCUIMessages.addAll(weCUIMessages4);
        
    	int counter = 0;
    	player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75s|polygon2d");
        for (String _message : weCUIMessages) {
        	player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75p2|" + counter + "|" + _message+"|0");
        	counter++;
        }
       	player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75mm|30|120");
        

    }
    
    private void smartRegenChunkAt(int x, int z, World world, ArrayList<String> reggedChunks) {
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

	private void setBiomeAt(World world, int x,int z, Biome biome) {
    	world.setBiome(x, z, biome);
    }

    private static final double lengthSq(double x, double z) {
        return (x * x) + (z * z);
    }
}

