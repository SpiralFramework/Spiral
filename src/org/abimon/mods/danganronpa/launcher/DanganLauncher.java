package org.abimon.mods.danganronpa.launcher;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import org.abimon.mods.danganronpa.launcher.windows.InstallFrame;
import org.abimon.mods.danganronpa.launcher.windows.PackFrame;
import org.abimon.mods.danganronpa.launcher.windows.ProgressFrame;
import org.abimon.mods.danganronpa.launcher.windows.SettingsFrame;
import org.abimon.omnis.io.Data;
import org.abimon.omnis.io.EmptyOutputStream;
import org.abimon.omnis.io.ZipData;
import org.abimon.omnis.ludus.Ludus;
import org.abimon.omnis.util.EnumOS;
import org.abimon.omnis.util.General;
import org.abimon.omnis.util.SteamAppIDs;
import org.abimon.omnis.util.SteamProtocol;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.jgoodies.forms.layout.FormLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.awt.event.ActionEvent;

public class DanganLauncher {

	private JFrame frame;
	private JFrame openFrame;

	public static File modsDir = new File("mods");
	public static File wadFileDR1;
	public static File wadFileDR2;

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

						if(game.equalsIgnoreCase("Danganronpa: Trigger Happy Havoc"))
							wadFile = DanganLauncher.wadFileDR1;
						if(game.equalsIgnoreCase("Danganronpa 2: Goodbye Despair"))
							wadFile = DanganLauncher.wadFileDR2;
						if(json.has("custom_games") && json.getAsJsonObject("custom_games").has(game))
							wadFile = new File(json.getAsJsonObject("custom_games").get(game).getAsString());

						if(wadFile != null && wadFile.exists()){
							try{

								progress = new ProgressFrame("Backing Up WAD", "Backing Up...");

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

						if(game.equalsIgnoreCase("Danganronpa: Trigger Happy Havoc"))
							wadFile = DanganLauncher.wadFileDR1;
						if(game.equalsIgnoreCase("Danganronpa 2: Goodbye Despair"))
							wadFile = DanganLauncher.wadFileDR2;
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

								if(game.equalsIgnoreCase("Danganronpa: Trigger Happy Havoc"))
									wadFile = DanganLauncher.wadFileDR1;
								if(game.equalsIgnoreCase("Danganronpa 2: Goodbye Despair"))
									wadFile = DanganLauncher.wadFileDR2;
								if(json.has("custom_games") && json.getAsJsonObject("custom_games").has(game))
									wadFile = new File(json.getAsJsonObject("custom_games").get(game).getAsString());

								if(wadFile != null && wadFile.exists()){
									File dir = new File(wadFile.getName().replace(".wad", "") + " Extract");
									DanganModding.makeWad(wadFile, dir, new PrintStream(new EmptyOutputStream()), false);
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

								if(game.equalsIgnoreCase("Danganronpa: Trigger Happy Havoc"))
									wadFile = DanganLauncher.wadFileDR1;
								if(game.equalsIgnoreCase("Danganronpa 2: Goodbye Despair"))
									wadFile = DanganLauncher.wadFileDR2;
								if(json.has("custom_games") && json.getAsJsonObject("custom_games").has(game))
									wadFile = new File(json.getAsJsonObject("custom_games").get(game).getAsString());

								if(wadFile != null && wadFile.exists()){
									File dir = new File(wadFile.getName().replace(".wad", "") + " Extract");
									DanganModding.extract(wadFile, dir, new PrintStream(new EmptyOutputStream()));
								}
							}
							else{
								JOptionPane.showMessageDialog(null, "You haven't selected a game to install to yet!", "Error: No Game Found", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						catch(Throwable th){}
					}
				}.start();
			}
		});

		JButton btnNonstopDebates = new JButton("DR2 THING");
		panel.add(btnNonstopDebates, "2, 12, fill, default");
		btnNonstopDebates.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					DanganModding.isDR1 = false;
					File dat = new File("dr2_data Extract/Dr2/data/us/bin/hanron_04_001.dat");
					File compiled = new File("dr2_data Extract/Dr2/data/us/bin/hanron_04_001.dat.json");
					DanganModding.extractNonstop(new Data(dat)).write(compiled);
					//DanganModding.packNonstop(new Data(compiled)).write(dat);
				}
				catch(Throwable th){
					th.printStackTrace();
				}
			}
		});

		JButton btnOpenGame = new JButton("Open Game");
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
								SteamProtocol.openGame(SteamAppIDs.DANGANRONPA_2_GOODBYE_DESPAIR	);
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
