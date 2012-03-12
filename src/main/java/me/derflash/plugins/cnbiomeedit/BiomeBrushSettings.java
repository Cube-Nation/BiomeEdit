package me.derflash.plugins.cnbiomeedit;

import org.bukkit.block.Biome;

public class BiomeBrushSettings {

	private int size;
	private Biome biome;
	private BiomeMode mode;

	public enum BiomeMode {
		ROUND(), SQUARE(), REPLACE()
	}

	// ###############
	
	
	public boolean setBiome(String biomeName) {
		Biome newBiome = BiomeBrushSettings.getBiomeFromString(biomeName);
		if (newBiome == null) {
			setBiome((Biome)null);
			return false;
		} else {
			setBiome(newBiome);
			return true;
			
		}
	}
	public boolean setBiome(Biome biome) {
		this.biome = biome;
		return true;
	}
	public Biome getBiome() {
		return biome;
	}

	
	public int getSize() {
		return size;
	}
	public boolean setSize(int size) {
		if (BiomeBrushSettings.isValidBiomeSize(size)) {
			this.size = size;
			return true;
			
		} else {
			return false;
			
		}
	}
	
	
	public BiomeMode getMode() {
		return mode;
	}
	public void setMode(BiomeMode mode) {
		this.mode = mode;
	}
	public boolean setMode(String _mode) {
		BiomeMode mode = BiomeBrushSettings.getModeFromString(_mode);
		if (mode == null) {
			setMode((BiomeMode)null);
			return false;
		} else {
			setMode(mode);
			return true;
		}
	}

	
	// ###############
	
	public static boolean isValidBiomeName(String biomeName) {
		return BiomeBrushSettings.getBiomeFromString(biomeName) != null;
	}
	public static Biome getBiomeFromString(String biomeName) {
		try {
			Biome newBiome = Biome.valueOf(biomeName.toUpperCase());
			return newBiome;
		} catch (Exception e) {}
		
		return null;
	}
	
	public static boolean isValidBiomeSize(int size) {
		if (size < 0 || size > 100) return false;
		else return true;
	}
	public static BiomeMode getModeFromString(String string) {
		try {
			BiomeMode bMode = BiomeMode.valueOf(string.toUpperCase());
			return bMode;
		} catch (Exception e) {}
		
		return null;
	}

}

