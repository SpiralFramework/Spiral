package org.abimon.mods.danganronpa.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;

import org.abimon.omnis.io.filefilters.FileExtensionFilter;
import org.abimon.omnis.util.EnumOS;

public class DanganLauncher {
	static Scanner in;
	static File modsDir = new File("mods");
	static File wadFileDR1;
	static File wadFileDR2;

	public static void main(String[] args) throws IOException{
		if(!modsDir.exists())
			modsDir.mkdir();

		if(EnumOS.determineOS() == EnumOS.WINDOWS){}
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
		while(true){
			System.out.println("Action (install, parse, backup, restore, extract)");
			System.out.print("> ");
			String action = in.nextLine();

			if(action.equalsIgnoreCase("install"))
				installMods();
			else if(action.equalsIgnoreCase("parse")){
				DDFile file = new DDFile(new File("dr1_data.wad"));
				file.changeFile("PastaGame.txt", "", 100);
				System.out.println(file.directory.search("PastaGame.txt"));
				System.out.println(file.directory.search("dr1_bgm_hca.awb.00000.ogg"));
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
					backupLoc.createNewFile();

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
					DanganModding.extract(wadFileDR1, new File("Danganronpa Extract"), System.out);
				}
				catch(Throwable th){}
			}
			else if(action.equalsIgnoreCase("view")){
				DDFile file = new DDFile(wadFileDR1);
				file.getClass();
			}
			else if(action.equalsIgnoreCase("exit"))
				break;
		}

		in.close();
	}

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

								//Then, create a backup mod file, or edit the one we already have
								File backupModFile = new File(modsDir, "backup.dr1");

								if(!backupModFile.exists()){
									backupModFile.createNewFile();

									ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupModFile));

									for(int i = 0; i < files.size(); i++){
										ZipEntry zipEntry = new ZipEntry(files.get(i));
										byte[] data = wad.read(files.get(i));
										zos.putNextEntry(zipEntry);
										zos.write(data);
									}
									zos.close();
								}
								else{
									try{
										ZipFile backupZip = new ZipFile(backupModFile);

										ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupModFile));

										for(String file : files){
											if(backupZip.getEntry(file) != null)
												continue;
											ZipEntry zipEntry = new ZipEntry(file);

											byte[] data = wad.read(file);

											zos.putNextEntry(zipEntry);
											zos.write(data);

										}
										zos.close();

										backupZip.close();
									}
									catch(Throwable th){
										System.err.println("Error: Could not backup files");
									}
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
