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
  * Template for password prompt dialog
  */
public class PasswordDialog extends JDialog {
	/**
	 * default serial version uid
	 */
	private static final long serialVersionUID = 1L;
	private JPanel root;
	private JPanel p;
	private JPanel stat;
	private JTextField msgf;
	private JLabel pl, _;
	private JPasswordField pf;
	private JButton sb;
	private String pwd;
	
    /**
      * Only constructor for now.
      */
	public PasswordDialog () {
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Password Dialog");
		// creation
		root = new JPanel();
		stat = new JPanel();
		p = new JPanel ();
		pf = new JPasswordField (20);
		pl = new JLabel ("Password");
		_ = new JLabel ("");
		sb = new JButton ("Submit");
		sb.addActionListener(new ActionListener () {
			public void actionPerformed (ActionEvent ae) {
				handleSubmit ();
			}
		});
		p.setLayout(new GridLayout (2, 2));
		p.add(pl);
		p.add(pf);
		p.add(_);
		p.add(sb);
		// status ui
		stat.setLayout(new GridLayout(1, 1));
		msgf = new JTextField(20);
		msgf.setForeground(Color.RED);
		// colors
		msgf.setEnabled(true);
		msgf.setEditable(false);
		msgf.setText("Enter password and press submit");
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
      * Takes care of the submitted password
      */
	protected void handleSubmit () {
		pwd = new String (pf.getPassword());
		//out.println ("In handleSubmit: " + pwd);
		// if password structure is illegal warn in status bar
		String sp = "[a-zA-Z_0-9!@#$%^&*]+";
		Pattern p = Pattern.compile(sp);
		Matcher m = p.matcher(pwd);
		boolean valid = m.matches();
		if(!valid) {
			out.println("Illegal password");
			msgf.setText("Illegal password: should satisfy regex: '[a-zA-Z_0-9!@#$%^&*]+'");
			pf.setText("");
		}
		else {
			this.dispose();
		}
	}
	
    /**
      * Getter for the password
      */
	public String password () {
		return pwd;
	}

}
