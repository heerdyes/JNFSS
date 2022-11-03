package realm.jnfss.comm;

import java.beans.*;
import javax.swing.SwingWorker.StateValue;
import java.awt.*;
import java.io.*;

/**
  * Template for the controller of uploads
  */
public class UploadController implements PropertyChangeListener {
  private UploadIndicator ui;
  private UploadTask ut;
  private String uiTitle;
  private int limit = 300;
  private int current = 0;
  
  /**
    * The pseudo main of this class
    */
  public boolean _main (OutputStream os, long fs, String fn, String fp) {
    boolean sflag = false;
    System.out.println ("UploadController._main ()");
    uiTitle = "Observer";

    ui = new UploadIndicator (uiTitle);
    ui.addPropertyChangeListener (this);
    
    ut = new UploadTask (os, fs, fn, fp);
    ut.addPropertyChangeListener (this);
    
    ui.setVisible (true);
    ui.pack ();
    ut.execute ();
    //
    try {
      String ret = ut.get ();
      System.out.println (ret);
      if (ret.equals ("file_uploaded"))
      {
        sflag = true;
      }
    }
    catch (Exception e) {
      System.out.println (e);
    }
    return sflag;
    //
  }

  /**
    * Called when a property is changed<br />
    * In this case progress, state, cancelled, bytes uploaded or speed
    */
  public void propertyChange (PropertyChangeEvent pce) {
    String prop = pce.getPropertyName ();
    if (prop.equals ("progress")) {
      int p = (Integer) pce.getNewValue ();
      ui.setProgressLevel (p*4);
      ui.setProgressText ("" + p*4 + "%");
    }
    else if (prop.equals ("state")) {
      if (pce.getNewValue ().equals (StateValue.STARTED)) {
        ui.setLabelText ("Transferring");
      }
      else if (pce.getNewValue ().equals (StateValue.DONE)) {
        ui.setLabelText ("Done");
        ui.setVisible (false);
        ui = null;
      }
    }
    else if (prop.equals ("cancelled")) {
      boolean result = ut.cancel (true);
      System.out.println ("cancellation result = " + result);
      ui = null;
    }
    else if (prop.equals ("bytesUploaded")) {
      ui.setLabelText ("bytes uploaded: " + (Long) pce.getNewValue ());
    }
    else if (prop.equals ("speed"))
    {
      if (current < limit)
      {
        // nothing
      }
      else {
        ui.setTitle (uiTitle + " [" + (pce.getNewValue ()).toString () + "] MBps");
        current = 0;
      }
      current++;
    }
    //
  }
  //
}
