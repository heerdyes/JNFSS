package realm.jnfss.serv;

import realm.jnfss.ui.ProgressWindow;
import realm.jnfss.comm.ServerConnection;

import javax.swing.JProgressBar;
import javax.swing.JDialog;
import javax.swing.JLabel;

import java.util.Observable;
import java.util.Observer;

/**
  * Template for a view service functionality
  */
public class ViewService extends Service implements Observer {
  //
  private boolean success = false;
  private ServerConnection servconn = null;
  private String flnm = null;
  private String filePath = null;
  
  /**
    * only constructor for now
    */
  public ViewService (long sid) {
      //\
     // \\
    //===\\
   //     \\
    super (sid);
  }
  
  /**
    * services view requests from the controller
    */
  public Object serviceViewRequest () {
    Object response = null;
    ProgressWindow pw = new ProgressWindow ("View Progress Dialog", "Accessing server ...");
    pw.showAndPack ();
    
    // connect to server
    try {
      pw.setProgressLabel ("obtaining server connection ...");
      servconn = new ServerConnection ();
      
      pw.setProgressLabel ("obtaining directory hierarchy ...");
      response = servconn.sendSyncRequest ("SID " + super.snID + " VIEW TREE JNFSP/1.0");
      
      System.out.println ("response: [" + response + "]");
    }
    catch (Exception e) {
      pw.setProgressLabel ("exception occurred ... ");
      System.out.println ("  // " + e);
    }
    
    pw.unShow ();
    pw.cleanup ();
    pw = null;
    
    return response;
  }
  
  /**
    * services preview requests from the controller
    */
  public boolean servicePreviewRequest (String fp) {
    success = false;
    filePath = fp;
    // connect to server
    try {
      servconn = new ServerConnection ();
      servconn.addObserver (this);
      System.out.println ("In servicePreviewRequest (), filepath = " + filePath);
      filePath = filePath.replace ('/', '\\');
      flnm = filePath.substring (filePath.lastIndexOf ('\\') + 1);
      
	  success = servconn.downloadFile ("SID " + super.snID + " VIEW FILE \"" + filePath + "\" JNFSP/1.0", flnm, true);
      
      if (!success) {
        System.out.println (flnm + " could not be downloaded.");
      }
      
      System.out.println ("response: [" + success + "]");
    }
    catch (Exception e) {
      System.out.println ("  // " + e);
    }
    
    return success;
  }
  //

  /**
    * called when the observed object notifies this
    */
  public void update (Observable o, Object arg) {
    if (arg.toString ().equals ("download_complete"))
    {
      System.out.println ("In viewservice: update (), preview download complete.");
      setChanged ();
      notifyObservers ((Object) new String ("preview_download_complete"));
    }
  }
}
