package realm.jnfss.ui;

import javax.swing.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/**
  * a save file dialog
  */
public class FileSaver {
  private UserInterface parent;
  
  /**
    * requires a UserInterface instance as parameter
	*/
  public FileSaver (UserInterface p) {
    parent = p;
  }
  
  /**
    * saves the chosen file
	*/
  public boolean saveFile (String fp, String fn) {
    // jfilechooser ...
    boolean flag = false;

    try {
      JFileChooser fc = new JFileChooser ();
      fc.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
      int retval = fc.showSaveDialog (parent);
      if (retval == JFileChooser.APPROVE_OPTION) {
      //
        //((Component) parent).setCursor (new Cursor (Cursor.WAIT_CURSOR));
        parent.swapCursors ();
        String selPath = fc.getSelectedFile ().getCanonicalPath ();
        String dstPath = selPath + File.separator + fn;
        System.out.println ("selectedPath: " + selPath);
        System.out.println ("destinationPath: " + dstPath);

        File conf = new File ("downloadfolder.txt");
        BufferedReader _br = new BufferedReader (new InputStreamReader (new FileInputStream (conf)));
        String currPath = _br.readLine () + File.separator + fn;
        System.out.println ("currPath = " + currPath);
        _br.close ();

        File src = new File (currPath);
        File dst = new File (dstPath);
        FileInputStream fis = new FileInputStream (src);
        FileOutputStream fos = new FileOutputStream (dst);

        long total = src.length ();
        long SZMAX = 419430400;    // 2^24 * 25 (i.e. 25 * 16MB)
        long SZMIN = 25600;        // 2^10 * 25 (i.e. 25 * 1KB)
        int CSMAX = 16777216;
        int CSMIN = 1024;
        int chunkSize = -1;

        if (total < SZMAX && total > SZMIN) {
          int parts = 25;
          chunkSize = (int) ((double) total / (double) parts);
        }
        else if (total > SZMAX) {
          chunkSize = CSMAX;
        }
        else if (total < SZMIN)
        {
          chunkSize = CSMIN;
        }
  
        byte[] buffer = new byte[chunkSize];
        long curr = 0;
        long prev = 0;
        int nb;
        
        long initial = Calendar.getInstance ().getTime ().getTime ();
        
        for (;;) {
          nb = fis.read (buffer);
          curr += nb;
          if (nb == -1) break;
          fos.write (buffer, 0, (int) (curr - prev));
          prev = curr;
        }

        long eventual = Calendar.getInstance ().getTime ().getTime ();
        System.out.println ("time period: " + (eventual - initial));
        //
        fis.close ();
        fos.close ();
        fis = null;
        fos = null;

        // temp file must be deleted
        src.delete ();

        flag = true;
      }
    }
    catch (Exception e) {
      System.out.println (e);
    }

    //((Component) parent).setCursor (new Cursor (Cursor.DEFAULT_CURSOR));
    parent.swapCursors ();
    return flag;
  }
}
