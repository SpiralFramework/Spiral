package org.abimon.mods.danganronpa.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JFileChooser;

import org.abimon.omnis.io.Data;
import org.abimon.omnis.io.MultiOutputStream;
import org.abimon.omnis.io.ZipData;
import org.abimon.omnis.io.filefilters.FileExtensionFilter;
import org.abimon.omnis.util.EnumOS;
import org.abimon.omnis.util.General;

public class DanganLauncher {
	static Scanner in;
	static File modsDir = new File("mods");
	static File wadFileDR1;
	static File wadFileDR2;

	public static void main(String[] args) throws Throwable{
		if(!modsDir.exists())
			modsDir.mkdir();

		if(EnumOS.determineOS() == EnumOS.WINDOWS){
			wadFileDR1 = new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Danganronpa Trigger Happy Havoc\\dr1_data.wad");
			if(!wadFileDR1.exists())
				wadFileDR1 = new File("C:\\Program Files\\Steam\\steamapps\\common\\Danganronpa Trigger Happy Havoc\\dr1_data.wad");
		}
		else if(EnumOS.determineOS() == EnumOS.MACOSX)
			wadFileDR1 = new File(EnumOS.determineOS().getStorageLocation("Steam") + "/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad");

		if(!wadFileDR1.exists()){
			JFileChooser jfc = new JFileChooser();

			jfc.setFileFilter(new FileExtensionFilter("wad"));
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int option = jfc.showDialog(null, "Patch");

			if(option == JFileChooser.APPROVE_OPTION){
				wadFileDR1 = jfc.getSelectedFile();
			}

			if(wadFileDR1.getName().endsWith(".app")){
				wadFileDR1 = new File(wadFileDR1, "Contents/Resources/dr1_data.wad");
			}
			if(!wadFileDR1.exists()){
				System.exit(0);
			}

		}

		in = new Scanner(System.in);
		while(in != null){
			handleActions();
		}
	}

	public static void handleActions(){
		try{
			System.out.println("Action (copy all, install, parse, backup, restore, extract, extract lin, extract pak, compile, compile lin, compile pak)");
			System.out.print("> ");
			String action = in.nextLine().trim();

			if(action.equalsIgnoreCase("install"))
				installMods();
			else if(action.equalsIgnoreCase("copy all")){
				System.out.print("File Format: ");
				String format = in.nextLine().trim();
				
				File copyDir = new File("Copy" + File.separator + format);
				if(!copyDir.exists())
					copyDir.mkdirs();
				
				LinkedList<File> files = General.iterate(new File("Danganronpa Extract"), false);
				for(File f : files)
					if(f.getName().endsWith("." + format)){
						System.out.println("Copied " + f);
						File copy = new File(copyDir, f.getName());
						Data data = new Data(f);
						data.write(copy);
					}
			}
			else if(action.equalsIgnoreCase("replace sprites")){
				System.out.print("Sprites to copy: ");
				String spriteset = in.nextLine();
				System.out.print("Sprites to replace: ");
				String replace = in.nextLine();
				System.out.print("Sprites Dir: ");
				File spriteDir = new File(in.nextLine());
				
				for(File f : spriteDir.listFiles()){
					if(f.getName().matches("bustup_" + replace + "_\\d\\d\\.tga")){
						File replacing = new File(f.getAbsolutePath().replace(replace, spriteset));
						if(!replacing.exists())
							replacing = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().length() - 6) + "00.tga");
						System.out.println("Replacing " + f + " with " + replacing);
						
						Data data = new Data(replacing);
						data.write(f);
					}
				}
			}
			else if(action.equalsIgnoreCase("backup")){
				try{
					File backupLoc = new File(wadFileDR1.getAbsolutePath().replace("dr1_data.wad", "dr1_data.backup"));

					if(backupLoc.exists()){
						System.err.println("Backup already exists.");
						return;
					}
					backupLoc.createNewFile();

					FileOutputStream out = new FileOutputStream(backupLoc);
					FileInputStream in = new FileInputStream(wadFileDR1);

					long filesize = in.available();
					long written = 0;

					byte[] buffer = new byte[65536];
					while(true){
						int read = in.read(buffer);
						if(read <= 0)
							break;
						out.write(buffer, 0, read);
						written += read;
						System.out.println(written + "/~" + filesize);
					}

					out.close();
					in.close();

					System.out.println("Backup complete!");
				}
				catch(Throwable th){
					th.printStackTrace();
				}
			}
			else if(action.equalsIgnoreCase("restore")){
				try{
					File backupLoc = new File(wadFileDR1.getAbsolutePath().replace("dr1_data.wad", "dr1_data.backup"));

					if(!backupLoc.exists()){
						System.err.println("Backup doesn't exist.");
						return;
					}
					
					System.out.println(backupLoc);

					FileOutputStream out = new FileOutputStream(wadFileDR1);
					FileInputStream in = new FileInputStream(backupLoc);
					
					long filesize = in.available();
					long written = 0;

					byte[] buffer = new byte[65536];
					while(true){
						int read = in.read(buffer);
						if(read <= 0)
							break;
						out.write(buffer, 0, read);
						written += read;
						System.out.println(written + "/~" + filesize);
					}

					out.close();
					in.close();

					System.out.println("Restore complete!");
				}
				catch(Throwable th){
					th.printStackTrace();
				}
			}
			else if(action.equalsIgnoreCase("extract")){
				try{
					File dir = new File("Danganronpa Extract");
					LinkedList<File> files = General.iterate(dir, false);
					for(File f : files)
						f.delete();
					
					while(true){
						LinkedList<File> remainingDirs = General.iterate(dir, true);
						if(remainingDirs.size() == 0)
							break;
						for(File f : remainingDirs)
							System.out.println("Tried deleting " + f + ": " + f.delete());
					}
					
					dir.delete();
					File log = new File(System.currentTimeMillis() + ".txt");
					DanganModding.extract(wadFileDR1, dir, new PrintStream(new MultiOutputStream(System.out, new FileOutputStream(log))));
				}
				catch(Throwable th){
					th.printStackTrace();
				}
			}
			else if(action.equalsIgnoreCase("compile")){
				try{
					File dir = new File("Danganronpa Extract");
					DanganModding.makeWad(wadFileDR1, dir, System.out, false);
				}
				catch(Throwable th){
					th.printStackTrace();
				}
			}
			else if(action.equalsIgnoreCase("extract lin")){
				System.out.print("LIN File Location: ");
				String loc = in.nextLine();
				File lin = new File(loc);

				File log = new File(System.currentTimeMillis() + ".txt");
				Data linData = DanganModding.linHandling(lin, new PrintStream(new MultiOutputStream(System.out, new FileOutputStream(log))));
				linData.write(new File(lin.getAbsolutePath() + ".txt"));
				linData = null;
				System.out.println("Wrote Text Data!");
			}
			else if(action.equalsIgnoreCase("compile lin")){
				System.out.print("Text File Location: ");
				String loc = in.nextLine();
				File lin = new File(loc);
				
				Data linData = DanganModding.compileLin(new Data(lin));
				linData.write(new File(lin.getAbsolutePath().replace(".txt", "")));
				linData = null;
				System.out.println("Wrote LIN Data!");
			}
			else if(action.equalsIgnoreCase("extract pak")){
				System.out.print("PAK File Location: ");
				String loc = in.nextLine();
				File pak = new File(loc);
				
				ZipData pakData = DanganModding.pakExtraction(new Data(pak));
				pakData.writeToFile(new File(pak.getAbsolutePath() + ".zip"));
				pakData = null;
				System.out.println("Wrote ZIP Data");
			}
			else if(action.equalsIgnoreCase("compile pak")){
				System.out.print("ZIP File Location: ");
				String loc = in.nextLine();
				File pak = new File(loc);
				
				Data pakData = DanganModding.compilePak(new ZipData(new Data(pak)));
				pakData.write(new File(pak.getAbsolutePath().replace(".zip", "")));
				pakData = null;
				System.out.println("Wrote PAK Data");
			}
			else if(action.equalsIgnoreCase("view")){
				DDFile file = new DDFile(wadFileDR1);
				file.getClass();
			}
			else if(action.equalsIgnoreCase("exit")){
				in.close();
				in = null;
			}
			else
				System.out.println(action);
		}
		catch(Throwable th){
			th.printStackTrace();
		}
	}
	
//	private static void extractLin() throws IOException, InterruptedException {
//		LinkedList<File> files = General.iterate(new File("Danganronpa Extract"), false);
//		
//		File f = null;
//		for(int i = 0; i < files.size(); i++){
//			f = files.get(i);
//			
//			if(f.getName().endsWith(".lin")){
//				DanganModding.linHandling(f).write(new File(f.getAbsolutePath() + ".txt"));
//				System.out.println("Extracting " + f.getName());
//			}
//		}
//	}

	public static void installMods(){
		HashMap<String, File> mods = new HashMap<String, File>();
		for(File f : modsDir.listFiles()){
			String name = f.getName();
			if(name.endsWith(".dr1"))
				mods.put(f.getName().substring(0, f.getName().length() - 4).trim(), f);
		}

		if(mods.isEmpty())
			System.out.println("No mods available!");
		else{
			System.out.println("Available Mods: ");
			for(String mod : mods.keySet())
				System.out.println("\t" + mod);

			while(true){
				try{
					System.out.print("> ");
					String line = in.nextLine().trim();
					String action = line.split("\\s+")[0].trim();
					String[] params = line.split("\\s+");
					if(action.equalsIgnoreCase("info")){
						File mod = mods.get(params[1].trim());
						if(mod == null)
							System.err.println("No such mod with name " + params[1].trim());
						else{
							ZipFile zip = new ZipFile(mod.getAbsolutePath());

							BufferedReader bin = new BufferedReader(new InputStreamReader(zip.getInputStream(new ZipEntry("mod.info"))));

							String name = bin.readLine();
							String auth = bin.readLine();
							String desc = bin.readLine();

							System.out.println();

							System.out.println("Name       	: " + name);
							System.out.println("Author     	: " + auth);
							System.out.println("Description	: " + desc);

							System.out.println();

							zip.close();
							bin.close();
						}
					}
					else if(action.equalsIgnoreCase("install")){

						File mod = mods.get(params[1].trim());
						if(mod == null)
							System.err.println("No such mod with name " + params[1].trim());
						else{
							ZipFile zip = new ZipFile(mod.getAbsolutePath());

							System.out.println("Would you like to install " + params[1].trim());

							boolean cont = in.nextLine().toLowerCase().charAt(0) == 'y';

							if(cont){

								DDFile wad = new DDFile(wadFileDR1);
								//Find all files to replace
								LinkedList<String> files = new LinkedList<String>();

								Enumeration<? extends ZipEntry> entries = zip.entries();
								ZipEntry entry = null;

								while(entries.hasMoreElements() && (entry = entries.nextElement()) != null){
									String name = entry.getName();

									if(!name.endsWith(".info") && !name.endsWith("/") && !name.startsWith("__") && !name.contains(".DS_Store"))
										files.add(name);
								}

								for(String s : files)
									wad.changeFile(s, s, zip.getEntry(s).getSize());
								wad.write(wadFileDR1, zip);
							}

							zip.close();
						}
					}
					else if(action.equalsIgnoreCase("exit"))
						break;
				}
				catch(Throwable th){
					th.printStackTrace();
				}
			}
		}
	}
}
