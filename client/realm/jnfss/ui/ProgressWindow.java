package realm.jnfss.ui;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
  * progress depicter
  */
public class ProgressWindow implements WindowListener {
  private JDialog progressDialog = null;
  private JProgressBar progressBar = null;
  private JLabel progressLabel = null;
  private boolean done = true;
  
  /**
    * requires title and label as parameters
	*/
  public ProgressWindow (String title, String label) {
    // creation
    progressBar = new JProgressBar ();
    progressLabel = new JLabel (label);
    progressDialog = new JDialog ();
    progressDialog.setTitle (title);
    
    // configuration
    progressBar.setIndeterminate (true);
    progressBar.setBorderPainted (true);
    progressBar.setStringPainted (true);
    progressBar.setString ("waiting");
    
    progressDialog.setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);
    progressDialog.setPreferredSize (new Dimension (400, 80));
    progressDialog.setLocationByPlatform (true);
    progressDialog.setLayout (new BorderLayout ());
    progressDialog.addWindowListener (this);
    
    // manifestation
    progressDialog.add (progressLabel, BorderLayout.CENTER);
    progressDialog.add (progressBar, BorderLayout.SOUTH);
  }
  
  
   // behavior
  //
  /**
    * display the progress window in its preferred size
	*/
  public void showAndPack () {
    progressDialog.setVisible (true);
    progressDialog.pack ();
    done = false;
  }
  
  /**
    * hide the progress window
	*/
  public void unShow () {
    done = true;
    progressDialog.setVisible (false);
  }
  
  /**
    * set whether mode is determinate or not
	*/
  public void determinateMode (boolean x) {
    progressBar.setIndeterminate (!x);
    progressDialog.validate ();
  }
  
  /**
    * set the progress level
	*/
  public void setProgressLevel (int v) {
    progressBar.setValue (v);
  }
  
  /**
    * set the progress label
	*/
  public void setProgressLabel (String text) {
    progressLabel.setText (text);
  }
  
  /**
    * revalidate the progress window
	*/
  public void refresh () {
    progressDialog.validate ();
  }
  
  /**
    * clear the variables by nullifying them
	*/
  public void cleanup () {
    progressLabel = null;
    progressBar = null;
    progressDialog = null;
  }
  
  
   // listen
  //
  public void windowActivated (WindowEvent we) {}
  
  /**
    * on window deactivated do this
	*/
  public void windowDeactivated (WindowEvent we) {
    if (!done) {
      progressDialog.setVisible (false);
      progressDialog.setVisible (true);
    }
  }
  
  
  public void windowClosed (WindowEvent we) {}
  
  public void windowClosing (WindowEvent we) {
    // while closing window
  }
  
  
  public void windowIconified (WindowEvent we) {}
  
  public void windowDeiconified (WindowEvent we) {}
  
  
  public void windowGainedFocus (WindowEvent we) {}
  
  public void windowLostFocus (WindowEvent we) {}
  
  
  public void windowOpened (WindowEvent we) {}
  
  public void windowStateChanged (WindowEvent we) {}
}
