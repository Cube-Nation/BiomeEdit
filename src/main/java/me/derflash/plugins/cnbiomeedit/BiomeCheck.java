package me.derflash.plugins.cnbiomeedit;

import java.util.HashSet;
import java.util.Stack;

public class BiomeCheck {
	
	public BiomeCheck() {
	}

	Stack<int[]> checkPoints = new Stack<int[]>();
	HashSet<int[]> foundPoints = new HashSet<int[]>();
	HashSet<int[]> outerPoints = new HashSet<int[]>();
	HashSet<String> checkedPoints = new HashSet<String>();
	
	int[] currentCheck;
	int[] locToCheck;
}
