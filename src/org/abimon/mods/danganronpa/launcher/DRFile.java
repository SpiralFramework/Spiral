package org.abimon.mods.danganronpa.launcher;

public class DRFile {
	
	public String name;
	public long size = 0L;
	public long offset = 0L;
	public long originalOffset = 0L;
	
	public DRFile(String name, long size, long offset){
		this.name = name;
		this.size = size;
		this.offset = offset;
		this.originalOffset = offset;
	}
}
