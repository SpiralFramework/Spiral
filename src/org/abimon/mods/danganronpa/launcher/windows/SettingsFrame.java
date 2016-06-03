package org.abimon.mods.danganronpa.launcher.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.abimon.mods.danganronpa.launcher.DanganLauncher;
import org.abimon.omnis.io.Data;
import org.abimon.omnis.io.filefilters.FileExtensionFilter;
import org.abimon.omnis.util.EnumOS;

import com.jgoodies.forms.layout.FormLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jgoodies.common.collect.LinkedListModel;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map.Entry;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JLabel;

public class SettingsFrame extends JFrame {

	private static final long serialVersionUID = -5964407125850948758L;
	private JPanel contentPane;
	private JFrame self;

	public LinkedListModel<String> settingList = new LinkedListModel<String>();
	public LinkedListModel<String> valueList = new LinkedListModel<String>();

	public JScrollPane settingsPane;
	public JScrollPane valuesPane;

	public JList<String> settingsList;
	public JList<String> valuesList;

	public JLabel lblTest;

	public JsonObject json;

	/**
	 * Create the frame.
	 */
	@SuppressWarnings("unchecked")
	public SettingsFrame() {
		self = this;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 480);
		contentPane = new JPanel();
		contentPane.setBackground(Color.GREEN);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

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

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),
				FormSpecs.DEFAULT_COLSPEC,
				ColumnSpec.decode("center:default:grow"),},
				new RowSpec[] {
						FormSpecs.RELATED_GAP_ROWSPEC,
						RowSpec.decode("default:grow"),
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,}));

		settingList.add("Name Font");
		settingList.add("Game");

		settingsPane = new JScrollPane();
		panel.add(settingsPane, "1, 2, fill, fill");

		settingsList = new JList<String>(settingList);
		settingsPane.setViewportView(settingsList);
		settingsList.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					valueList.clear();
					lblTest.setText("");
					String option = settingsList.getSelectedValue();
					if(option.equalsIgnoreCase("Name Font")){
						GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
						for(Font font : ge.getAllFonts()){
							valueList.add(font.getName());
							if(json.has("name_font") && json.get("name_font").getAsString().equalsIgnoreCase(font.getName())){
								valuesList.setSelectedIndex(valueList.indexOf(font.getName()));
								valuesPane.getViewport().setViewPosition(new Point(0, (int) (valuesList.getCellBounds(0, valuesList.getSelectedIndex() - 10).getHeight())));
							}
						}

						lblTest.setText("Makoto Naegi");
					}
					else if(option.equalsIgnoreCase("Game")){
						if(DanganLauncher.wadFileDR1 != null && DanganLauncher.wadFileDR1.exists())
							valueList.add("Danganronpa: Trigger Happy Havoc");
						if(DanganLauncher.wadFileDR2 != null && DanganLauncher.wadFileDR2.exists())
							valueList.add("Danganronpa 2: Goodbye Despair");

						JsonObject customGames = new JsonObject();
						if(json.has("custom_games"))
							customGames = json.getAsJsonObject("custom_games");

						for(Entry<String, JsonElement> entry : customGames.entrySet())
							valueList.add(entry.getKey());

						valueList.add("Add...");

						if(json.has("game"))
							for(String game : valueList){
								try{
									if(game != null && game.equalsIgnoreCase(json.get("game").getAsString())){
										valuesList.setSelectedIndex(valueList.indexOf(game));
										valuesPane.getViewport().setViewPosition(new Point(0, (int) (valuesList.getCellBounds(0, valuesList.getSelectedIndex() - 10).getHeight())));
									}
								}
								catch(Throwable th){}
							}
					}
				}
			}

		});

		valuesPane = new JScrollPane();
		panel.add(valuesPane, "3, 2, fill, fill");

		valuesList = new JList<String>(valueList);
		valuesPane.setViewportView(valuesList);
		valuesList.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					String option = settingsList.getSelectedValue();
					if(option.equalsIgnoreCase("Name Font")){
						GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
						Font font = null;
						for(Font f : ge.getAllFonts())
							if(f.getName().equalsIgnoreCase(valuesList.getSelectedValue())){
								font = f;
								break;
							}

						if(font != null){
							lblTest.setFont(font.deriveFont(lblTest.getFont().getSize2D()));
							json.addProperty("name_font", font.getName());
						}
					}
					else if(option.equalsIgnoreCase("Game")){
						String game = valuesList.getSelectedValue();
						if(game != null && game.equalsIgnoreCase("Add...")){
							File dir = EnumOS.getHomeLocation();
							if(EnumOS.determineOS() == EnumOS.WINDOWS){
								dir = new File("C:\\Program Files (x86)\\Steam\\steamapps\\common");
								if(!dir.exists())
									dir = new File("C:\\Program Files\\Steam\\steamapps\\common");
							}
							else if(EnumOS.determineOS() == EnumOS.MACOSX){
								dir = new File(EnumOS.determineOS().getStorageLocation("Steam"), "steamapps/common");
							}

							if(dir == null || !dir.exists())
								dir = EnumOS.getHomeLocation();

							JFileChooser jfc = new JFileChooser(dir);

							jfc.setFileFilter(new FileExtensionFilter("wad"));
							jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
							int jfcOption = jfc.showDialog(null, "Patch");

							if(jfcOption == JFileChooser.APPROVE_OPTION){
								File wadFile = jfc.getSelectedFile();
								if(wadFile.getName().endsWith(".app")){
									if(new File(wadFile, "Contents/Resources/dr1_data.wad").exists())
										wadFile = new File(wadFile, "Contents/Resources/dr1_data.wad");
									else
										wadFile = new File(wadFile, "Contents/Resources/dr2_data.wad");
								}

								if(wadFile.exists()){
									String name = JOptionPane.showInputDialog("Name this game");

									JsonObject customGames = new JsonObject();
									if(json.has("custom_games"))
										customGames = json.getAsJsonObject("custom_games");

									customGames.addProperty(name, wadFile.getAbsolutePath());

									json.add("custom_games", customGames);

									valueList.clear();
									if(DanganLauncher.wadFileDR1.exists())
										valueList.add("Danganronpa: Trigger Happy Havoc");
									if(DanganLauncher.wadFileDR2.exists())
										valueList.add("Danganronpa 2: Goodbye Despair");

									for(Entry<String, JsonElement> entry : customGames.entrySet())
										valueList.add(entry.getKey());

									valueList.add("Add...");
								}
							}
						}
						else if(game != null){
							json.addProperty("game", game);
						}
					}
				}
			}

		});

		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				self.setVisible(false);
			}
		});

		lblTest = new JLabel("");
		lblTest.setFont(lblTest.getFont().deriveFont(Font.PLAIN, 28));
		panel.add(lblTest, "3, 3, 1, 3, center, default");

		JButton btnRestoreDefaults = new JButton("Restore Defaults");
		panel.add(btnRestoreDefaults, "1, 5");
		panel.add(btnClose, "2, 5");
		btnRestoreDefaults.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				json.addProperty("name_font", "Goodbye Despair");
				json.add("custom_games", new JsonObject());
				json.addProperty("game", "Danganronpa: Trigger Happy Havoc");
			}
		});

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					Data data = new Data(json.toString());
					data.write(new File(".spiral_settings"));
				}
				catch(Throwable th){}
			}
		});
		panel.add(btnSave, "2, 3");
	}

}
