package realm.jnfss.comm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
  * Template for the upload progessBar user interface
  */
public class UploadIndicator extends JFrame {
  private JProgressBar progBar;
  private JPanel panel;
  private JLabel label;
  
  /**
    * Only constructor for now
    */
  public UploadIndicator (String title) {
    setPreferredSize (new Dimension (400, 80));
    setTitle (title);
    setLocationByPlatform (true);
    
    addWindowListener (
      new WindowAdapter () {
        public void windowClosing (WindowEvent we) {
          setVisible (false);
          firePropertyChange ("cancelled", false, true);
        }
      }
    );
    
    progBar = new JProgressBar (0, 100);
    progBar.setIndeterminate (false);
    progBar.setBorderPainted (true);
    progBar.setStringPainted (true);
    
    label = new JLabel ("");
    panel = new JPanel ();
    
    panel.setLayout (new BorderLayout ());
    panel.add (label, BorderLayout.CENTER);
    panel.add (progBar, BorderLayout.SOUTH);
    
    add (panel);
  }
  
  /**
    * Setter for progress value
    */
  public void setProgressLevel (int x) {
    progBar.setValue (x);
  }
  
  /**
    * Setter for text label
    */
  public void setLabelText (String s) {
    label.setText (s);
  }
  
  /**
    * Setter for progress text
    */
  public void setProgressText (String s) {
    progBar.setString (s);
  }
  //
}
