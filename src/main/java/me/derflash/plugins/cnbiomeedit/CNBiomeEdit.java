package me.derflash.plugins.cnbiomeedit;

import java.util.HashMap;
import java.util.HashSet;

import me.derflash.plugins.cnbiomeedit.BiomeBrushSettings.BiomeMode;

import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class CNBiomeEdit extends JavaPlugin implements Listener {
	HashSet<Byte> transparentBlocks = null;
	public HashMap<Player, BiomeBrushSettings> currentBrushers = new HashMap<Player, BiomeBrushSettings>();

	public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

		transparentBlocks = new HashSet<Byte>();
		transparentBlocks.add((byte) 0);
		transparentBlocks.add((byte) 20);
		
		new PlayerListener(this);
    }
    
    public void onDisable() {
    }
    
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	// player is the warned player; player1 is the sender
    	// permissions cubewarn.staff & cubewarn.admin
    	Player player = (Player) sender;
    	
		if (!player.hasPermission("cnbiome.admin")) return true;
    	
    	if (label.equalsIgnoreCase("biome")) {
    		
        		if(args.length > 2 && args[0].equalsIgnoreCase("set")) {
        			BiomeMode _mode = BiomeBrushSettings.getModeFromString(args[1]);
        			if (_mode == null) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "There's no such mode. See " + ChatColor.AQUA + "/" + label + " help");
        				return true;
        			}
        			
        			Biome _biome = BiomeBrushSettings.getBiomeFromString(args[2]);
        			if (_biome == null) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "There's no such biome. See " + ChatColor.AQUA + "/" + label + " list");
        				return true;
        			}
        
        			if (_mode.equals(BiomeMode.ROUND)) {
        				if (args.length < 4) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "You need to provide a size for this mode. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}
        				
            			int _biomeSize = Integer.parseInt(args[3]);
        				if (!BiomeBrushSettings.isValidBiomeSize(_biomeSize)) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "This is no valid size. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}
        				
        				BiomeEditor.makeAndMarkCylinderBiome(player, _biome, _biomeSize, -1);
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Round biome with radius "+ _biomeSize +" created: " + _biome.toString());

            			
        			} else if (_mode.equals(BiomeMode.SQUARE)) {
        				if (args.length < 4) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "You need to provide a size for this mode. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}

            			int _biomeSize = Integer.parseInt(args[3]);
        				if (!BiomeBrushSettings.isValidBiomeSize(_biomeSize)) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "This is no valid size. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}
        				
        				BiomeEditor.makeAndMarkSquareBiome(player, _biome, _biomeSize, -1);
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Square biome with radius "+ _biomeSize +" created: " + _biome.toString());
            			
            			
        			} else if (_mode.equals(BiomeMode.REPLACE)) {
        				if (args.length > 3) {
                			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "You can not set the size in replace mode. See " + ChatColor.AQUA + "/" + label + " help");
            				return true;
        				}

        				BiomeEditor.replaceAndMarkBiome(player, _biome, -1);
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Biome was replaced to: " + _biome.toString());

        			}
        			
        			
        		} else if(args.length > 2 && args[0].equalsIgnoreCase("brush") ) {
        			if (args[1].equalsIgnoreCase("off")) {
        				deactivateBrush(player);
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Biome brush mode deactived.");
        				return true;
        			}
        			
        			BiomeBrushSettings _settings = new BiomeBrushSettings();
        			
        			if (!_settings.setMode(args[1])) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "There's no such mode. See " + ChatColor.AQUA + "/" + label + " list");
        				return true;
        			}
        			
        			if (!_settings.setBiome(args[2])) {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "There's no such biome. See " + ChatColor.AQUA + "/" + label + " list");
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
        			} else {
            			player.sendMessage(ChatColor.AQUA + "[BiomeEdit] " + ChatColor.WHITE + "Biome brush mode could not be activated. :-(");
        			}

        			
        		} else if(args.length > 0 && args[0].equalsIgnoreCase("info") ) {
        			UIStuff.markBiome(player.getLocation(), player, -1);
        			
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
        			player.sendMessage(ChatColor.AQUA + "* " + ChatColor.WHITE + "/"+label+" set <mode> <biome> [radius] - Sets the biome with this radius on the current player location");
        			player.sendMessage(ChatColor.AQUA + "* " + ChatColor.WHITE + "/"+label+" brush <mode> <biome> [radius] - Activate biome brush");
        			player.sendMessage(ChatColor.AQUA + "* " + ChatColor.WHITE + "/"+label+" brush off - Deactivate biome brush");
        			player.sendMessage(ChatColor.AQUA + "* " + ChatColor.WHITE + "/"+label+" info - Gives you informations about the biome you're currently standing in");
        			player.sendMessage(ChatColor.AQUA + "* " + ChatColor.WHITE + "/"+label+" list - Lists the servers' available biomes");

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
    
}

