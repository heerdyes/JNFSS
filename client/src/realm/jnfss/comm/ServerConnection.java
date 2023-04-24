package realm.jnfss.comm;

import realm.jnfss.ui.ProgressWindow;
import vortex.jnfss.FSTreeNode;

import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.net.Socket;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.JOptionPane;

import javax.swing.tree.TreeModel;

import java.awt.Dimension;

import java.util.Observable;
import java.util.Calendar;

import static java.lang.System.out;

/**
  * Responsible for creating connection to the server
  * Instantiate this class to talk to the server.
  */
public class ServerConnection extends Observable {
  private int port;
  private Socket sock = null;
  
  /**
    * Create socket...
    */
  public ServerConnection () throws ConnectException, UnknownHostException, IOException {
    try {
      BufferedReader temp = new BufferedReader (new InputStreamReader (new FileInputStream (new File ("address.conf"))));
      String ipaddr = temp.readLine ();
      URL url = new URL (ipaddr);
      port = url.getPort ();
      sock = new Socket (url.getHost (), port);
    }
    catch (ConnectException ce) {
      throw ce;
    }
    catch (UnknownHostException uhe) {
      throw uhe;
    }
    catch (IOException ioe) {
      throw ioe;
    }
  }
  
  public Socket getSock() {
    return sock;
  }

  /**
    * Use this method to upload a file to the server.
    */
  public boolean uploadFile (String request, String fn, String fp, long len) {
    //
    boolean uploading = false;
    try {
      final InputStream is = sock.getInputStream ();
      final OutputStream os = sock.getOutputStream ();
      
      PrintWriter pw = new PrintWriter (os, true);
      BufferedReader br = new BufferedReader (new InputStreamReader (is));
      
      pw.println (request);
      String response = br.readLine ();
      System.out.println (response);
      
      if (response.equals ("REWRITE???"))
      {
        int ret = JOptionPane.showConfirmDialog (null, "Rewrite File on Server ?", "Warning", JOptionPane.YES_NO_OPTION);
        if (ret == JOptionPane.YES_OPTION)
        {
          pw.println ("YES");
          response = br.readLine ();
        }
        else {
          pw.println ("NO");
          response = br.readLine ();
        }
      }
      System.out.println ("msg from server: " + response);
      if (response.equals ("WAITING...")) {
        final long fsize = len;
        final String fname = fn;
        final String fpath = fp;
        System.out.println ("file size: " + fsize);

        new Thread (
          new Runnable () {
            public void run () {
              UploadController uc = new UploadController ();
              boolean x = uc._main (os, fsize, fname, fpath);
              System.out.println ("success: " + x);
              // fire an event to signal task done
              setChanged ();
              notifyObservers ("upload_complete");
            }
          }
        ).start ();

        uploading = true;
      }
      else if (response.equals ("ABORTED!!!")){
        // cancelled (no rewrite)
        JOptionPane.showMessageDialog (null, "ABORTED!!!", "Server says...", JOptionPane.INFORMATION_MESSAGE);
      }
      else {
        JOptionPane.showMessageDialog (null, response, "Server says...", JOptionPane.INFORMATION_MESSAGE);
      }
      //
    }
    catch (Exception e) {
      System.out.println ("In ServerConnection:: " + e);
    }

    return uploading;
  }

  /**
    * Use this method to download a file
    */
  public boolean downloadFile (String req, String fname, boolean delx) {
    boolean success = true;
    final boolean dx = delx;
    final String fn = fname;
    try {
      final InputStream is = sock.getInputStream ();
      final OutputStream os = sock.getOutputStream ();
      
      PrintWriter pw = new PrintWriter (os, true);
      BufferedReader br = new BufferedReader (new InputStreamReader (is));
      
      pw.println (req);
      String res = br.readLine ();
      String[] parts = res.split ("\\s");
      System.out.println ("  // response: " + res);
      
      if (parts[0].equals ("SENDING...")) {
        final long fs = Long.parseLong (parts[1]);
        System.out.println ("file size: " + fs);

        new Thread (
          new Runnable () {
            public void run () {
              System.out.println ("going to start download...");
              DownloadController dc = new DownloadController ();
              boolean x = dc._main (is, fs, fn, dx);
              System.out.println ("success: " + x);
              // fire an event to signal task done
              System.out.println ("download complete");
              setChanged ();
              notifyObservers ("download_complete");
            }
          }
        ).start ();
      }
      //
    }
    catch (Exception e) {}
    
    return success;
  }
  
  /**
    * Use this method to get the file tree on the server
    * Used for View case
    */
  public Object sendSyncRequest (String req) {
    Object res = null;
    
    ObjectInputStream ois = null;
    PrintWriter pw = null;
    
    try {
      InputStream is = sock.getInputStream ();
      OutputStream os = sock.getOutputStream ();
      
      pw = new PrintWriter (os, true);

      System.out.println ("  // req = " + req);
      pw.println (req);
      
      ois = new ObjectInputStream (is);
      res = ois.readObject ();
    }
    catch (Exception e) {
      System.out.println ("  // " + e);
      e.printStackTrace();
    }
    finally {
      return res;
    }
  }
  //

  /**
      >> Method to get the subtree of the dir to be synced. <br />
      >> Sends a request to server; waits for response (TreeModel) <br />
      >> Returns (TreeModel)
  */
  public TreeModel requestTargetSubtree (String req) {
    // code is similar to method 'sendSyncRequest ()'
    TreeModel res = null;
    
    ObjectInputStream ois = null;
    PrintWriter pw = null;
    
    try {
      System.out.println ("sock = " + sock);
      InputStream is = sock.getInputStream ();
      OutputStream os = sock.getOutputStream ();
      
      pw = new PrintWriter (os, true);

      System.out.println (" .. in requestTargetSubtree (): os: " + os);
      System.out.println ("  // req = " + req);
      pw.println (req);
      
      ois = new ObjectInputStream (is);
      System.out.println (" .. just before reading object");
      Object tmp = ois.readObject ();
      System.out.println (" .. tmp = " + tmp);
      res = (TreeModel) tmp;

      System.out.println ("  // res = " + res);
    }
    catch (Exception e) {
      System.out.println ("  // " + e);
    }
    finally {
      return res;
    }
  }
  //

  /**
   *  Method to download file from server while syncing.
   */
  public void pullFromServer (String request, File dest) {
    System.out.println ("pullFromServer [ \n\trequest: " + request + ";");
    System.out.println ("\tdestination: " + dest + "\n]");
    //
    try
    {
        OutputStream os = sock.getOutputStream ();
        InputStream is = sock.getInputStream ();
    	PrintWriter pw = new PrintWriter (os, true);
        pw.println (request);
        // listen for incoming file...
        DataInputStream dis = new DataInputStream (is);
        FileOutputStream fos = new FileOutputStream (dest);
      //
      long total = dest.length ();
      long SZMAX = 419430400;    // 2^24 * 25 (i.e. 25 * 16MB)
      long SZMIN = 25600;        // 2^10 * 25 (i.e. 25 * 1KB)
      int CSMAX = 67108864;
      int CSMIN = 1024;
      int chunkSize = -1;

      if (total < SZMAX && total > SZMIN) {
        int parts = 25;
        chunkSize = (int) ((double) total / (double) parts);
      }
      else if (total > SZMAX) {
        chunkSize = CSMAX;
      }
      else if (total < SZMIN)
      {
        chunkSize = CSMIN;
      }

      byte[] buffer = new byte[chunkSize];
      long curr = 0;
      long prev = 0;
      int nb;
      
      long initial = Calendar.getInstance ().getTime ().getTime ();
      for (;;) {
        nb = dis.read (buffer);
        curr += nb;
        if (nb == -1) break;
        fos.write (buffer, 0, (int) (curr - prev));

        prev = curr;
      }
      
      long eventual = Calendar.getInstance ().getTime ().getTime ();
      System.out.println ("time period: " + (eventual - initial));
      //
      dis.close ();
      fos.close ();
    }
    catch (Exception e)
    {
        System.out.println ("while syncDownloading... " + e);
    }
  }

  /**
   * Requests dir creation on server given server folder path. <br />
   * Returns boolean.
   */
  public boolean mkdirOnServer (String request) {
    // send request.
    boolean done = false;
    try
    {
    	OutputStream os = sock.getOutputStream ();
        InputStream is = sock.getInputStream ();
        PrintWriter pw = new PrintWriter (os, true);
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        // request is like ... SYNC CREATE DIRECTORY "<DIR_PATH>" JNFSP/1.0
        pw.println (request);
        String response = br.readLine ();
        if (response.equals ("TRUE"))
        {
            done = true;
        }
    }
    catch (Exception e)
    {
        System.out.println ("exception: " + e);
    }
    return done;
  }

  /**
   * Requests file creation on server given server file path. <br />
   * Returns boolean.
   */
  public boolean createNewFileOnServer (String request) {
    // send request.
    boolean done = false;
    try
    {
    	OutputStream os = sock.getOutputStream ();
        InputStream is = sock.getInputStream ();
        PrintWriter pw = new PrintWriter (os, true);
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        // request is like ... SYNC CREATE FILE "<FILE_PATH>" JNFSP/1.0
        pw.println (request);
        String response = br.readLine ();
        if (response.equals ("TRUE"))
        {
            System.out.println ("Reply from server: " + response);
            done = true;
        }
    }
    catch (Exception e)
    {
        System.out.println ("exception: " + e);
    }
    return done;
  }

  /**
   * Requests file upload on server given server file path. <br />
   * Returns nothing.
   */
  public void pushToServer (String request, File src) {
    // send request.
    try
    {
    	OutputStream os = sock.getOutputStream ();
        InputStream is = sock.getInputStream ();
        PrintWriter pw = new PrintWriter (os, true);
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        // request is like ... SYNC UPLOAD FILE "<FILE_PATH>" "<file_size>" JNFSP/1.0
        pw.println (request);
        String response = br.readLine ();
        if (response.equals ("OK"))
        {
            // send file to server.
          System.out.println ("Server sent OK");
          FileInputStream fis = new FileInputStream (src);
          DataOutputStream dos = new DataOutputStream (os);
      
          long total = src.length ();
          long SZMAX = 419430400;    // 2^24 * 25 (i.e. 25 * 16MB)
          long SZMIN = 25600;        // 2^10 * 25 (i.e. 25 * 1KB)
          int CSMAX = 67108864;
          int CSMIN = 1024;
          int chunkSize = -1;
      
          if (total < SZMAX && total > SZMIN) {
            int parts = 25;
            chunkSize = (int) ((double) total / (double) parts);
          }
          else if (total > SZMAX) {
            chunkSize = CSMAX;
          }
          else if (total < SZMIN)
          {
            chunkSize = CSMIN;
          }

          byte[] buffer = new byte[chunkSize];
          long curr = 0;
          long prev = 0;
          int nb;
      
          long initial = Calendar.getInstance ().getTime ().getTime ();
          for (;;) {
            nb = fis.read (buffer);
            curr += nb;
            if (nb == -1) break;
            dos.write (buffer, 0, (int) (curr - prev));
            prev = curr;
          }
      
          long eventual = Calendar.getInstance ().getTime ().getTime ();
          System.out.println ("time period: " + (eventual - initial));
          //
          fis.close ();
          dos.close ();
          fis = null;
        }
    }
    catch (Exception e)
    {
        System.out.println ("exception: " + e);
    }
    //
  }

  public String doRegister (String u, String p) {
    String response = null;
    try
    {
    	// try to register
        out.println (">>>> ServerConnection.doRegister (): registering...");
        OutputStream os = sock.getOutputStream ();
        InputStream is = sock.getInputStream ();
        PrintWriter pw = new PrintWriter (os, true);
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        pw.println ("REGISTER UID " + u + " PWD " + p + " JNFSP/1.0");
        response = br.readLine ();
    }
    catch (Exception e)
    {
        out.println (e);
    }
    return response;
  }

  public String doLogin (String u, String p) {
    String response = null;
    try
    {
    	// try to log in
        out.println (">>>> ServerConnection.doLogout (): logging in...");
        OutputStream os = sock.getOutputStream ();
        InputStream is = sock.getInputStream ();
        PrintWriter pw = new PrintWriter (os, true);
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        pw.println ("LOGIN UID " + u + " PWD " + p + " JNFSP/1.0");
        response = br.readLine ();
    }
    catch (Exception e)
    {
        out.println (e);
    }
    return response;
  }

  public void doLogout (long sid) {
    try
    {
    	// send logout message and then close all
        out.println (">>>> in ServerConnection.doLogout (): logging out");
        OutputStream os = sock.getOutputStream ();
        InputStream is = sock.getInputStream ();
        PrintWriter pw = new PrintWriter (os, true);
        pw.println ("LOGOUT SID " + sid + " JNFSP/1.0");
        pw.close ();
        is.close ();
        sock.close ();
    }
    catch (Exception e)
    {
        out.println (e);
    }
  }
  //
}
