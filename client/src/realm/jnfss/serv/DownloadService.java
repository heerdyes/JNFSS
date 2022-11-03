package realm.jnfss.serv;

import realm.jnfss.comm.ServerConnection;

import java.util.Observable;
import java.util.Observer;

/**
  * Template for a download service functionality
  */
public class DownloadService extends Service implements Observer {
  private boolean success = false;
  private ServerConnection servconn;
  private String flnm;
  private String filePath;

  /**
    * Only constructor for now
    */
  public DownloadService (long sid) {
    //
    super (sid);
  }

  /**
    * Services a download request from the controller
    */
  public boolean serviceDownloadRequest (String fp) {
    filePath = fp;

    // connect to server
    try {
      servconn = new ServerConnection ();
      servconn.addObserver (this);
      flnm = filePath.substring (filePath.lastIndexOf ('\\') + 1);
      
      System.out.println ("FLNM = " + flnm);
      System.out.println ("In downloadservice... Filepath: " + filePath);
	  //success = servconn.downloadFile ("DOWNLOAD FILE \"" + filePath + "\" JNFSP/1.0", flnm, false);
      success = servconn.downloadFile ("SID " + super.snID + " DOWNLOAD FILE \"" + filePath + "\" JNFSP/1.0", flnm, false);
      
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

  /**
    * Called when observed object notifies this
    */
  public void update (Observable o, Object arg) {
    if (arg.toString ().equals ("download_complete"))
    {
      setChanged ();
      notifyObservers (arg);
    }
  }
}
