package org.abimon.mods.danganronpa.launcher.windows;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JButton;

public class ProgressFrame extends JFrame {

	private static final long serialVersionUID = -6236560046071529238L;
	private JPanel contentPane;
	private JFrame self;
	
	JLabel lblSittingHere;
	JProgressBar progressBar;
	JButton btnFinish;

	/**
	 * Create the frame.
	 */
	public ProgressFrame(String operation, String firstAction) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("100px"),
				FormSpecs.GROWING_BUTTON_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				RowSpec.decode("16px"),
				FormSpecs.GLUE_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				RowSpec.decode("252px"),}));
		
		JLabel lblNewLabel = new JLabel(operation);
		contentPane.add(lblNewLabel, "2, 1, center, default");
		
		JLabel lblCurrentAction = new JLabel("Current Action:");
		contentPane.add(lblCurrentAction, "1, 3");
		
		lblSittingHere = new JLabel(firstAction);
		contentPane.add(lblSittingHere, "2, 3, center, default");
		
		progressBar = new JProgressBar();
		progressBar.setMaximum(1000);
		contentPane.add(progressBar, "1, 5, 3, 1");
		
		btnFinish = new JButton("Finish");
		btnFinish.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				self.setVisible(false);
			}
		});
		contentPane.add(btnFinish, "2, 6, default, center");
		self = this;
		self.setVisible(true);
		btnFinish.setVisible(false);
	}
	
	public void updateProgress(float val){
		this.progressBar.setValue((int) (val * 10));
		if(val == 100)
			btnFinish.setVisible(true);
	}
	
	public void updateProgress(float val, String newOperation){
		this.progressBar.setValue((int) (val * 10));
		this.lblSittingHere.setText(newOperation);
		if(val == 100)
			btnFinish.setVisible(true);
	}

}
