package org.abimon.mods.danganronpa.launcher;

import java.util.Arrays;

public class ScriptEntry {
	byte opCode = 0;
	int[] args = new int[0];
	String text;
	
	public void setArgs(Integer[] args){
		this.args = new int[args.length];
		
		for(int i = 0; i < args.length; i++){
			this.args[i] = args[i];
		}
	}
	
	public String toString(){
		if(text != null)
			return "Text{" + text + "}";
		String name = DanganModding.Opcodes.get("DR1").containsKey(opCode) && DanganModding.Opcodes.get("DR1").get(opCode).getKey() != null ? DanganModding.Opcodes.get("DR1").get(opCode).getKey() : "0x" + Integer.toHexString(opCode).toUpperCase();
		String str = Arrays.toString(args);
		return name + "{" + str.substring(1, str.length() - 1) + "}";
	}
}
