package realm.jnfss.ui;

import javax.swing.*;
import java.io.*;

/**
  * an open file dialog
  */
public class FileOpener {
  private JFileChooser fc;
  private UserInterface parent;
  
  /**
    * requires a UserInterface instance as parameter
	*/
  public FileOpener (UserInterface ui) {
    parent = ui;
  }
  
  /**
    * opens the chosen file
	*/
  public File openFile () {
    //
    File sf = null;
    fc = new JFileChooser ();
    int retval = fc.showOpenDialog (parent);
    if (retval == JFileChooser.APPROVE_OPTION) {
      sf = fc.getSelectedFile ();
      System.out.println ("File Chosen: " + sf);
    }
    return sf;
  }
}
