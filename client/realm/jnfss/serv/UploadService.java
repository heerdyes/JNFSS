package realm.jnfss.serv;

import realm.jnfss.comm.ServerConnection;
import java.util.*;
import java.io.*;

/**
  * Template for an upload service functionality
  */
public class UploadService extends Service implements Observer {
  private ServerConnection servconn;
  private String flnm;

  /**
    * only constructor for now
    */
  public UploadService (long sid) {
      super (sid);
  }

  /**
    * Services upload requests from controller
    */
  public boolean serviceUploadRequest (File fu, String dstpath) {
    boolean done = false;
    try
    {
      //
      servconn = new ServerConnection ();
      servconn.addObserver (this);
      flnm = fu.getName ();
      
	  done = servconn.uploadFile ("SID " + super.snID + " UPLOAD FILE \"" + dstpath + "\" " + fu.length () + " JNFSP/1.1", flnm, fu.getCanonicalPath (), fu.length ());
      //
    }
    catch (Exception e)
    {
      System.out.println (e);
    }
    return done;
  }

  /**
    * called when observed object notifies this
    */
  public void update (Observable o, Object arg) {
    // observe
    if (arg.toString ().equals ("upload_complete"))
    {
      setChanged ();
      notifyObservers (arg);
    }
  }
}
