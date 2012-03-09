package me.derflash.plugins.cnbiomeedit;

import org.bukkit.block.Biome;

public class BiomeBrushSettings {

	private int size;
	private Biome biome;
	
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
}
