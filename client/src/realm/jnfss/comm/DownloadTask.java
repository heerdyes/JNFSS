package realm.jnfss.comm;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
  * Template for the download worker thread
  */
public class DownloadTask extends SwingWorker<String, String>
{
  private InputStream is;
  private long fileSize;
  private String flnm;
  private boolean isDeleteOnExit;

  /**
    * Only constructor for now
    */
  public DownloadTask (InputStream is, long fs, String fn, boolean isDOE) {
    this.is = is;
	fileSize = fs;
    flnm = fn;
    isDeleteOnExit = isDOE;
  }

  /**
    * This is the download task
    */
  @Override
  public String doInBackground () {
    try {
      String fout;
      File conf = new File ("downloadfolder.txt");
      BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (conf)));
      fout = br.readLine ();
      fout += File.separator + flnm;
      
      File outfile = new File (fout);
      if (isDeleteOnExit)
      {
        outfile.deleteOnExit ();
      }
      DataInputStream dis = new DataInputStream (is);
      FileOutputStream fos = new FileOutputStream (outfile);
      
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
        nb = dis.read (buffer);
        curr += nb;
        if (nb == -1) break;
        fos.write (buffer, 0, (int) (curr - prev));

        ftemp = Calendar.getInstance ().getTime ().getTime ();
        currspeed = (double)(1000) * ((double) (curr)) / (((double) (ftemp - itemp)) * ((double) 1048576.0));

        setProgress ((int) (25L * curr / total));
        firePropertyChange ("bytesDownloaded", prev, curr);
        firePropertyChange ("speed", prevspeed, currspeed);

        prev = curr;
        prevspeed = currspeed;
      }
      
      long eventual = Calendar.getInstance ().getTime ().getTime ();
      System.out.println ("time period: " + (eventual - initial));
      //
      dis.close ();
      fos.close ();
      fos = null;
    }
    catch (Exception e) {}
    
    return "file_copied";
  }
}
