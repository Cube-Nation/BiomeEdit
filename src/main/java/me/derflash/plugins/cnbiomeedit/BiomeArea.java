package me.derflash.plugins.cnbiomeedit;

import java.util.HashSet;

public class BiomeArea {

	private HashSet<int[]> points = null;
	private HashSet<int[]> outerPoints = null;
	
	public BiomeArea(HashSet<int[]> points, HashSet<int[]> outerPoints) {
		this.points = points;
		this.outerPoints = outerPoints;
	}
	
	public HashSet<int[]> getPoints() {
		return this.points;
	}
	
	public HashSet<int[]> getOuterPoints() {
		return this.outerPoints;
	}
	
}
