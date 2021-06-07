package realm.jnfss;

import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import static java.lang.System.out;
import static java.lang.System.exit;
import realm.jnfss.comm.ServerConnection;

/**
  * This is the template for the initializer<br />
  * For now this performs the login attempt<br />
  * Can be improved to a more generic version
  */
public class Initializer {
  private String uid = null;
  private String pwd = null;
  private JDialog f;
  private JPanel p;
  private JTextField uidtf;
  private JPasswordField pwdpf;
  private JLabel uidl, pwdl, blank;
  private JButton submit;
  private UserIDDialog ud;
  private PasswordDialog pd;

  /**
    * Method to attempt login <br />
    * Prompts for user id and password and sends them to the server for verification
    */
  public AppConfig attemptLogin () {
    String ret = null;
    long sessionID = -1;
    File sf = null;
    AppConfig appConfig = new AppConfig ();
    try {
      // before getting server connection verify the location of machine
      File ac = new File ("address.conf");
      FileInputStream stream = new FileInputStream (ac);
      BufferedReader reader = new BufferedReader (new InputStreamReader (stream));
      String addr = reader.readLine ();
      reader.close ();
      int ans = JOptionPane.showConfirmDialog (null, "Is Server Machine URL = " + addr, "Server Machine Confirmation", JOptionPane.YES_NO_OPTION);
      if (ans == JOptionPane.NO_OPTION)
      {
        // server is on some other machine
        // ask for new location
        String newaddr = JOptionPane.showInputDialog (null, "Enter the new Server URL:", "Server URL", JOptionPane.QUESTION_MESSAGE);
        if (newaddr != null)
        {
          // write newaddr to config file
          try {
            PrintWriter writer = new PrintWriter (new FileOutputStream (ac), true);
            writer.println (newaddr);
            writer.close ();
          }
          catch (Exception e) {
            out.println (e);
          }
        }
        else
        {
          // newaddr is null hence exit
          exit (0);
        }
      }
      // now obtain server connection
      ServerConnection sc = new ServerConnection ();
      if (sc != null) {
        // ask for uid
        ud = new UserIDDialog ();
        uid = ud.userid ();
        if (uid.length () == 0)
        {
            JOptionPane.showMessageDialog (null, "Null User ID!", "Error!", JOptionPane.ERROR_MESSAGE);
            exit (1);
        }
        ud = null;
        // ask for pwd
        pd = new PasswordDialog ();
        pwd = pd.password ();
        if (pwd.length () == 0)
        {
            JOptionPane.showMessageDialog (null, "Null Password!", "Error!", JOptionPane.ERROR_MESSAGE);
            exit (1);
        }
        pd = null;
        // before logging in check if user about to login is allowed sync option
        sf = new File ("syncflag");
        if (sf.exists ())
        {
          // check if uid is same as that of sf
          BufferedReader tbr = new BufferedReader (new InputStreamReader (new FileInputStream (sf)));
          String tnm = tbr.readLine ();
          tbr.close ();
          if (tnm.equals (uid))
          {
            // all is well, syncable
            appConfig.setIsSyncable (true);
          }
          else
          {
            // not syncable
            appConfig.setIsSyncable (false);
          }
        }
        sf = null;
        // send to server
        String res = sc.doLogin (uid, pwd);

        // analyze server's reply
        out.println (res);
        // there are two flow paths now,
        // 1. login is successful: a session ID is returned
        // 2. login is unsuccessful: server prompts for registration
        if (res.startsWith ("SUCCESSFUL LOGIN"))
        {
            // login successful, get session id
            // response: SUCCESSFUL LOGIN SID <sid> JNFSP/1.0
            int s0 = res.indexOf (' ');
            int s1 = res.indexOf (' ', s0+1);
            int sb = res.indexOf (' ', s1+1);
            int se = res.indexOf (' ', sb+1);
            ret = res.substring (sb+1, se);
            sessionID = Long.parseLong (ret);
            out.println (">>>> in Initializer.attemptLogin (): sessionID = " + sessionID);
        }
        else if (res.startsWith ("UNSUCCESSFUL LOGIN"))
        {
            // login unsuccessful
            // response: UNSUCCESSFUL LOGIN PASSWORD MISMATCH JNFSP/1.0
            // or      : UNSUCCESSFUL LOGIN USERID NONEXISTENT JNFSP/1.0
            // 1. password mismatch
            // 2. userid non-existent
            int s0 = res.indexOf (' ');
            int s1 = res.indexOf (' ', s0+1);
            int s2 = res.indexOf (' ', s1+1);
            int s3 = res.indexOf (' ', s2+1);
            if (res.substring (s1+1, s2).equals ("PASSWORD"))
            {
              // pwd mismatch
              JOptionPane.showMessageDialog (null, "Password Mismatch", "Server says...", JOptionPane.INFORMATION_MESSAGE);
              exit (1);
            }
            else
            {
                // uid nonexistent
                int result = JOptionPane.showConfirmDialog (null, "User ID non-existent. Register Now ???", "Server says...", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION)
                {
                    // now accept uid and pwd for regn.
                    ud = new UserIDDialog ();
                    uid = ud.userid ();
                    if (uid.length () == 0)
                    {
                        exit (1);
                    }
                    pd = new PasswordDialog ();
                    pwd = pd.password ();
                    if (pwd.length () == 0)
                    {
                        exit (1);
                    }
                    // uid and pwd are proper
                    sf = new File ("syncflag");
                    if (sf.exists ())
                    {
                        // some user is already using this computer
                        // check if its the same user who just registered
                        try
                        {
                        	BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (sf)));
                            String nm = br.readLine ();
                            br.close ();
                            if (uid.equals (nm))
                            {
                              // same user
                              JOptionPane.showMessageDialog (null, "You already have a sync config file", "Report", JOptionPane.INFORMATION_MESSAGE);
                            }
                            else
                            {
                              // cannot accommodate new user
                              // report and exit
                              JOptionPane.showMessageDialog (null, "This computer is being used by one user already", "Could Not Register", JOptionPane.INFORMATION_MESSAGE);
                              exit (0);
                            }
                        }
                        catch (Exception e)
                        {
                            out.println ("exception >>>> " + e);
                        }
                    }
                    else
                    {
                        // attempt to create syncflag
                        try
                        {
                            String path = null;
                            JFileChooser fc = new JFileChooser ();
                            fc.setDialogTitle ("Synchronization Folder Selection");
                            fc.setApproveButtonText ("Choose");
                            fc.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
                            int retval = fc.showSaveDialog (null);
                            if (retval == JFileChooser.APPROVE_OPTION) {
                              path = fc.getSelectedFile ().getCanonicalPath ();
                            }
                            else {
                              path = "C:\\Users\\Public";
                            }
                            // try to create sync folder
                            File sd = new File (path + File.separator + uid);
                            sd.mkdir ();
                            // now write to syncflag
                        	FileOutputStream fos = new FileOutputStream (sf);
                            PrintWriter writer = new PrintWriter (fos, true);
                            writer.println (uid);
                            writer.println (path);
                            writer.close ();
                        }
                        catch (Exception e)
                        {
                            out.println (e);
                        }
                    }

                    // get a new server connection before trying to register
                    if ((sc = new ServerConnection ()) == null)
                    {
                      out.println (">>>> Initializer.attemptLogin (): sc = null");
                      exit (1);
                    }
                    String regack = sc.doRegister (uid, pwd);
                    // analyze server's response
                    // in this case just print
                    out.println (regack);
                    // if regack is successful then exit
                    if (regack.startsWith ("SUCCESSFUL"))
                    {
                      // create client side sync folder with uid as name
                      
                      // show registration success
                      JOptionPane.showMessageDialog (null, "You have registered successfully.", "Registration", JOptionPane.INFORMATION_MESSAGE);
                      exit (0);
                    }
                }
                else
                {
                    // don't register. exit
                    sc = null;
                    out.println (">>>> Initializer.attemptLogin (): exit by choice.");
                    exit (0);
                }
            }
            //
        }
      }
      else {
        out.println ("server connection is null!!");
      }
    }
    catch (ConnectException ce) {
      out.println (ce);
      int x = JOptionPane.showConfirmDialog (null, "Unable to make Connection with Server", "Exception", JOptionPane.OK_CANCEL_OPTION);
      out.println (x);
      exit (0);
    }
    catch (RuntimeException re) {
      int x = JOptionPane.showConfirmDialog (null, re.toString (), "Exception", JOptionPane.OK_CANCEL_OPTION);
      exit (0);
    }
    catch (Exception e) {
      JOptionPane.showConfirmDialog (null, e.toString (), "Exception", JOptionPane.ERROR_MESSAGE);
      exit (0);
    }
    // update ac's sid
    appConfig.setSID (sessionID);
    return appConfig;
  }
  /*
   * the above function awaits serious refactoring
   * code is not being reused, its being repasted!!
   */

  /**
    * Shows the login prompt
    */
  protected void showLoginBox () {
    // login ui
    f = new JDialog ();
    p = new JPanel ();
    blank = new JLabel ("");
    uidl = new JLabel ("Enter User ID: ");
    pwdl = new JLabel ("Enter Password: ");
    uidtf = new JTextField (20);
    pwdpf = new JPasswordField (20);
    submit = new JButton ("Submit");
    submit.addActionListener (
      new ActionListener () {
        public void actionPerformed (ActionEvent ae) {
          f.setVisible (false);
          setLoginData ();
        }
      }
    );
    p.setLayout (new GridLayout (3, 2));
    p.add (uidl);
    p.add (uidtf);
    p.add (pwdl);
    p.add (pwdpf);
    p.add (blank);
    p.add (submit);
    f.add (p);
    f.setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);
    f.setLocationByPlatform (true);
    f.setVisible (true);
    f.pack ();
  }

  /**
    * Setter for user id and password
    */
  protected void setLoginData () {
    // get the data in ui
    uid = uidtf.getText ();
    pwd = new String (pwdpf.getPassword ());
    //
  }
}
