package me.derflash.plugins.cnbiomeedit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import me.derflash.plugins.cnbiomeedit.BiomeBrushSettings.BiomeMode;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;


public class CNBiomeEdit extends JavaPlugin implements Listener {
	public HashMap<Player, BiomeBrushSettings> currentBrushers = new HashMap<Player, BiomeBrushSettings>();
	HashSet<Byte> transparentBlocks = null;
	HashSet<Player> cuiSupported = new HashSet<Player>();

	public static CNBiomeEdit plugin;
	private WorldEditPlugin _wePlugin;
	private WorldGuardPlugin _wgPlugin;

	// major settings
	private File settingsFile;
	public YamlConfiguration settings;
	int perSweep = 1000;
	boolean threaded = true;
	
	
	public void onEnable() {
		CNBiomeEdit.plugin = this; // static access
		
		File dFolder = getDataFolder();
		if(!dFolder.exists()) dFolder.mkdirs();
		
		settingsFile = new File(dFolder, "config.yml");
        if (settingsFile.exists()) {
        	settings = YamlConfiguration.loadConfiguration(settingsFile);
        	perSweep = settings.getInt("perSweep", 1000);
        	threaded = settings.getBoolean("threaded", true);

        } else {
        	settings = new YamlConfiguration();
        	settings.set("maxRadius", 500);
        	settings.set("threaded", true);
        	settings.set("perSweep", 1000);
        	saveSettings();
        	
        }
		
        getServer().getPluginManager().registerEvents(this, this);

		transparentBlocks = new HashSet<Byte>();
		transparentBlocks.add((byte) 0);
		transparentBlocks.add((byte) 20);
		
		new PlayerListener(this);
    }
    
    public void onDisable() {
    }
    
    
	public boolean saveSettings() {
		if (!settingsFile.exists()) {
			settingsFile.getParentFile().mkdirs();
		}
		
		try {
			settings.save(settingsFile);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
    
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	// player is the warned player; player1 is the sender
    	// permissions cubewarn.staff & cubewarn.admin
    	final Player player = (Player) sender;
    	
		if (!player.hasPermission("cnbiome.admin")) return true;
    	
    	if (true /* label.equalsIgnoreCase("biome")*/) {
    		
        		if(args.length > 2 && args[0].equalsIgnoreCase("set")) {
        			final Biome _biome = BiomeBrushSettings.getBiomeFromString(args[1]);
        			if (_biome == null) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "There's no such biome. See " + ChatColor.AQUA + "/" + label + " list");
        				return true;
        			}

        			BiomeMode _mode = BiomeBrushSettings.getModeFromString(args[2]);
        			if (_mode == null) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "There's no such mode. See " + ChatColor.AQUA + "/" + label + " help");
        				return true;
        			}

        			if (_mode.equals(BiomeMode.ROUND)) {
        				if (args.length < 4) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "You need to provide a size for this mode. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}
        				
            			final int _biomeSize = Integer.parseInt(args[3]);
        				if (!BiomeBrushSettings.isValidBiomeSize(_biomeSize)) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "This is no valid size. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}
        				
        				if (threaded) {
            				Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, new Runnable() {
            					public void run() {
                    				BiomeEditor.makeAndMarkCylinderBiome(player, _biome, _biomeSize, -1);
            					}});
        				} else {
            				BiomeEditor.makeAndMarkCylinderBiome(player, _biome, _biomeSize, -1);
        				}
            			
        			} else if (_mode.equals(BiomeMode.SQUARE)) {
        				if (args.length < 4) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "You need to provide a size for this mode. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}

            			final int _biomeSize = Integer.parseInt(args[3]);
        				if (!BiomeBrushSettings.isValidBiomeSize(_biomeSize)) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "This is no valid size. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}
        				
        				if (threaded) {
            				Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, new Runnable() {
            					public void run() {
                    				BiomeEditor.makeAndMarkSquareBiome(player, _biome, _biomeSize, -1);
            					}});
        				} else {
            				BiomeEditor.makeAndMarkSquareBiome(player, _biome, _biomeSize, -1);
        				}
            			
        			} else if (_mode.equals(BiomeMode.REPLACE)) {
        				if (args.length > 3) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Replace only needs the biome parameter. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}

        				if (threaded) {
            				Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, new Runnable() {
            					public void run() {
                    				BiomeEditor.replaceAndMarkCompleteBiome(player, _biome, -1);
            					}});
        				} else {
            				BiomeEditor.replaceAndMarkCompleteBiome(player, _biome, -1);
        				}

        			} else if (_mode.equals(BiomeMode.WE)) {
        				if (wePlugin() == null) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "You need to install the WorldEdit plugin for this");
            				return true;
        				}
        				
        				if (args.length > 3) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "WE only needs the biome parameter. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}

        				if (threaded) {
            				Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, new Runnable() {
            					public void run() {
                    				BiomeEditor.makeWEBiome(player, _biome);
            					}});
        				} else {
            				BiomeEditor.makeWEBiome(player, _biome);
        				}
        				
        			} else if (_mode.equals(BiomeMode.WG)) {
        				if (wgPlugin() == null) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "You need to install the WorldGuard plugin for this");
            				return true;
        				}
        				
        				if (args.length < 4) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "You need to provide a WorldGuard regionID for this. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}
        				if (args.length > 4) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "WG only needs the biome and WorldGuard regionID parameter. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}
        				final String regionID = args[3];
        				
        				if (threaded) {
            				Bukkit.getScheduler().scheduleAsyncDelayedTask(CNBiomeEdit.plugin, new Runnable() {
            					public void run() {
                    				BiomeEditor.makeWGBiome(player, regionID, _biome);
            					}});
        				} else {
            				BiomeEditor.makeWGBiome(player, regionID, _biome);
        				}
        			}
        			
        			
        		} else if(args.length > 1 && args[0].equalsIgnoreCase("brush") ) {
        			if (args[1].equalsIgnoreCase("off")) {
        				deactivateBrush(player);
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Biome brush mode deactived.");
        				return true;
        			}
        			
        			BiomeBrushSettings _settings = new BiomeBrushSettings();
        			
        			if (!_settings.setBiome(args[1])) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "There's no such biome. See " + ChatColor.AQUA + "/" + label + " list");
        				return true;
        			}
        			
        			if (args.length < 3 || !_settings.setMode(args[2])) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "There's no such mode. See " + ChatColor.AQUA + "/" + label + " list");
        				return true;
        			}
        			
        			if (args.length > 3) {
        				if (_settings.getMode().equals(BiomeMode.REPLACE)) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "You can not set the size in replace mode. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
            				
        				} else if (!_settings.setSize(Integer.parseInt(args[3]))) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "This is no valid size. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}
        			}
        			        			        			
        			if (activateBrush(player, _settings)) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Biome brush mode activated. [Biome: " + _settings.getBiome().toString() + " | Mode: " + _settings.getMode().toString() + " | Size: " + _settings.getSize() + "]");
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "You may now use a dead shrub to start brushing!");
        			} else {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Biome brush mode could not be activated. :-(");
        			}

        			
        		} else if(args.length > 0 && args[0].equalsIgnoreCase("info") ) {
        			Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
        			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "You are currently standing in: " + ChatColor.AQUA + biome.toString());
        			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Calculating biome boundaries...");
        			
        			BiomeArea bArea = null;
        			if (UIStuff.hasCUISupport(player)) {
        				bArea = UIStuff.markBiome(player.getLocation(), player, -1);
        			} else {
        				bArea = BiomeEditor.findBiomeArea(player.getLocation());
        			}
        			if (bArea != null) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Done. The current biome has " + bArea.getPoints().size() + " blocks.");
        			}
        			
        		} else if(args.length > 0 && args[0].equalsIgnoreCase("reload") ) {
        			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Calculating biome boundaries...");
        			BiomeArea bArea = BiomeEditor.findBiomeArea(player.getLocation());
        			
        			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Done. The current biome has " + bArea.getPoints().size() + " blocks and will be reloaded now...");
        			Functions.smartReloadChunks(bArea.getPoints(), player.getWorld());
        			
        			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Done.");
        			
        		} else if(args.length > 0 && args[0].equalsIgnoreCase("list") ) {
        			String biomes = null;
        			for (Biome biome : Biome.values()) {
        				if (biomes == null) biomes = biome.toString();
        				else biomes += ", " + biome.toString();
        			}
        			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Available biomes: " + ChatColor.AQUA + biomes);
        			
        		} else if(args.length > 0 && args[0].equalsIgnoreCase("modes") ) {
        			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "- Mode overview -");
        			player.sendMessage(ChatColor.AQUA + "- " + ChatColor.WHITE + "ROUND - Creates a round biome");
        			player.sendMessage(ChatColor.AQUA + "- " + ChatColor.WHITE + "SQUARE - Creates a square biome");
        			player.sendMessage(ChatColor.AQUA + "- " + ChatColor.WHITE + "REPLACE - Uses the boundaries of the current biome at the location");
        			player.sendMessage(ChatColor.AQUA + "- " + ChatColor.WHITE + "WE* - Uses your WorldEdit selection");
        			player.sendMessage(ChatColor.AQUA + "- " + ChatColor.WHITE + "WG* - Uses a WorldGuard regionID");
        			player.sendMessage(ChatColor.AQUA + "* only when using: /brush set");

        			
        		} else {
        			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "- Command overview -");
        			player.sendMessage(ChatColor.AQUA + "- " + ChatColor.WHITE + "/"+label+" set <biome> <mode> [radius|regionID] - Sets the biome using the current player location");
        			player.sendMessage(ChatColor.AQUA + "- " + ChatColor.WHITE + "/"+label+" brush <biome> <mode> [radius] - Activate biome brush");
        			player.sendMessage(ChatColor.AQUA + "- " + ChatColor.WHITE + "/"+label+" brush off - Deactivate biome brush");
        			player.sendMessage(ChatColor.AQUA + "- " + ChatColor.WHITE + "/"+label+" info - Gives you informations about the biome you're currently standing in");
        			player.sendMessage(ChatColor.AQUA + "- " + ChatColor.WHITE + "/"+label+" list - Lists the servers' available biomes");
        			player.sendMessage(ChatColor.AQUA + "- " + ChatColor.WHITE + "/"+label+" modes - Lists the replacement modes");

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
		if (!player.getInventory().contains(Material.DEAD_BUSH)) {
			ItemStack newShrub = new ItemStack(Material.DEAD_BUSH, 1);
			if (player.getItemInHand() == null) {
				player.getInventory().addItem(newShrub);
				player.setItemInHand(newShrub);
			} else {
				player.getInventory().addItem(newShrub);
			}
		}
		
		currentBrushers.put(player, _settings);
		return true;
	}

	
	public WorldEditPlugin wePlugin() {
		if (_wePlugin == null) {
			_wePlugin = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
			if (_wePlugin == null) {
				getLogger().info("[BiomeEdit] WEPlugin not found");
				return null;
			}
		}
		return _wePlugin;
	}
	
	
	public WorldGuardPlugin wgPlugin() {
		if (_wgPlugin == null) {
			_wgPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
			if (_wgPlugin == null) {
				getLogger().info("[BiomeEdit] WGPlugin not found");
				return null;
			}
		}
		return _wgPlugin;
	}
}

