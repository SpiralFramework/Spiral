package org.abimon.mods.danganronpa.launcher;

public class DRFile {
	
	String name;
	long size = 0L;
	long offset = 0L;
	long originalOffset = 0L;
	
	public DRFile(String name, long size, long offset){
		this.name = name;
		this.size = size;
		this.offset = offset;
		this.originalOffset = offset;
	}
}
