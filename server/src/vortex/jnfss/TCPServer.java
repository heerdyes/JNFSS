package vortex.jnfss;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import static java.lang.System.out;

/**
  * the tcp server
  */
public class TCPServer extends Observable implements Cloneable, Runnable {
  Thread runner = null;
  ServerSocket server = null;
  Socket data = null;
  volatile boolean shouldStop = false;
  protected volatile Properties ht = null;
  protected volatile Hashtable<Long, Member> snHT = null;
  protected volatile Register register = null;
  protected static long SID = 0L;
  
  /**
    * creates a tcp server with properties, register and hashtable
	*/
  public TCPServer (Properties ht, Register reg, Hashtable<Long, Member> sht) {
    this.ht = ht;
    this.register = reg;
    this.snHT = sht;
    out.println ("In TCPServer.TCPServer (): register: " + register);
  }
  
  /**
    * start the server
	*/
  public synchronized void startServer () throws IOException {
    if (runner == null) {
      try {
        int port = Integer.parseInt (ht.getProperty ("SERVER_PORT"));
        System.out.println ("in startServer... creating new server socket...");
        server = new ServerSocket (port);
        runner = new Thread (this);
        runner.start ();
        
        setChanged ();
        notifyObservers ((Object) new String ("created ServerSocket and started server ..."));
      }
      catch (Exception e) {
        System.out.println ("  <!> " + e + " </!> ");
      }
    }
  }
  
  /**
    * stop the server
	*/
  public synchronized void stopServer () {
    if (server != null) {
      setChanged ();
      notifyObservers ((Object) new String ("in stopServer... shouldStop = true ..."));
      shouldStop = true;
      runner.interrupt ();
      runner = null;
      try {
        setChanged ();
        notifyObservers ((Object) new String ("attempting to close server socket..."));
        server.close ();
        // clear session Hashtable
        out.println (">>>> in TCPServer.stopServer (): snHT= " + snHT);
        snHT.clear ();
        snHT = null;
        // close and store the register
        storeRegister (register, new File (ht.getProperty ("REGISTER_FILE")));
        register.wipe ();
        register = null;
        // clear config properties after storing properties
        FileOutputStream fos = new FileOutputStream (new File (ht.getProperty ("CONF_FILE")));
        ht.store (fos, "Application Configuration File [DO NOT DELETE]");
        fos.close ();
        ht = null;
      }
      catch (IOException ioe) {}
      server = null;
      
      setChanged ();
      notifyObservers ((Object) new String ("shutdown_server"));
    }
  }

  /**
    * run the server
	*/
  public void run () {
    System.out.println ("in run () of TCPServer: ["+this+"]");
    if (server != null) {
      while (!shouldStop) {
        try {
          setChanged ();
          notifyObservers ((Object) new String ("going to wait for connection..."));
          Socket dataSocket = server.accept ();
          
          setChanged ();
          notifyObservers ((Object) new String ("connection obtained ... cloning ..."));
          TCPServer newSocket = (TCPServer) clone ();
          newSocket.server = null;
          newSocket.data = dataSocket;
          newSocket.runner = new Thread (newSocket);
          
          setChanged ();
          notifyObservers ((Object) new String ("cloned server ... starting new socket listening instance ..."));
          newSocket.runner.start ();
          
          setChanged ();
          notifyObservers ((Object) new String ("obtained connection and cloned server; started server clone ..."));
        }
        catch (Exception e) {
          setChanged ();
          notifyObservers ((Object) new String ("exception occurred : [" + e.toString () + "]"));
        }
      }
    }
    else {
      run (data);
    }
    //
    setChanged ();
    notifyObservers ((Object) new String ("exiting TCPServer.run () ..."));
  }

  /**
    * to be overridden
	*/
  public void run (Socket data) {}
  //

  /**
    * generate SID by incrementing
	*/
  protected long generateSID () {
    return SID++;
  }

    // stores register in the given file
	protected static void storeRegister(Register r, File f) {
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

}

