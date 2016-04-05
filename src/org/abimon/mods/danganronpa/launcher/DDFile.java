package org.abimon.mods.danganronpa.launcher;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.abimon.omnis.io.Data;
import org.abimon.omnis.io.VirtualDirectory;
import org.abimon.omnis.io.VirtualFile;

public class DDFile {

	HashMap<String, DRFile> fileStructure = new HashMap<String, DRFile>();
	VirtualDirectory directory = new VirtualDirectory("");

	boolean agar = false;
	long major = 0;
	long minor = 0;
	long header = 0;
	long readOffset = 12;
	
	File wadFile;

	public DDFile(File wadFile) throws IOException{
		long start = System.currentTimeMillis();

		this.wadFile = wadFile;
		DataInputStream in = new DataInputStream(new FileInputStream(wadFile));

		agar = readString(in, 4).equalsIgnoreCase("AGAR");
		major = readInt(in);
		minor = readInt(in);
		header = readInt(in);
		
		readOffset = 20 + header;
		
		in.skip(header);

		long files = readInt(in);

		for(int i = 0; i < files; i++){
			long nameLen = readInt(in);
			String name = readString(in, (int) nameLen);
			long size = readLong(in);
			long offset = readLong(in);

			fileStructure.put(name, new DRFile(name, size, offset));
			readOffset += 4 + nameLen + 8 + 8;
		}

		System.out.println("Now reading DIRs");

		long numDirs = readInt(in);

		System.out.println(numDirs + " directories");

		long nameLen = readInt(in);
		String name = readString(in, (int) nameLen);
		long numEntries = readInt(in);
		readOffset += 12 + nameLen;

		for(int j = 0; j < numEntries; j++){
			long entryNameLen = readInt(in);
			String entryName = readString(in, (int) entryNameLen);
			boolean file = in.read() == 0;

			directory.addSubFile(file ? new VirtualFile(entryName) : new VirtualDirectory(entryName));
			readOffset += 4 + entryNameLen + 1;
		}

		for(int i = 1; i < numDirs; i++){
			nameLen = readInt(in);
			name = readString(in, (int) nameLen);
			numEntries = readInt(in);
			readOffset += 4 + nameLen + 4;

			VirtualFile search = this.directory.search("/" + name);
			VirtualDirectory vDir = search instanceof VirtualDirectory && search != null ? (VirtualDirectory) search : new VirtualDirectory(name);

			for(int j = 0; j < numEntries; j++){
				long entryNameLen = readInt(in);
				String entryName = readString(in, (int) entryNameLen);
				boolean file = in.read() == 0;

				readOffset += 4 + entryNameLen + 1;
				vDir.addSubFile(file ? new VirtualFile(entryName) : new VirtualDirectory(entryName));
			}

			if(vDir.getParent() != null){
				vDir.getParent().removeSubFile(vDir.getName());
				vDir.getParent().addSubFile(vDir);
			}
			else
				System.out.println(vDir + " is missing a parent!");
		}
		
		System.out.println(readOffset + " and chill");

		long end = System.currentTimeMillis();

		long time = end-start;

		long minutes = time / 1000 / 60;
		long seconds = time / 1000 % 60;
		long millis = time % 1000;

		System.out.println("Took " + minutes + " minutes, " + seconds + " seconds and " + millis + " milliseconds.");
	}

	public void changeFile(String name, String fullPath, long length){
		DRFile file = new DRFile(name, length, -1);
		this.fileStructure.put(name, file);
		this.directory.addSubFileWithPath(new VirtualFile(name), fullPath);
	}

	public void write(File newWad, ZipFile... modsToAdd) throws IOException{

		File backupWadFile = new File(wadFile.getAbsolutePath().replace(".wad", ".wad.backup"));
		if(!backupWadFile.exists()){
			System.out.println("Making a backup...");

			FileInputStream wadIn = new FileInputStream(wadFile);
			FileOutputStream backupWadOut = new FileOutputStream(backupWadFile);

			long filesize = wadIn.available();
			long written = 0;

			byte[] buffer = new byte[8192];
			while(true){
				int read = wadIn.read(buffer);
				if(read <= 0)
					break;
				backupWadOut.write(buffer, 0, read);
				written += read;
				System.out.println(written + "/~" + filesize);
			}

			wadIn.close();
			backupWadOut.close();
		}

		FileOutputStream out = new FileOutputStream(newWad);

		out.write(agar ? "AGAR".getBytes() : "UNKN".getBytes());

		writeInt(out, major);
		writeInt(out, minor);
		writeInt(out, header);

		byte[] headerData = new byte[(int) header];
		new Random().nextBytes(headerData);
		out.write(headerData);

		LinkedList<VirtualFile> files = directory.getAllSubFiles();

		@SuppressWarnings("unchecked")
		LinkedList<VirtualFile> copy = (LinkedList<VirtualFile>) files.clone();


		for(VirtualFile file : copy){
			String name = file.toString().substring(1);
			DRFile drf = fileStructure.get(name);
			if(drf == null || file instanceof VirtualDirectory)
				files.remove(file);
		}

		long fileCount = files.size();

		System.out.println("Files: " + fileCount);

		writeInt(out, fileCount);

		long offset = 0;

		for(VirtualFile f : files){
			String name = f.toString().substring(1);
			DRFile drf = fileStructure.get(name);
			if(drf == null || f instanceof VirtualDirectory)
				continue;
			writeInt(out, name.length());
			out.write(name.getBytes());
			write(out, drf.size, 8);
			write(out, offset, 8);
			offset += drf.size;
		}

		System.out.println("Wrote: FileData");

		LinkedList<VirtualDirectory> dirs = directory.getAllSubDirs();
		
		System.out.println(dirs.size());
		System.out.println(dirs);

		writeInt(out, dirs.size());

		for(VirtualDirectory dir : dirs){
			String name = dir.toString().substring(Math.min(dir.toString().length(), 1));
			writeInt(out, name.length());
			out.write(name.getBytes());
			LinkedList<VirtualFile> sub = dir.subfiles;
			writeInt(out, sub.size());

			for(VirtualFile f : sub){
				String entryName = f.getName();
				writeInt(out, entryName.length());
				out.write(entryName.getBytes());
				out.write(f instanceof VirtualDirectory ? 1 : 0);
			}
		}

		System.out.println("Wrote: Directory Structure");

		HashMap<String, ZipFile> patches = new HashMap<String, ZipFile>();

		for(ZipFile f : modsToAdd){
			if(f == null)
				continue;
			Enumeration<? extends ZipEntry> entries = f.entries();
			ZipEntry entry = null;

			while(entries.hasMoreElements() && (entry = entries.nextElement()) != null){
				String name = entry.getName();

				if(!name.endsWith(".info"))
					patches.put(name, f);
			}
		}

		System.out.println("Writing: Files");

		for(VirtualFile f : files){
			String name = f.toString().substring(1);

			DRFile file = fileStructure.get(name);
			if(file == null)
				continue;
			InputStream in = null;

			if(patches.containsKey(name)){
				in = patches.get(name).getInputStream(new ZipEntry(name));

				System.out.println("Patching through " + name + " which is " + file.size + " bytes");
			}
			else //Vanilla File, or no patch is present
			{
				in = new FileInputStream(backupWadFile);
				in.skip(readOffset);
				in.skip(file.originalOffset);
			}

			byte[] data = new byte[(int) file.size];
			in.read(data);
			out.write(data, 0, (int) file.size);

			in.close();
		}

		out.close();
	}

	/** Note: Do NOT use this UNLESS you're confident you're not patching much data in */
	public void write(File newWad, HashMap<String, Data> dataToPatch){

	}

	public byte[] read(String name) throws IOException{

		if(fileStructure.containsKey(name)){
			DRFile file = fileStructure.get(name);

			FileInputStream in = new FileInputStream(wadFile);

			in.skip(file.offset);

			byte[] data = new byte[(int) file.size];
			in.read(data);
			in.close();

			return data;
		}

		return new byte[0];
	}

	public static String readString(InputStream in, int len) throws IOException{
		byte[] data = new byte[len];
		in.read(data);

		return new String(data);
	}

	public static String readString(InputStream in, int len, String encoding) throws IOException{
		ByteBuffer buf = ByteBuffer.allocate(len);
		for(int i = 0; i < len; i++)
			buf.put((byte) in.read());
		return Charset.forName(encoding).decode(buf).toString();
	}

	public static long readInt(InputStream in) throws IOException{
		return read(in, 4);
	}

	public static long readLong(InputStream in) throws IOException{
		return read(in, 8);
	}

	public static long read(InputStream in, int len){
		String s = "0";

		try{
			String[] bin = new String[len];
			for(int i = 0; i < len; i++)
				bin[i] = Long.toBinaryString(in.read() & 0xffffffffl);

			String base = "00000000";
			for(int i = len-1; i >= 0; i--)
				s += base.substring(bin[i].length()) + bin[i];
		}
		catch(Throwable th){}

		return Long.parseLong(s, 2);
	}

	public static void writeInt(OutputStream out, long num){
		write(out, num, 4);
	}

	public static void write(OutputStream out, long num, int len){
		try{
			String ss = Long.toBinaryString(num);
			String base = "";
			for(int i = 0; i < len; i++)
				base += "00000000";
			String nums = base.substring(ss.length()) + ss;

			for(int i = len-1; i >= 0; i--){
				out.write(Integer.parseInt(nums.substring(i * 8, (i+1) * 8), 2));
			}
		}
		catch(Throwable th){}
	}
}
