package realm.jnfss.comm;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
  * Template for the download worker thread
  */
public class UploadTask extends SwingWorker<String, String> {
  private OutputStream os;
  private long fileSize;
  private String flnm;
  private String flpa;

  /**
    * Only constructor for now
    */
  public UploadTask (OutputStream os, long fs, String fn, String fp) {
    this.os = os;
    fileSize = fs;
    flnm = fn;
    flpa = fp;
  }

  /**
    * This is the upload task
    */
  @Override
  public String doInBackground () {
    try {
      FileInputStream fis = new FileInputStream (new File (flpa));
      DataOutputStream dos = new DataOutputStream (os);
      
      long total = fileSize;
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
      long itemp = initial;
      long ftemp = initial;
      double prevspeed = 0, currspeed = 0;
      
      for (;;) {
        nb = fis.read (buffer);
        curr += nb;
        if (nb == -1) break;
        dos.write (buffer, 0, (int) (curr - prev));

        ftemp = Calendar.getInstance ().getTime ().getTime ();
        currspeed = (double)(1000) * ((double) (curr)) / (((double) (ftemp - itemp)) * ((double) 1048576.0));

        setProgress ((int) (25L * curr / total));
        firePropertyChange ("bytesUploaded", prev, curr);
        firePropertyChange ("speed", prevspeed, currspeed);

        prev = curr;
        prevspeed = currspeed;
      }
      
      long eventual = Calendar.getInstance ().getTime ().getTime ();
      System.out.println ("time period: " + (eventual - initial));
      //
      fis.close ();
      dos.close ();
      fis = null;
    }
    catch (Exception e) {
      System.out.println ("In UploadTask:: " + e);
    }
    
    return "file_uploaded";
  }
}
