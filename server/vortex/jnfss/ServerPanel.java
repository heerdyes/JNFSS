package vortex.jnfss;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Observer;
import java.util.Observable;

/**
  * the gui for the server
  */
public class ServerPanel extends JPanel implements Observer {
  JButton startServer = null;
  JButton stopServer = null;
  JPanel control = null;
  JTextArea log = null;
  TCPServer servRef = null;
  JScrollPane scroll = null;
  
  /**
    * takes a tcp server as parameter
	*/
  ServerPanel (TCPServer s) throws Exception {
    // get tcp server
    servRef = s;
    servRef.addObserver (this);
    
    // layout
    setLayout (new BorderLayout ());
    
    // components
    startServer = new JButton ("Start Server");
      startServer.setEnabled (true);
    stopServer = new JButton ("Stop Server");
      stopServer.setEnabled (false);
    log = new JTextArea (10, 50);
    control = new JPanel ();
    scroll = new JScrollPane (log);
    
    // listeners
    // 1. start server
    startServer.addActionListener (
      new ActionListener () {
        public void actionPerformed (ActionEvent ae) {
          try { servRef.startServer (); }
          catch (Exception e) {}
          startServer.setEnabled (false);
          stopServer.setEnabled (true);
          log.append ("Starting server...\nlistening on port 7531...\n");
        }
      }
    );
    
    // 2. stop server
    stopServer.addActionListener (
      new ActionListener () {
        public void actionPerformed (ActionEvent ae) {
          servRef.stopServer ();
          stopServer.setEnabled (false);
          log.append ("Stopping server...\n");
        }
      }
    );
    
    // additions
    add (scroll, BorderLayout.CENTER);
    
    control.add (startServer);
    control.add (stopServer);
    
    add (control, BorderLayout.SOUTH);
    
    validate ();
  }
  
  // notification observations
  /**
    * called when observed object (server) notifies
	*/
  public void update (Observable o, Object arg) {
    log.append ("notification :: [" + arg.toString () + "]\n");
  }
}
