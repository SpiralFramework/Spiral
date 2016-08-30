package org.abimon.mods.danganronpa.launcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.abimon.omnis.io.*;
import org.abimon.omnis.ludus.Ludus;
import org.abimon.omnis.util.General;

public class DDFile {

	public HashMap<String, DRFile> fileStructure = new HashMap<String, DRFile>();
	public VirtualDirectory directory = new VirtualDirectory("");

	boolean agar = false;
	long major = 0;
	long minor = 0;
	long header = 0;

	MarkableFileInputStream in;

	File wadFile;
	
	boolean isDR1 = true;

	public DDFile(File wadFile) throws IOException{
		long start = System.currentTimeMillis();

		this.wadFile = wadFile;
		in = new MarkableFileInputStream(new FileInputStream(wadFile));

		agar = readString(in, 4).equalsIgnoreCase("AGAR");
		major = readInt(in);
		minor = readInt(in);
		header = readInt(in);

		in.skip(header);

		long files = readInt(in);

		for(int i = 0; i < files; i++){
			long nameLen = readInt(in);
			String name = readString(in, (int) nameLen);
			long size = readLong(in);
			long offset = readLong(in);
			
			if(name.contains("Dr2"))
				this.isDR1 = false;

			fileStructure.put(name, new DRFile(name, size, offset));
		}

		long numDirs = readInt(in);

		long nameLen = readInt(in);
		String name = readString(in, (int) nameLen);
		long numEntries = readInt(in);

		for(int j = 0; j < numEntries; j++){
			long entryNameLen = readInt(in);
			String entryName = readString(in, (int) entryNameLen);
			boolean file = in.read() == 0;

			directory.addSubFile(file ? new VirtualFile(entryName) : new VirtualDirectory(entryName));
		}

		for(int i = 1; i < numDirs; i++){
			nameLen = readInt(in);
			name = readString(in, (int) nameLen);
			numEntries = readInt(in);

			VirtualFile search = this.directory.search("/" + name);
			VirtualDirectory vDir = search instanceof VirtualDirectory && search != null ? (VirtualDirectory) search : new VirtualDirectory(name);

			for(int j = 0; j < numEntries; j++){
				long entryNameLen = readInt(in);
				String entryName = readString(in, (int) entryNameLen);
				boolean file = in.read() == 0;

				vDir.addSubFile(file ? new VirtualFile(entryName) : new VirtualDirectory(entryName));
			}

			if(vDir.getParent() != null){
				vDir.getParent().removeSubFile(vDir.getName());
				vDir.getParent().addSubFile(vDir);
			}
			else{
				this.directory.addSubFile(vDir);
			}
		}

		in.mark(-1);

		long end = System.currentTimeMillis();

		long time = end-start;

		long minutes = time / 1000 / 60;
		long seconds = time / 1000 % 60;
		long millis = time % 1000;
	}

	public void changeFile(String name, String fullPath, long length){
		DRFile file = new DRFile(name, length, -1);
		this.fileStructure.put(name, file);
		this.directory.addSubFileWithPath(new VirtualFile(name), fullPath);
	}

	public void write(File newWad, String[] modsToRemove, ZipFile... modsToAdd) throws IOException{
		DanganLauncher.progress.updateProgress(5, "Detecting Patches...");

		JsonArray installedMods = fileStructure.containsKey("installed_mods.json") ? new Data(read("installed_mods.json")).getAsJsonArray() : new JsonArray();

		HashMap<String, ZipFile> patches = new HashMap<String, ZipFile>();

		for(ZipFile f : modsToAdd){
			if(f == null)
				continue;
			Enumeration<? extends ZipEntry> entries = f.entries();
			ZipEntry entry = null;

			JsonObject mod = new JsonObject();
			JsonArray modFiles = new JsonArray();

			while(entries.hasMoreElements() && (entry = entries.nextElement()) != null){
				String name = entry.getName();

				if(!name.endsWith("/") && !name.startsWith("__") && !name.contains(".DS_Store")){
					if(name.endsWith(".info")){
						Data data = new Data(f.getInputStream(entry));

						JsonObject modProperties = data.getAsJsonObject();

						mod.addProperty("name", modProperties.get("name").getAsString());
						mod.addProperty("version", modProperties.get("version").getAsString());
					}
					else{
						patches.put(name, f);
						modFiles.add(name);
					}
				}
			}

			if(!mod.has("name")){
				File file = new File(f.getName());
				mod.addProperty("name", file.getName().substring(0, file.getName().lastIndexOf('.')));
			}

			if(!mod.has("version")){
				File file = new File(f.getName());
				mod.addProperty("version", file.length() < 1000 * 10 ? new Data(file).getAsMD5Hash() : new Data(file.getAbsolutePath()).getAsMD5Hash());
			}

			mod.add("files", modFiles);

			installedMods.add(mod);
		}

		try{
			JsonObject settingsJson = new JsonObject();

			try{
				Data jsonData = new Data(new File(".spiral_settings"));
				JsonElement element = new JsonParser().parse(jsonData.getAsString());
				if(element.isJsonObject())
					settingsJson = element.getAsJsonObject();
			}
			catch(Throwable th){}

			HashMap<String, Data> patching = new HashMap<String, Data>();

			File backupFile = new File("mods" + File.separator + "backup.drs");
			ZipData zipData = new ZipData();

			if(backupFile.exists()){
				zipData = new ZipData(new Data(backupFile));

				if(fileStructure.containsKey("installed_mods.json")){
					JsonArray mods = new Data(read("installed_mods.json")).getAsJsonArray();

					for(JsonElement elem : mods){
						JsonObject modJson = elem.getAsJsonObject();

						String modID = modJson.get("name").getAsString() + " v" + modJson.get("version").getAsString();
						for(String id : modsToRemove){
							if(id.equalsIgnoreCase(modID)){
								if(modJson.has("files")){
									JsonArray files = modJson.get("files").getAsJsonArray();
									for(JsonElement fileElem : files){
										String name = fileElem.getAsString();
										if(zipData.containsKey(name))
											patches.put(name, new ZipFile(backupFile));
										else
											patching.put(name, new Data(new byte[0]));

										if(name.endsWith(".tga.png")){
											String trueName = name.replace(".tga.png", ".tga");
											if(zipData.containsKey(trueName))
												patches.put(trueName, new ZipFile(backupFile));
											else
												patching.put(trueName, new Data(new byte[0]));
										}
										if(name.endsWith(".pak.zip")){
											String trueName = name.replace(".pak.zip", ".pak");
											if(zipData.containsKey(trueName))
												patches.put(trueName, new ZipFile(backupFile));
											else
												patching.put(trueName, new Data(new byte[0]));
										}
										if(name.equalsIgnoreCase("people.json")){
											String trueName = "Dr1/data/us/cg/tex_cmn_name.pak";
											if(zipData.containsKey(trueName))
												patches.put(trueName, new ZipFile(backupFile));
											else
												patching.put(trueName, new Data(new byte[0]));
										}
										System.out.println(name);
									}
								}
								installedMods.remove(modJson);
							}
						}
					}
				}
			}

			File tmpDir = new File("tmp");

			DanganLauncher.progress.updateProgress(10, "Writing WAD to disk...");
			DanganModding.sendNotification("Mod Installation", "Writing WAD to disk...");

			float perFile = 20.0f / fileStructure.keySet().size();
			float fileNum = 0.0f;

			for(String s : fileStructure.keySet()){
				fileNum += perFile;
				DanganLauncher.progress.updateProgress(10 + fileNum, "Writing " + s.substring(0, Math.min(s.length(), 32)) + (s.length() > 32 ? "..." : ""));
				File dirs = new File(tmpDir, s.substring(0, Math.max(0, s.lastIndexOf('/'))));
				dirs.mkdirs();

				File f = new File(tmpDir, s);

				if(s.endsWith("aglogo.tga")){
					if(Ludus.hasData("spirallogo.tga.png"))
						Ludus.getData("spirallogo.tga.png").write(f);
				}
				else{
					FileOutputStream out = new FileOutputStream(f);
					out.write(read(s));
					out.close();
				}
			}

			DanganLauncher.progress.updateProgress(30, "Writing patches to disk...");
			DanganModding.sendNotification("Mod Installation", "Writing patches to disk...");

			File modList = new File(tmpDir, "installed_mods.json");
			new Data(installedMods.toString()).write(modList);
			
			System.out.println("Patches: " + patches.keySet());
			System.out.println("Patching: " + patching.keySet());

			for(String s : patches.keySet()){
				if(s.equalsIgnoreCase("people.json")){
					JsonArray array = new JsonParser().parse(new Data(patches.get(s).getInputStream(new ZipEntry(s))).getAsString()).getAsJsonArray();

					BufferedImage buf = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
					Font font = new Font("Goodbye Despair", Font.PLAIN, 28);

					if(settingsJson.has("name_font")){
						GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
						for(Font f : ge.getAllFonts())
							if(f.getName().equalsIgnoreCase(settingsJson.get("name_font").getAsString())){
								font = f.deriveFont(56.0f);
								break;
							}
					}

					Graphics g = buf.getGraphics();
					while(true){
						g.setFont(font);
						FontMetrics metrics = g.getFontMetrics();
						if(metrics.getStringBounds("Makoto Naegi", g).getHeight() > 25)
							font = font.deriveFont(font.getSize2D() - 1.0f);
						else
							break;
					}

					FontMetrics metrics = g.getFontMetrics();

					ZipData entries = DanganModding.pakExtraction(new Data(read("Dr1/data/us/cg/tex_cmn_name.pak")));

					for(JsonElement elem : array){
						JsonObject json = elem.getAsJsonObject();

						String name = json.get("name").getAsString();
						int index = json.get("index").getAsInt();

						if(json.has("aliases")){}
						else{
							String initials = "";

							for(String n : name.split("\\s+"))
								if(n.length() >= 1)
									initials += n.charAt(0);

							DanganModding.characterIDs.put(initials, index);
							DanganModding.characterIDs.put(name, index);
							DanganModding.characterIDs.put(name.split("\\s+")[0], index);
						}

						if(json.has("sprites")){
							JsonObject sprites = json.getAsJsonObject("sprites");

							HashMap<String, Integer> emotionSet = new HashMap<String, Integer>();

							for(Entry<String, JsonElement> emotion : sprites.entrySet()){
								emotionSet.put(emotion.getKey(), emotion.getValue().getAsInt());
							}

							DanganModding.emotions.put(index, emotionSet);
						}

						Rectangle2D size = metrics.getStringBounds(name, g);

						BufferedImage img = new BufferedImage((int) size.getWidth(), (int) size.getHeight(), BufferedImage.TYPE_INT_ARGB);
						Graphics nameG = img.getGraphics();
						nameG.setFont(font);
						nameG.setColor(Color.WHITE);
						nameG.drawString(name, 0, (int) (img.getHeight()/3*2.5f));

						entries.put(index + ".png", new Data(img));
					}

					if(!zipData.containsKey("Dr1/data/us/cg/tex_cmn_name.pak"))
						zipData.put("Dr1/data/us/cg/tex_cmn_name.pak", new Data(read("Dr1/data/us/cg/tex_cmn_name.pak")));
					DanganModding.compilePak(entries).write(new File(tmpDir, "Dr1/data/us/cg/tex_cmn_name.pak"));
				}
				else{
					if(s.endsWith(".lin.txt")){
						System.out.println("Compiling " + s);
						Data data = DanganModding.compileLin(new Data(patches.get(s).getInputStream(new ZipEntry(s))));
						data.write(new File(tmpDir, s.replace(".lin.txt", ".lin")));
						if(!zipData.containsKey(s.replace(".lin.txt", ".lin")))
							zipData.put(s.replace(".lin.txt", ".lin"), new Data(read(s.replace(".lin.txt", ".lin"))));
					}
					else if(s.endsWith(".pak.zip")){

						System.out.println("Packing " + s);
						ZipData zip = new ZipData(new Data(patches.get(s).getInputStream(new ZipEntry(s))));
						Data compiledPak = DanganModding.compilePak(zip);
						compiledPak.write(new File(tmpDir, s.replace(".pak.zip", ".pak")));
						if(!zipData.containsKey(s.replace(".pak.zip", ".pak")))
							zipData.put(s.replace(".pak.zip", ".pak"), new Data(read(s.replace(".pak.zip", ".pak"))));
					}
					else if(s.endsWith(".tga.png")){
						System.out.println("Decrypting " + s);
						Data data = new Data(TGAWriter.writeImage(new Data(patches.get(s).getInputStream(new ZipEntry(s))).getAsImage()));
						data.write(new File(tmpDir, s.replace(".tga.png", ".tga")));
						if(!zipData.containsKey(s.replace(".tga.png", ".tga")))
							zipData.put(s.replace(".tga.png", ".tga"), new Data(read(s.replace(".tga.png", ".tga"))));
					}
					else if(fileStructure.containsKey(s)){
						new Data(patches.get(s).getInputStream(new ZipEntry(s))).write(new File(tmpDir, s));
						zipData.put(s, new Data(read(s)));
					}
					else
						new Data(patches.get(s).getInputStream(new ZipEntry(s))).write(new File(tmpDir, s));
				}
			}

			for(String s : patching.keySet()){
				System.out.println("Patching: " + s);
				File f = new File(tmpDir, s);
				if(patching.get(s).length() == 0)
					f.delete();
				else
					patching.get(s).write(f);
			}

			if(!zipData.isEmpty()){
				JsonObject json = new JsonObject();
				json.addProperty("name", "DanganBackup (Don't Manually Install)");
				json.addProperty("version", General.formatDate(new Date(), "yyyy.mm.dd"));
				zipData.put("backup_mod.info", new Data(json.toString()));
				zipData.writeToFile(backupFile);
			}

			//TODO: Write Header, Count Files

			//			FileOutputStream out = new FileOutputStream(tmp);
			//
			//			out.write("AGAR".getBytes());
			//
			//			writeInt(out, major);
			//			writeInt(out, minor);
			//			writeInt(out, header);
			//
			//			byte[] headerData = new byte[(int) header];
			//			new Random().nextBytes(headerData);
			//			out.write(headerData);
			//
			//			LinkedList<File> files = General.iterate(tmpDir, false);
			//
			//			long fileCount = files.size();
			//			System.out.println("Files: " + fileCount);
			//			writeInt(out, fileCount);
			//
			//			long offset = 0;	
			//
			//			for(File f : files){
			//				Data d = new Data(f);
			//				String name = f.getAbsolutePath().replace(tmpDir.getAbsolutePath() + File.separator, "");
			//				writeInt(out, name.length());
			//				out.write(name.getBytes());
			//				write(out, d.size(), 8);
			//				write(out, offset, 8);
			//				offset += d.size();
			//				d = null;
			//			}
			//			System.out.println("Wrote: File Data");
			//
			//			//TODO: Directory Count
			//
			//			LinkedList<File> dirs = General.iterateDirs(tmpDir);
			//
			//			writeInt(out, dirs.size());
			//
			//			for(File dir : dirs){
			//				String name = dir.getName();
			//				if(name.equalsIgnoreCase("tmp"))
			//					name = "";
			//				writeInt(out, name.length());
			//				out.write(name.getBytes());
			//				LinkedList<File> sub = new LinkedList<File>();
			//				for(File f : dir.listFiles())
			//					if(!f.getName().startsWith("."))
			//						sub.add(f);
			//				writeInt(out, sub.size());
			//
			//				for(File f : sub){
			//					String entryName = f.getName().replace('\\', '/');
			//					writeInt(out, entryName.length());
			//					out.write(entryName.getBytes());
			//					out.write(f.isFile() ? 0 : 1);
			//				}
			//			}
			//
			//			System.out.println("Wrote: Directory Structure");
			//
			//			for(File f : files){
			//				System.out.println("Writing " + f.getAbsolutePath().replace(tmpDir.getAbsolutePath() + File.separator, ""));
			//				out.write(new Data(f).getData());
			//				f.delete();
			//			}
			//
			//			out.close();
			//
			//			FileInputStream tin = new FileInputStream(tmp);
			//			FileOutputStream don = new FileOutputStream(newWad);
			//
			//			long filesize = tin.available();
			//			long written = 0;
			//
			//			byte[] buffer = new byte[65536];
			//			while(true){
			//				int read = tin.read(buffer);
			//				if(read <= 0)
			//					break;
			//				don.write(buffer, 0, read);
			//				written += read;
			//				System.out.println(written + "/~" + filesize);
			//			}

			DanganModding.makeWad(newWad, tmpDir, new PrintStream(new EmptyOutputStream()), true);
		}
		catch(Throwable th){
			th.printStackTrace();
		}
	}

	/** Note: Do NOT use this UNLESS you're confident you're not patching much data in */
	public void write(File newWad, HashMap<String, Data> dataToPatch){

	}

	public byte[] read(String fileName) throws IOException{

		if(fileStructure.containsKey(fileName)){
			DRFile file = fileStructure.get(fileName);
			in.reset();
			in.skip(file.offset);
			byte[] data = new byte[(int) file.size];
			in.read(data);
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
