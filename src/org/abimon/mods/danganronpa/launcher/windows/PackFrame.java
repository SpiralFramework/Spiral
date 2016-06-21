package org.abimon.mods.danganronpa.launcher.windows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.abimon.mods.danganronpa.launcher.DanganLauncher;
import org.abimon.omnis.io.Data;
import org.abimon.omnis.ludus.Ludus;
import org.abimon.omnis.util.General;

import javax.swing.JTree;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.JCheckBox;
import com.jgoodies.forms.layout.FormLayout;
import com.google.gson.JsonObject;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import java.awt.Choice;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.JButton;

public class PackFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private Choice displayMode;

	File dir = null;
	File[] changedFiles = null;

	Hashtable<String, Object> enabled = new Hashtable<String, Object>();
	Hashtable<String, Object> disabled = new Hashtable<String, Object>();
	Hashtable<String, Object> all = new Hashtable<String, Object>();

	private HashMap<String, JsonObject> configurations = new HashMap<String, JsonObject>();

	JPanel panel_1;

	JsonObject json = Ludus.getDataUnsafe("dictionary.json").getAsJsonObject();

	JLabel providedName;
	JTextPane providedDesc;

	public static void sortTree(DefaultMutableTreeNode root) {
		Enumeration<?> e = root.depthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			if (!node.isLeaf()) {
				sort2(node);
			}
		}
	}

	public static Comparator< DefaultMutableTreeNode> tnc = new Comparator< DefaultMutableTreeNode>() {
		@Override public int compare(DefaultMutableTreeNode a, DefaultMutableTreeNode b) {
			//Sort the parent and child nodes separately:
			if (a.isLeaf() && !b.isLeaf()) {
				return 1;
			} else if (!a.isLeaf() && b.isLeaf()) {
				return -1;
			} else {
				String sa = a.getUserObject().toString();
				String sb = b.getUserObject().toString();
				return sa.compareToIgnoreCase(sb);
			}
		}
	};

	public static void sort2(DefaultMutableTreeNode parent) {
		int n = parent.getChildCount();
		for (int i = 0; i < n - 1; i++) {
			int min = i;
			for (int j = i + 1; j < n; j++) {
				if (tnc.compare((DefaultMutableTreeNode) parent.getChildAt(min),
						(DefaultMutableTreeNode) parent.getChildAt(j)) > 0) {
					min = j;
				}
			}
			if (i != min) {
				MutableTreeNode a = (MutableTreeNode) parent.getChildAt(i);
				MutableTreeNode b = (MutableTreeNode) parent.getChildAt(min);
				parent.insert(b, i);
				parent.insert(a, min);
			}
		}
	}

	public String fileSelected = "";
	private HashMap<String, File> fileMap = new HashMap<String, File>();
	private DefaultMutableTreeNode root = null;
	private DefaultTreeModel model;

	/**
	 * Create the frame.
	 * @param changedFiles 
	 * @param dir 
	 */
	@SuppressWarnings("unchecked")
	public PackFrame(File dir, File[] changedFiles) {

		this.dir = dir;
		this.changedFiles = changedFiles;

		long totalSize = 0L;

		for(File f : changedFiles){

			try {
				totalSize += new Data(f).size();
			} catch (IOException e1) {}

			String name = f.getAbsolutePath().replace(dir.getAbsolutePath() + File.separator, "");

			if(!name.contains(File.separator))
				name = "meta" + File.separator + name;

			Hashtable<String, Object> parent = null;
			String[] dirs = name.replace(f.getName(), "").split("\\" + File.separator);
			for(String s : dirs){
				if(parent == null){
					if(enabled.containsKey(s))
						parent = (Hashtable<String, Object>) enabled.get(s);
					else{
						parent = new Hashtable<String, Object>();
						enabled.put(s, parent);
					}
				}
				else{
					if(!parent.containsKey(s))
						parent.put(s, new Hashtable<String, Object>());
					parent = (Hashtable<String, Object>) parent.get(s);
				}
			}

			//if(parent != null)
			parent.put(name, "End");
			fileMap.put(name, f);
		}

		for(File f : General.iterate(dir, false)){
			if(f.isDirectory())
				continue;
			String name = f.getAbsolutePath().replace(dir.getAbsolutePath() + File.separator, "");
			Hashtable<String, Object> parent = null;
			String[] dirs = name.contains(File.separator) ? name.substring(0, name.lastIndexOf(File.separator)).split("\\" + File.separator) : new String[0];
			for(String s : dirs){
				if(parent == null){
					if(all.containsKey(s))
						parent = (Hashtable<String, Object>) all.get(s);
					else{
						parent = new Hashtable<String, Object>();
						all.put(s, parent);
					}
				}
				else{
					if(!parent.containsKey(s))
						parent.put(s, new Hashtable<String, Object>());
					parent = (Hashtable<String, Object>) parent.get(s);
				}
			}

			if(parent == null)
				all.put(name, "End");
			else
				parent.put(name, "End");
			if(!fileMap.containsKey(name))
				fileMap.put(name, f);
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 863, 490);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("left:420px:grow"),},
				new RowSpec[] {
						RowSpec.decode("258px:grow"),}));

		JPanel panel = new JPanel();
		contentPane.add(panel, "1, 1, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("left:448px"),
				ColumnSpec.decode("145px:grow"),},
				new RowSpec[] {
						FormSpecs.UNRELATED_GAP_ROWSPEC,
						RowSpec.decode("294px:grow"),
						RowSpec.decode("32px"),}));

		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, "1, 2, fill, fill");

		JScrollPane scrollPane_1 = new JScrollPane();
		JCheckBox chckbxEnabled = new JCheckBox("Enabled");

		root = new DefaultMutableTreeNode("SPIRAL");
		DynamicUtilTreeNode.createChildren(root, enabled);
		sortTree(root);
		model = new DefaultTreeModel(root);
		JTree tree = new JTree(model);
		JButton btnDisableAll = new JButton("Disable All");
		scrollPane.setViewportView(tree);
		tree.addTreeSelectionListener(new TreeSelectionListener(){
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if(e.getPath().getLastPathComponent().toString().contains(".")){
					String filename = e.getPath().getLastPathComponent().toString();
					fileSelected = filename;

					if(json.has(filename)){
						providedName.setText(json.getAsJsonObject(filename).get("name").getAsString());
						providedDesc.setText(json.getAsJsonObject(filename).get("desc").getAsString());
					}
					else{
						providedName.setText(filename);
						providedDesc.setText("Hey there! \nThis file hasn't been catalogued yet, \nwhich means it's either a custom file, \na file we haven't gotten around to cataloging, \nor it's something which should be pretty obvious.\nIf you feel this is an issue, feel free to drop an issue at\nhttps://github.com/Undermybrella/Spiral/issues");
					}

					chckbxEnabled.setSelected(displayMode.getSelectedItem().equalsIgnoreCase("Enabled"));
					btnDisableAll.setVisible(tree.getSelectionCount() > 1);
					providedName.scrollRectToVisible(new Rectangle(0, 0, 2, 2));

					panel_1.setVisible(true);
					scrollPane_1.getViewport().scrollRectToVisible(new Rectangle(0, 0, 1, 1));
				}
				else
					panel_1.setVisible(false);
			}
		});

		panel.add(scrollPane_1, "2, 2, fill, fill");

		panel_1 = new JPanel();
		scrollPane_1.setViewportView(panel_1);
		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
				new RowSpec[] {
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						RowSpec.decode("default:grow"),}));


		JLabel lblKb = new JLabel((totalSize / 1000.0f / 1000.0f) + " mB");
		chckbxEnabled.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {

				if(chckbxEnabled.isSelected()){
					String name = fileSelected;

					Hashtable<String, Object> parent = null;
					String[] dirs = name.contains(File.separator) ? name.substring(0, name.lastIndexOf(File.separator)).split("\\" + File.separator) : new String[0];
					for(String s : dirs){
						if(parent == null){
							if(enabled.containsKey(s))
								parent = (Hashtable<String, Object>) enabled.get(s);
							else{
								parent = new Hashtable<String, Object>();
								enabled.put(s, parent);
							}
						}
						else{
							if(!parent.containsKey(s))
								parent.put(s, new Hashtable<String, Object>());
							parent = (Hashtable<String, Object>) parent.get(s);
						}
					}

					if(parent == null)
						enabled.put(name, "End");
					else
						parent.put(name, "End");

					Hashtable<String, Object> otherparent = null;
					for(String s : dirs){
						if(otherparent == null){
							if(disabled.containsKey(s))
								otherparent = (Hashtable<String, Object>) disabled.get(s);
							else{
								otherparent = new Hashtable<String, Object>();
								disabled.put(s, otherparent);
							}
						}
						else{
							if(!otherparent.containsKey(s))
								otherparent.put(s, new Hashtable<String, Object>());
							otherparent = (Hashtable<String, Object>) otherparent.get(s);
						}
					}

					if(otherparent == null)
						disabled.remove(name);
					else
						otherparent.remove(name);
				}
				else{
					String name = fileSelected;

					Hashtable<String, Object> parent = null;
					String[] dirs = name.contains(File.separator) ? name.substring(0, name.lastIndexOf(File.separator)).split("\\" + File.separator) : new String[0];
					for(String s : dirs){
						if(parent == null){
							if(disabled.containsKey(s))
								parent = (Hashtable<String, Object>) disabled.get(s);
							else{
								parent = new Hashtable<String, Object>();
								disabled.put(s, parent);
							}
						}
						else{
							if(!parent.containsKey(s))
								parent.put(s, new Hashtable<String, Object>());
							parent = (Hashtable<String, Object>) parent.get(s);
						}
					}

					if(parent == null)
						disabled.put(name, "End");
					else
						parent.put(name, "End");

					Hashtable<String, Object> otherparent = null;
					for(String s : dirs){
						if(otherparent == null){
							if(enabled.containsKey(s))
								otherparent = (Hashtable<String, Object>) enabled.get(s);
							else{
								otherparent = new Hashtable<String, Object>();
								enabled.put(s, otherparent);
							}
						}
						else{
							if(!otherparent.containsKey(s))
								otherparent.put(s, new Hashtable<String, Object>());
							otherparent = (Hashtable<String, Object>) otherparent.get(s);
						}
					}

					if(otherparent == null)
						enabled.remove(name);
					else
						otherparent.remove(name);
				}

				if(displayMode.getSelectedItem().equalsIgnoreCase("Disabled")){
					DefaultMutableTreeNode root = new DefaultMutableTreeNode("SPIRAL");
					DynamicUtilTreeNode.createChildren(root, disabled);
					sortTree(root);
					tree.setModel(new DefaultTreeModel(root, false));

					long totalSize = 0L;

					for(String file : allKeys(disabled)){
						try {
							totalSize += new Data(fileMap.get(file)).size();
						} catch (IOException e1) {}
					}

					lblKb.setText((totalSize / 1000.0f / 1000.0f) + " mB");
				}
				else{
					DefaultMutableTreeNode root = new DefaultMutableTreeNode("SPIRAL");
					DynamicUtilTreeNode.createChildren(root, enabled);
					sortTree(root);
					tree.setModel(new DefaultTreeModel(root, false));

					long totalSize = 0L;

					for(String file : allKeys(enabled)){
						try {
							if(fileMap.containsKey(file))
								totalSize += new Data(fileMap.get(file)).size();
						} catch (IOException e1) {}
					}

					lblKb.setText((totalSize / 1000.0f / 1000.0f) + " mB");
				}

				//				TreePath curr = null;
				//
				//				while(expandedPaths.hasMoreElements() && (curr = expandedPaths.nextElement()) != null){
				//					DefaultMutableTreeNode iterating = root;
				//
				//					for(int index = 1; index < curr.getPathCount(); index++){
				//						Enumeration<?> children = iterating.children();
				//						Object child = null;
				//
				//						while(children.hasMoreElements() && (child = children.nextElement()) != null){
				//							if(child.toString().equals(curr.getPath()[index].toString())){
				//								iterating = (DefaultMutableTreeNode) child;
				//								System.out.println("Arrays: " + Arrays.toString(Arrays.copyOfRange(curr.getPath(), 0, index+1)));
				//								tree.expandPath(new TreePath(Arrays.copyOfRange(curr.getPath(), 0, index+1)));
				//								break;
				//							}
				//						}
				//					}
				//
				//					tree.expandPath(curr);
				//					System.out.println(curr);
				//				}

				panel_1.setVisible(false);
			}
		});
		chckbxEnabled.setSelected(true);
		panel_1.add(chckbxEnabled, "2, 2");

		btnDisableAll.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(displayMode.getSelectedItem().equalsIgnoreCase("Disabled") || displayMode.getSelectedItem().equalsIgnoreCase("All")){

					for(TreePath path : tree.getSelectionPaths()){
						String name = path.getLastPathComponent().toString();

						Hashtable<String, Object> parent = null;
						String[] dirs = name.contains(File.separator) ? name.substring(0, name.lastIndexOf(File.separator)).split("\\" + File.separator) : new String[0];
						for(String s : dirs){
							if(parent == null){
								if(enabled.containsKey(s))
									parent = (Hashtable<String, Object>) enabled.get(s);
								else{
									parent = new Hashtable<String, Object>();
									enabled.put(s, parent);
								}
							}
							else{
								if(!parent.containsKey(s))
									parent.put(s, new Hashtable<String, Object>());
								parent = (Hashtable<String, Object>) parent.get(s);
							}
						}

						if(parent == null)
							enabled.put(name, "End");
						else
							parent.put(name, "End");

						Hashtable<String, Object> otherparent = null;
						for(String s : dirs){
							if(otherparent == null){
								if(disabled.containsKey(s))
									otherparent = (Hashtable<String, Object>) disabled.get(s);
								else{
									otherparent = new Hashtable<String, Object>();
									disabled.put(s, otherparent);
								}
							}
							else{
								if(!otherparent.containsKey(s))
									otherparent.put(s, new Hashtable<String, Object>());
								otherparent = (Hashtable<String, Object>) otherparent.get(s);
							}
						}

						if(otherparent == null)
							disabled.remove(name);
						else
							otherparent.remove(name);
					}

					DefaultMutableTreeNode root = new DefaultMutableTreeNode("SPIRAL");
					DynamicUtilTreeNode.createChildren(root, disabled);
					sortTree(root);
					tree.setModel(new DefaultTreeModel(root, false));

					long totalSize = 0L;

					for(String file : allKeys(disabled)){
						try {
							totalSize += new Data(fileMap.get(file)).size();
						} catch (IOException e1) {}
					}

					lblKb.setText((totalSize / 1000.0f / 1000.0f) + " mB");
				}
				else{
					for(TreePath path : tree.getSelectionPaths()){
						String name = path.getLastPathComponent().toString();

						Hashtable<String, Object> parent = null;
						String[] dirs = name.contains(File.separator) ? name.substring(0, name.lastIndexOf(File.separator)).split("\\" + File.separator) : new String[0];
						for(String s : dirs){
							if(parent == null){
								if(disabled.containsKey(s))
									parent = (Hashtable<String, Object>) disabled.get(s);
								else{
									parent = new Hashtable<String, Object>();
									disabled.put(s, parent);
								}
							}
							else{
								if(!parent.containsKey(s))
									parent.put(s, new Hashtable<String, Object>());
								parent = (Hashtable<String, Object>) parent.get(s);
							}
						}

						if(parent == null)
							disabled.put(name, "End");
						else
							parent.put(name, "End");

						Hashtable<String, Object> otherparent = null;
						for(String s : dirs){
							if(otherparent == null){
								if(enabled.containsKey(s))
									otherparent = (Hashtable<String, Object>) enabled.get(s);
								else{
									otherparent = new Hashtable<String, Object>();
									enabled.put(s, otherparent);
								}
							}
							else{
								if(!otherparent.containsKey(s))
									otherparent.put(s, new Hashtable<String, Object>());
								otherparent = (Hashtable<String, Object>) otherparent.get(s);
							}
						}

						if(otherparent == null)
							enabled.remove(name);
						else
							otherparent.remove(name);
					}

					DefaultMutableTreeNode root = new DefaultMutableTreeNode("SPIRAL");
					DynamicUtilTreeNode.createChildren(root, enabled);
					sortTree(root);
					tree.setModel(new DefaultTreeModel(root, false));

					long totalSize = 0L;

					for(String file : allKeys(enabled)){
						try {
							if(fileMap.containsKey(file))
								totalSize += new Data(fileMap.get(file)).size();
						} catch (IOException e1) {}
					}

					lblKb.setText((totalSize / 1000.0f / 1000.0f) + " mB");
				}

			}
		});
		panel_1.add(btnDisableAll, "6, 2");

		JSeparator separator_1 = new JSeparator();
		panel_1.add(separator_1, "2, 4");

		JLabel lblName = new JLabel("Name:");
		panel_1.add(lblName, "2, 6");

		JSeparator separator = new JSeparator();
		panel_1.add(separator, "4, 6");

		providedName = new JLabel("Provided Name Here");
		panel_1.add(providedName, "6, 6");

		JLabel lblDesc = new JLabel("Desc:");
		panel_1.add(lblDesc, "2, 8");

		providedDesc = new JTextPane();
		providedDesc.setText("Provided Desc Here");
		panel_1.add(providedDesc, "6, 8, 1, 3, fill, fill");
		panel_1.setVisible(false);

		JPanel panel_2 = new JPanel();
		panel.add(panel_2, "1, 3, fill, fill");
		panel_2.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,},
				new RowSpec[] {
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,}));

		displayMode = new Choice();
		panel_2.add(displayMode, "2, 2");
		displayMode.add("Enabled");
		displayMode.add("Disabled");
		displayMode.add("All");
		displayMode.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e){
				if(e.getItem().equals("Disabled")){
					DefaultMutableTreeNode root = new DefaultMutableTreeNode("SPIRAL");
					DynamicUtilTreeNode.createChildren(root, disabled);
					sortTree(root);
					tree.setModel(new DefaultTreeModel(root, false));

					long totalSize = 0L;

					for(String file : allKeys(disabled)){
						try {
							if(fileMap.containsKey(file))
								totalSize += new Data(fileMap.get(file)).size();
						} catch (IOException e1) {}
					}

					lblKb.setText((totalSize / 1000.0f / 1000.0f) + " mB");
					btnDisableAll.setText("Enable All");
				}
				else if(e.getItem().equals("Enabled")){
					DefaultMutableTreeNode root = new DefaultMutableTreeNode("SPIRAL");
					DynamicUtilTreeNode.createChildren(root, enabled);
					sortTree(root);
					tree.setModel(new DefaultTreeModel(root, false));

					long totalSize = 0L;

					for(String file : allKeys(enabled)){
						try {
							if(fileMap.containsKey(file))
								totalSize += new Data(fileMap.get(file)).size();
						} catch (IOException e1) {}
					}

					lblKb.setText((totalSize / 1000.0f / 1000.0f) + " mB");
					btnDisableAll.setText("Disable All");
				}
				else if(e.getItem().equals("All")){

					DefaultMutableTreeNode root = new DefaultMutableTreeNode("SPIRAL");
					DynamicUtilTreeNode.createChildren(root, all);
					sortTree(root);
					tree.setModel(new DefaultTreeModel(root, false));

					long totalSize = 0L;

					//					for(String file : allKeys(all)){
					//						try {
					//							if(fileMap.containsKey(file))
					//								totalSize += new Data(fileMap.get(file)).size();
					//						} catch (IOException e1) {}
					//					}

					lblKb.setText((totalSize / 1000.0f / 1000.0f) + " mB");
					btnDisableAll.setText("Enable All");
				}
				else
					System.out.println(e.getItem());
			}
		});

		Choice configs = new Choice();
		configs.add("None");
		//configs.add("New...");
		panel_2.add(configs, "4, 2, default, fill");

		JScrollPane scrollPane_2 = new JScrollPane();
		panel_2.add(scrollPane_2, "12, 2, 7, 1, fill, fill");

		JPanel panel_3 = new JPanel();
		scrollPane_2.setViewportView(panel_3);
		panel_3.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,},
				new RowSpec[] {
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,}));

		JLabel lblEstFileSize = new JLabel("Est. File Size:");
		panel_3.add(lblEstFileSize, "6, 2");

		panel_3.add(lblKb, "8, 2");

		JPanel panel_4 = new JPanel();
		panel.add(panel_4, "2, 3, fill, fill");
		panel_4.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("max(109dlu;pref):grow"),
				ColumnSpec.decode("198px:grow"),},
				new RowSpec[] {
						FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
						RowSpec.decode("29px"),}));

		JButton btnCobbleTogether = new JButton("Cobble Together");
		panel_4.add(btnCobbleTogether, "1, 2, fill, top");
		btnCobbleTogether.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				try{
					String file = JOptionPane.showInputDialog(panel, "Name the Mod");



					DanganLauncher.progress = new ProgressFrame("Mod Creation", "Preparing Files...");

					new Thread(){

						public void run(){
							try{
								float perFile = 100.0f / allKeys(enabled).length;
								float total = 0.0f;

								File modFile = new File("mods" + File.separator + file + ".drs");
								if(!modFile.exists())
									modFile.createNewFile();
								ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(modFile));

								for(String s : allKeys(enabled)){
									try{
										s = s.replace("meta" + File.separator, "");
										total += perFile;
										DanganLauncher.progress.updateProgress(total, "Writing " + s);
										zos.putNextEntry(new ZipEntry(s));
										FileInputStream in = new FileInputStream(new File(dir, s));
										byte[] data = new byte[in.available()];
										in.read(data);
										zos.write(data);
										in.close();
									}catch(Throwable th){
										th.printStackTrace();
									}
								}

								zos.close();
							}
							catch(Throwable th){}
						}
					}.start();

					DanganLauncher.progress.updateProgress(100.0f, "Done!");
				}
				catch(Throwable th){
					th.printStackTrace();
				}
			}
		});

		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setVisible(false);
				DanganLauncher.progress.setVisible(false);
			}
		});
		panel_4.add(btnClose, "2, 2");
	}

	@SuppressWarnings("unchecked")
	public static String[] allKeys(Hashtable<String, Object> keyset){
		LinkedList<String> keys = new LinkedList<String>();

		for(String key : keyset.keySet()){
			if(keyset.get(key).toString().equalsIgnoreCase("End"))
				keys.add(key);
			else{
				for(String s : allKeys((Hashtable<String, Object>) keyset.get(key)))
					keys.add(s);
			}
		}

		return keys.toArray(new String[0]);
	}

}
