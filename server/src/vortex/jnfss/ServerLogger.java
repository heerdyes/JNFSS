package vortex.jnfss;

import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
  * the server log manager
  */
public class ServerLogger implements Observer {
  private FileWriter fw = null;
  private PrintWriter pw;
  private String logFileName;
  private FileHandler fh;
  
  /**
	* creates a default file namely "server.log" <br />
	* creates a file handler
	*/
  public ServerLogger () {
    logFileName = "server.log";
    try
    {
    	fh = new FileHandler (logFileName, true);
    }
    catch (Exception e)
    {
        System.out.println (e);
    }
  }
  
  /**
	* called when observed object (server) notifies
	*/
  public void update (Observable o, Object arg) {
    logg ("source object::[" + o.toString () + "] notified this :: [" + arg.toString () + "]");
    if (arg.toString ().equals ("shutdown_server"))
    {
      shutdownLog ();
    }
  }
  //

  /**
	* write to log file
	*/
  public void logg (String s) {
    System.out.println ("INFORMATION: " + s);
    fh.publish (new LogRecord (Level.INFO, s));
  }

  /**
	* close file handler and log file
	*/
  public void shutdownLog () {
    if (fh != null)
    {
      fh.close ();
    }
  }
  //
}
