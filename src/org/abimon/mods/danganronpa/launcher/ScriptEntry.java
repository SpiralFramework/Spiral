package org.abimon.mods.danganronpa.launcher;

import java.util.Arrays;

public class ScriptEntry {
	byte opCode = 0;
	byte[] args = new byte[0];
	String text;
	
	public void setArgs(Byte[] args){
		this.args = new byte[args.length];
		
		for(int i = 0; i < args.length; i++){
			this.args[i] = args[i];
		}
	}
	
	public String toString(){
		if(text != null)
			return "Text[" + text.replace((char) 0 + "", "") + "]";
		String name = DanganModding.Opcodes.get("DR1").containsKey(opCode) && DanganModding.Opcodes.get("DR1").get(opCode).getKey() != null ? DanganModding.Opcodes.get("DR1").get(opCode).getKey() : "0x" + Integer.toHexString(opCode).toUpperCase();
		return name + Arrays.toString(args);
	}
}
