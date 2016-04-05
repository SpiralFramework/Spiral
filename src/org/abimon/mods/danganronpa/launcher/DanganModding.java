package org.abimon.mods.danganronpa.launcher;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class DanganModding {

	public static LinkedList<DRFile> files = new LinkedList<DRFile>();

	public static void extract(File wad) throws Throwable{
		long start = System.currentTimeMillis();

		DataInputStream in = new DataInputStream(new FileInputStream(wad));
		PrintStream out = new PrintStream(new File("Files.txt"));

		boolean agar = readString(in, 4).equalsIgnoreCase("AGAR");
		long major = readInt(in);
		long minor = readInt(in);
		long header = readInt(in);

		in.skip(header);

		long files = readInt(in);

		System.out.println((agar ? "AbstractGames .WAD" : "UNKNOWN") + " v" + major + "." + minor + " with " + header + " bytes of header");
		System.out.println("Files: " + files);
		
		out.println("Files: " + files);
		
		for(int i = 0; i < files; i++){
			long nameLen = readInt(in);
			String name = readString(in, (int) nameLen);
			long size = readLong(in);
			long offset = readLong(in);
			System.out.println("File: " + name + " (" + nameLen + " chars), " + size + "B and " + offset + "B from start");
			out.println("File: " + name + " (" + nameLen + " chars), " + size + "B and " + offset + "B from start");

			DanganModding.files.add(new DRFile(name, size, offset));
		}

		System.out.println("Now reading DIRs");

		long numDirs = readInt(in);

		System.out.println(numDirs + " directories");
		out.println(numDirs + " directories");

		for(int i = 0; i < numDirs; i++){
			long nameLen = readInt(in);
			String name = readString(in, (int) nameLen);
			long numEntries = readInt(in);
			System.out.println("Dir: " + name + " (" + nameLen + " chars), " + numEntries + " entries");
			out.println("Dir: " + name + " (" + nameLen + " chars), " + numEntries + " entries");

			for(int j = 0; j < numEntries; j++){
				long entryNameLen = readInt(in);
				String entryName = readString(in, (int) entryNameLen);
				boolean file = in.read() == 0;

				System.out.println("\tEntry: " + entryName + " (" + entryNameLen + " chars), " + (file ? "is a file" : " is a directory"));
				out.println("\tEntry: " + entryName + " (" + entryNameLen + " chars), " + (file ? "is a file" : " is a directory"));
			}
		}

		System.out.println("Extracting All...");

		for(int i = 0; i < DanganModding.files.size(); i++){
			DRFile drfile = DanganModding.files.get(i);

			byte[] data = new byte[(int) drfile.size];
			in.read(data);

			File dirs = new File("Danganronpa Extract" + File.separator + drfile.name.substring(0, drfile.name.length() - drfile.name.split("/")[drfile.name.split("/").length - 1].length()));
			dirs.mkdirs();
			File output = new File("Danganronpa Extract" + File.separator + drfile.name);

			FileOutputStream fos = new FileOutputStream(output);
			fos.write(data);
			fos.close();
		}

		long end = System.currentTimeMillis();

		long time = end-start;

		long minutes = time / 1000 / 60;
		long seconds = time / 1000 % 60;
		long millis = time % 1000;

		System.out.println("Took " + minutes + " minutes, " + seconds + " seconds and " + millis + " milliseconds.");
	}

	public static HashMap<String, HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>>> Opcodes = new HashMap<String, HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>>>();

	static
	{
		HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>> dr1 = new HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>>();
		HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>> dr2 = new HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>>();

		dr1.put((byte) 0x00, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr1.put((byte) 0x01, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x02, new AbstractMap.SimpleEntry<String, Integer>("Text", 2));
		dr1.put((byte) 0x03, new AbstractMap.SimpleEntry<String, Integer>(null, 1));
		dr1.put((byte) 0x04, new AbstractMap.SimpleEntry<String, Integer>(null, 4));
		dr1.put((byte) 0x05, new AbstractMap.SimpleEntry<String, Integer>("Movie", 2));
		dr1.put((byte) 0x06, new AbstractMap.SimpleEntry<String, Integer>(null, 8));
		dr1.put((byte) 0x08, new AbstractMap.SimpleEntry<String, Integer>("Voice", 5));
		dr1.put((byte) 0x09, new AbstractMap.SimpleEntry<String, Integer>("Music", 3));
		dr1.put((byte) 0x0A, new AbstractMap.SimpleEntry<String, Integer>("Sound", 3));
		dr1.put((byte) 0x0B, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr1.put((byte) 0x0C, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr1.put((byte) 0x0D, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x0E, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr1.put((byte) 0x0F, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x10, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x11, new AbstractMap.SimpleEntry<String, Integer>(null, 4));
		dr1.put((byte) 0x14, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x15, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x19, new AbstractMap.SimpleEntry<String, Integer>("LoadScript", 3));
		dr1.put((byte) 0x1A, new AbstractMap.SimpleEntry<String, Integer>("ExecuteScript", 0));
		dr1.put((byte) 0x1B, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x1C, new AbstractMap.SimpleEntry<String, Integer>(null, 0));
		dr1.put((byte) 0x1E, new AbstractMap.SimpleEntry<String, Integer>("Sprite", 5));
		dr1.put((byte) 0x1F, new AbstractMap.SimpleEntry<String, Integer>(null, 7));
		dr1.put((byte) 0x20, new AbstractMap.SimpleEntry<String, Integer>(null, 5));
		dr1.put((byte) 0x21, new AbstractMap.SimpleEntry<String, Integer>("Speaker", 1));
		dr1.put((byte) 0x22, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x23, new AbstractMap.SimpleEntry<String, Integer>(null, 5));
		dr1.put((byte) 0x25, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr1.put((byte) 0x26, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x27, new AbstractMap.SimpleEntry<String, Integer>(null, 1));
		dr1.put((byte) 0x29, new AbstractMap.SimpleEntry<String, Integer>(null, 1));
		dr1.put((byte) 0x2A, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr1.put((byte) 0x2B, new AbstractMap.SimpleEntry<String, Integer>(null, 1));
		dr1.put((byte) 0x2C, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr1.put((byte) 0x2E, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr1.put((byte) 0x2F, new AbstractMap.SimpleEntry<String, Integer>(null, 10));
		dr1.put((byte) 0x30, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x32, new AbstractMap.SimpleEntry<String, Integer>(null, 1));
		dr1.put((byte) 0x33, new AbstractMap.SimpleEntry<String, Integer>(null, 4));
		dr1.put((byte) 0x34, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr1.put((byte) 0x38, new AbstractMap.SimpleEntry<String, Integer>(null, -1));
		dr1.put((byte) 0x39, new AbstractMap.SimpleEntry<String, Integer>(null, 5));
		dr1.put((byte) 0x3A, new AbstractMap.SimpleEntry<String, Integer>("WaitInput", 0));
		dr1.put((byte) 0x3B, new AbstractMap.SimpleEntry<String, Integer>("WaitFrame", 0));
		dr1.put((byte) 0x3C, new AbstractMap.SimpleEntry<String, Integer>(null, 0));
		dr1.put((byte) 0x4B, new AbstractMap.SimpleEntry<String, Integer>("WaitInputDR2", -1));
		dr1.put((byte) 0x4C, new AbstractMap.SimpleEntry<String, Integer>("WaitFrameDR2", 0));
		dr1.put((byte) 0x4D, new AbstractMap.SimpleEntry<String, Integer>(null, -1));

		dr2.put((byte) 0x01, new AbstractMap.SimpleEntry<String, Integer>(null, 4));
		dr2.put((byte) 0x14, new AbstractMap.SimpleEntry<String, Integer>(null, 6));
		dr2.put((byte) 0x15, new AbstractMap.SimpleEntry<String, Integer>(null, 4));
		dr2.put((byte) 0x19, new AbstractMap.SimpleEntry<String, Integer>("LoadScript", 5));
		dr2.put((byte) 0x1B, new AbstractMap.SimpleEntry<String, Integer>(null, 5));
		dr2.put((byte) 0x29, new AbstractMap.SimpleEntry<String, Integer>(null, 0xD));
		dr2.put((byte) 0x2A, new AbstractMap.SimpleEntry<String, Integer>(null, 0xC));
		dr2.put((byte) 0x2E, new AbstractMap.SimpleEntry<String, Integer>(null, 5));
		dr2.put((byte) 0x30, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr2.put((byte) 0x34, new AbstractMap.SimpleEntry<String, Integer>(null, 1));
		dr2.put((byte) 0x3A, new AbstractMap.SimpleEntry<String, Integer>("WaitInputDR1", 4));
		dr2.put((byte) 0x3B, new AbstractMap.SimpleEntry<String, Integer>("WaitFrameDR1", 2));
		dr2.put((byte) 0x4B, new AbstractMap.SimpleEntry<String, Integer>("WaitInput", -1));
		dr2.put((byte) 0x4C, new AbstractMap.SimpleEntry<String, Integer>("WaitFrame", 0));

		Opcodes.put("DR1", dr1);
		Opcodes.put("DR2", dr2);
	};
	
	public static void makeWad(File wadDir) throws IOException{
		if(!wadDir.exists())
			throw new IOException("WAD Directory does not exist");

		FileOutputStream out = new FileOutputStream("dr1_data.wad");

		out.write("AGAR".getBytes());
		int major = 1;
		int minor = 1;
		int header = 0;

		writeInt(out, major);
		writeInt(out, minor);
		writeInt(out, header);

		byte[] headerData = new byte[header];
		new Random().nextBytes(headerData);
		out.write(headerData);

		LinkedList<File> files = iterate(wadDir, false);

		long fileCount = files.size();

		System.out.println("Files: " + fileCount);

		writeInt(out, fileCount);

		long offset = 0;

		for(File f : files){
			String name = f.getAbsolutePath().replace(wadDir.getAbsolutePath() + File.separator, "");
			FileInputStream in = new FileInputStream(f);
			int len = in.available();
			in.close();
			writeInt(out, name.length());
			out.write(name.getBytes());
			write(out, len, 8);
			write(out, offset, 8);
			offset += len;
		}

		System.out.println("Wrote: FileData");

		LinkedList<File> dirs = new LinkedList<File>();
		dirs.add(wadDir);
		dirs.addAll(iterateDirs(wadDir));

		writeInt(out, dirs.size());

		for(File dir : dirs){
			String name = dir.getAbsolutePath().replace(wadDir.getAbsolutePath() + File.separator, "");
			writeInt(out, name.length());
			out.write(name.getBytes());
			LinkedList<File> sub = new LinkedList<File>();
			for(File f : dir.listFiles())
				if(!f.getName().startsWith("."))
					sub.add(f);
			writeInt(out, sub.size());

			for(File f : sub){
				String entryName = f.getName();
				writeInt(out, entryName.length());
				out.write(entryName.getBytes());
				out.write(f.isFile() ? 0 : 1);
				
			}
		}

		System.out.println("Wrote: Directory Structure");

		for(File f : files){
			FileInputStream in = new FileInputStream(f);
			byte[] data = new byte[in.available()];
			in.read(data);
			in.close();

			out.write(data);

			System.out.println("Wrote File: " + f);
		}

		out.close();
	}

	public static LinkedList<File> iterate(File dir, boolean addDirs){
		LinkedList<File> files = new LinkedList<File>();

		for(File f : dir.listFiles())
			if(f.getName().startsWith("."));
			else
				if(f.isDirectory()){
					files.addAll(iterate(f, addDirs));
					if(addDirs)
						files.add(f);
				}
				else
					files.add(f);

		return files;
	}

	public static LinkedList<File> iterateDirs(File dir){
		LinkedList<File> files = new LinkedList<File>();

		for(File f : dir.listFiles()){
			if(f.getName().startsWith("."));
			else
				if(f.isDirectory()){
					files.add(f);
					files.addAll(iterateDirs(f));
				}
		}

		return files;
	}

	public static void linHandling() throws IOException{
		File sampleLin = new File("SampleLin.lin");

		DataInputStream din = new DataInputStream(new FileInputStream(sampleLin));

		LinkedList<ScriptEntry> entries = new LinkedList<ScriptEntry>();

		long type = readInt(din);
		long headerSpace = readInt(din);
		if(type == 1){ //Not Text

		}
		long textBlock = 0;
		long size = 0;
		if(type == 2){ //Text
			textBlock = readInt(din);
			size = readInt(din);

			System.out.println("TextBlock: " + textBlock + ", with size " + size);

			DataInputStream in = new DataInputStream(new FileInputStream(sampleLin));

			byte[] data = new byte[in.available()];
			in.read(data);

			in.close();

			for(int i = (int) headerSpace; i < textBlock; i++){
				if(data[i] == 0x70){
					i++;
					ScriptEntry entry = new ScriptEntry();
					entry.opCode = data[i];
					System.out.println("OpCode: 0x" + Integer.toHexString(entry.opCode));

					int argCount = Opcodes.get("DR1").containsKey(entry.opCode) ? Opcodes.get("DR1").get(entry.opCode).getValue() : -1;

					if(argCount == -1){
						LinkedList<Byte> args = new LinkedList<Byte>();
						while (data[i + 1] != 0x70)
						{
							args.add(data[i + 1]);
							i++;
						}
						entry.setArgs(args.toArray(new Byte[0]));
						entries.add(entry);
						continue;
					}
					else
					{
						entry.args = new byte[argCount];
						for (int a = 0; a < entry.args.length; a++)
						{
							entry.args[a] = data[i + 1];
							i++;
						}
						entries.add(entry);
					}
				}
				else
				{
					System.err.println("Got: " + data[i]);
					// EOF?
					while (i < textBlock)
					{
						if (data[i] != 0x00)
						{
							System.err.println("[read] error: expected 0x70, got 0x" + data[i] + ".");
						}
						i++;
					}
					break;
				}
			}
		}

		int textEntries = (int) readInt(din);
		
		DataInputStream in = new DataInputStream(new FileInputStream(sampleLin));

		byte[] data = new byte[in.available()];
		in.readFully(data);
		in.close();
		
		ByteArrayInputStream bin = new ByteArrayInputStream(data);

		LinkedList<Integer> textIDs = new LinkedList<Integer>();
		for(int i = 0; i < entries.size(); i++)
		{
			if(entries.get(i).opCode == 0x02)
			{
				byte first = entries.get(i).args[0];
				byte second = entries.get(i).args[1];
				int textID = first << 8 | second;

				textIDs.add(textID);
				
				bin.reset();
				bin.skip(textBlock + (textID + 1) * 4);
				int textPos = (int) readInt(bin);
				
				bin.reset();
				bin.skip(textBlock + (textID + 2) * 4);
				int nextTextPos = (int) readInt(bin);
				if(textID == textEntries - 1)
					nextTextPos = (int) (size - textBlock);
				
				bin.reset();
				bin.skip(textBlock + textPos);
				entries.get(i).text = readString(bin, nextTextPos - textPos);
			}
			else
			{
				entries.get(i).text = null;
			}
		}

		din.close();
		
		PrintStream out = new PrintStream("SampleLin.txt");
		for(ScriptEntry entry : entries){
			out.println(entry);
		}
		out.close();
	}

	public static void pakExtraction() throws IOException {
		File samplePak = new File("SamplePak.pak");

		DataInputStream din = new DataInputStream(new FileInputStream(samplePak));

		byte[] ddata = new byte[din.available()];
		din.readFully(ddata);
		din.close();

		ByteArrayInputStream in = new ByteArrayInputStream(ddata);

		long numFiles = readInt(in);

		long[] offsets = new long[(int) numFiles + 1];

		for(int i = 0; i < numFiles; i++){
			offsets[i] = readInt(in);
		}
		offsets[(int) numFiles] = samplePak.length();

		File dir = new File("SamplePak");
		dir.mkdir();

		for(int i = 0; i < numFiles; i++){
			File f = new File(dir.getAbsolutePath() + File.separator + i);
			if(!f.exists())
				f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			byte[] data = Arrays.copyOfRange(ddata, (int) offsets[i], (int) offsets[i+1]);

			System.out.println(new String(data) + "\n" + Arrays.toString(data));
			fos.write(data);
			fos.close();
		}

		in.close();
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
		System.out.println(buf);
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
