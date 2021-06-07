package realm.jnfss;

import static java.lang.System.out;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  * Template for user id prompt dialog
  */
public class UserIDDialog extends JDialog {
	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private JPanel root;
	private JPanel p;
	private JPanel stat;
	private JTextField msgf;
	private JLabel ul, _u;
	private JTextField tf;
	private JButton sb;
	private String uid;
	
    /**
      * Only constructor for now
      * Dialog is of application modality type
      */
	public UserIDDialog () {
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("UserID Dialog");
		// creation
		root = new JPanel();
		stat = new JPanel();
		p = new JPanel ();
		tf = new JTextField (20);
		ul = new JLabel ("UserID");
		_u = new JLabel ("");
		sb = new JButton ("Submit");
		sb.addActionListener(new ActionListener () {
			public void actionPerformed (ActionEvent ae) {
				handleSubmit ();
			}
		});
		p.setLayout(new GridLayout (2, 2));
		p.add(ul);
		p.add(tf);
		p.add(_u);
		p.add(sb);
		// status ui
		stat.setLayout(new GridLayout(1, 1));
		msgf = new JTextField(20);
		msgf.setForeground(Color.RED);
		// colors
		msgf.setEnabled(true);
		msgf.setEditable(false);
		msgf.setText("Enter user id and press submit");
		stat.add(msgf);
		// root pane
		root.setLayout(new BorderLayout());
		root.add(p, BorderLayout.CENTER);
		root.add(stat, BorderLayout.SOUTH);
		//
		this.add(root);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		Toolkit t = Toolkit.getDefaultToolkit();
		Dimension scr = t.getScreenSize();
		Point dlgp = new Point (scr.width*3/8 - 20, scr.height*7/16);
		Dimension dlgd = new Dimension (scr.width/4 + 40, scr.height/8 + 30);
		this.setLocation(dlgp);
		this.setSize(dlgd);
		this.setVisible(true);
	}
	
    /**
      * Takes care of the submitted user id
      */
	protected void handleSubmit () {
		uid = tf.getText();
		//out.println ("In handleSubmit: " + uid);
		// if userid is illegal warn in status bar
		String sp = "[a-zA-Z_0-9!@#$%^&*]+";
		Pattern p = Pattern.compile(sp);
		Matcher m = p.matcher(uid);
		boolean valid = m.matches();
		if(!valid) {
			out.println("Illegal user id");
			msgf.setText("Illegal user id: should satisfy regex: '[a-zA-Z_0-9!@#$%^&*]+'");
			tf.setText("");
		}
		else {
			this.dispose();
		}
	}
	
    /**
      * Getter for user id
      */
	public String userid () {
		return uid;
	}

}
