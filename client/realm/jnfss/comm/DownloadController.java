package realm.jnfss.comm;

import java.beans.*;
import javax.swing.SwingWorker.StateValue;
import java.awt.*;
import java.io.*;

/**
  * Template for the controller of downloads
  */
public class DownloadController implements PropertyChangeListener
{
  private DownloadIndicator di;
  private DownloadTask dt;
  private String diTitle;
  private int limit = 350;
  private int current = 0;
  
  /**
    * The pseudo main of this class
    */
  public boolean _main (InputStream is, long fs, String flnm, boolean delx) {
    boolean sflag = false;
    System.out.println ("DownloadController._main ()");
    diTitle = "Observer";

    di = new DownloadIndicator (diTitle);
    di.addPropertyChangeListener (this);
    
    dt = new DownloadTask (is, fs, flnm, delx);
    dt.addPropertyChangeListener (this);
    
    di.setVisible (true);
    di.pack ();
    dt.execute ();
    //
    try {
      String ret = dt.get ();
      System.out.println (ret);
      if (ret.equals ("file_copied"))
      {
        sflag = true;
      }
    }
    catch (Exception e) {}
    return sflag;
    //
  }

  /**
    * Called when a property is changed<br />
    * In this case progress
    */
  public void propertyChange (PropertyChangeEvent pce) {
    String prop = pce.getPropertyName ();
    if (prop.equals ("progress")) {
      int p = (Integer) pce.getNewValue ();
      di.setProgressLevel (p*4);
      di.setProgressText ("" + p*4 + "%");
    }
    else if (prop.equals ("state")) {
      if (pce.getNewValue ().equals (StateValue.STARTED)) {
        di.setLabelText ("Transferring");
      }
      else if (pce.getNewValue ().equals (StateValue.DONE)) {
        di.setLabelText ("Done");
        di.setVisible (false);
        di = null;
      }
    }
    else if (prop.equals ("cancelled")) {
      boolean result = dt.cancel (true);
      System.out.println ("cancellation result = " + result);
      di = null;
    }
    else if (prop.equals ("bytesDownloaded")) {
      di.setLabelText ("bytes downloaded: " + (Long) pce.getNewValue ());
    }
    else if (prop.equals ("speed"))
    {
      if (current < limit)
      {
        // nothing
      }
      else {
        di.setTitle (diTitle + " [" + (pce.getNewValue ()).toString () + "] MBps");
        current = 0;
      }
      current++;
    }
    //
  }
  //
}
