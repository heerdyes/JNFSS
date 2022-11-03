package vortex.jnfss;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import static java.lang.System.out;
import static java.lang.System.exit;

/**
  * The startup class... launches the server
  */
public class JNFSServer {
  public static void main (String... args) throws Exception {
  //
    // the configuration file
    String fileName = "config.conf";
    // make a hashtable to access configuration stored in a file
    // instead use a Properties object for config.
    Properties ht = new Properties ();
    loadProperties (ht, fileName);
    // load the register from file
    File rf = new File (ht.getProperty ("REGISTER_FILE"));
    Register r = loadRegister (rf);
	if (r == null) {
		// no register yet, so store an empty register
		r = new Register ();
		storeRegister (r, rf);
	}
	else {
		out.println (r);
	}
    // create a logged-in hashtable that shows who has logged in
    Hashtable<Long, Member> sessionHT = new Hashtable<Long, Member> ();
    sessionHT.clear ();

    // check state of user sync space
    String ssdir = ht.getProperty ("SYNC_ROOT");
    out.println (">>>> JNFSServer.main (): ssdir = " + ssdir);
    File syncRootDir = new File (ssdir);
    if (!syncRootDir.exists ())
    {
      // create the sync root folder
      try
      {
      	syncRootDir.mkdir ();
      }
      catch (Exception e)
      {
        out.println ("  <!> "+e+" </!>");
      }
    }
    else
    {
      // check each user's sync space
      // lookup register for each member check syncDir
      // do this only if register is not empty
      if (!r.isEmpty ())
      {
        // register contains atleast one element
        ArrayList<Member> al = r.getMembers ();
        for (Member m : al)
        {
          // check syncDir's existence
          File fm = m.getSyncDir ();
          if (fm == null)
          {
            // serious error
            out.println (">>>> " + m.getUID () + " has not been assigned syncspace.");
          }
          else if (fm.exists ())
          {
            // such a sync dir exists. no problem
            out.println (">>>> " + m.getUID () + " has been assigned syncspace.");
          }
          else
          {
            // sync dir does not exist
            out.println (">>>> " + m.getUID () + "'s syncspace has somehow been deleted.");
          }
        }
      }
      else
      {
        // register is empty
        // do nothing
      }
    }
    
    final TCPServer server = new ServerHandler (ht, r, sessionHT);
    
    EventQueue.invokeLater (
      new Runnable () {
        public void run () {
          JFrame f = new JFrame ("JNFSServer");
          final ServerLogger sl = new ServerLogger ();
          server.addObserver (sl);
          //f.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
          f.addWindowListener (
            new WindowAdapter () {
              public void windowClosing (WindowEvent we) {
                sl.shutdownLog ();
                exit (0);
              }
            }
          );
          
          try {
            f.add (new ServerPanel (server));
          }
          catch (Exception e) {}
          
          /*
          ServerLogger sl = new ServerLogger ();
          server.addObserver (sl);
          */
          
          f.setVisible (true);
          f.pack ();
        }
      }
    );
  //
  }
  
  private static void loadProperties (Properties ht, String fname) {
    File conf = new File (fname);
    FileInputStream fis = null;
    
    if (conf.exists ()) {
      try {
        fis = new FileInputStream (conf);
        ht.load (fis);
        fis.close ();
      }
      catch (Exception e) {
        System.out.println ("  <!> " + e + " </!> ");
      }
      
      System.out.println (ht);
    }
    else {
      ht = null;
    }
  }
  //

    // stores register in the given file
	private static void storeRegister(Register r, File f) {
		// TODO Auto-generated method stub
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream (new FileOutputStream (f));
			oos.writeObject(r);
			oos.close();
			oos = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    // loads register from the given file
	private static Register loadRegister (File f) {
		if (!f.exists()) return null;
		ObjectInputStream ois = null;
		Register reg = null;
		try {
			ois = new ObjectInputStream (new FileInputStream (f));
			reg = (Register) ois.readObject();
			ois.close();
			ois = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reg;
	}
    //

}
