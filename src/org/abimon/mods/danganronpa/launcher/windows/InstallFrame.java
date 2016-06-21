package org.abimon.mods.danganronpa.launcher.windows;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.zip.ZipFile;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.abimon.mods.danganronpa.launcher.DDFile;
import org.abimon.mods.danganronpa.launcher.DanganLauncher;
import org.abimon.omnis.io.Data;
import org.abimon.omnis.io.ZipData;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jgoodies.common.collect.LinkedListModel;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class InstallFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JFrame self;

	LinkedListModel<String> installedList = new LinkedListModel<String>();
	LinkedListModel<String> availableList = new LinkedListModel<String>();

	LinkedList<ZipFile> newMods = new LinkedList<ZipFile>();
	LinkedList<String> removedMods = new LinkedList<String>();

	public JList<String> availableMods;
	public JList<String> installedMods;

	/**
	 * Create the frame.
	 */
	@SuppressWarnings("unchecked")
	public InstallFrame() {

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
					DDFile wad = new DDFile(wadFile);

					if(wad.fileStructure.containsKey("installed_mods.json")){
						JsonArray modList = new Data(wad.read("installed_mods.json")).getAsJsonArray();

						for(JsonElement elem : modList){
							JsonObject mod = elem.getAsJsonObject();

							installedList.add(mod.get("name").getAsString() + " v" + mod.get("version").getAsString());
						}
					}
				}
			}
			else{
				JOptionPane.showMessageDialog(null, "You haven't selected a game to install to yet!", "Error: No Game Found", JOptionPane.ERROR_MESSAGE);
				return;
			}

			File modDir = new File("mods");
			
			if(!modDir.exists())
				modDir.mkdir();

			for(File f : modDir.listFiles()){

				if(f.isDirectory() || !(f.getName().endsWith(".drs") || f.getName().endsWith(".dr1") || f.getName().endsWith(".dr2") || f.getName().endsWith(".zip")))
					continue;

				String name = f.getName().substring(0, f.getName().lastIndexOf('.'));
				String version = f.length() < 1000 * 10 ? new Data(f).getAsMD5Hash() : new Data(f.getAbsolutePath()).getAsMD5Hash();;

				System.out.println(f);
				ZipData zData = new ZipData(f);
				for(String key : zData.keySet())
					if(key.endsWith(".info")){
						try{
							JsonObject mod = zData.getAsJsonObject(key);
							name = mod.has("name") ? mod.get("name").getAsString() : f.getName().substring(0, f.getName().lastIndexOf('.'));
							version = mod.has("version") ? mod.get("version").getAsString() : f.length() < 1000 * 10 ? new Data(f).getAsMD5Hash() : new Data(f.getAbsolutePath()).getAsMD5Hash();
						}
						catch(Throwable th){}
					}

				if(!installedList.contains(name + " v" + version))
					availableList.add(name + " v" + version);
			}
		}
		catch(Throwable th){
			th.printStackTrace();
		}

		self = this;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
				new RowSpec[] {
						FormSpecs.RELATED_GAP_ROWSPEC,
						RowSpec.decode("default:grow"),
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,}));

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "2, 2, fill, fill");

		availableMods = new JList<String>(availableList);
		scrollPane.setViewportView(availableMods);

		JPanel panel = new JPanel();
		contentPane.add(panel, "4, 2, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("75px"),},
				new RowSpec[] {
						RowSpec.decode("78px"),
						RowSpec.decode("29px"),
						FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
						RowSpec.decode("29px"),}));

		JButton button = new JButton("->");
		panel.add(button, "1, 2, left, center");
		button.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String modToAdd = availableMods.getSelectedValue();

				if(modToAdd != null){
					File modDir = new File("mods");
					try{

						for(File f : modDir.listFiles()){

							if(f.isDirectory() || !(f.getName().endsWith(".drs") || f.getName().endsWith(".dr1") || f.getName().endsWith(".dr2") || f.getName().endsWith(".zip")))
								continue;

							String name = f.getName().substring(0, f.getName().lastIndexOf('.'));
							String version = f.length() < 1000 * 10 ? new Data(f).getAsMD5Hash() : new Data(f.getAbsolutePath()).getAsMD5Hash();;

							ZipData zData = new ZipData(f);
							for(String key : zData.keySet()){
								if(key.endsWith(".info")){
									try{
										JsonObject mod = zData.getAsJsonObject(key);
										name = mod.has("name") ? mod.get("name").getAsString() : f.getName().substring(0, f.getName().lastIndexOf('.'));
										version = mod.has("version") ? mod.get("version").getAsString() : f.length() < 1000 * 10 ? new Data(f).getAsMD5Hash() : new Data(f.getAbsolutePath()).getAsMD5Hash();
									}
									catch(Throwable th){
										System.err.println(key);
									}
								}
							}

							if((name + " v" + version).equalsIgnoreCase(modToAdd) && !installedList.contains(modToAdd)){
								availableList.remove(modToAdd);
								installedList.add(modToAdd);
								removedMods.remove(modToAdd);
								newMods.add(new ZipFile(f));
								return;
							}
						}
						removedMods.remove(modToAdd);
						availableList.remove(modToAdd);
						installedList.add(modToAdd);
					}
					catch(Throwable th){
						th.printStackTrace();
					}
				}
			}

		});

		JButton button_1 = new JButton("<-");
		panel.add(button_1, "1, 4, left, center");
		button_1.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String modToRemove = installedMods.getSelectedValue();

				if(modToRemove != null){
					File modDir = new File("mods");
					try{

						for(File f : modDir.listFiles()){

							if(f.isDirectory() || !(f.getName().endsWith(".drs") || f.getName().endsWith(".dr1") || f.getName().endsWith(".dr2") || f.getName().endsWith(".zip")))
								continue;

							String name = f.getName().substring(0, f.getName().lastIndexOf('.'));
							String version = f.length() < 1000 * 10 ? new Data(f).getAsMD5Hash() : new Data(f.getAbsolutePath()).getAsMD5Hash();;

							ZipData zData = new ZipData(f);
							for(String key : zData.keySet())
								if(key.endsWith(".info")){
									JsonObject mod = zData.getAsJsonObject(key);
									name = mod.has("name") ? mod.get("name").getAsString() : f.getName().substring(0, f.getName().lastIndexOf('.'));
									version = mod.has("version") ? mod.get("version").getAsString() : f.length() < 1000 * 10 ? new Data(f).getAsMD5Hash() : new Data(f.getAbsolutePath()).getAsMD5Hash();
								}

							if((name + " v" + version).equalsIgnoreCase(modToRemove) && !availableList.contains(modToRemove)){
								availableList.add(modToRemove);
								installedList.remove(modToRemove);
								removedMods.add(modToRemove);
								newMods.remove(f);
								return;
							}
						}
						removedMods.add(modToRemove);
						installedList.remove(modToRemove);
						availableList.add(modToRemove);
					}
					catch(Throwable th){
						th.printStackTrace();
					}
				}
			}

		});

		JScrollPane scrollPane_1 = new JScrollPane();
		contentPane.add(scrollPane_1, "6, 2, fill, fill");

		installedMods = new JList<String>(installedList);
		scrollPane_1.setViewportView(installedMods);

		JButton btnClose = new JButton("Close");
		contentPane.add(btnClose, "4, 4");

		JButton btnInstall = new JButton("Install All Mods");
		btnInstall.addActionListener(new ActionListener() {
			File wadFile = null;

			public void actionPerformed(ActionEvent e) {
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
							DanganLauncher.progress = new ProgressFrame("Installing Mods", "Reading existing WAD file...");
							DanganLauncher.progress.setVisible(true);
							new Thread(){
								public void run(){
									try{
										DDFile file = new DDFile(wadFile);
										file.write(wadFile, removedMods.toArray(new String[0]), newMods.toArray(new ZipFile[0]));
										self.setVisible(false);
									}
									catch(Throwable th){
										th.printStackTrace();
									}
								}
							}.start();
						}
						catch(Throwable th){
							th.printStackTrace();
						}
					}
				}
			}
		});
		contentPane.add(btnInstall, "6, 4");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				self.setVisible(false);
			}
		});
	}

}
