package org.abimon.mods.danganronpa.launcher;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import org.abimon.omnis.io.Data;
import org.abimon.omnis.io.TGAReader;
import org.abimon.omnis.io.TGAWriter;
import org.abimon.omnis.io.ZipData;
import org.abimon.omnis.ludus.Ludus;

public class DanganModding {

	static{
		Ludus.registerDataPool(new File("resources"));
		Ludus.registerDataPool(DanganModding.class.getClassLoader());
	}

	public static LinkedList<DRFile> files = new LinkedList<DRFile>();

	public static void extract(File wad, File extractDir, PrintStream out) throws Throwable{
		files.clear();
		long start = System.currentTimeMillis();

		DataInputStream in = new DataInputStream(new FileInputStream(wad));

		boolean agar = readString(in, 4).equalsIgnoreCase("AGAR");
		long major = readInt(in);
		long minor = readInt(in);
		long header = readInt(in);

		in.skip(header);

		long files = readInt(in);

		out.println((agar ? "AbstractGames .WAD" : "UNKNOWN") + " v" + major + "." + minor + " with " + header + " bytes of header");

		out.println("Files: " + files);

		for(int i = 0; i < files; i++){
			long nameLen = readInt(in);
			String name = readString(in, (int) nameLen);
			long size = readLong(in);
			long offset = readLong(in);
			out.println("File: " + name + " (" + nameLen + " chars), " + size + "B and " + offset + "B from start");

			DanganModding.files.add(new DRFile(name, size, offset));
		}

		out.println("Now reading DIRs");

		long numDirs = readInt(in);

		out.println(numDirs + " directories");

		for(int i = 0; i < numDirs; i++){
			long nameLen = readInt(in);
			String name = readString(in, (int) nameLen);
			long numEntries = readInt(in);
			out.println("Dir: " + name + " (" + nameLen + " chars), " + numEntries + " entries");

			for(int j = 0; j < numEntries; j++){
				long entryNameLen = readInt(in);
				String entryName = readString(in, (int) entryNameLen);
				boolean file = in.read() == 0;

				out.println("\tEntry: " + entryName + " (" + entryNameLen + " chars), " + (file ? "is a file" : " is a directory"));
			}
		}

		out.println("Extracting All...");

		if(!extractDir.exists())
			extractDir.mkdir();

		for(int i = 0; i < DanganModding.files.size(); i++){
			try{
				DRFile drfile = DanganModding.files.get(i);

				out.println("Extracting " + drfile.name);

				byte[] data = new byte[(int) drfile.size];
				in.read(data);

				File dirs = new File(extractDir.getAbsolutePath() + File.separator + drfile.name.substring(0, drfile.name.length() - drfile.name.split("/")[drfile.name.split("/").length - 1].length()).replace("/", File.separator).replace("\\", File.separator));
				dirs.mkdirs();
				File output = new File(extractDir.getAbsolutePath() + File.separator + drfile.name.replace("/", File.separator).replace("\\", File.separator));


				if(drfile.name.endsWith("aglogo.tga")){
					File spiralLogo = new File("spirallogo.tga");
					if(spiralLogo.exists())
						data = new Data(spiralLogo).toArray();
				}

				if(drfile.name.endsWith(".lin")){
					Data linData = DanganModding.linHandling(new Data(data), out);
					linData.write(new File(output.getAbsolutePath() + ".txt"));
					linData = null;
				}
				else if(drfile.name.endsWith(".pak")){
					ZipData pakData = DanganModding.pakExtraction(new Data(data));
					pakData.writeToFile(new File(output.getAbsolutePath() + ".zip"));
					pakData = null;
				}
				else if(drfile.name.endsWith(".tga")){
					BufferedImage img = TGAReader.readImage(data);
					File file = new File(output.getAbsolutePath() + ".png");
					if(!file.exists())
						file.createNewFile();

					ImageIO.write(img, "PNG", file);
				}
				else{
					FileOutputStream fos = new FileOutputStream(output);
					fos.write(data);
					fos.close();
				}

				data = null;
			}
			catch(Throwable th){
				th.printStackTrace();
			}
		}

		long end = System.currentTimeMillis();

		long time = end-start;

		long minutes = time / 1000 / 60;
		long seconds = time / 1000 % 60;
		long millis = time % 1000;

		out.println("Took " + minutes + " minutes, " + seconds + " seconds and " + millis + " milliseconds.");
	}

	public static HashMap<String, HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>>> Opcodes = new HashMap<String, HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>>>();
	public static HashMap<String, Integer> characterIDs = new HashMap<String, Integer>();

	static
	{
		HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>> dr1 = new HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>>();
		HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>> dr2 = new HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>>();

		dr1.put((byte) 0x00, new AbstractMap.SimpleEntry<String, Integer>("TextCount", 2));
		dr1.put((byte) 0x01, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x02, new AbstractMap.SimpleEntry<String, Integer>("Text", 2));
		dr1.put((byte) 0x03, new AbstractMap.SimpleEntry<String, Integer>(null, 1));
		dr1.put((byte) 0x04, new AbstractMap.SimpleEntry<String, Integer>(null, 4));
		dr1.put((byte) 0x05, new AbstractMap.SimpleEntry<String, Integer>("Movie", 2));
		dr1.put((byte) 0x06, new AbstractMap.SimpleEntry<String, Integer>("Animation", 8));
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

		characterIDs.put("Makoto Naegi", 0);
		characterIDs.put("Kiyotaka Ishimaru", 1);
		characterIDs.put("Byakuya Togami", 2);
		characterIDs.put("Mondo Owada", 3);
		characterIDs.put("Leon Kuwata", 4);
		characterIDs.put("Hifumi Yamada", 5);
		characterIDs.put("Yasuhiro Hagakure", 6);
		characterIDs.put("Sayaka Maizono", 7);
		characterIDs.put("Kyoko Kirigiri", 8);
		characterIDs.put("Aoi Asahina", 9);
		characterIDs.put("Toko Fukawa", 10);
		characterIDs.put("Sakura Ogami", 11);
		characterIDs.put("Celeste", 12);
		characterIDs.put("Junko Enoshima", 13);
		characterIDs.put("Chihiro Fujisaki", 14);
		characterIDs.put("Monokuma", 15);
		characterIDs.put("Real Junko Enoshima", 16);
		characterIDs.put("Alter Ego", 17);
		characterIDs.put("Genocider Syo", 18);
		characterIDs.put("Jim Kirigiri", 19);
		characterIDs.put("Makoto's Mum", 20);
		characterIDs.put("Makoto's Dad", 21);
		characterIDs.put("Komaru Naegi", 22);
		characterIDs.put("Kiyondo Ishida", 23);
		characterIDs.put("Daiya Owada", 24);
		characterIDs.put("MN", 0);
		characterIDs.put("KI", 1);
		characterIDs.put("BT", 2);
		characterIDs.put("MO", 3);
		characterIDs.put("LK", 4);
		characterIDs.put("HY", 5);
		characterIDs.put("YH", 6);
		characterIDs.put("SM", 7);
		characterIDs.put("KK", 8);
		characterIDs.put("AA", 9);
		characterIDs.put("TF", 10);
		characterIDs.put("SO", 11);
		characterIDs.put("CL", 12);
		characterIDs.put("JE", 13);
		characterIDs.put("CF", 14);
		characterIDs.put("MK", 15);
		characterIDs.put("RJE", 16);
		characterIDs.put("AE", 17);
		characterIDs.put("GS", 18);
		characterIDs.put("JK", 19);
		characterIDs.put("MM", 20);
		characterIDs.put("MD", 21);
		characterIDs.put("KN", 22);
		characterIDs.put("DO", 24);
		characterIDs.put("???", 30);
		characterIDs.put("Narrator", 31);
	};

	public static void makeWad(File newWad, File wadDir, PrintStream pOut, boolean tmp) throws IOException{
		if(!wadDir.exists())
			throw new IOException("WAD Directory does not exist");
		files.clear();
		FileOutputStream out = new FileOutputStream(newWad);

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

		for(int i = 0; i < files.size(); i++){
			File f = files.get(i);
			if(f.getName().endsWith(".lin.txt")){
				pOut.println("Compiling " + f.getName());
				Data compiledLin = DanganModding.compileLin(new Data(f));
				File compiledFile = new File(f.getAbsolutePath().replace(".lin.txt", ".lin"));
				compiledLin.write(compiledFile);
				files.set(i, compiledFile);
			}
			if(f.getName().endsWith(".pak.zip")){
				pOut.println("Packing " + f.getName());
				ZipData zip = new ZipData(new Data(f));
				Data compiledPak = DanganModding.compilePak(zip);
				File compiledFile = new File(f.getAbsolutePath().replace(".pak.zip", ".pak"));
				compiledPak.write(compiledFile);
				files.set(i, compiledFile);
			}
			if(f.getName().endsWith(".tga.png")){
				pOut.println("Decrypting " + f.getName());
				File decrypted = new File(f.getAbsolutePath().replace(".tga.png", ".tga"));
				Data data = new Data(TGAWriter.writeImage(new Data(f).getAsImage()));
				data.write(decrypted);
				files.set(i, decrypted);
			}
		}

		long fileCount = files.size();

		pOut.println("Files: " + fileCount);

		writeInt(out, fileCount);

		long offset = 0;

		for(File f : files){
			String name = f.getAbsolutePath().replace(wadDir.getAbsolutePath() + File.separator, "").replace('\\', '/');
			FileInputStream in = new FileInputStream(f);
			int len = in.available();
			in.close();
			writeInt(out, name.length());
			out.write(name.getBytes());
			write(out, len, 8);
			write(out, offset, 8);
			offset += len;
		}

		pOut.println("Wrote: FileData");

		LinkedList<File> dirs = new LinkedList<File>();
		dirs.add(wadDir);
		dirs.addAll(iterateDirs(wadDir));

		writeInt(out, dirs.size());

		for(File dir : dirs){
			String name = dir.getAbsolutePath().replace(wadDir.getAbsolutePath() + File.separator, "").replace('\\', '/');
			writeInt(out, name.length());
			out.write(name.getBytes());
			LinkedList<File> sub = new LinkedList<File>();
			for(File f : dir.listFiles())
				if(!f.getName().startsWith("."))
					sub.add(f);
			writeInt(out, sub.size());

			for(File f : sub){
				String entryName = f.getName().replace('\\', '/');
				writeInt(out, entryName.length());
				out.write(entryName.getBytes());
				out.write(f.isFile() ? 0 : 1);
			}
		}

		pOut.println("Wrote: Directory Structure");

		for(File f : files){
			FileInputStream in = new FileInputStream(f);
			byte[] data = new byte[in.available()];
			in.read(data);
			in.close();

			if(tmp)
				f.delete();

			out.write(data);

			pOut.println("Wrote File: " + f);
		}

		if(!tmp){
			for(File f : files){
				if(f.getName().endsWith(".lin")){
					f.delete();
				}
				else if(f.getName().endsWith(".pak")){
					f.delete();
				}
				else if(f.getName().endsWith(".tga"))
					f.delete();
			}
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
	public static Data linHandling(File lin, PrintStream out) throws IOException{
		return linHandling(new Data(lin), out);
	}
	public static Data linHandling(Data lin, PrintStream out) throws IOException{
		DataInputStream din = new DataInputStream(lin.getAsInputStream());

		LinkedList<ScriptEntry> entries = new LinkedList<ScriptEntry>();

		long type = readIntNorm(din);
		long headerSpace = readIntNorm(din);
		if(type == 1){ //Not Text

		}
		long textBlock = 0;
		long size = 0;
		if(type == 2){ //Text
			textBlock = readIntNorm(din);
			size = readIntNorm(din);

			byte[] data = lin.toArray();

			for(int i = (int) headerSpace; i < textBlock; i++){
				if(data[i] == 0x70){
					i++;
					ScriptEntry entry = new ScriptEntry();
					entry.opCode = data[i];

					int argCount = Opcodes.get("DR1").containsKey(entry.opCode) ? Opcodes.get("DR1").get(entry.opCode).getValue() : -1;

					if(argCount == -1){
						LinkedList<Integer> args = new LinkedList<Integer>();
						while (data[i + 1] != 0x70)
						{
							args.add(data[i + 1] & 0xFF);
							i++;
						}
						entry.setArgs(args.toArray(new Integer[0]));
						entries.add(entry);
						continue;
					}
					else
					{
						entry.args = new int[argCount];
						for (int a = 0; a < entry.args.length; a++)
						{
							entry.args[a] = data[i + 1] & 0xFF;
							i++;
						}
						entries.add(entry);
					}
				}
				else
				{
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

		din.reset();
		din.skip(textBlock);
		int textEntries = (int) readInt(din);

		LinkedList<Integer> textIDs = new LinkedList<Integer>();
		for(int i = 0; i < entries.size(); i++)
		{
			if(entries.get(i).opCode == 0x02)
			{
				int first = entries.get(i).args[0];
				int second = entries.get(i).args[1];

				int textID = ((first << 8) | second);

				textIDs.add(textID);

				din.reset();
				din.skip(textBlock + (textID + 1) * 4);
				int textPos = (int) readInt(din);

				din.reset();
				din.skip(textBlock + (textID + 2) * 4);
				int nextTextPos = (int) readInt(din);
				if(textID == textEntries - 1)
					nextTextPos = (int) ((long) size - (long) textBlock);

				din.reset();
				din.skip(textBlock + textPos);
				entries.get(i).text = readString(din, nextTextPos - textPos, "UTF-16");
			}
			else
			{
				entries.get(i).text = null;
			}
		}

		din.close();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream sout = new PrintStream(baos);
		for(ScriptEntry entry : entries){
			sout.println(entry.toString());
		}
		sout.close();
		entries = null;
		lin = null;
		return new Data(baos.toByteArray());
	}

	public static Data compileLin(Data data){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream entries = new ByteArrayOutputStream();

		int textLine = 0;

		if(data.getAsString().toLowerCase().startsWith("spiral"))
			return compileSpiralLin(data);

		for(String s : data.getAsStringArray("\\}")){
			s = s.trim();
			if(s.equals(""))
				continue;

			entries.write(0x70);
			byte opCode = -1;
			for(byte code : Opcodes.get("DR1").keySet()){
				if(s.startsWith("0x" + Integer.toHexString(code).toUpperCase() + "{")){
					opCode = code;
					break;
				}
				else if(Opcodes.get("DR1").get(code).getKey() != null && s.startsWith(Opcodes.get("DR1").get(code).getKey())){
					opCode = code;
					break;
				}
			}

			if(opCode == -1){
				try{
					opCode = Byte.parseByte((s.indexOf('{') != -1 ? s.substring(2, s.indexOf('{')) : s), 16);
				}
				catch(Throwable th){}
			}
			entries.write(opCode);

			int args = Opcodes.get("DR1").containsKey(opCode) ? Opcodes.get("DR1").get(opCode).getValue() : -1;

			if(opCode != 0x02 && s.split("\\{").length > 1){
				String[] params = s.split("\\{")[1].split(",");
				for(int i = 0; i < params.length; i++){
					try{
						entries.write(Integer.parseInt(params[i].trim()));
					}
					catch(Throwable th){
						System.err.println(params[i]);
					}
				}
			}
			else if(opCode == 0x02){
				int arg0 = textLine / 256;
				int arg1 = textLine % 256;
				entries.write(arg0);
				entries.write(arg1);
				textLine++;
			}
		}

		//Text Type
		writeInt(out, 2);
		//Header Space
		writeInt(out, 16);
		//Text Block Location
		writeInt(out, 16+entries.size());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		//Begin writing entries
		baos.write(entries.toByteArray(), 0, entries.size());
		//Write Text Entries
		writeInt(baos, textLine);
		//Write Text
		ByteArrayOutputStream text = new ByteArrayOutputStream();

		int offset = (textLine+1) * 4;

		for(String s : data.getAsStringArray("\\}")){
			s = s.trim();
			if(s.equals(""))
				continue;
			if(s.startsWith("Text{")){
				try{
					String txt = s.substring(5).trim();
					byte[] txtDataStr = txt.getBytes("UTF-16LE");
					byte[] txtData = new byte[txtDataStr.length + 2];

					txtData[0] = (byte) 0xFF;
					txtData[1] = (byte) 0xFE;

					for(int i = 0; i < txtDataStr.length; i++)
						txtData[i+2] = txtDataStr[i];

					writeInt(baos, offset);
					text.write(txtData);
					text.write(0);
					text.write(0);
					offset += txtData.length+2;
				}
				catch(Throwable th){}
			}
		}

		//Write Text
		baos.write(text.toByteArray(), 0, text.size());

		//Size
		writeInt(out, 16+baos.size());
		//Rest of The File
		out.write(baos.toByteArray(), 0, baos.size());

		return new Data(out.toByteArray());
	}

	private static Data compileSpiralLin(Data data) {
		String newLin = "";

		int textCount = 0;
		for(String s : data.getAsStringArray("\n")){
			s = s.trim();
			if(s.equalsIgnoreCase("SPIRAL"))
				continue;
			if(s.startsWith("0x"))
				newLin += s;
			else if(s.startsWith("[Sprite:")){
				s = s.replace("[", "").replace("]", "");
				String person = s.split(":", 3)[1].trim();
				String spriteNum = s.split(":", 3)[2].trim();

				int charID = characterIDs.containsKey(person.trim()) ? characterIDs.get(person.trim()) : characterIDs.get("???");

				newLin += "Sprite{0, " + charID + ", " + spriteNum + ", 1, 2}";
			}
			else if(s.matches(".*\\:.*")){
				String person = s.split(":", 2)[0];
				String text = s.split(":", 2)[1].replace("<br>", "\n");

				boolean self = person.contains("(To Self)");
				if(self){
					person = person.replace("(To Self)", "");
					text = "<CLT 4>" + text;
					text = text.replace("<bold>", "<CLT><CLT 3>").replace("</bold>", "<CLT><CLT4>");
					text = text + "<CLT>";
				}

				int charID = characterIDs.containsKey(person.trim()) ? characterIDs.get(person.trim()) : characterIDs.get("???");

				newLin += "Speaker{" + charID + "}\n" + "Text{" + text + "}\n";
				newLin += "WaitFrame{}\n0x3{0}\nWaitInput{}\n0x3{4}";
				textCount++;
			}
			else{
				for(byte code : Opcodes.get("DR1").keySet()){
					if(Opcodes.get("DR1").get(code).getKey() != null && s.startsWith(Opcodes.get("DR1").get(code).getKey())){
						newLin += s;
						break;
					}
				}
			}

			newLin += "\n";
		}

		newLin = "0x0{" + textCount % 256 + "," + textCount / 256 + "}\n" + newLin;

		System.out.println(newLin);

		return compileLin(new Data(newLin));
	}

	public static ZipData pakExtraction(File pak) throws IOException {
		return pakExtraction(new Data(pak));
	}

	public static ZipData pakExtraction(Data pak) throws IOException {

		byte[] data = pak.toArray();

		ByteArrayInputStream in = new ByteArrayInputStream(data);

		long numFiles = readInt(in);

		long[] offsets = new long[(int) numFiles + 1];

		for(int i = 0; i < numFiles; i++){
			offsets[i] = readInt(in);
		}
		offsets[(int) numFiles] = pak.size();

		ZipData zipData = new ZipData();

		for(int i = 0; i < numFiles; i++){
			byte[] tmpData = Arrays.copyOfRange(data, (int) offsets[i], (int) offsets[i+1]);

			String name = Integer.toString(i);

			boolean alreadyParsed = false;

			//Check if it's a string
			if(((tmpData[0] == -1 && tmpData[1] == -2) || (tmpData[0] == -2 && tmpData[1] == -1)) && !alreadyParsed){
				name += ".txt";
				alreadyParsed = true;
			}
			else{
				//Try reading as tga
				try{
					if(!alreadyParsed){
						BufferedImage img = TGAReader.readImage(tmpData);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ImageIO.write(img, "PNG", baos);
						tmpData = baos.toByteArray();
						name += ".png";
						alreadyParsed = true;
					}
				}
				catch(Throwable e){}
				try{
					if(!alreadyParsed){
						ByteArrayInputStream bin = new ByteArrayInputStream(tmpData);
						int files = (int) readInt(bin);
						int off1 = (int) readInt(bin);
						int off2 = (int) readInt(bin);
						
						if(off2 - off1 < tmpData.length && files < 1024 && files > 1){ //Generic Support
							ByteArrayOutputStream baos = new ByteArrayOutputStream();

							ZipData zip = DanganModding.pakExtraction(new Data(tmpData));
							zip.writeTo(baos);
							tmpData = baos.toByteArray();
							name += ".pak.zip";

							baos.close();

							alreadyParsed = true;
						}
					}					
				}
				catch(Throwable e){}
			}

			zipData.put(name, new Data(tmpData));
		}

		in.close();
		return zipData;
	}

	public static Data compilePak(ZipData zipPak){
		ByteArrayOutputStream out = new ByteArrayOutputStream();


		String[] keys = zipPak.keySet().toArray(new String[0]);

		for(String s : keys){
			try{
				if(s.startsWith(".") || s.startsWith("__") || s.contains("MACOSX"))
					zipPak.remove(s);
				else if(s.endsWith(".png")){
					zipPak.put(s.replace(".png", ""), new Data(TGAWriter.writeImage(zipPak.get(s).getAsImage())));
					zipPak.remove(s);
				}
				else if(s.endsWith(".pak.zip")){
					
				}
				else if(s.endsWith(".txt")){
					zipPak.put(s.replace(".txt", ""), zipPak.get(s));
					zipPak.remove(s);
				}
				else if(s.matches("\\d*\\..*")){
					zipPak.put(s.split("\\.")[0], zipPak.get(s));
				}
				else if(s.matches(".*\\D+.*")){
					zipPak.remove(s);
				}
			}
			catch(Throwable th){}
		}

		writeInt(out, zipPak.size());

		int dataOffset = 4 + (zipPak.size() * 4);
		int offset = dataOffset;

		for(int i = 0; i < zipPak.entrySet().size(); i++){
			String s = Integer.toString(i);

			if(!zipPak.containsKey(s)){
				break;
			}

			writeInt(out, offset);
			offset += zipPak.get(s).length();
		}

		for(int i = 0; i < zipPak.entrySet().size(); i++){
			try {
				String s = Integer.toString(i);

				if(!zipPak.containsKey(s))
					break;

				byte[] data = zipPak.get(s).toArray();
				out.write(data);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return new Data(out.toByteArray());
	}

	public static String readString(InputStream in, int len) throws IOException{
		byte[] data = new byte[Math.max(0, len)];
		in.read(data);
		String s = new String(data);
		data = null;
		return s;
	}

	public static String readString(InputStream in, int len, String encoding) throws IOException{
		byte[] data = new byte[Math.min(Math.max(0, len), 8192)];
		in.read(data);
		String s = new String(data, encoding);
		data = null;
		return s;
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

	public static long readInt(InputStream in, boolean unsigned) throws IOException{
		return read(in, 4, unsigned);
	}

	public static long readLong(InputStream in, boolean unsigned) throws IOException{
		return read(in, 8);
	}

	public static long read(InputStream in, int len, boolean unsigned){
		String s = "0";

		try{
			String[] bin = new String[len];
			for(int i = 0; i < len; i++)
				bin[i] = Long.toBinaryString(unsigned ? (in.read() & 0xffffffffl) : in.read());

			String base = "00000000";
			for(int i = len-1; i >= 0; i--)
				s += base.substring(bin[i].length()) + bin[i];
		}
		catch(Throwable th){}

		return Long.parseLong(s, 2);
	}

	public static long readIntNorm(InputStream in) throws IOException{
		return read(in, 4);
	}

	public static long readLongNorm(InputStream in) throws IOException{
		return read(in, 8);
	}

	public static long readNorm(InputStream in, int len){
		String s = "0";

		try{
			for(int i = 0; i < len; i++)
				s += Long.toBinaryString(in.read() & 0xffffffffl);
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
