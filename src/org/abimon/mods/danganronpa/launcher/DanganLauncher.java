package org.abimon.mods.danganronpa.launcher;

import apple.applescript.AppleScriptEngineFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import org.abimon.mods.danganronpa.launcher.windows.InstallFrame;
import org.abimon.mods.danganronpa.launcher.windows.PackFrame;
import org.abimon.mods.danganronpa.launcher.windows.ProgressFrame;
import org.abimon.mods.danganronpa.launcher.windows.SettingsFrame;
import org.abimon.omnis.io.Data;
import org.abimon.omnis.io.EmptyOutputStream;
import org.abimon.omnis.io.MultiOutputStream;
import org.abimon.omnis.io.ZipData;
import org.abimon.omnis.ludus.Ludus;
import org.abimon.omnis.util.EnumOS;
import org.abimon.omnis.util.General;
import org.abimon.omnis.util.SteamAppIDs;
import org.abimon.omnis.util.SteamProtocol;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

public class DanganLauncher {

	private JFrame frame;
	private JFrame openFrame;

	public static File modsDir = new File("mods");
	public static File wadFileDR1;
	public static File wadFileDR1Lang;
	public static File wadFileDR2;
	public static File wadFileDR2Lang;

	/**
	 * The progress frame that is in use for the modding components.
	 */
	public static ProgressFrame progress = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		Ludus.registerDataPool(new File("resources"));
		Ludus.registerDataPool(DanganModding.class.getClassLoader());

		try{
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Ludus.getData("DorBlue.ttf").getAsInputStream()));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Ludus.getData("debug_menu.ttf").getAsInputStream()));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Ludus.getData("goodbyeDespair.ttf").getAsInputStream()));
		}
		catch(Throwable th){}

		System.out.println(EnumOS.determineOS());

		if(EnumOS.determineOS() == EnumOS.WINDOWS){
			wadFileDR1 = new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Danganronpa Trigger Happy Havoc\\dr1_data.wad");
			if(!wadFileDR1.exists())
				wadFileDR1 = new File("C:\\Program Files\\Steam\\steamapps\\common\\Danganronpa Trigger Happy Havoc\\dr1_data.wad");

			wadFileDR2 = new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Danganronpa 2 Goodbye Despair\\dr2_data.wad");
			if(!wadFileDR2.exists())
				wadFileDR2 = new File("C:\\Program Files\\Steam\\steamapps\\common\\Danganronpa 2 Goodbye Despair\\dr2_data.wad");
		}
		else if(EnumOS.determineOS() == EnumOS.MACOSX){
			wadFileDR1 = new File(EnumOS.determineOS().getStorageLocation("Steam") + "/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data.wad");
			wadFileDR2 = new File(EnumOS.determineOS().getStorageLocation("Steam") + "/steamapps/common/Danganronpa 2 Goodbye Despair/Danganronpa2.app/Contents/resources/dr2_data.wad");

			File dr1Lang = new File(EnumOS.determineOS().getStorageLocation("Steam") + "/steamapps/common/Danganronpa Trigger Happy Havoc/drlanguage.txt");
			if(dr1Lang.exists()){
				try {
					String lang = new Data(dr1Lang).getAsString();
					switch(lang) {
						case "English":
							wadFileDR1Lang = new File(EnumOS.determineOS().getStorageLocation("Steam") + "/steamapps/common/Danganronpa Trigger Happy Havoc/Danganronpa.app/Contents/Resources/dr1_data_us.wad");
							break;
						case "Japanese":
							wadFileDR1Lang = new File(EnumOS.determineOS().getStorageLocation("Steam") + "/steamapps/common/Danganronpa Trigger Happy Havoc/dr1_data_jp.wad");
							break;
						case "Chinese":
							wadFileDR1Lang = new File(EnumOS.determineOS().getStorageLocation("Steam") + "/steamapps/common/Danganronpa Trigger Happy Havoc/dr1_data_ch.wad");
							break;
					}
				}
				catch(Throwable th){}
			}

			File dr2Lang = new File(EnumOS.determineOS().getStorageLocation("Steam") + "/steamapps/common/Danganronpa 2 Goodbye Despair/drlanguage.txt");
			if(dr2Lang.exists()){
				try {
					String lang = new Data(dr2Lang).getAsString();
					switch(lang) {
						case "English":
							wadFileDR2Lang = new File(EnumOS.determineOS().getStorageLocation("Steam") + "/steamapps/common/Danganronpa 2 Goodbye Despair/Danganronpa2.app/Contents/Resources/dr2_data_us.wad");
							break;
						case "Japanese":
							wadFileDR2Lang = new File(EnumOS.determineOS().getStorageLocation("Steam") + "/steamapps/common/Danganronpa 2 Goodbye Despair/dr2_data_jp.wad");
							break;
						case "Chinese":
							wadFileDR2Lang = new File(EnumOS.determineOS().getStorageLocation("Steam") + "/steamapps/common/Danganronpa 2 Goodbye Despair/dr2_data_ch.wad");
							break;
					}
				}
				catch(Throwable th){}
			}

			try{
				new Socket("localhost", 11038).close();
			}
			catch(Throwable th){
				File applications = new File("/Applications/");
				File execute = new File(applications, "MacOSXNotifier.app/Contents/MacOS/MacOSXNotifier");
				if(execute.exists()){
					try{
						execute.setExecutable(true);
						Runtime.getRuntime().exec(execute.getAbsolutePath());
					}
					catch(Throwable the){}
				}
				else{
					int option = JOptionPane.showConfirmDialog(null, "A custom notification server is available, to give extra information about SPIRAL's installation process. Would you like to install this now?", "Notifications", JOptionPane.YES_NO_OPTION);

					if(option == JOptionPane.YES_OPTION){
						try{
							ZipData notifierData = new ZipData(Ludus.getData("Notifier.zip"));
							notifierData.extract(applications);

							JOptionPane.showMessageDialog(null, "Successfully installed. Running now...");

							execute.setExecutable(true);
							Runtime.getRuntime().exec(execute.getAbsolutePath());
						}
						catch(Throwable the){
							JOptionPane.showMessageDialog(null, "An error occured with the installation: " + the.getMessage());
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							the.printStackTrace(new PrintStream(baos));
							try{
								new Data(baos.toByteArray()).write(new File("Crash Log - " + General.formatDate(new Date(), "dd-mm (min-ss).log")));
							}
							catch(Throwable wtf){
								JOptionPane.showMessageDialog(null, "An error occured while logging the error: " + wtf.getMessage());
							}
						}
					}
				}
			}
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DanganLauncher window = new DanganLauncher();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public DanganLauncher() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 494, 409);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.GROWING_BUTTON_COLSPEC,
				ColumnSpec.decode("center:max(55dlu;pref):grow"),
				FormSpecs.GROWING_BUTTON_COLSPEC,},
				new RowSpec[] {
						FormSpecs.GLUE_ROWSPEC,
						FormSpecs.GLUE_ROWSPEC,
						FormSpecs.GLUE_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.GLUE_ROWSPEC,
						FormSpecs.GLUE_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.GLUE_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						RowSpec.decode("29px"),
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.GLUE_ROWSPEC,}));

		JLabel lblSpiralFramework = new JLabel("SPIRAL Framework");
		panel.add(lblSpiralFramework, "2, 1, center, fill");

		JButton btnInstallMods = new JButton("Install Mods");
		panel.add(btnInstallMods, "2, 3, fill, center");
		btnInstallMods.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(openFrame != null)
					openFrame.setVisible(false);
				openFrame = new InstallFrame();
				openFrame.setVisible(true);
			}

		});

		JButton btnBackup = new JButton("Backup");
		panel.add(btnBackup, "2, 5, fill, default");
		btnBackup.addActionListener(new ActionListener(){
			File wadFile = null;
			File langWad = null;

			public void actionPerformed(ActionEvent e){
				try{
					JsonObject json;
					try{
						Data jsonData = new Data(new File(".spiral_settings"));
						JsonElement element = new JsonParser().parse(jsonData.getAsString());
						if(element.isJsonObject())
							json = element.getAsJsonObject();
						else
							json = new JsonObject();
					}
					catch(Throwable th){
						json = new JsonObject();
					}

					if(json.has("game")){
						String game = json.get("game").getAsString();

						if(game.equalsIgnoreCase("Danganronpa: Trigger Happy Havoc")) {
							wadFile = DanganLauncher.wadFileDR1;
							langWad = DanganLauncher.wadFileDR1Lang;
						}
						if(game.equalsIgnoreCase("Danganronpa 2: Goodbye Despair")) {
							wadFile = DanganLauncher.wadFileDR2;
							langWad = DanganLauncher.wadFileDR2Lang;
						}
						if(json.has("custom_games") && json.getAsJsonObject("custom_games").has(game))
							wadFile = new File(json.getAsJsonObject("custom_games").get(game).getAsString());

						if(wadFile != null && wadFile.exists()){
							try{

								progress = new ProgressFrame("Backing Up WAD", "Backing Up " +  wadFile.getName() + "...");

								new Thread(){
									public void run(){
										try{
											FileOutputStream out = new FileOutputStream(new File(wadFile.getAbsolutePath().replace(".wad", ".wad.backup")));
											FileInputStream in = new FileInputStream(wadFile);

											long filesize = in.available();
											long written = 0;

											byte[] buffer = new byte[65536];
											while(true){
												int read = in.read(buffer);
												if(read <= 0)
													break;
												out.write(buffer, 0, read);
												written += read;
												filesize = ((long) written) + ((long) in.available());
												progress.updateProgress((written * 100 / filesize));
											}

											out.close();
											in.close();
										}
										catch(Throwable th){}


										if(langWad != null && langWad.exists()) {
											progress.updateProgress(0, "Backing Up " + langWad.getName() + "...");
											try {
												FileOutputStream out = new FileOutputStream(new File(langWad.getAbsolutePath().replace(".wad", ".wad.backup")));
												FileInputStream in = new FileInputStream(langWad);

												long filesize = in.available();
												long written = 0;

												byte[] buffer = new byte[65536];
												while (true) {
													int read = in.read(buffer);
													if (read <= 0)
														break;
													out.write(buffer, 0, read);
													written += read;
													filesize = ((long) written) + ((long) in.available());
													progress.updateProgress((written * 100 / filesize));
												}

												out.close();
												in.close();
											} catch (Throwable th) {
												th.printStackTrace();
											}
										}
									}

								}.start();
							}
							catch(Throwable th){
								th.printStackTrace();
							}
						}
					}
					else{
						JOptionPane.showMessageDialog(null, "You haven't selected a game to install to yet!", "Error: No Game Found", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				catch(Throwable th){}
			}
		});

		JButton btnRestore = new JButton("Restore");
		panel.add(btnRestore, "2, 6, fill, center");
		btnRestore.addActionListener(new ActionListener(){
			File wadFile = null;
			File langWad = null;

			public void actionPerformed(ActionEvent e){
				try{
					JsonObject json;
					try{
						Data jsonData = new Data(new File(".spiral_settings"));
						JsonElement element = new JsonParser().parse(jsonData.getAsString());
						if(element.isJsonObject())
							json = element.getAsJsonObject();
						else
							json = new JsonObject();
					}
					catch(Throwable th){
						json = new JsonObject();
					}

					if(json.has("game")){
						String game = json.get("game").getAsString();

						if(game.equalsIgnoreCase("Danganronpa: Trigger Happy Havoc")) {
							wadFile = DanganLauncher.wadFileDR1;
							langWad = DanganLauncher.wadFileDR1Lang;
						}
						if(game.equalsIgnoreCase("Danganronpa 2: Goodbye Despair")) {
							wadFile = DanganLauncher.wadFileDR2;
							langWad = DanganLauncher.wadFileDR2Lang;
						}
						if(json.has("custom_games") && json.getAsJsonObject("custom_games").has(game))
							wadFile = new File(json.getAsJsonObject("custom_games").get(game).getAsString());

						if(wadFile != null && wadFile.exists()){
							try{

								progress = new ProgressFrame("Restoring File", "Restoring...");

								new Thread(){
									public void run(){
										try{
											FileOutputStream out = new FileOutputStream(wadFile);
											FileInputStream in = new FileInputStream(new File(wadFile.getAbsolutePath().replace(".wad", ".wad.backup")));

											long filesize = in.available();
											long written = 0;

											byte[] buffer = new byte[65536];
											while(true){
												int read = in.read(buffer);
												if(read <= 0)
													break;
												out.write(buffer, 0, read);
												written += read;
												filesize = ((long) written) + ((long) in.available());
												progress.updateProgress((written * 100 / filesize));
											}

											out.close();
											in.close();
										}
										catch(Throwable th){}

										if(langWad != null && langWad.exists()) {
											progress.updateProgress(0, "Restoring from " + langWad.getName() + "...");
											try {
												FileOutputStream out = new FileOutputStream(langWad);
												FileInputStream in = new FileInputStream(new File(langWad.getAbsolutePath().replace(".wad", ".wad.backup")));

												long filesize = in.available();
												long written = 0;

												byte[] buffer = new byte[65536];
												while(true){
													int read = in.read(buffer);
													if(read <= 0)
														break;
													out.write(buffer, 0, read);
													written += read;
													filesize = ((long) written) + ((long) in.available());
													progress.updateProgress((written * 100 / filesize));
												}

												out.close();
												in.close();
											} catch (Throwable th) {
												th.printStackTrace();
											}
										}
									}

								}.start();
							}
							catch(Throwable th){
								th.printStackTrace();
							}
						}
					}
					else{
						JOptionPane.showMessageDialog(null, "You haven't selected a game to install to yet!", "Error: No Game Found", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				catch(Throwable th){}
			}
		});

		JButton btnCompile = new JButton("Compile");
		//btnCompile.setEnabled(false);
		btnCompile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DanganLauncher.progress = new ProgressFrame("Danganronpa Compilation", "Beginning Compilation");
				new Thread(){
					public void run(){
						try{
							JsonObject json;
							try{
								Data jsonData = new Data(new File(".spiral_settings"));
								JsonElement element = new JsonParser().parse(jsonData.getAsString());
								if(element.isJsonObject())
									json = element.getAsJsonObject();
								else
									json = new JsonObject();
							}
							catch(Throwable th){
								json = new JsonObject();
							}

							if(json.has("game")){
								String game = json.get("game").getAsString();

								File wadFile = null;
								File langWad = null;

								if(game.equalsIgnoreCase("Danganronpa: Trigger Happy Havoc")) {
									wadFile = DanganLauncher.wadFileDR1;
									langWad = DanganLauncher.wadFileDR1Lang;
								}
								if(game.equalsIgnoreCase("Danganronpa 2: Goodbye Despair")) {
									wadFile = DanganLauncher.wadFileDR2;
									langWad = DanganLauncher.wadFileDR2Lang;
								}

								if(json.has("custom_games") && json.getAsJsonObject("custom_games").has(game))
									wadFile = new File(json.getAsJsonObject("custom_games").get(game).getAsString());

								if(wadFile != null){
									if(langWad != null){
										File dir = new File(wadFile.getName().replace(".wad", "") + " Extract");
										File langDir = new File(".tmp_dir_" + langWad.getName().replace(".wad", ""));
										System.out.println(langDir.mkdir());
										System.out.println(langDir.exists() + ":" + langDir);

										for(File f : General.iterate(dir, false)){
											if(f.getName().startsWith("."))
												continue;
											else if((f.getPath().contains(File.separator + "data" + File.separator) && f.getPath().contains(File.separator + "us" + File.separator))
													|| f.getName().equalsIgnoreCase("evidence.json") || f.getName().equalsIgnoreCase("people.json")){
												Data data = new Data(f);
												progress.updateProgress(0, "Moving " + f.getName());
												File newF = new File(f.getAbsolutePath().replace(dir.getAbsolutePath(), langDir.getAbsolutePath()));
												System.out.println(newF);
												File dirs = new File(f.getAbsolutePath().replace(dir.getAbsolutePath(), langDir.getAbsolutePath()).substring(0, f.getAbsolutePath().replace(dir.getAbsolutePath(), langDir.getAbsolutePath()).lastIndexOf(File.separator)));
												System.out.println(dirs + ":" + dirs.mkdirs());
												data.write(newF);
												newF.setLastModified(f.lastModified());
												f.delete();
											}
										}

										DanganModding.makeWad(wadFile, dir, new PrintStream(new EmptyOutputStream()), false);
										progress.updateProgress(0.0f, "Beginning Compilation of " + langWad.getName());
										DanganModding.makeWad(langWad, langDir, new PrintStream(new EmptyOutputStream()), false);
										for(File f : General.iterate(langDir, false)){
											System.out.println(f);
											progress.updateProgress(99, "Moving " + f.getName());
											Data data = new Data(f);
											File newF = new File(f.getAbsolutePath().replace(langDir.getAbsolutePath(), dir.getAbsolutePath()));
											data.write(newF);
											newF.setLastModified(f.lastModified());
											f.delete();
										}

										progress.updateProgress(100, "Finished!");

										while(General.iterateDirs(langDir).size() > 0)
											for(File f : General.iterateDirs(langDir))
												f.delete();
									}
									else {
										File dir = new File(wadFile.getName().replace(".wad", "") + " Extract");
										DanganModding.makeWad(wadFile, dir, new PrintStream(new EmptyOutputStream()), false);
									}
								}
							}
							else{
								JOptionPane.showMessageDialog(null, "You haven't selected a game to install to yet!", "Error: No Game Found", JOptionPane.ERROR_MESSAGE);
							}
						}
						catch(Throwable th){
							th.printStackTrace();
							System.exit(0);
						}
					}
				}.start();

			}
		});
		panel.add(btnCompile, "2, 7, fill, center");

		JButton btnPackMod = new JButton("Pack Mod");
		panel.add(btnPackMod, "2, 9, fill, default");
		btnPackMod.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				DanganLauncher.progress = new ProgressFrame("Danganronpa Mod Packing", "Beginning Detection");
				new Thread(){
					public void run(){
						try{
							JsonObject json;
							try{
								Data jsonData = new Data(new File(".spiral_settings"));
								JsonElement element = new JsonParser().parse(jsonData.getAsString());
								if(element.isJsonObject())
									json = element.getAsJsonObject();
								else
									json = new JsonObject();
							}
							catch(Throwable th){
								json = new JsonObject();
							}

							if(json.has("game")){
								String game = json.get("game").getAsString();

								File wadFile = null;

								if(game.equalsIgnoreCase("Danganronpa: Trigger Happy Havoc"))
									wadFile = DanganLauncher.wadFileDR1;
								if(game.equalsIgnoreCase("Danganronpa 2: Goodbye Despair"))
									wadFile = DanganLauncher.wadFileDR2;
								if(json.has("custom_games") && json.getAsJsonObject("custom_games").has(game))
									wadFile = new File(json.getAsJsonObject("custom_games").get(game).getAsString());

								wadFile = new File(wadFile.getAbsolutePath().replace(".wad", ".wad.backup"));

								if(wadFile != null && wadFile.exists()){
									File dir = new File(wadFile.getName().replace(".wad.backup", "") + " Extract");
									File[] changedFiles = DanganModding.detectChangesFromWad(wadFile, dir, new PrintStream(new EmptyOutputStream()), false);
									Arrays.sort(changedFiles);
									openFrame = new PackFrame(dir, changedFiles);
									openFrame.setVisible(true);
								}
							}
							else{
								JOptionPane.showMessageDialog(null, "You haven't selected a game to install to yet!", "Error: No Game Found", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						catch(Throwable th){
							th.printStackTrace();
						}
					}
				}.start();
			}
		});

		JButton btnExtractFiles = new JButton("Extract Files");
		panel.add(btnExtractFiles, "2, 10, fill, center");
		btnExtractFiles.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				DanganLauncher.progress = new ProgressFrame("Danganronpa Extraction", "Beginning Extraction");
				new Thread(){
					public void run(){
						try{
							JsonObject json;
							try{
								Data jsonData = new Data(new File(".spiral_settings"));
								JsonElement element = new JsonParser().parse(jsonData.getAsString());
								if(element.isJsonObject())
									json = element.getAsJsonObject();
								else
									json = new JsonObject();
							}
							catch(Throwable th){
								json = new JsonObject();
							}

							if(json.has("game")){
								String game = json.get("game").getAsString();

								File wadFile = null;
								File langWad = null;

								if(game.equalsIgnoreCase("Danganronpa: Trigger Happy Havoc")) {
									wadFile = DanganLauncher.wadFileDR1;
									langWad = wadFileDR1Lang;
								}
								if(game.equalsIgnoreCase("Danganronpa 2: Goodbye Despair")) {
									wadFile = DanganLauncher.wadFileDR2;
									langWad = wadFileDR2Lang;
								}
								if(json.has("custom_games") && json.getAsJsonObject("custom_games").has(game))
									wadFile = new File(json.getAsJsonObject("custom_games").get(game).getAsString());

								if(wadFile != null && wadFile.exists()){
									File dir = new File(wadFile.getName().replace(".wad", "") + " Extract");
									File log = new File(System.currentTimeMillis() + ".txt");
									DanganModding.extract(wadFile, dir, new PrintStream(new FileOutputStream(log)));
								}

								if(wadFile != null && wadFile.exists() && langWad != null && langWad.exists()){
									DanganLauncher.progress.updateProgress(0.0f, "Beginning Extraction of " + langWad);
									File dir = new File(wadFile.getName().replace(".wad", "") + " Extract");
									File log = new File(System.currentTimeMillis() + ".txt");
									DanganModding.extract(langWad, dir, new PrintStream(new FileOutputStream(log)));
								}
							}
							else{
								JOptionPane.showMessageDialog(null, "You haven't selected a game to install to yet!", "Error: No Game Found", JOptionPane.ERROR_MESSAGE);
							}
						}
						catch(Throwable th){}
					}
				}.start();
			}
		});

		JButton btnOpenGame = new JButton("Open Game");
		JButton btnNonstopDebates = new JButton("DR2 THING");
		panel.add(btnNonstopDebates, "2, 12, fill, default");
		btnNonstopDebates.addActionListener(e -> {
			try {
				File modify = new File("dr2_data Extract/Dr2/data/us/bin/nonstop_04_001.dat.json");

				try {
//								File folderOne = new File("0");
//								File folderTwo = new File("1");
//								File output = new File("0xE - 0 vs 1");
//
//								if(!output.exists())
//									output.mkdir();
//
//								int difference = 0;
//								double highestPercent = 0.0d;
//
//								BufferedImage test = new Data(new File(folderOne, "1.png")).getAsImage();
//
//								double numPixels = test.getWidth() * test.getHeight();
//
//								//Synchronisation first
//								for(int i = 1; i < 185 && highestPercent < 100; i++){
//									BufferedImage img = new Data(new File(folderTwo, i + ".png")).getAsImage();
//									double same = 0;
//
//									for(int x = 0; x < img.getWidth(); x++)
//										for(int y = 0; y < img.getHeight(); y++)
//											if(img.getRGB(x, y) == test.getRGB(x, y))
//												same++;
//
//									double percent = same * 100.0 / numPixels;
//									System.out.println("Checked " + i + ": " + percent + "( " + same + ":" + numPixels + ")");
//
//									if(percent > highestPercent){
//										highestPercent = percent;
//										difference = i;
//									}
//								}
//
//								System.out.println("There's a difference of " + difference);
//
//								for(File screen : folderOne.listFiles()){
//									System.out.println("Comparing " + screen);
//									if(screen.getName().endsWith(".png") && new File(folderTwo, (Integer.parseInt(screen.getName().replaceAll("\\D", "")) + difference) + ".png").exists()) {
//										BufferedImage one = new Data(screen).getAsImage();
//										BufferedImage two = new Data(new File(folderTwo, (Integer.parseInt(screen.getName().replaceAll("\\D", "")) + difference) + ".png")).getAsImage();
//										BufferedImage remove = new BufferedImage(one.getWidth(), one.getHeight(), BufferedImage.TYPE_INT_ARGB);
//										BufferedImage highlight = new BufferedImage(one.getWidth(), one.getHeight(), BufferedImage.TYPE_INT_ARGB);
//
//										Graphics g = highlight.getGraphics();
//										g.setColor(new Color(128, 0, 128, 128));
//
//										for(int x = 0; x < one.getWidth(); x++) {
//											for (int y = 0; y < one.getHeight(); y++) {
//												int oneRGB = one.getRGB(x, y);
//												int twoRGB = two.getRGB(x, y);
//
//												highlight.setRGB(x, y, oneRGB);
//
//												if(oneRGB != twoRGB) {
//													remove.setRGB(x, y, oneRGB);
//													g.fillRect(x, y, 1, 1);
//												}
//												else
//													remove.setRGB(x, y, new Color(0, 255, 0, 0).getRGB());
//											}
//										}
//
//										new Data(remove).write(new File(output, "Remove - " + screen.getName()));
//										new Data(highlight).write(new File(output, "Highlight - " + screen.getName()));
//									}
//								}
//
//								if(output.exists())
//									return;

					JsonObject json = new Data(modify).getAsJsonObject();

					String value = "0xE";

					Robot robot = new Robot();

					robot.setAutoDelay(250);

					Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

					ScriptEngine engine = new AppleScriptEngineFactory().getScriptEngine();

					System.out.println("Engine: " + engine);

					for (int i = 0; i < 65536; i++) {
						try {
							System.out.println("Running Test #" + i + " for value " + value);
							json.getAsJsonArray("text").get(0).getAsJsonObject().addProperty(value, i);
							new Data(json.toString()).write(modify);
//										btnCompile.getActionListeners()[0].actionPerformed(null);
//										while(!DanganLauncher.progress.isComplete())
//											Thread.sleep(100);
//
//										Thread.sleep(4000);
//
//										while(!DanganLauncher.progress.isComplete())
//											Thread.sleep(100);
//
//										while(new File(".tmp_dir_dr2_data_us").exists()){
//											Thread.sleep(1000);
//										}
//
//										DanganLauncher.progress.pressFinish();

							Thread.sleep(100);

							btnOpenGame.getActionListeners()[0].actionPerformed(null);

							Thread.sleep(4000);

							new Data(robot.createScreenCapture(screenSize)).write(new File("Intro.png"));

							engine.eval("tell application \"Danganronpa2\" to activate");
							System.out.println("Intros take time");
							robot.keyPress(KeyEvent.VK_ESCAPE);
							Thread.sleep(100);
							robot.keyRelease(KeyEvent.VK_ESCAPE);

							robot.keyPress(KeyEvent.VK_ESCAPE);
							Thread.sleep(100);
							robot.keyRelease(KeyEvent.VK_ESCAPE);

							Thread.sleep(6000);

							BufferedImage trial = robot.createScreenCapture(screenSize);
							Color topCorner = new Color(trial.getRGB(trial.getWidth() - 10, 60));
							int presses = 0;
							while (topCorner.getBlue() > 128 || topCorner.getRed() < 40) {
								robot.keyPress(KeyEvent.VK_ENTER);
								Thread.sleep(125);
								robot.keyRelease(KeyEvent.VK_ENTER);
								Thread.sleep(10);
								trial = robot.createScreenCapture(screenSize);
								topCorner = new Color(trial.getRGB(trial.getWidth() - 10, 60));

								System.out.println(topCorner);
								new Data(trial).write(new File("Trial-" + presses++ + ".png"));
							}

							new Data(trial).write(new File("Trial.png"));

							Thread.sleep(100);

							robot.keyPress(KeyEvent.VK_DOWN);
							Thread.sleep(100);
							robot.keyRelease(KeyEvent.VK_DOWN);

							Thread.sleep(100);

							robot.keyPress(KeyEvent.VK_DOWN);
							Thread.sleep(100);
							robot.keyRelease(KeyEvent.VK_DOWN);

							Thread.sleep(500);

							robot.keyPress(KeyEvent.VK_ENTER);
							Thread.sleep(100);
							robot.keyRelease(KeyEvent.VK_ENTER);

							Thread.sleep(500);

							robot.keyPress(KeyEvent.VK_ENTER);
							Thread.sleep(100);
							robot.keyRelease(KeyEvent.VK_ENTER);

							File dir = new File(value + " - " + i);

							dir.mkdir();

							Thread.sleep(1000 * 5);

							engine.eval("tell application \"OBS\" to activate");
							engine.eval("tell application \"System Events\" to key code 105");
							engine.eval("tell application \"Danganronpa2\" to activate");

							Thread.sleep(1000 * 20);

							engine.eval("tell application \"OBS\" to activate");
							engine.eval("tell application \"System Events\" to key code 107");
							engine.eval("tell application \"IntelliJ IDEA\" to activate");

							File recorded = null;

							for (File f : new File("/Users/undermybrella/Movies/SPIRAL").listFiles())
								if (!f.getName().startsWith(".")) {
									recorded = f;
									break;
								}


							Process find = Runtime.getRuntime().exec("ps -e");

							Scanner findIn = new Scanner(find.getInputStream());
							String line = null;

							while (findIn.hasNext() && (line = findIn.nextLine()) != null) {
								if (line.contains("Danganronpa2.app")) {
									Runtime.getRuntime().exec("kill -9 " + line.split("\\s+")[0]);
									System.out.println("Killed");
									break;
								}
							}


							if (recorded != null) {
								File newFile = new File(value + " - " + i + ".mp4");
								if (newFile.exists())
									newFile.delete();


								newFile.createNewFile();
								FileInputStream in = new FileInputStream(recorded);
								FileOutputStream out = new FileOutputStream(newFile);

								byte[] transfer = new byte[8192];

								while (true) {
									int read = in.read(transfer);
									if (read <= 0)
										break;
									out.write(transfer, 0, read);
								}

								recorded.delete();
							}

							break;
						} catch (Throwable th) {
							th.printStackTrace();
							break;
						}
					}
				} catch (Throwable th) {
					th.printStackTrace();
				}
			} catch (Throwable th) {
				th.printStackTrace();
			}
		});

		panel.add(btnOpenGame, "2, 13, fill, default");
		btnOpenGame.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					JsonObject json;
					try{
						Data jsonData = new Data(new File(".spiral_settings"));
						JsonElement element = new JsonParser().parse(jsonData.getAsString());
						if(element.isJsonObject())
							json = element.getAsJsonObject();
						else
							json = new JsonObject();
					}
					catch(Throwable th){
						json = new JsonObject();
					}

					if(json.has("game")){
						String game = json.get("game").getAsString();

						File wadFile = null;

						if(game.equalsIgnoreCase("Danganronpa: Trigger Happy Havoc"))
							wadFile = DanganLauncher.wadFileDR1;
						if(game.equalsIgnoreCase("Danganronpa 2: Goodbye Despair"))
							wadFile = DanganLauncher.wadFileDR2;
						if(json.has("custom_games") && json.getAsJsonObject("custom_games").has(game))
							wadFile = new File(json.getAsJsonObject("custom_games").get(game).getAsString());

						if(wadFile != null && wadFile.exists()){
							DDFile dd = new DDFile(wadFile);

							if(dd.isDR1)
								SteamProtocol.openGame(SteamAppIDs.DANGANRONPA_TRIGGER_HAPPY_HAVOC);
							else
								SteamProtocol.openGame(SteamAppIDs.DANGANRONPA_2_GOODBYE_DESPAIR);
						}
					}
					else{
						JOptionPane.showMessageDialog(null, "You haven't selected a game to install to yet!", "Error: No Game Found", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				catch(Throwable th){
					th.printStackTrace();
				}
			}
		});

		JButton btnSettings = new JButton("Settings");
		panel.add(btnSettings, "2, 15, fill, center");
		btnSettings.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(openFrame != null)
					openFrame.setVisible(false);
				openFrame = new SettingsFrame();
				openFrame.setVisible(true);
			}

		});

		JButton btnClose = new JButton("Close");
		panel.add(btnClose, "2, 17");
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});


	}
}
