package org.abimon.mods.danganronpa.launcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
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
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;

import javax.imageio.ImageIO;

import org.abimon.omnis.io.Data;
import org.abimon.omnis.io.TGAReader;
import org.abimon.omnis.io.TGAWriter;
import org.abimon.omnis.io.ZipData;
import org.abimon.omnis.ludus.Ludus;
import org.abimon.omnis.net.Website;
import org.abimon.omnis.util.ExtraArrays;
import org.abimon.omnis.util.General;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class DanganModding {

	public static final String SPIRAL_URL = "https://36.media.tumblr.com/7505ef037a7f5e4afc4c153d5f8a27c1/tumblr_o69vcyTaJt1une6n4o1_75sq.png";

	public static boolean NOTIFICATION_SERVER = false;	static{
		try{
			new Socket("localhost", 11038).close();
			NOTIFICATION_SERVER = true;
		}
		catch(Throwable th){}
	}

	public static LinkedList<DRFile> files = new LinkedList<DRFile>();

	public static void sendNotification(String subtitle, String message){
		try{
			if(NOTIFICATION_SERVER){
				Website website = new Website("localhost:11038/notifications?name=Spiral&desc=" + URLEncoder.encode(message, "UTF-8") + "&subtitle=" + URLEncoder.encode(subtitle, "UTF-8") + (new Website(SPIRAL_URL).canConnect() ? ("&iconurl=" + URLEncoder.encode(SPIRAL_URL, "UTF-8")) : ""));
				website.retrieveContent();
			}
		}
		catch(Throwable th){
		}
	}

	public static boolean isDR1 = true;

	public static void extract(File wad, File extractDir, PrintStream out) throws Throwable{
		sendNotification("Extraction", "Begun Extraction to " + extractDir);
		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(0, "Beginning Extraction...");
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

		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(5, "Reading information about " + files + " files...");

		float perfile = 15.0f / files;

		for(int i = 0; i < files; i++){
			long nameLen = readInt(in);
			String name = readString(in, (int) nameLen);
			long size = readLong(in);
			long offset = readLong(in);
			String s = "File: " + name + " (" + nameLen + " chars), " + size + "B and " + offset + "B from start";
			out.println(s);

			if(DanganLauncher.progress != null)
				DanganLauncher.progress.updateProgress(5 + (perfile * i), s.substring(0, Math.min(32, s.length())) + (s.length() > 32 ? "..." : ""));
			DanganModding.files.add(new DRFile(name, size, offset));
		}

		out.println("Now reading DIRs");

		long numDirs = readInt(in);

		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(20, "Reading information about " + numDirs + " directories...");

		out.println(numDirs + " directories");

		float perDir = 10.0f / numDirs;

		for(int i = 0; i < numDirs; i++){
			long nameLen = readInt(in);
			String name = readString(in, (int) nameLen);
			if(name.equalsIgnoreCase("Dr2"))
				isDR1 = false;
			long numEntries = readInt(in);
			out.println("Dir: " + name + " (" + nameLen + " chars), " + numEntries + " entries");
			if(DanganLauncher.progress != null)
				DanganLauncher.progress.updateProgress(20 + (i * perDir), "Reading information about " + name);

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
		float perFile = 70.0f / DanganModding.files.size();

		for(int i = 0; i < DanganModding.files.size(); i++){
			try{
				DRFile drfile = DanganModding.files.get(i);

				byte[] data = new byte[(int) drfile.size];
				in.read(data);

				out.println("Extracting " + drfile.name);

				if(DanganLauncher.progress != null)
					DanganLauncher.progress.updateProgress(30 + (i * perFile), "Extracting " + drfile.name);

				File dirs = new File(extractDir.getAbsolutePath() + File.separator + drfile.name.substring(0, drfile.name.length() - drfile.name.split("/")[drfile.name.split("/").length - 1].length()).replace("/", File.separator).replace("\\", File.separator));
				dirs.mkdirs();
				File output = new File(extractDir.getAbsolutePath() + File.separator + drfile.name.replace("/", File.separator).replace("\\", File.separator));


				if(drfile.name.endsWith("aglogo.tga")){
					if(Ludus.hasData("spirallogo.tga.png"))
						Ludus.getData("spirallogo.tga.png").write(output);
					else{
						FileOutputStream fos = new FileOutputStream(output);
						fos.write(data);
						fos.close();
					}
				}
				else if(drfile.name.endsWith(".lin")){
					Data linData = DanganModding.linHandling(new Data(data), out);
					linData.write(new File(output.getAbsolutePath() + ".txt"));
					linData = null;

					FileOutputStream fos = new FileOutputStream(output);
					fos.write(data);
					fos.close();
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
				else if(drfile.name.endsWith(".dat") && drfile.name.contains("nonstop")){
					Data nonstopData = DanganModding.extractNonstop(new Data(data));
					nonstopData.write(new File(output.getAbsolutePath() + ".json"));
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

		File timestamp = new File(extractDir, ".timestamp");
		if(timestamp.exists())
			timestamp.setLastModified(System.currentTimeMillis());
		else
			timestamp.createNewFile();

		out.println("Took " + minutes + " minutes, " + seconds + " seconds and " + millis + " milliseconds.");
		sendNotification("Extraction", "Extraction complete");
		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(100, "Extraction Complete");
	}

	public static HashMap<String, HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>>> Opcodes = new HashMap<String, HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>>>();
	public static HashMap<String, Integer> characterIDs = new HashMap<String, Integer>();
	public static HashMap<Integer, HashMap<String, Integer>> emotions = new HashMap<Integer, HashMap<String, Integer>>();
	public static HashMap<Integer, HashMap<String, Integer>> barks = new HashMap<Integer, HashMap<String, Integer>>();
	public static HashMap<String, Integer> musicNames = new HashMap<String, Integer>();
	public static HashMap<String, Integer> animations = new HashMap<String, Integer>();
	public static HashMap<String, Integer> evidenceMap = new HashMap<String, Integer>();
	public static HashMap<Integer, String> nonstopOpCodes = new HashMap<Integer, String>();

	public static final int magicOne = 1399866996;
	public static final int magicTwo = 4;

	static
	{
		HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>> dr1 = new HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>>();
		HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>> dr2 = new HashMap<Byte, AbstractMap.SimpleEntry<String, Integer>>();

		dr1.put((byte) 0x00, new AbstractMap.SimpleEntry<String, Integer>("TextCount", 2));
		dr1.put((byte) 0x01, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x02, new AbstractMap.SimpleEntry<String, Integer>("Text", 2));
		dr1.put((byte) 0x03, new AbstractMap.SimpleEntry<String, Integer>("CLT", 1));
		dr1.put((byte) 0x04, new AbstractMap.SimpleEntry<String, Integer>("Filter", 4));
		dr1.put((byte) 0x05, new AbstractMap.SimpleEntry<String, Integer>("Movie", 2));
		dr1.put((byte) 0x06, new AbstractMap.SimpleEntry<String, Integer>("Animation", 8));
		dr1.put((byte) 0x08, new AbstractMap.SimpleEntry<String, Integer>("Voice", 5));
		dr1.put((byte) 0x09, new AbstractMap.SimpleEntry<String, Integer>("Music", 3));
		dr1.put((byte) 0x0A, new AbstractMap.SimpleEntry<String, Integer>("Sound", 3));
		dr1.put((byte) 0x0B, new AbstractMap.SimpleEntry<String, Integer>("SoundB", 2));
		dr1.put((byte) 0x0C, new AbstractMap.SimpleEntry<String, Integer>("SetTruthBullet", 2));
		dr1.put((byte) 0x0D, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x0E, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr1.put((byte) 0x0F, new AbstractMap.SimpleEntry<String, Integer>("SetTitle", 3));
		dr1.put((byte) 0x10, new AbstractMap.SimpleEntry<String, Integer>("SetReportInfo", 3));
		dr1.put((byte) 0x11, new AbstractMap.SimpleEntry<String, Integer>(null, 4));
		dr1.put((byte) 0x14, new AbstractMap.SimpleEntry<String, Integer>("TrialCamera", 3));
		dr1.put((byte) 0x15, new AbstractMap.SimpleEntry<String, Integer>("LoadMap", 3));
		dr1.put((byte) 0x19, new AbstractMap.SimpleEntry<String, Integer>("GoToScript", 3));
		dr1.put((byte) 0x1A, new AbstractMap.SimpleEntry<String, Integer>("StopScript", 0));
		dr1.put((byte) 0x1B, new AbstractMap.SimpleEntry<String, Integer>("RunScript", 3));
		dr1.put((byte) 0x1C, new AbstractMap.SimpleEntry<String, Integer>(null, 0));
		dr1.put((byte) 0x1E, new AbstractMap.SimpleEntry<String, Integer>("Sprite", 5));
		dr1.put((byte) 0x1F, new AbstractMap.SimpleEntry<String, Integer>(null, 7));
		dr1.put((byte) 0x20, new AbstractMap.SimpleEntry<String, Integer>(null, 5));
		dr1.put((byte) 0x21, new AbstractMap.SimpleEntry<String, Integer>("Speaker", 1));
		dr1.put((byte) 0x22, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr1.put((byte) 0x23, new AbstractMap.SimpleEntry<String, Integer>(null, 5));
		dr1.put((byte) 0x25, new AbstractMap.SimpleEntry<String, Integer>("ChangeUI", 2));
		dr1.put((byte) 0x26, new AbstractMap.SimpleEntry<String, Integer>("SetFlag", 3));
		dr1.put((byte) 0x27, new AbstractMap.SimpleEntry<String, Integer>("CharacterResponse", 1));
		dr1.put((byte) 0x29, new AbstractMap.SimpleEntry<String, Integer>("ObjectResponse", 1));
		dr1.put((byte) 0x2A, new AbstractMap.SimpleEntry<String, Integer>("SetLabel", 2));
		dr1.put((byte) 0x2B, new AbstractMap.SimpleEntry<String, Integer>("Choice", 1));
		dr1.put((byte) 0x2C, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr1.put((byte) 0x2E, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr1.put((byte) 0x2F, new AbstractMap.SimpleEntry<String, Integer>(null, 10));
		dr1.put((byte) 0x30, new AbstractMap.SimpleEntry<String, Integer>("ShowBackground", 3));
		dr1.put((byte) 0x32, new AbstractMap.SimpleEntry<String, Integer>(null, 1));
		dr1.put((byte) 0x33, new AbstractMap.SimpleEntry<String, Integer>(null, 4));
		dr1.put((byte) 0x34, new AbstractMap.SimpleEntry<String, Integer>("GotoLabel", 2));
		dr1.put((byte) 0x35, new AbstractMap.SimpleEntry<String, Integer>("Check Flag A", -1));
		dr1.put((byte) 0x36, new AbstractMap.SimpleEntry<String, Integer>("Check Flag B", -1));
		dr1.put((byte) 0x38, new AbstractMap.SimpleEntry<String, Integer>(null, -1));
		dr1.put((byte) 0x39, new AbstractMap.SimpleEntry<String, Integer>(null, 5));
		dr1.put((byte) 0x3A, new AbstractMap.SimpleEntry<String, Integer>("WaitInput", 0));
		dr1.put((byte) 0x3B, new AbstractMap.SimpleEntry<String, Integer>("WaitFrame", 0));
		dr1.put((byte) 0x3C, new AbstractMap.SimpleEntry<String, Integer>("FlagCheckEnd", 0));
		dr1.put((byte) 0x4B, new AbstractMap.SimpleEntry<String, Integer>("WaitInputDR2", -1));
		dr1.put((byte) 0x4C, new AbstractMap.SimpleEntry<String, Integer>("WaitFrameDR2", 0));
		dr1.put((byte) 0x4D, new AbstractMap.SimpleEntry<String, Integer>(null, -1));

		dr2.put((byte) 0x00, new AbstractMap.SimpleEntry<String, Integer>("TextCount", 2));
		dr2.put((byte) 0x01, new AbstractMap.SimpleEntry<String, Integer>(null, 4));
		dr2.put((byte) 0x02, new AbstractMap.SimpleEntry<String, Integer>("Text", 2));
		dr2.put((byte) 0x03, new AbstractMap.SimpleEntry<String, Integer>("CLT", 1));
		dr2.put((byte) 0x04, new AbstractMap.SimpleEntry<String, Integer>("Filter", 4));
		dr2.put((byte) 0x05, new AbstractMap.SimpleEntry<String, Integer>("Movie", 2));
		dr2.put((byte) 0x06, new AbstractMap.SimpleEntry<String, Integer>("Animation", 8));
		dr2.put((byte) 0x08, new AbstractMap.SimpleEntry<String, Integer>("Voice", 5));
		dr2.put((byte) 0x09, new AbstractMap.SimpleEntry<String, Integer>("Music", 3));
		dr2.put((byte) 0x0A, new AbstractMap.SimpleEntry<String, Integer>("Sound", 3));
		dr2.put((byte) 0x0B, new AbstractMap.SimpleEntry<String, Integer>("SoundB", 2));
		dr2.put((byte) 0x0C, new AbstractMap.SimpleEntry<String, Integer>("SetTruthBullet", 2));
		dr2.put((byte) 0x0D, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr2.put((byte) 0x0E, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr2.put((byte) 0x0F, new AbstractMap.SimpleEntry<String, Integer>("SetTitle", 3));
		dr2.put((byte) 0x10, new AbstractMap.SimpleEntry<String, Integer>("SetReportInfo", 3));
		dr2.put((byte) 0x11, new AbstractMap.SimpleEntry<String, Integer>(null, 4));
		dr2.put((byte) 0x14, new AbstractMap.SimpleEntry<String, Integer>("TrialCamera", 6));
		dr2.put((byte) 0x15, new AbstractMap.SimpleEntry<String, Integer>("LoadMap", 4));
		dr2.put((byte) 0x19, new AbstractMap.SimpleEntry<String, Integer>("GoToScript", 5));
		dr2.put((byte) 0x1A, new AbstractMap.SimpleEntry<String, Integer>("StopScript", 0));
		dr2.put((byte) 0x1B, new AbstractMap.SimpleEntry<String, Integer>("RunScript", 5));
		dr2.put((byte) 0x1C, new AbstractMap.SimpleEntry<String, Integer>(null, 0));
		dr2.put((byte) 0x1E, new AbstractMap.SimpleEntry<String, Integer>("Sprite", 5));
		dr2.put((byte) 0x1F, new AbstractMap.SimpleEntry<String, Integer>(null, 7));
		dr2.put((byte) 0x20, new AbstractMap.SimpleEntry<String, Integer>(null, 5));
		dr2.put((byte) 0x21, new AbstractMap.SimpleEntry<String, Integer>("Speaker", 1));
		dr2.put((byte) 0x22, new AbstractMap.SimpleEntry<String, Integer>(null, 3));
		dr2.put((byte) 0x23, new AbstractMap.SimpleEntry<String, Integer>(null, 5));
		dr2.put((byte) 0x25, new AbstractMap.SimpleEntry<String, Integer>("ChangeUI", 2));
		dr2.put((byte) 0x26, new AbstractMap.SimpleEntry<String, Integer>("SetFlag", 3));
		dr2.put((byte) 0x27, new AbstractMap.SimpleEntry<String, Integer>("CharacterResponse", 1));
		dr2.put((byte) 0x29, new AbstractMap.SimpleEntry<String, Integer>("ObjectResponse", 0xD));
		dr2.put((byte) 0x2A, new AbstractMap.SimpleEntry<String, Integer>("SetLabel", 0xC));
		dr2.put((byte) 0x2B, new AbstractMap.SimpleEntry<String, Integer>("Choice", 1));
		dr2.put((byte) 0x2C, new AbstractMap.SimpleEntry<String, Integer>(null, 2));
		dr2.put((byte) 0x2E, new AbstractMap.SimpleEntry<String, Integer>(null, 5));
		dr2.put((byte) 0x2F, new AbstractMap.SimpleEntry<String, Integer>(null, 10));
		dr2.put((byte) 0x30, new AbstractMap.SimpleEntry<String, Integer>("ShowBackground", 2));
		dr2.put((byte) 0x32, new AbstractMap.SimpleEntry<String, Integer>(null, 1));
		dr2.put((byte) 0x33, new AbstractMap.SimpleEntry<String, Integer>(null, 4));
		dr2.put((byte) 0x34, new AbstractMap.SimpleEntry<String, Integer>("GotoLabel", 1));
		dr2.put((byte) 0x35, new AbstractMap.SimpleEntry<String, Integer>("Check Flag A", -1));
		dr2.put((byte) 0x36, new AbstractMap.SimpleEntry<String, Integer>("Check Flag B", -1));
		dr2.put((byte) 0x38, new AbstractMap.SimpleEntry<String, Integer>(null, -1));
		dr2.put((byte) 0x39, new AbstractMap.SimpleEntry<String, Integer>(null, 5));
		dr2.put((byte) 0x3A, new AbstractMap.SimpleEntry<String, Integer>("WaitInput", 0));
		dr2.put((byte) 0x3B, new AbstractMap.SimpleEntry<String, Integer>("WaitFrame", 0));
		dr2.put((byte) 0x3C, new AbstractMap.SimpleEntry<String, Integer>("FlagCheckEnd", 0));
		dr2.put((byte) 0x4B, new AbstractMap.SimpleEntry<String, Integer>("WaitInputDR2", -1));
		dr2.put((byte) 0x4C, new AbstractMap.SimpleEntry<String, Integer>("WaitFrameDR2", 0));
		dr2.put((byte) 0x4D, new AbstractMap.SimpleEntry<String, Integer>(null, -1));

		dr2.put((byte) 0x15, new AbstractMap.SimpleEntry<String, Integer>(null, 4));
		dr2.put((byte) 0x19, new AbstractMap.SimpleEntry<String, Integer>("LoadScript", 5));
		dr2.put((byte) 0x1A, new AbstractMap.SimpleEntry<String, Integer>("StopScript", 0));
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
		characterIDs.put("Monobear", 15);
		characterIDs.put("Real Junko Enoshima", 16);
		characterIDs.put("Alter Ego", 17);
		characterIDs.put("Genocider Syo", 18);
		characterIDs.put("Jin Kirigiri", 19);
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

		characterIDs.put("Hajime Hinata", 0);
		characterIDs.put("Izuru Kamukura", 0);
		characterIDs.put("Nagito Komaeda", 1);
		characterIDs.put("Byakuya Togami", 2);
		characterIDs.put("Byakuya Twogami", 2);
		characterIDs.put("Imposter", 2);
		characterIDs.put("Impostor", 2);
		characterIDs.put("Gundham Tanaka", 3);
		characterIDs.put("Gundam Tanaka", 3);
		characterIDs.put("Kazuichi Souda", 4);
		characterIDs.put("Kazuichi Soda", 4);
		characterIDs.put("Teruteru Hanamura", 5);
		characterIDs.put("Nekomaru Nidai", 6);
		characterIDs.put("Fuyuhiko Kuzuryu", 7);
		characterIDs.put("Fuyuhiko Kuzuryuu", 7);
		characterIDs.put("Akane Owari", 8);
		characterIDs.put("Chiaki Nanami", 9);
		characterIDs.put("Sonia Nevermind", 10);
		characterIDs.put("Hiyoko Saionji", 11);
		characterIDs.put("Mahiru Koizumi", 12);
		characterIDs.put("Mikan Tsumiki", 13);
		characterIDs.put("Ibuki Mioda", 14);
		characterIDs.put("Monomi", 17);
		characterIDs.put("Usami", 17);
		characterIDs.put("Mechamaru Nidai", 19);
		characterIDs.put("Real Byakuya Togami", 22);

		characterIDs.put("Hajime", 0);
		characterIDs.put("Nagito", 1);
		characterIDs.put("Byakuya", 2);
		characterIDs.put("Gundham", 3);
		characterIDs.put("Gundam", 3);
		characterIDs.put("Kazuichi", 4);
		characterIDs.put("Teruteru", 5);
		characterIDs.put("Nekomaru", 6);
		characterIDs.put("Fuyuhiko", 7);
		characterIDs.put("Akane", 8);
		characterIDs.put("Chiaki", 9);
		characterIDs.put("Sonia", 10);
		characterIDs.put("Hiyoko", 11);
		characterIDs.put("Bitch", 11);
		characterIDs.put("Mahiru", 12);
		characterIDs.put("Mikan", 13);
		characterIDs.put("Ibuki", 14);

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

		nonstopOpCodes.put(0, "TextID");
		nonstopOpCodes.put(1, "Type");
		nonstopOpCodes.put(3, "ShootWithEvidence");
		nonstopOpCodes.put(4, "ShootWithWeakPoint");
		nonstopOpCodes.put(6, "HasWeakPoint");
		nonstopOpCodes.put(7, "Advance");
		nonstopOpCodes.put(10, "Zoom");
		nonstopOpCodes.put(11, "Fadeout");
		nonstopOpCodes.put(12, "Horizontal");
		nonstopOpCodes.put(13, "Vertical");
		nonstopOpCodes.put(21, "Character");
		nonstopOpCodes.put(22, "Sprite");
		nonstopOpCodes.put(25, "Voice");
		nonstopOpCodes.put(27, "Chapter");
	};

	private static File workingDir = null;

	//TODO: Make WAD
	public static void makeWad(File newWad, File wadDir, PrintStream pOut, boolean tmp) throws IOException{
		internalCounter = 0;
		workingDir = wadDir;
		if(!wadDir.exists())
			throw new IOException("WAD Directory does not exist");

		isDR1 = true;
		
		for(File f : wadDir.listFiles())
			if(f.getName().startsWith("Dr2"))
				isDR1 = false;

		characterIDs.clear();

		if(!isDR1){
			characterIDs.put("Hajime Hinata", 0);
			characterIDs.put("Izuru Kamukura", 0);
			characterIDs.put("Nagito Komaeda", 1);
			characterIDs.put("Byakuya Togami", 2);
			characterIDs.put("Byakuya Twogami", 2);
			characterIDs.put("Imposter", 2);
			characterIDs.put("Impostor", 2);
			characterIDs.put("Gundham Tanaka", 3);
			characterIDs.put("Gundam Tanaka", 3);
			characterIDs.put("Kazuichi Souda", 4);
			characterIDs.put("Kazuichi Soda", 4);
			characterIDs.put("Teruteru Hanamura", 5);
			characterIDs.put("Nekomaru Nidai", 6);
			characterIDs.put("Fuyuhiko Kuzuryu", 7);
			characterIDs.put("Fuyuhiko Kuzuryuu", 7);
			characterIDs.put("Akane Owari", 8);
			characterIDs.put("Chiaki Nanami", 9);
			characterIDs.put("Sonia Nevermind", 10);
			characterIDs.put("Hiyoko Saionji", 11);
			characterIDs.put("Mahiru Koizumi", 12);
			characterIDs.put("Mikan Tsumiki", 13);
			characterIDs.put("Ibuki Mioda", 14);
			characterIDs.put("Monomi", 17);
			characterIDs.put("Usami", 17);
			characterIDs.put("Mechamaru Nidai", 19);
			characterIDs.put("Real Byakuya Togami", 22);

			characterIDs.put("Hajime", 0);
			characterIDs.put("Nagito", 1);
			characterIDs.put("Byakuya", 2);
			characterIDs.put("Gundham", 3);
			characterIDs.put("Gundam", 3);
			characterIDs.put("Kazuichi", 4);
			characterIDs.put("Teruteru", 5);
			characterIDs.put("Nekomaru", 6);
			characterIDs.put("Fuyuhiko", 7);
			characterIDs.put("Akane", 8);
			characterIDs.put("Chiaki", 9);
			characterIDs.put("Sonia", 10);
			characterIDs.put("Hiyoko", 11);
			characterIDs.put("Bitch", 11);
			characterIDs.put("Mahiru", 12);
			characterIDs.put("Mikan", 13);
			characterIDs.put("Ibuki", 14);

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
			characterIDs.put("Peko Pekoyama", 15);
			characterIDs.put("Peko", 15);
			characterIDs.put("MK", 15);
			characterIDs.put("Monokuma", 16);
			characterIDs.put("Monobear", 16);
			characterIDs.put("Real Junko Enoshima", 18);
			characterIDs.put("Makoto Naegi", 20);
			characterIDs.put("Kyoko Kirigiri", 21);
			characterIDs.put("Alter Ego", 24);
			characterIDs.put("???", 41);
			characterIDs.put("Narrator", 42);
			characterIDs.put("None", 63);
		}
		else{
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
			characterIDs.put("Monobear", 15);
			characterIDs.put("Real Junko Enoshima", 16);
			characterIDs.put("Alter Ego", 17);
			characterIDs.put("Genocider Syo", 18);
			characterIDs.put("Jin Kirigiri", 19);
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
			characterIDs.put("None", 63);
		}

		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(40, "Beginning WAD Compilation...");
		sendNotification("Mod Installation", "Beginning WAD Compilation");

		JsonObject settingsJson = new JsonObject();

		try{
			Data jsonData = new Data(new File(".spiral_settings"));
			JsonElement element = new JsonParser().parse(jsonData.getAsString());
			if(element.isJsonObject())
				settingsJson = element.getAsJsonObject();
		}
		catch(Throwable th){}

		File people = new File(wadDir, "people.json");
		if(people.exists()){

			JsonArray array = new JsonParser().parse(new Data(people).getAsString()).getAsJsonArray();

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

			File tex = new File(wadDir, (isDR1 ? "Dr1" : "Dr2") + File.separator + "data" + File.separator + "us" + File.separator + "cg" + File.separator + (isDR1 ? "tex_cmn_name.pak.zip" : "chara_name.pak.zip"));

			ZipData entries = new ZipData(new Data(tex));

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

					characterIDs.put(initials, index);
					characterIDs.put(name, index);
					characterIDs.put(name.split("\\s+")[0], index);
				}

				if(json.has("sprites")){
					JsonObject sprites = json.getAsJsonObject("sprites");

					HashMap<String, Integer> emotionSet = new HashMap<String, Integer>();

					for(Entry<String, JsonElement> emotion : sprites.entrySet()){
						emotionSet.put(emotion.getKey().toLowerCase(), emotion.getValue().getAsInt());
					}

					emotions.put(index, emotionSet);
				}

				if(json.has("barks")){
					JsonObject barks = json.getAsJsonObject("barks");

					HashMap<String, Integer> barkSet = new HashMap<String, Integer>();

					for(Entry<String, JsonElement> bark : barks.entrySet()){
						barkSet.put(bark.getKey().toLowerCase(), bark.getValue().getAsInt());
					}

					DanganModding.barks.put(index, barkSet);
				}

				if(json.has("redraw") && json.get("redraw").getAsBoolean()){
					Rectangle2D size = metrics.getStringBounds(name, g);

					BufferedImage img = new BufferedImage((int) size.getWidth(), (int) size.getHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics nameG = img.getGraphics();
					nameG.setFont(font);
					nameG.setColor(Color.WHITE);
					nameG.drawString(name, 0, (int) (img.getHeight()/3*2.5f));

					entries.put(index + ".png", new Data(img));
				}
			}

			entries.write(tex);
		}

		File music = new File(wadDir, "music.json");
		if(music.exists()){
			JsonObject json = new Data(music).getAsJsonObject();

			for(Entry<String, JsonElement> entry : json.entrySet())
				DanganModding.musicNames.put(entry.getKey(), entry.getValue().getAsInt());
		}

		File animations = new File(wadDir, "animations.json");
		if(animations.exists()){
			JsonObject json = new Data(animations).getAsJsonObject();

			for(Entry<String, JsonElement> entry : json.entrySet())
				DanganModding.animations.put(entry.getKey(), entry.getValue().getAsInt());
		}

		File evidence = new File(wadDir, "evidence.json");
		if(evidence.exists()){
			JsonArray evidenceArray = new JsonParser().parse(new Data(evidence).getAsString()).getAsJsonArray();

			ZipData names;
			ZipData desc1;
			ZipData desc2;
			ZipData desc3;

			if(isDR1){
				File evidenceNames = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "06_KotodamaName.pak.zip");
				File evidenceDesc1 = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "07_KotodamaDesc1.pak.zip");
				File evidenceDesc2 = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "08_KotodamaDesc2.pak.zip");
				File evidenceDesc3 = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "09_KotodamaDesc3.pak.zip");

				names = new ZipData(evidenceNames);
				desc1 = new ZipData(evidenceDesc1);
				desc2 = new ZipData(evidenceDesc2);
				desc3 = new ZipData(evidenceDesc3);
			}
			else{
				File evidenceFile = new File(wadDir, "Dr2" + File.separator + "data" + File.separator + "us" + File.separator + "bin" + File.separator + "bin_progress_font_l.pak.zip");

				ZipData evidenceZip = new ZipData(evidenceFile);

				names = new ZipData(evidenceZip.get("4.pak.zip"));
				desc1 = new ZipData(evidenceZip.get("5.pak.zip"));
				desc2 = new ZipData(evidenceZip.get("6.pak.zip"));
				desc3 = new ZipData(evidenceZip.get("7.pak.zip"));
			}

			for(JsonElement elem : evidenceArray){
				JsonObject json = elem.getAsJsonObject();

				String name 				= json.get("name").getAsString();
				String id 					= json.has("id") ? json.get("id").getAsString() : name;
				String desc 				= json.has("desc") ? General.splitEveryXCharacters(json.get("desc").getAsString(), 30) : "";
				String descUpdated 			= json.has("desc_1") ? General.splitEveryXCharacters(json.get("desc_1").getAsString(), 30) : "";
				String descUpdatedUpdated 	= json.has("desc_2") ? General.splitEveryXCharacters(json.get("desc_2").getAsString(), 30) : "";
				int index 					= json.get("index").getAsInt();
				int def						= json.has("default") ? json.get("default").getAsInt() : 1;

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] txtDataStr = name.getBytes("UTF-16LE");
				byte[] txtData = new byte[txtDataStr.length + 2];

				txtData[0] = (byte) 0xFF;
				txtData[1] = (byte) 0xFE;

				for(int i = 0; i < txtDataStr.length; i++)
					txtData[i+2] = txtDataStr[i];

				out.write(txtData);
				out.write(0);
				out.write(0);

				names.put(index + ".txt", new Data(out.toByteArray()));
				out.reset();

				txtDataStr = desc.getBytes("UTF-16LE");
				txtData = new byte[txtDataStr.length + 2];

				txtData[0] = (byte) 0xFF;
				txtData[1] = (byte) 0xFE;

				for(int i = 0; i < txtDataStr.length; i++)
					txtData[i+2] = txtDataStr[i];

				out.write(txtData);
				out.write(0);
				out.write(0);

				desc1.put(index + ".txt", new Data(out.toByteArray()));
				out.reset();

				txtDataStr = descUpdated.getBytes("UTF-16LE");
				txtData = new byte[txtDataStr.length + 2];

				txtData[0] = (byte) 0xFF;
				txtData[1] = (byte) 0xFE;

				for(int i = 0; i < txtDataStr.length; i++)
					txtData[i+2] = txtDataStr[i];

				out.write(txtData);
				out.write(0);
				out.write(0);

				desc2.put(index + ".txt", new Data(out.toByteArray()));
				out.reset();

				txtDataStr = descUpdatedUpdated.getBytes("UTF-16LE");
				txtData = new byte[txtDataStr.length + 2];

				txtData[0] = (byte) 0xFF;
				txtData[1] = (byte) 0xFE;

				for(int i = 0; i < txtDataStr.length; i++)
					txtData[i+2] = txtDataStr[i];

				out.write(txtData);
				out.write(0);
				out.write(0);

				desc3.put(index + ".txt", new Data(out.toByteArray()));
				out.reset();

				evidenceMap.put(id, index | def << 8);
			}

			if(isDR1){
				File evidenceNames = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "06_KotodamaName.pak.zip");
				File evidenceDesc1 = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "07_KotodamaDesc1.pak.zip");
				File evidenceDesc2 = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "08_KotodamaDesc2.pak.zip");
				File evidenceDesc3 = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "09_KotodamaDesc3.pak.zip");

				names.write(evidenceNames);
				desc1.write(evidenceDesc1);
				desc2.write(evidenceDesc2);
				desc3.write(evidenceDesc3);
			}
			else{
				File evidenceFile = new File(wadDir, "Dr2" + File.separator + "data" + File.separator + "us" + File.separator + "bin" + File.separator + "bin_progress_font_l.pak.zip");

				ZipData evidenceZip = new ZipData(evidenceFile);

				evidenceZip.put("4.pak.zip", names.toData());
				evidenceZip.put("5.pak.zip", desc1.toData());
				evidenceZip.put("6.pak.zip", desc2.toData());
				evidenceZip.put("7.pak.zip", desc3.toData());

				evidenceZip.write(evidenceFile);
			}
		}

		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(50, "Writing header data...");
		DanganModding.sendNotification("Mod Installation", "Writing header data...");

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

		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(55, "Detecting files to write...");
		DanganModding.sendNotification("Mod Installation", "Detecting files to write...");

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();


		for(File f : iterate(new File(wadDir, "Font"), false))
			if(f.getName().endsWith(".ttf")){
				try{
					ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(f)));
				}
				catch(Throwable th){}
			}

		for(int i = 0; i < files.size(); i++){
			File f = files.get(i);

			pOut.println("Handling: " + f.getName());
			if(DanganLauncher.progress != null)
				DanganLauncher.progress.updateProgress(55, "Handling " + f.getName());

			if(f.getName().endsWith(".lin.txt")){
				File compiledFile = new File(f.getAbsolutePath().replace(".lin.txt", ".lin"));

				//				if(compiledFile.exists() && compiledFile.lastModified() >= f.lastModified()){
				//					files.set(i, null);
				//					continue;
				//				}

				pOut.println("Compiling " + f.getName());
				Data compiledLin = DanganModding.compileLin(new Data(f));
				compiledLin.write(compiledFile);
				files.set(i, compiledFile);
			}
			if(f.getName().equalsIgnoreCase("font.pak.zip")){

				try{

					LinkedList<Integer> dialogueCharacters = new LinkedList<Integer>();
					LinkedList<Integer> nonstopCharacters = new LinkedList<Integer>();

					if(Ludus.hasData("dialogue.dat")){
						InputStream in = Ludus.getData("dialogue.dat").getAsInputStream();

						read(in, 4);
						read(in, 4);

						read(in, 4);
						read(in, 4);
						long numChunks = read(in, 4);
						read(in, 4);

						read(in, 4);
						read(in, 4);

						for(int pos = 0; pos < numChunks; pos++){
							long chunk = read(in, 2);
							if(chunk != 65535)
								dialogueCharacters.add(pos);
						}
					}

					if(Ludus.hasData("nonstop.dat")){
						InputStream in = Ludus.getData("nonstop.dat").getAsInputStream();

						read(in, 4);
						read(in, 4);

						read(in, 4);
						read(in, 4);
						long numChunks = read(in, 4);
						read(in, 4);

						read(in, 4);
						read(in, 4);

						for(int pos = 0; pos < numChunks; pos++){
							long chunk = read(in, 2);
							if(chunk != 65535)
								nonstopCharacters.add(pos);
						}
					}

					ZipData zip = new ZipData(new Data(f));

					String[] keys = zip.keySet().toArray(new String[0]);

					for(String file : keys)
						if(file.endsWith(".json")){
							int index = Integer.parseInt(file.replaceAll("\\D", ""));
							JsonObject json = zip.getAsJsonObject(file);

							String fontName = json.get("font").getAsString();
							int size = json.has("size") ? json.get("size").getAsInt() : 28;

							int imageIndex = index % 2 == 0 ? index : index - 1;

							LinkedList<Integer> encoding = new LinkedList<Integer>();

							JsonArray characters = json.getAsJsonArray("characters");

							for(JsonElement element : characters)
								if(element.getAsJsonPrimitive().isString()){
									JsonPrimitive prim = element.getAsJsonPrimitive();
									String s = prim.getAsString();
									if(s.equalsIgnoreCase("UPPERCASE"))
										for(char c : "QWERTYUIOPASDFGHJKLZXCVBNM".toCharArray())
											encoding.add((int) c);

									else if(s.equalsIgnoreCase("lowercase"))
										for(char c : "qwertyuiopasdfghjklzxcvbnm".toCharArray())
											encoding.add((int) c);

									else if(s.equalsIgnoreCase("ASCII"))
										for(int c = 0; c < 94; c++)
											encoding.add(c + 32);

									else if(s.toLowerCase().startsWith("dialo"))
										encoding.addAll(dialogueCharacters);

									else if(s.toLowerCase().startsWith("nonstop") || s.toLowerCase().startsWith("debate"))
										encoding.addAll(nonstopCharacters);

									else
										encoding.add((int) s.charAt(0));
								}
								else if(element.getAsJsonPrimitive().isNumber())
									encoding.add(element.getAsNumber().intValue());

							Collections.sort(encoding);

							int chunks = 65536;
							int startPos = 0x20;

							BufferedImage img = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);

							for(int x = 0; x < img.getWidth(); x++)
								for(int y = 0; y < img.getHeight(); y++)
									img.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());

							Graphics g = img.getGraphics();
							g.setColor(Color.white);

							BufferedImage glyphImg = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
							Graphics gi = glyphImg.getGraphics();
							gi.setColor(Color.white);
							gi.setFont(new Font(fontName, Font.PLAIN, size));
							FontMetrics metrics = gi.getFontMetrics();

							for(int x = 0; x < 1024; x++)
								for(int y = 0; y < 1024; y++)
									glyphImg.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());

							ByteArrayOutputStream chunkData = new ByteArrayOutputStream();

							for(int gl = 0; gl < chunks; gl++){
								if(encoding.contains(gl)){
									write(chunkData, encoding.indexOf(gl), 2);
								}
								else
									write(chunkData, 65535, 2);
							}

							ByteArrayOutputStream glyphTable = new ByteArrayOutputStream();

							HashMap<Integer, Rectangle2D> boxes = new HashMap<Integer, Rectangle2D>();
							HashMap<Integer, Point> positions = new HashMap<Integer, Point>();

							for(int gl : encoding){
								char c = (char) gl;

								String s = c + "";
								Rectangle2D bounds = metrics.getStringBounds(s, gi);

								gi.drawString(s, 128, 128);
								if(bounds.getWidth() <= 0)
									continue;

								BufferedImage glyph = glyphImg.getSubimage(127 + (int) bounds.getX(), 127 + (int) bounds.getY(), (int) bounds.getWidth() + 2, (int) bounds.getHeight() + 2);

								//							int highestPoint = 0;
								//							int lowestPoint = glyph.getHeight() - 1;
								//
								//							int leftmostPoint = 0;
								//							int rightmostPoint = glyph.getWidth() - 1;
								//
								//							boolean found = false;
								//
								//							for(int y = 0; y < glyph.getHeight() && !found; y++){
								//								for(int x = 0; x < glyph.getWidth(); x++){
								//									if(new Color(glyph.getRGB(x, y)).getRed() > 0){
								//										found = true;
								//										highestPoint = y;
								//									}
								//								}
								//							}
								//							
								//							found = false;
								//
								//							for(int y = glyph.getHeight() - 1; y >= 0 && !found; y--){
								//								for(int x = 0; x < glyph.getWidth(); x++){
								//									if(new Color(glyph.getRGB(x, y)).getRed() > 0){
								//										found = true;
								//										lowestPoint = y;
								//									}
								//								}
								//							}

								Rectangle2D realBounds = new Rectangle(0, 0, glyph.getWidth(), glyph.getHeight());
								boxes.put(gl, realBounds);

								for(int xx = 0; xx < 1024; xx++)
									for(int yy = 0; yy < 1024; yy++)
										glyphImg.setRGB(xx, yy, new Color(0, 0, 0, 0).getRGB());

								System.out.println("Boxed " + c);
							}

							LinkedList<Entry<Integer, Rectangle2D>> boxesUpdated = new LinkedList<>();

							boxesUpdated.addAll(boxes.entrySet());

							Collections.sort(boxesUpdated, new Comparator<Entry<Integer, Rectangle2D>>(){

								@Override
								public int compare(Entry<Integer, Rectangle2D> o1, Entry<Integer, Rectangle2D> o2) {
									double h1 = o1.getValue().getHeight();
									double h2 = o2.getValue().getHeight();

									return (h1 > h2 - 0.01 && h1 < h2 + 0.001) ? 0 : h1 < (h2 - 0.01) ? -1 : 1; 
								}
							});

							int x = 0;
							int y = 0;

							for(Entry<Integer, Rectangle2D> entry : boxesUpdated){
								Rectangle2D rect = entry.getValue();
								if((x + rect.getWidth()) > img.getWidth()){
									x = 0;
									y += rect.getHeight();
								}

								positions.put(entry.getKey(), new Point(x, y));

								char c = (char) entry.getKey().intValue();

								String s = c + "";
								Rectangle2D bounds = metrics.getStringBounds(s, gi);

								gi.drawString(s, 128, 128);
								if(bounds.getWidth() <= 0)
									continue;

								BufferedImage glyph = glyphImg.getSubimage(127 + (int) bounds.getX(), 127 + (int) bounds.getY(), (int) bounds.getWidth() + 2, (int) bounds.getHeight() + 2);

								g.drawImage(glyph.getSubimage((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) Math.max(1, rect.getHeight())), x, y, null);
								x += rect.getWidth();

								for(int xx = 0; xx < 1024; xx++)
									for(int yy = 0; yy < 1024; yy++)
										glyphImg.setRGB(xx, yy, new Color(0, 0, 0, 0).getRGB());

								System.out.println("Put " + c + " away");
							}

							for(int gl : encoding){

								write(glyphTable, gl, 2);

								write(glyphTable, positions.get(gl).x, 2);
								write(glyphTable, positions.get(gl).y, 2);
								write(glyphTable, (int) boxes.get(gl).getWidth(), 2);
								write(glyphTable, (int) boxes.get(gl).getHeight(), 2);
								write(glyphTable, 0, 6);
							}

							ByteArrayOutputStream baos = new ByteArrayOutputStream();

							write(baos, magicOne, 4);
							write(baos, magicTwo, 4);

							write(baos, encoding.size(), 4);
							write(baos, startPos + chunkData.size(), 4);
							write(baos, chunks, 4);
							write(baos, startPos, 4);

							write(baos, 73, 4);
							write(baos, 1, 4);

							chunkData.writeTo(baos);
							glyphTable.writeTo(baos);

							baos.close();

							zip.remove(file);
							zip.put(imageIndex + ".png", new Data(img));
							zip.put((imageIndex + 1) + "", new Data(baos.toByteArray()));
						}

					zip.write(new File(f.getAbsolutePath().replace(".zip", ".test.zip")));
					pOut.println("Packing " + f.getName());
					File compiledFile = new File(f.getAbsolutePath().replace(".pak.zip", ".pak"));
					Data compiledPak = DanganModding.compilePak(zip);
					compiledPak.write(compiledFile);
					files.set(i, compiledFile);
				}
				catch(Throwable th){
					th.printStackTrace();
				}
			}
			else if(f.getName().endsWith(".pak.zip")){
				File compiledFile = new File(f.getAbsolutePath().replace(".pak.zip", ".pak"));

				if(compiledFile.exists() && compiledFile.lastModified() >= f.lastModified()){
					files.set(i, null);
					continue;
				}

				pOut.println("Packing " + f.getName());
				ZipData zip = new ZipData(new Data(f));
				Data compiledPak = DanganModding.compilePak(zip);
				compiledPak.write(compiledFile);
				files.set(i, compiledFile);
			}
			if(f.getName().endsWith(".tga.png")){
				File decrypted = new File(f.getAbsolutePath().replace(".tga.png", ".tga"));

				if(decrypted.exists() && decrypted.lastModified() >= f.lastModified()){
					files.set(i, null);
					continue;
				}
				pOut.println("Decrypting " + f.getName());
				Data data = new Data(TGAWriter.writeImage(new Data(f).getAsImage()));
				data.write(decrypted);
				files.set(i, decrypted);
			}
			if(f.getName().endsWith(".dat.json") && f.getName().contains("nonstop")){
				File nonstop = new File(f.getAbsolutePath().replace(".dat.json", ".dat"));
				if(nonstop.exists() && nonstop.lastModified() >= f.lastModified()){
					files.set(i, null);
					continue;
				}
				try{
					pOut.println("Stopping " + f.getName());
					Data data = DanganModding.packNonstop(new Data(f));
					data.write(nonstop);
					files.set(i, nonstop);
				}
				catch(Throwable th){
					th.printStackTrace();
				}
			}
		}

		while(files.remove(null));

		long fileCount = files.size();

		pOut.println("Files: " + fileCount);

		writeInt(out, fileCount);

		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(60, "Writing the information about " + fileCount + " files into the WAD...");
		DanganModding.sendNotification("Mod Installation", "Writing the information about " + fileCount + "...");

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

		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(70, "Writing the information about " + dirs.size() + " directories into the WAD...");
		DanganModding.sendNotification("Mod Installation", "Writing the information about " + dirs.size() + " directories into the WAD...");

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

		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(80, "Packing data back into the WAD");
		DanganModding.sendNotification("Mod Installation", "Packing data back into the WAD...");


		float perFile = 20.0f / files.size();
		float fileNum = 0.0f;

		for(File f : files){
			fileNum += perFile;

			if(DanganLauncher.progress != null)
				DanganLauncher.progress.updateProgress(80 + fileNum, "Writing " + f.getName().substring(0, Math.min(f.getName().length(), 32)) + (f.getName().length() > 32 ? "..." : ""));

			FileInputStream in = new FileInputStream(f);
			byte[] data = new byte[in.available()];
			in.read(data);
			in.close();

			if(tmp)
				f.delete();

			out.write(data);

			pOut.println("Wrote File: " + f);
		}

		//		if(!tmp){
		//			for(File f : files){
		//				if(f.getName().endsWith(".lin")){
		//					f.delete();
		//				}
		//				else if(f.getName().endsWith(".pak")){
		//					f.delete();
		//				}
		//				else if(f.getName().endsWith(".tga"))
		//					f.delete();
		//			}
		//		}

		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(100, "Finished!");
		sendNotification("Mod Installation", "Finished!");

		out.close();
	}

	//TODO: Detect Changes
	public static File[] detectChangesFromWad(File checkWad, File wadDir, PrintStream pOut, boolean tmp) throws IOException{

		internalCounter = 0;
		workingDir = wadDir;
		if(!wadDir.exists())
			throw new IOException("WAD Directory does not exist");

		for(File f : wadDir.listFiles())
			if(f.getName().startsWith("Dr2"))
				isDR1 = false;

		characterIDs.clear();

		if(!isDR1){
			characterIDs.put("Hajime Hinata", 0);
			characterIDs.put("Izuru Kamukura", 0);
			characterIDs.put("Nagito Komaeda", 1);
			characterIDs.put("Byakuya Togami", 2);
			characterIDs.put("Byakuya Twogami", 2);
			characterIDs.put("Imposter", 2);
			characterIDs.put("Impostor", 2);
			characterIDs.put("Gundham Tanaka", 3);
			characterIDs.put("Gundam Tanaka", 3);
			characterIDs.put("Kazuichi Souda", 4);
			characterIDs.put("Kazuichi Soda", 4);
			characterIDs.put("Teruteru Hanamura", 5);
			characterIDs.put("Nekomaru Nidai", 6);
			characterIDs.put("Fuyuhiko Kuzuryu", 7);
			characterIDs.put("Fuyuhiko Kuzuryuu", 7);
			characterIDs.put("Akane Owari", 8);
			characterIDs.put("Chiaki Nanami", 9);
			characterIDs.put("Sonia Nevermind", 10);
			characterIDs.put("Hiyoko Saionji", 11);
			characterIDs.put("Mahiru Koizumi", 12);
			characterIDs.put("Mikan Tsumiki", 13);
			characterIDs.put("Ibuki Mioda", 14);
			characterIDs.put("Monomi", 17);
			characterIDs.put("Usami", 17);
			characterIDs.put("Mechamaru Nidai", 19);
			characterIDs.put("Real Byakuya Togami", 22);

			characterIDs.put("Hajime", 0);
			characterIDs.put("Nagito", 1);
			characterIDs.put("Byakuya", 2);
			characterIDs.put("Gundham", 3);
			characterIDs.put("Gundam", 3);
			characterIDs.put("Kazuichi", 4);
			characterIDs.put("Teruteru", 5);
			characterIDs.put("Nekomaru", 6);
			characterIDs.put("Fuyuhiko", 7);
			characterIDs.put("Akane", 8);
			characterIDs.put("Chiaki", 9);
			characterIDs.put("Sonia", 10);
			characterIDs.put("Hiyoko", 11);
			characterIDs.put("Bitch", 11);
			characterIDs.put("Mahiru", 12);
			characterIDs.put("Mikan", 13);
			characterIDs.put("Ibuki", 14);

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
			characterIDs.put("Peko Pekoyama", 15);
			characterIDs.put("Peko", 15);
			characterIDs.put("MK", 15);
			characterIDs.put("Monokuma", 16);
			characterIDs.put("Monobear", 16);
			characterIDs.put("Real Junko Enoshima", 18);
			characterIDs.put("Makoto Naegi", 20);
			characterIDs.put("Kyoko Kirigiri", 21);
			characterIDs.put("Alter Ego", 24);
			characterIDs.put("???", 41);
			characterIDs.put("Narrator", 42);
			characterIDs.put("None", 63);
		}
		else{
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
			characterIDs.put("Monobear", 15);
			characterIDs.put("Real Junko Enoshima", 16);
			characterIDs.put("Alter Ego", 17);
			characterIDs.put("Genocider Syo", 18);
			characterIDs.put("Jin Kirigiri", 19);
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
			characterIDs.put("None", 63);
		}

		File autoinclude = new File(wadDir, "autoinclude.txt");

		String includeRegex = autoinclude.exists() ? new Data(autoinclude).getAsString() : "";

		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(00, "Beginning WAD Compilation...");
		sendNotification("Mod Packing", "Beginning WAD Compilation");

		JsonObject settingsJson = new JsonObject();

		try{
			Data jsonData = new Data(new File(".spiral_settings"));
			JsonElement element = new JsonParser().parse(jsonData.getAsString());
			if(element.isJsonObject())
				settingsJson = element.getAsJsonObject();
		}
		catch(Throwable th){}

		File people = new File(wadDir, "people.json");
		if(people.exists()){

			characterIDs.clear();

			characterIDs.put("???", 30);
			characterIDs.put("Narrator", 31);

			JsonArray array = new JsonParser().parse(new Data(people).getAsString()).getAsJsonArray();

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

			File tex = new File(wadDir, (isDR1 ? "Dr1" : "Dr2") + File.separator + "data" + File.separator + "us" + File.separator + "cg" + File.separator + (isDR1 ? "tex_cmn_name.pak.zip" : "chara_name.pak.zip"));

			ZipData entries = new ZipData(new Data(tex));

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

					characterIDs.put(initials, index);
					characterIDs.put(name, index);
					characterIDs.put(name.split("\\s+")[0], index);
				}

				if(json.has("sprites")){
					JsonObject sprites = json.getAsJsonObject("sprites");

					HashMap<String, Integer> emotionSet = new HashMap<String, Integer>();

					for(Entry<String, JsonElement> emotion : sprites.entrySet()){
						emotionSet.put(emotion.getKey().toLowerCase(), emotion.getValue().getAsInt());
					}

					emotions.put(index, emotionSet);
				}

				Rectangle2D size = metrics.getStringBounds(name, g);

				BufferedImage img = new BufferedImage((int) size.getWidth(), (int) size.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics nameG = img.getGraphics();
				nameG.setFont(font);
				nameG.setColor(Color.WHITE);
				nameG.drawString(name, 0, (int) (img.getHeight()/3*2.5f));

				entries.put(index + ".png", new Data(img));
			}

			entries.write(tex);
		}

		File music = new File(wadDir, "music.json");
		if(music.exists()){
			JsonObject json = new Data(music).getAsJsonObject();

			for(Entry<String, JsonElement> entry : json.entrySet())
				DanganModding.musicNames.put(entry.getKey(), entry.getValue().getAsInt());
		}

		File animations = new File(wadDir, "animations.json");
		if(animations.exists()){
			JsonObject json = new Data(animations).getAsJsonObject();

			for(Entry<String, JsonElement> entry : json.entrySet())
				DanganModding.animations.put(entry.getKey(), entry.getValue().getAsInt());
		}

		File evidence = new File(wadDir, "evidence.json");
		if(evidence.exists()){
			JsonArray evidenceArray = new JsonParser().parse(new Data(evidence).getAsString()).getAsJsonArray();

			ZipData names;
			ZipData desc1;
			ZipData desc2;
			ZipData desc3;

			if(isDR1){
				File evidenceNames = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "06_KotodamaName.pak.zip");
				File evidenceDesc1 = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "07_KotodamaDesc1.pak.zip");
				File evidenceDesc2 = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "08_KotodamaDesc2.pak.zip");
				File evidenceDesc3 = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "09_KotodamaDesc3.pak.zip");

				names = new ZipData(evidenceNames);
				desc1 = new ZipData(evidenceDesc1);
				desc2 = new ZipData(evidenceDesc2);
				desc3 = new ZipData(evidenceDesc3);
			}
			else{
				File evidenceFile = new File(wadDir, "Dr2" + File.separator + "data" + File.separator + "us" + File.separator + "bin" + File.separator + "bin_progress_font_l.pak.zip");

				ZipData evidenceZip = new ZipData(evidenceFile);

				names = new ZipData(evidenceZip.get("4.pak.zip"));
				desc1 = new ZipData(evidenceZip.get("5.pak.zip"));
				desc2 = new ZipData(evidenceZip.get("6.pak.zip"));
				desc3 = new ZipData(evidenceZip.get("7.pak.zip"));
			}

			for(JsonElement elem : evidenceArray){
				JsonObject json = elem.getAsJsonObject();

				String name 				= json.get("name").getAsString();
				String id 					= json.has("id") ? json.get("id").getAsString() : name;
				String desc 				= json.has("desc") ? General.splitEveryXCharacters(json.get("desc").getAsString(), 30) : "";
				String descUpdated 			= json.has("desc_1") ? General.splitEveryXCharacters(json.get("desc_1").getAsString(), 30) : "";
				String descUpdatedUpdated 	= json.has("desc_2") ? General.splitEveryXCharacters(json.get("desc_2").getAsString(), 30) : "";
				int index 					= json.get("index").getAsInt();
				int def						= json.has("default") ? json.get("default").getAsInt() : 1;

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] txtDataStr = name.getBytes("UTF-16LE");
				byte[] txtData = new byte[txtDataStr.length + 2];

				txtData[0] = (byte) 0xFF;
				txtData[1] = (byte) 0xFE;

				for(int i = 0; i < txtDataStr.length; i++)
					txtData[i+2] = txtDataStr[i];

				out.write(txtData);
				out.write(0);
				out.write(0);

				names.put(index + ".txt", new Data(out.toByteArray()));
				out.reset();

				txtDataStr = desc.getBytes("UTF-16LE");
				txtData = new byte[txtDataStr.length + 2];

				txtData[0] = (byte) 0xFF;
				txtData[1] = (byte) 0xFE;

				for(int i = 0; i < txtDataStr.length; i++)
					txtData[i+2] = txtDataStr[i];

				out.write(txtData);
				out.write(0);
				out.write(0);

				desc1.put(index + ".txt", new Data(out.toByteArray()));
				out.reset();

				txtDataStr = descUpdated.getBytes("UTF-16LE");
				txtData = new byte[txtDataStr.length + 2];

				txtData[0] = (byte) 0xFF;
				txtData[1] = (byte) 0xFE;

				for(int i = 0; i < txtDataStr.length; i++)
					txtData[i+2] = txtDataStr[i];

				out.write(txtData);
				out.write(0);
				out.write(0);

				desc2.put(index + ".txt", new Data(out.toByteArray()));
				out.reset();

				txtDataStr = descUpdatedUpdated.getBytes("UTF-16LE");
				txtData = new byte[txtDataStr.length + 2];

				txtData[0] = (byte) 0xFF;
				txtData[1] = (byte) 0xFE;

				for(int i = 0; i < txtDataStr.length; i++)
					txtData[i+2] = txtDataStr[i];

				out.write(txtData);
				out.write(0);
				out.write(0);

				desc3.put(index + ".txt", new Data(out.toByteArray()));
				out.reset();

				evidenceMap.put(id, index | def << 8);
			}

			if(isDR1){
				File evidenceNames = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "06_KotodamaName.pak.zip");
				File evidenceDesc1 = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "07_KotodamaDesc1.pak.zip");
				File evidenceDesc2 = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "08_KotodamaDesc2.pak.zip");
				File evidenceDesc3 = new File(wadDir, "Dr1" + File.separator + "data" + File.separator + "us" + File.separator + "script" + File.separator + "09_KotodamaDesc3.pak.zip");

				names.write(evidenceNames);
				desc1.write(evidenceDesc1);
				desc2.write(evidenceDesc2);
				desc3.write(evidenceDesc3);
			}
			else{
				File evidenceFile = new File(wadDir, "Dr2" + File.separator + "data" + File.separator + "us" + File.separator + "bin" + File.separator + "bin_progress_font_l.pak.zip");

				ZipData evidenceZip = new ZipData(evidenceFile);

				evidenceZip.put("4.pak.zip", names);
				evidenceZip.put("5.pak.zip", desc1);
				evidenceZip.put("6.pak.zip", desc2);
				evidenceZip.put("7.pak.zip", desc3);

				evidenceZip.write(evidenceFile);
			}
		}
		files.clear();

		LinkedList<File> files = iterate(wadDir, false);

		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(10, "Removing packed files...");
		DanganModding.sendNotification("Mod Packing", "Removing packed files...");

		long timestamp = new File(wadDir, ".timestamp").exists() ? new File(wadDir, ".timestamp").lastModified() : 0L;

		for(int i = 0; i < files.size(); i++){
			File f = files.get(i);

			String name = f.getAbsolutePath().replace(wadDir.getAbsolutePath() + File.separator, "");

			pOut.println("Handling: " + f.getName());

			if(f.lastModified() < timestamp && !name.matches(includeRegex))
				files.set(i, null);
			if(f.getName().endsWith(".lin")){
				files.set(i, null);
			}
			if(f.getName().endsWith(".pak")){
				files.set(i, null);
			}
			if(f.getName().endsWith(".tga")){
				files.set(i, null);
			}
			if(f.getName().endsWith(".dat") && f.getName().contains("nonstop")){
				files.set(i, null);
			}
		}

		while(files.remove(null));

		DDFile wad = new DDFile(checkWad);

		LinkedList<File> changedFiles = new LinkedList<File>();

		float perFile = 90.0f / files.size();
		float prev = 10.0f;

		for(File f : files){
			try{
				String name = f.getAbsolutePath().replace(wadDir.getAbsolutePath() + File.separator, "");

				prev += perFile;
				if(DanganLauncher.progress != null)
					DanganLauncher.progress.updateProgress(prev, "Checking " + name);

				if(wad.fileStructure.containsKey(name)){
					Data fileData = new Data(f);
					Data wadData = new Data(wad.read(name));

					if(!fileData.equals(wadData))
						changedFiles.add(f);
				}
				else if(name.endsWith(".lin.txt") && wad.fileStructure.containsKey(name.replace(".lin.txt", ".lin"))){
					Data fileData = new Data(f);
					Data wadData = DanganModding.linHandling(new Data(wad.read(name.replace(".lin.txt", ".lin"))), pOut);

					if(!fileData.equals(wadData))
						changedFiles.add(f);
				}
				else if(name.endsWith(".pak.zip") && wad.fileStructure.containsKey(name.replace(".pak.zip", ".pak"))){
					Data fileData = new Data(f);
					Data wadData = DanganModding.pakExtraction(new Data(wad.read(name.replace(".pak.zip", ".pak"))));

					if(!fileData.equals(wadData))
						changedFiles.add(f);
				}
				else if(name.endsWith(".tga.png") && wad.fileStructure.containsKey(name.replace(".tga.png", ".tga"))){
					Data fileData = new Data(f);
					BufferedImage img = TGAReader.readImage(wad.read(name.replace(".tga.png", ".tga")));
					Data wadData = new Data(img);

					if(!fileData.equals(wadData))
						changedFiles.add(f);
				}
				else if(name.endsWith(".dat.json") && name.contains("nonstop") && wad.fileStructure.containsKey(name.replace(".dat.json", ".dat"))){
					Data fileData = new Data(f);
					Data wadData = DanganModding.extractNonstop(new Data(wad.read(name.replace(".dat.json", ".dat"))));

					if(!fileData.equals(wadData))
						changedFiles.add(f);
				}
				else
					changedFiles.add(f);
			}
			catch(Throwable th){
				th.printStackTrace();
				System.err.println(f.getAbsolutePath());
			}
		}

		if(DanganLauncher.progress != null)
			DanganLauncher.progress.updateProgress(100, "Finished!");
		sendNotification("Mod Packing", "Finished!");

		return changedFiles.toArray(new File[0]);
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

	//TODO: Handle Lin
	public static Data linHandling(Data lin, PrintStream out) throws IOException{
		DataInputStream din = new DataInputStream(lin.getAsInputStream());

		LinkedList<ScriptEntry> entries = new LinkedList<ScriptEntry>();

		long type = readIntNorm(din);
		long headerSpace = readIntNorm(din);

		long textBlock = 0;
		long size = 0;
		if(type == 1){ //Not Text
			size = readIntNorm(din);
			textBlock = size;
		}
		else if(type == 2){
			textBlock = readIntNorm(din);
			size = readIntNorm(din);
		}

		{


			byte[] data = lin.toArray();

			for(int i = (int) headerSpace; i < textBlock; i++){
				if(data[i] == 0x70){
					i++;
					ScriptEntry entry = new ScriptEntry();
					entry.opCode = data[i];

					int argCount = Opcodes.get((isDR1 ? "DR1" : "DR2")).containsKey(entry.opCode) ? Opcodes.get((isDR1 ? "DR1" : "DR2")).get(entry.opCode).getValue() : -1;

					if(argCount == -1){
						LinkedList<Integer> args = new LinkedList<Integer>();
						while (i+1 < data.length && data[i + 1] != 0x70)
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
			String name = Opcodes.get(isDR1 ? "DR1" : "DR2").containsKey(entries.get(i).opCode) ? Opcodes.get(isDR1 ? "DR1" : "DR2").get(entries.get(i).opCode).getKey() : "";
			if(name != null && name.equalsIgnoreCase("Text"))
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

	//TODO: Compile Lin
	public static Data compileLin(Data data){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream entries = new ByteArrayOutputStream();

		int textLine = 0;

		if(data.getAsString().toLowerCase().trim().startsWith("spiral"))
			return compileSpiralLin(data);

		for(String s : data.getAsStringArray("\\}")){
			s = s.trim();
			if(s.equals(""))
				continue;

			entries.write(0x70);
			byte opCode = -1;
			for(byte code : Opcodes.get((isDR1 ? "DR1" : "DR2")).keySet()){
				if(s.startsWith("0x" + Integer.toHexString(code).toUpperCase() + "{")){
					opCode = code;
					break;
				}
				else if(Opcodes.get((isDR1 ? "DR1" : "DR2")).containsKey(code) && Opcodes.get((isDR1 ? "DR1" : "DR2")).get(code).getKey() != null && s.startsWith(Opcodes.get((isDR1 ? "DR1" : "DR2")).get(code).getKey() + "{")){
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

			String name = Opcodes.get((isDR1 ? "DR1" : "DR2")).containsKey(opCode) && Opcodes.get((isDR1 ? "DR1" : "DR2")).get(opCode).getKey() != null ? Opcodes.get((isDR1 ? "DR1" : "DR2")).get(opCode).getKey() : "";
			if(!name.equalsIgnoreCase("Text") && !name.equalsIgnoreCase("SetTruthBullet") && s.split("\\{").length > 1){
				String[] params = s.split("\\{")[1].split(",");
				for(int i = 0; i < params.length; i++){
					try{
						entries.write(Integer.parseInt(params[i].trim()));
					}
					catch(Throwable th){
						System.err.println(params[i] + ":" + s);
					}
				}
			}
			else if(name.equalsIgnoreCase("SetTruthBullet")){
				String truthBullet = s.substring(15).trim();
				if(evidenceMap.containsKey(truthBullet)){
					int evidence = evidenceMap.get(truthBullet);

					entries.write(evidence % 256);
					entries.write(evidence / 256);
				}
				else{
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
			}
			else if(name.equalsIgnoreCase("Text")){
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

	public static String[] split(String s, String delimiter, int cap){
		LinkedList<String> strings = new LinkedList<String>();

		for(String str : s.split(delimiter + "(?![^\\[]*\\])", cap)){
			if(str.startsWith("\""))
				str = str.substring(1);
			if(str.endsWith("\""))
				str = str.substring(0, str.length() - 1);
			strings.add(str);
		}

		return strings.toArray(new String[0]);
	}

	private static int internalCounter = 0;
	private static int base = 11037;

	//TODO: Compile Spiral Lin
	private static Data compileSpiralLin(Data data) {
		String newLin = "";

		int textCount = 0;
		int state = 0;
		
		String externalFileLocation = "";

		String longText = "";
		boolean handlingLong = false;

		for(String s : data.getAsStringArray("\n")){
			try{
				s = s.trim();
				if(handlingLong){
					if(s.equalsIgnoreCase("[Text:End]")){

						BufferedImage img = new BufferedImage(960, 544, BufferedImage.TYPE_INT_ARGB);

						Graphics g = img.getGraphics();
						g.setFont(new Font("Goodbye Despair", Font.PLAIN, 28));

						int xBoundaries = 20;
						int yBoundaries = 10;

						LinkedList<String> lines = new LinkedList<String>();
						String phrase = "";

						FontMetrics m = g.getFontMetrics();

						Font current = g.getFont();

						for(String line : longText.split("\n")){
							for(String str : split(line, "\\s+", -1)){
								if(str.toLowerCase().startsWith("[font")){
									str = str.trim().replace("[", "").replace("]", "");
									current = new Font(str.split(":")[1], str.split(":").length == 4 ? str.split(":")[3].equalsIgnoreCase("bold") ? Font.BOLD : str.split(":")[3].equalsIgnoreCase("italic") ? Font.ITALIC : Font.PLAIN : Font.PLAIN, str.split(":").length >= 3 ? Integer.parseInt(str.split(":")[2].replaceAll("\\D", "")) : g.getFont().getSize());
									g.setFont(current);
									m = g.getFontMetrics();
									str = "[" + str + "]";
								}
								if(!(str.startsWith("[") && str.endsWith("]")))
									if(m.getStringBounds(phrase.replaceAll("\\[.*\\:.*\\]", "") + str, g).getWidth() > (img.getWidth() - (xBoundaries * 2))){
										for(String p : phrase.split("\n"))
											lines.add(p);
										phrase = "";
									}
								phrase += str + " ";
							}

							lines.add(phrase);
							phrase = "";
						}

						g.setFont(new Font("Goodbye Despair", Font.PLAIN, 28));
						m = g.getFontMetrics();

						int y = yBoundaries;

						Color lastUsed = g.getColor();
						Color lastUsedOverlay = new Color(0, 0, 0, 0);
						BufferedImage lastUsedImg = new BufferedImage(img.getHeight(), img.getWidth(), BufferedImage.TYPE_INT_ARGB);
						Font lastUsedFont = g.getFont();

						for(String tmp : lines){

							String[] processing = new String[]{tmp, null};
							for(String line : processing){
								if(line == null)
									continue;
								int x = xBoundaries;
								if(!line.replaceAll("\\[.*\\:.*\\]", "").trim().isEmpty())
									y += m.getStringBounds(line, g).getHeight();

								if((y + yBoundaries) >= img.getHeight()){
									try{
										ZipData template = new ZipData(Ludus.getDataUnsafe("textTemplate.zip"));

										template.put("1.png", new Data(img));

										File f = new File(workingDir, (isDR1 ? "Dr1" : "Dr2") + File.separator + "data" + File.separator + "us" + File.separator + "flash" + File.separator + "fla_" + (internalCounter + base) + ".pak.zip");
										template.write(f);
										DanganModding.compilePak(template).write(new File(f.getAbsolutePath().replace(".pak.zip", ".pak")));
									}
									catch(Throwable th){
										th.printStackTrace();
									}

									newLin += "Animation{" + (internalCounter + base) / 256 + "," + (internalCounter + base) % 256 + ",0,0,0,0,0,3}";
									newLin += "ChangeUI{1,0}Text{}WaitFrame{}\n0x3{0}\nWaitInput{}\nChangeUI{1,1}";
									internalCounter++;

									img = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
									g = img.getGraphics();

									g.setColor(lastUsedOverlay);

									g.drawImage(lastUsedImg, 0, 0, img.getWidth(), img.getHeight(), null);
									g.fillRect(0, 0, img.getWidth(), img.getHeight());

									g.setFont(lastUsedFont);
									g.setColor(lastUsed);

									x = xBoundaries;
									y = yBoundaries;

									y += m.getStringBounds(line, g).getHeight();
								}

								for(String str : split(line, "\\s+", -1)){
									try{
										if(str.toLowerCase().startsWith("[font")){
											str = str.trim().replace("[", "").replace("]", "");
											current = new Font(str.split(":")[1], str.split(":").length == 4 ? str.split(":")[3].equalsIgnoreCase("bold") ? Font.BOLD : str.split(":")[3].equalsIgnoreCase("italic") ? Font.ITALIC : Font.PLAIN : Font.PLAIN, str.split(":").length >= 3 ? Integer.parseInt(str.split(":")[2].replaceAll("\\D", "")) : g.getFont().getSize());
											g.setFont(current);
											m = g.getFontMetrics();
											lastUsedFont = current;
										}
										else if(str.toLowerCase().startsWith("[colo")){
											String colourComponent = str.replace("[", "").replace("]", "").split(":")[1];

											try{
												Field field = Color.class.getField(colourComponent.toUpperCase().replaceAll("\\s+", "_"));
												g.setColor((Color) field.get(null));
											}
											catch(Throwable th){
												int rc = Integer.parseInt(colourComponent.split(":", 2)[0]);
												int gc = Integer.parseInt(colourComponent.split(":").length >= 2 ? colourComponent.split(":")[1] : "0");
												int bc = Integer.parseInt(colourComponent.split(":").length >= 3 ? colourComponent.split(":")[2] : "0");
												int ac = Integer.parseInt(colourComponent.split(":").length >= 4 ? colourComponent.split(":")[3] : "255");
												g.setColor(new Color(rc, gc, bc, ac));
											}

											lastUsed = g.getColor();
										}
										else if(str.toLowerCase().startsWith("[overlay")){
											Color color = g.getColor();
											if(str.contains(":")){
												String colourComponent = str.replace("[", "").replace("]", "").split(":", 2)[1];

												try{
													Field field = Color.class.getField(colourComponent.toUpperCase().replaceAll("\\s+", "_"));
													color = ((Color) field.get(null));
												}
												catch(Throwable th){
													int rc = Integer.parseInt(colourComponent.split(":")[0]);
													int gc = Integer.parseInt(colourComponent.split(":").length >= 2 ? colourComponent.split(":")[1] : "0");
													int bc = Integer.parseInt(colourComponent.split(":").length >= 3 ? colourComponent.split(":")[2] : "0");
													int ac = Integer.parseInt(colourComponent.split(":").length >= 4 ? colourComponent.split(":")[3] : "255");
													color = (new Color(rc, gc, bc, ac));
												}
											}

											Color orig = g.getColor();
											g.setColor(color);
											g.fillRect(0, 0, img.getWidth(), img.getHeight());
											g.setColor(orig);

											lastUsedOverlay = color;

											//										System.out.println(new Color(img.getRGB(0, 0)));
											//										
											//										for(int imgX = 0; imgX < img.getWidth(); imgX++)
											//											for(int imgY = 0; imgY < img.getHeight(); imgY++){
											//												Color existing = new Color(img.getRGB(imgX, imgY), true);
											//												int newRed = existing.getRed() - (color.getAlpha() * ((256-color.getRed())/256));
											//												int newGreen = existing.getGreen() - (color.getAlpha() * ((256-color.getGreen())/256));
											//												int newBlue = existing.getBlue() - (color.getAlpha() * ((256-color.getBlue())/256));
											//												
											//												int newAlpha = (int) (existing.getAlpha() * 1 + (color.getAlpha() / 256.0f));
											//												
											//												img.setRGB(imgX, imgY, new Color(Math.min(newRed & 0xFF, 255), Math.min(newGreen & 0xFF, 255), Math.min(newBlue & 0xFF, 255), Math.min(newAlpha & 0xFF, 255)).getRGB());
											//											}
											//										
											//										System.out.println(new Color(img.getRGB(0, 0)));
										}
										else if(str.toLowerCase().startsWith("[back")){
											String path = str.trim().replace("[", "]").replace("]", "").split(":")[1].replace("/", File.separator).replace("\\", File.separator);
											File backingImage = new File(DanganModding.workingDir, path);
											//if(!backingImage.exists())

											BufferedImage backImg = new Data(backingImage).getAsImage();

											g.drawImage(backImg, 0, 0, img.getWidth(), img.getHeight(), null);

											lastUsedImg = backImg;
										}
										else if(str.toLowerCase().startsWith("[im")){

											System.out.println(y);
											String[] comps = str.trim().replace("[", "]").replace("]", "").split(":");
											String path = comps[1].replace("/", File.separator).replace("\\", File.separator);
											File backingImage = new File(DanganModding.workingDir, path);
											//if(!backingImage.exists())

											BufferedImage backImg = new Data(backingImage).getAsImage();

											int width = comps.length >= 3 ? Integer.parseInt(comps[2]) : backImg.getWidth();
											int height = comps.length >= 4 ? Integer.parseInt(comps[3]) : backImg.getHeight();

											if((y + height + yBoundaries) >= img.getHeight()){
												processing[1] = line.substring(line.indexOf(str));
												break;
											}

											g.drawImage(backImg, x, y, width, height, null);

											x += width;
											y += height;
										}
										else{
											g.drawString(str + " ", x, y);
											x += m.getStringBounds(str + " ", g).getWidth();
										}
									}
									catch(Throwable th){
										th.printStackTrace();
									}
								}
							}
						}

						try{
							ZipData template = new ZipData(Ludus.getDataUnsafe("textTemplate.zip"));

							template.put("1.png", new Data(img));

							File f = new File(workingDir, (isDR1 ? "Dr1" : "Dr2") + File.separator + "data" + File.separator + "us" + File.separator + "flash" + File.separator + "fla_" + (internalCounter + base) + ".pak.zip");
							template.write(f);
							DanganModding.compilePak(template).write(new File(f.getAbsolutePath().replace(".pak.zip", ".pak")));
						}
						catch(Throwable th){
							th.printStackTrace();
						}

						newLin += "Animation{" + (internalCounter + base) / 256 + "," + (internalCounter + base) % 256 + ",0,0,0,0,0,3}";
						newLin += "ChangeUI{1,0}Text{}WaitFrame{}\n0x3{0}\nWaitInput{}\nChangeUI{1,1}";
						handlingLong = false;
						longText = "";
						internalCounter++;
					}
					else
						longText += s + "\n";
					continue;
				}
				if(s.equalsIgnoreCase("SPIRAL")){
					//newLin += "0x22{1, 0, 1}\n";
					continue;
				}
				if(s.startsWith("0x"))
					newLin += s;
				else if(s.startsWith("[SetTrial]"))
					state = 1;
				else if(s.startsWith("[SetNonstop")){
					state = 2;
					externalFileLocation = (isDR1 ? "Dr1" : "Dr2") + File.separator + "data" + File.separator + "us" + File.separator + "bin" + File.separator + "nonstop_" + s.replace("[", "]").replace("]", "").split(":")[1];
				}
				else if(s.equalsIgnoreCase("[Text:Start]"))
					handlingLong = true;
				else if(s.startsWith("[SetupTextUI]")){
					newLin += "0x22{0, 1, 24}\nChangeUI{11, 1}\nChangeUI{0, 1}\nChangeUI{51, 1}\nChangeUI{1, 1}\nChangeUI{9, 1}";
				}
				else if(s.startsWith("[TrialCam:") || s.startsWith("[TrialCamera:")){
					s = s.replace("[", "").replace("]", "");
					String person = s.split(":", 3)[1].trim();
					String movement = s.split(":", 3).length == 3 ? s.split(":", 3)[2] : "14";

					int charID = characterIDs.containsKey(person.trim()) ? characterIDs.get(person.trim()) : characterIDs.get("???");

					int angle = Integer.parseInt(movement);
					
					newLin += "TrialCamera{" + charID + ", " + (angle / 256) + ", " + (angle % 256) + (isDR1 ? "" : ", 0, 0, 0") + "}";
				}
				else if(s.toLowerCase().startsWith("[flash") || s.toLowerCase().startsWith("[ani")){
					s = s.replace("[", "").replace("]", "");
					String file = s.split(":", 3)[1].trim();
					String frame = s.split(":", 3).length == 3 ? s.split(":", 3)[2] : "255";

					int fileNum = animations.containsKey(file.trim()) ? animations.get(file.trim()) : 0;

					newLin += "Animation{" + fileNum / 256 + "," + fileNum % 256 + ",0,0,0,0,0," + frame + "}";
				}
				else if(s.toLowerCase().startsWith("[music") || s.toLowerCase().startsWith("[bgm")){
					s = s.replace("[", "").replace("]", "");
					String song = s.split(":", 2)[1].trim();

					int music = musicNames.containsKey(song.trim()) ? musicNames.get(song.trim()) : Integer.parseInt(song.replaceAll("\\D", ""));

					newLin += "Music{" + music + ",100,0}";
				}
				else if(s.startsWith("[Sprite:")){
					s = s.replace("[", "").replace("]", "");
					String person = s.split(":")[1].trim();
					int charID = characterIDs.containsKey(person.trim()) ? characterIDs.get(person.trim()) : characterIDs.get("???");

					String sprite = s.split(":")[2].trim();

					if(sprite.equalsIgnoreCase("Clear"))
						newLin += "Sprite{0, 0, 99, 1, 2}";
					else if(sprite.equalsIgnoreCase("Hide"))
						newLin += "Sprite{0, 0, 99, 4, 2}";
					else{

						String spriteState = s.split(":").length > 3 ? s.split(":")[3] : "1";
						String spriteType = s.split(":").length > 4 ? s.split(":")[4] : "2";

						HashMap<String, Integer> emotion = emotions.getOrDefault(charID, new HashMap<String, Integer>());

						if(emotion.containsKey(sprite.toLowerCase()))
							sprite = Integer.toString(emotion.get(sprite.toLowerCase()));

						newLin += "Sprite{" + charID + ", " + charID + ", " + sprite + ", " + spriteState.trim() + ", " + spriteType.trim() + "}";
					}
				}
				else if(s.startsWith("[Bark:") || s.startsWith("[Voice:")){
					s = s.replace("[", "").replace("]", "");
					String person = s.split(":")[1].trim();
					int charID = characterIDs.containsKey(person.trim()) ? characterIDs.get(person.trim()) : characterIDs.get("???");

					String bark = s.split(":")[2].trim();

					String volume = s.split(":").length > 3 ? s.split(":")[3] : "100";

					HashMap<String, Integer> barkSet = barks.getOrDefault(charID, new HashMap<String, Integer>());

					if(barkSet.containsKey(bark.toLowerCase()))
						bark = Integer.toString(barkSet.get(bark.toLowerCase()));

					newLin += "Voice{" + charID + ", 99, 0, " + bark + ", " + volume + "}";
				}
				else if(s.startsWith("[TrialSprite:")){
					s = s.replace("[", "").replace("]", "");
					String person = s.split(":", 3)[1].trim();
					if(person.equalsIgnoreCase("Clear"))
						newLin += "Sprite{0, 0, 0, 0, 0}";
					else{
						String spriteNum = s.split(":", 3)[2].trim();

						int charID = characterIDs.containsKey(person.trim()) ? characterIDs.get(person.trim()) : person.matches("\\d+") ? Integer.parseInt(person.trim()) : characterIDs.get("???");

						HashMap<String, Integer> emotion = emotions.getOrDefault(charID, new HashMap<String, Integer>());

						if(emotion.containsKey(spriteNum.toLowerCase()))
							spriteNum = Integer.toString(emotion.get(spriteNum.toLowerCase()));

						newLin += "Sprite{0, " + charID + ", " + spriteNum + ", 0, 0}";
					}
				}
				else if(s.startsWith("[Wait")){
					int frames = s.indexOf(":") == -1 ? 1 : Integer.parseInt(s.replaceAll("\\D", ""));

					for(int i = 0; i < frames; i++)
						newLin += "WaitFrame{}\n";
					newLin = newLin.trim();
				}
				else if(s.matches(".*\\:.*\\:.*\\:.*")){
					String person = s.split(":", 4)[0];
					String sprite = s.split(":", 4)[1];
					String camera = s.split(":", 4)[2];
					String text = s.split(":", 4)[3].replace("<br>", "\n").trim();

					boolean self = person.contains("(To Self)");
					if(self){
						person = person.replace("(To Self)", "");
						text = "<CLT 4>" + text;
						text = text.replace("<bold>", "<CLT><CLT 3>").replace("</bold>", "<CLT><CLT 4>");
						text = text + "<CLT>";
					}
					else
						text = text.replace("<bold>", "<CLT 3>").replace("</bold>", "<CLT>").replace("<break>", "<CLT 17>").replace("</break>", "<CLT>").replace("<agree>", "<CLT 69>").replace("</agree>", "<CLT>");

					int charID = characterIDs.containsKey(person.trim()) ? characterIDs.get(person.trim()) : person.matches("\\d+") ? Integer.parseInt(person.trim()) : characterIDs.get("???");

					HashMap<String, Integer> emotion = emotions.getOrDefault(charID, new HashMap<String, Integer>());

					if(emotion.containsKey(sprite.toLowerCase()))
						sprite = Integer.toString(emotion.get(sprite.toLowerCase()));

					if(state == 1){
						int angle = Integer.parseInt(camera);
						if(!camera.equalsIgnoreCase("-1"))
							newLin += "TrialCamera{" + charID + ", " + (angle / 256) + ", " + (angle % 256) + (isDR1 ? "" : ", 0, 0, 0") + "}\n";
					}
					else{ //Audio Bark Time
						if(!camera.equalsIgnoreCase("-1") && !camera.equalsIgnoreCase("none")){
							HashMap<String, Integer> barkSet = barks.getOrDefault(charID, new HashMap<String, Integer>());
							if(barkSet.containsKey(camera.toLowerCase()))
								newLin += "Voice{" + charID + ", 99, 0, " + Integer.toString(barkSet.get(camera.toLowerCase())) + ", 100}\n";
						}
					}

					newLin += (sprite.equalsIgnoreCase("none") ? "" : "Sprite{" + (state == 1 ? charID : 0) + ", " + charID + ", " + sprite + ", " + (state == 1 ? "0, 0" : "1, 2") + "}\n") + "0x3{4}\nSpeaker{" + charID + "}\n" + "Text{" + text + "}\n";
					newLin += "WaitFrame{}\n0x3{0}\nWaitInput{}";
					textCount++;
				}
				else if(s.matches(".*\\:.*\\:.*")){

					String person = s.split(":", 3)[0];
					String sprite = s.split(":", 3)[1];
					String text = s.split(":", 3)[2].replace("<br>", "\n").trim();

					boolean self = person.contains("(To Self)");
					if(self){
						person = person.replace("(To Self)", "");
						text = "<CLT 4>" + text;
						text = text.replace("<bold>", "<CLT><CLT 3>").replace("</bold>", "<CLT><CLT 4>");
						text = text + "<CLT>";
					}
					else
						text = text.replace("<bold>", "<CLT 3>").replace("</bold>", "<CLT>").replace("<break>", "<CLT 17>").replace("</break>", "<CLT>").replace("<agree>", "<CLT 69>").replace("</agree>", "<CLT>");

					int charID = characterIDs.containsKey(person.trim()) ? characterIDs.get(person.trim()) : person.matches("\\d+") ? Integer.parseInt(person.trim()) : characterIDs.get("???");

					HashMap<String, Integer> emotion = emotions.getOrDefault(charID, new HashMap<String, Integer>());

					if(emotion.containsKey(sprite.toLowerCase()))
						sprite = Integer.toString(emotion.get(sprite.toLowerCase()));

					if(state == 1)
						if(!newLin.split("\n")[newLin.split("\n").length-1].matches("TrialCamera\\{.*\\}")){
							boolean done = false;
							for(int i = newLin.split("\n").length - 1; i >= 0; i--)
								if(newLin.split("\n")[i].startsWith("Speaker{")){
									if(newLin.split("\n")[i].startsWith("Speaker{" + charID))
										done = true;
									break;
								}
							if(!done)
								newLin += "TrialCamera{" + charID + ", 0, 14" + (isDR1 ? "" : ", 0, 0, 0") + "}\n";
						}
					newLin += "Sprite{" + (state == 1 ? charID : 0) + ", " + charID + ", " + sprite + ", " + (state == 1 ? "0, 0" : "1, 2") + "}\n0x3{4}\nSpeaker{" + charID + "}\n";
					LinkedList<String> lines = new LinkedList<String>();
					String tmp = "";

					for(String txt : text.split("\n")){
						for(String str : txt.split(" ")){
							if((tmp + str).length() >= 60){
								lines.add(tmp.trim());
								tmp = "";
							}
							tmp += str + " ";
						}
					}

					if(!tmp.trim().isEmpty())
						lines.add(tmp.trim());
					newLin += "Text{";
					for(int i = 0; i < lines.size(); i++){
						if(i > 0 && i % 3 == 0)
							newLin += "}\nWaitFrame{}\n0x3{0}\nWaitInput{}\nText{";
						newLin += lines.get(i).trim() + "\n";
					}

					newLin += "}WaitFrame{}\n0x3{0}\nWaitInput{}";
					textCount++;
				}
				else if(s.matches(".*\\:.*")){
					String person = s.split(":", 2)[0];
					String text = s.split(":", 2)[1].replace("<br>", "\n").trim();

					boolean self = person.contains("(To Self)");
					boolean narrator = person.contains("(As Narrator)");
					if(self){
						person = person.replace("(To Self)", "");
						text = "<CLT 4>" + text;
						text = text.replace("<bold>", "<CLT><CLT 3>").replace("</bold>", "<CLT><CLT 4>");
						text = text + "<CLT>";
					}
					else if(narrator){
						person = person.replace("(As Narrator)", "");
						text = "<CLT 6>" + text;
						text = text.replace("<bold>", "<CLT><CLT 6>").replace("</bold>", "<CLT><CLT 6>");
						text = text + "<CLT>";
					}
					else if(person.equalsIgnoreCase("Narrator")){
						text = "<CLT 6>" + text;
						text = text.replace("<bold>", "<CLT><CLT 6>").replace("</bold>", "<CLT><CLT 6>");
						text = text + "<CLT>";
					}
					else
						text = text.replace("<bold>", "<CLT 3>").replace("</bold>", "<CLT>").replace("<break>", "<CLT 17>").replace("</break>", "<CLT>").replace("<agree>", "<CLT 69>").replace("</agree>", "<CLT>");

					int charID = characterIDs.containsKey(person.trim()) ? characterIDs.get(person.trim()) : person.matches("\\d+") ? Integer.parseInt(person.trim()) : characterIDs.get("???");

					newLin += "0x3{4}\nSpeaker{" + charID + "}\n" + "Text{" + text + "}\n";
					newLin += "WaitFrame{}\n0x3{0}\nWaitInput{}";
					textCount++;
				}
				else{
					for(byte code : Opcodes.get((isDR1 ? "DR1" : "DR2")).keySet()){
						if(Opcodes.get((isDR1 ? "DR1" : "DR2")).get(code).getKey() != null && s.startsWith(Opcodes.get((isDR1 ? "DR1" : "DR2")).get(code).getKey())){
							newLin += s;
							break;
						}
					}
				}

				newLin += "\n";
			}
			catch(Throwable th){
				th.printStackTrace();
			}
		}

		newLin = "0x0{" + textCount % 256 + "," + textCount / 256 + "}\n" + newLin.trim() + "\nStopScript{}\nStopScript{}";

		System.out.println(newLin);

		return compileLin(new Data(newLin));
	}

	public static Gson gson = null;

	//TODO: Nonstop
	public static Data extractNonstop(Data nonstop) throws IOException{

		if(gson == null)
			gson = new GsonBuilder().setPrettyPrinting().create();

		InputStream in = nonstop.getAsInputStream();

		long seconds = read(in, 2) * 2;

		int sections = (int) read(in, 2);
		int bytesPerSec = (nonstop.size() - 4) / sections;
		
		System.out.println(bytesPerSec);

		JsonObject debate = new JsonObject();

		debate.addProperty("Time", seconds);

		JsonArray text = new JsonArray();

		for(int i = 0; i < sections; i++){
			JsonObject json = new JsonObject();
			for(int j = 0; j < bytesPerSec / 2; j++){
				json.addProperty((nonstopOpCodes.containsKey(j) ? nonstopOpCodes.get(j) : "0x" + Integer.toHexString(j).toUpperCase()), (int) read(in, 2));
			}
			text.add(json);
		}

		debate.add("text", text);

		return new Data(gson.toJson(debate));
	}

	public static Data packNonstop(Data nonstop) throws IOException{

		JsonObject json = nonstop.getAsJsonObject();

		int perSeg = !isDR1 ? 0x44 : 0x3C;

		int count = json.getAsJsonArray("text").size();

		System.out.println(perSeg + ":" + count);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		write(baos, json.get("Time").getAsInt() / 2, 2);
		write(baos, count, 2);

		int[] tmpData = null;

		JsonArray lines = json.getAsJsonArray("text");

		for(JsonElement e : lines){
			JsonObject obj = e.getAsJsonObject();
			tmpData = new int[perSeg];

			for(int i = 0; i < tmpData.length; i++)
				tmpData[i] = -1;

			System.out.println("---");
			
			for(Entry<String, JsonElement> entry : obj.entrySet()){
				String s = entry.getKey();
				int code = entry.getValue().getAsInt();
				
				for(int i = 0; i < perSeg; i++){
					String key = nonstopOpCodes.containsKey(i) ? nonstopOpCodes.get(i) : "0x" + Integer.toHexString(i).toUpperCase();
					if(s.equalsIgnoreCase(key)){
						tmpData[i] = code;
						break;
					}
				}
			}

			for(int i : tmpData)
				if(i != -1)
					write(baos, i, 2);
		}

		return new Data(baos.toByteArray());
	}
	
	public static Data extractRebuttal(Data nonstop) throws IOException{

		if(gson == null)
			gson = new GsonBuilder().setPrettyPrinting().create();

		InputStream in = nonstop.getAsInputStream();

		long seconds = read(in, 2) * 2;

		int sections = (int) read(in, 2);
		int bytesPerSec = (nonstop.size() - 4) / sections;
		
		System.out.println(bytesPerSec);

		JsonObject debate = new JsonObject();

		debate.addProperty("Time", seconds);

		JsonArray text = new JsonArray();

		for(int i = 0; i < sections; i++){
			JsonObject json = new JsonObject();
			for(int j = 0; j < bytesPerSec / 2; j++){
				json.addProperty((nonstopOpCodes.containsKey(j) ? nonstopOpCodes.get(j) : "0x" + Integer.toHexString(j).toUpperCase()), (int) read(in, 2));
			}
			text.add(json);
		}

		debate.add("text", text);

		return new Data(gson.toJson(debate));
	}

	public static Data packRebuttal(Data nonstop) throws IOException{

		JsonObject json = nonstop.getAsJsonObject();

		int perSeg = 70;

		int count = json.getAsJsonArray("text").size();

		System.out.println(perSeg + ":" + count);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		write(baos, json.get("Time").getAsInt() / 2, 2);
		write(baos, count, 2);

		int[] tmpData = null;

		JsonArray lines = json.getAsJsonArray("text");

		for(JsonElement e : lines){
			JsonObject obj = e.getAsJsonObject();
			tmpData = new int[perSeg];

			for(int i = 0; i < tmpData.length; i++)
				tmpData[i] = -1;

			System.out.println("---");
			
			for(Entry<String, JsonElement> entry : obj.entrySet()){
				String s = entry.getKey();
				int code = entry.getValue().getAsInt();
				
				for(int i = 0; i < perSeg; i++){
					String key = nonstopOpCodes.containsKey(i) ? nonstopOpCodes.get(i) : "0x" + Integer.toHexString(i).toUpperCase();
					if(s.equalsIgnoreCase(key)){
						tmpData[i] = code;
						break;
					}
				}
			}

			for(int i : tmpData)
				if(i != -1)
					write(baos, i, 2);
		}

		return new Data(baos.toByteArray());
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
				//Try reading as targa
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
					zipPak.put(s.replace(".pak.zip", ""), compilePak(new ZipData(zipPak.get(s))));
					zipPak.remove(s);
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
			catch(Throwable th){
				th.printStackTrace();
			}
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
		byte[] data = new byte[len];
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
