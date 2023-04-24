package vortex.jnfss;

import java.net.*;
import java.io.*;
import java.util.*;
import static java.lang.System.out;

import javax.swing.tree.*;
import realm.jnfss.serv.XQ;

/**
  * this is the actual server side script/program
  */
public class ServerHandler extends TCPServer {
  private InputStream is;
  private OutputStream os;
  private String uploadPath;
  private Socket sock;

  /**
	* creates a tcp server with properties, a register and a hashtable
	*/
  public ServerHandler (Properties h, Register r, Hashtable<Long, Member> sht) {
    super (h, r, sht);
  }
  
  /**
	* overriding run of <code>TCPServer</code>
	*/
  public void run (Socket data) {
    try {
      is = data.getInputStream ();
      os = data.getOutputStream ();
      sock = data;
      
      BufferedReader br = new BufferedReader (new InputStreamReader (is));
      //System.out.println ("br = " + br);
      
      respondToRequest (br.readLine ());
      
      System.out.println ("respondToRequest () returned; \n  about to close bufferedreader...");
      br.close ();
      br = null;
    }
    catch (IOException e) {
      System.out.println ("io_exception: " + e);
    }
    catch (Exception e) {
      System.out.println ("exception: " + e);
    }
    finally {
      try {
        if (is != null) is.close ();
        os.close ();
        data.close ();
      }
      catch (Exception ioe) {}
      is = null;
      os = null;
      data = null;
    }
  }
  
  /**
	* the responder to all client requests and queries
	*/
  public void respondToRequest (String request) {
    System.out.println ("  // request: " + request);
    String token0 = null;
    String token1 = null;
    String token2 = null;
    int s0=-1, s1=-1, s2=-1;
    long _sid=-1;
    // request: CMD UID <uid> PWD <pwd> JNFSP/1.0
    // or     : SID <sid> CMD ... JNFSP/1.0
    if (request.contains (" "))
    {
      // obtain token0
      s0 = request.indexOf (' ');
      token0 = request.substring (0, s0);
      // obtain token1
      s1 = request.indexOf (' ', s0+1);
      token1 = request.substring (s0+1, s1);
      // obtain token2
      s2 = request.indexOf (' ', s1+1);
      token2 = request.substring (s1+1, s2);
    }
    else
    {
      token0 = request;
    }

    // if request starts with SID verify sid
    if (token0.equals ("SID"))
    {
      _sid = Long.parseLong (token1);
      // if sid unknown
      if (!verifySID (_sid))
      {
        // unknown user
        out.println ("unknown session ID.");
        sendMsg ("ERROR SID UNKNOWN JNFSP/1.0");
        return;
      }
    }
    
    try {
      if (token2.equals ("VIEW")) {
        doViewResponse (request.substring (s1+1));
      }
      else if (token2.equals ("UPLOAD")) {
        doUploadResponse (request.substring (s1+1));
      }
      else if (token2.equals ("DOWNLOAD")) {
        doDownloadResponse (request.substring (s1+1));
      }
      else if (token2.equals ("SYNC")) {
        doSyncResponse (request.substring (s1+1), _sid);
      }
      else if (token0.equals ("GET"))
      {
        doHttpResponse (request);
      }
      else if (token0.equals ("LOGIN"))
      {
        // check if user exists in the register
        // if so append to the logged_in hashtable
        doLoginResponse (request);
      }
      else if (token0.equals ("LOGOUT"))
      {
        // remove user from logged_in hashtable
        doLogoutResponse (request);
      }
      else if (token0.equals ("REGISTER"))
      {
        // get userid and password and append to the register
        // this means a new user is added to the register
        doRegisterResponse (request);
      }
      else {
        // echo the request back
        // i.e. do nothing
        doEchoResponse (request);
      }
    }
    catch (Exception e) {}
  }

  /**
	* respond to a login attempt
	*/
  protected void doLoginResponse (String cmd) {
    // get writer
    PrintWriter pw = new PrintWriter (os, true);
    // get user id
    // request: LOGIN UID <uid> PWD <pwd> JNFSP/1.0
    int s0 = cmd.indexOf (' ');
    int ub = cmd.indexOf (' ', s0+1);
    int ue = cmd.indexOf (' ', ub+1);
    String uid = cmd.substring (ub+1, ue);
    int pb = cmd.indexOf (' ', ue+1);
    int pe = cmd.indexOf (' ', pb+1);
    String pwd = cmd.substring (pb+1, pe);
    // check if userid exists
    Member mem = register.search (uid);
    if (mem != null)
    {
      // check if password matches
      if (mem.passwordMatches (pwd))
      {
        // login successful; send session id
        long sid = generateSID ();
        out.println (">>>> in ServerHandler.doLoginResponse (): sid = " + sid);
        pw.println ("SUCCESSFUL LOGIN SID " + sid + " JNFSP/1.0");
        // set the machine field of member
        // i.e. if it is not already present
        InetAddress ia = sock.getInetAddress ();
        if (ia == null)
        {
            // no point continuing ((!machineless client!))
            pw.println ("ERROR MACHINE IP NULL JNFSP/1.0");
            return;
        }
        boolean alright = mem.setInetAddress (ia);
        // if alright update hashtable else 
        // now put (sessionID, Member) kv pair into session Hashtable.
        if (!alright)
        {
            pw.println ("SYNCHRONIZABILITY FALSE JNFSP/1.0");
        }
        else
        {
            pw.println ("SYNCHRONIZABILITY TRUE JNFSP/1.0");
        }
        super.snHT.put (sid, mem);
        out.println (">>>> in doLoginResponse () : appended sessionHT: " + super.snHT);
      }
      else
      {
        // login unsuccessful
        // due to password mismatch
        pw.println ("UNSUCCESSFUL LOGIN PASSWORD MISMATCH JNFSP/1.0");
      }
    }
    else
    {
      // login unsuccessful
      // due to userid non-existence
      pw.println ("UNSUCCESSFUL LOGIN USERID NONEXISTENT JNFSP/1.0");
    }
  }

  /**
	* respond to a logout attempt
	*/
  protected void doLogoutResponse (String cmd) {
    // get writer
    PrintWriter pw = new PrintWriter (os, true);
    // get user id
    // request: LOGOUT SID <sid> JNFSP/1.0
    int s0 = cmd.indexOf (' ');
    int sb = cmd.indexOf (' ', s0+1);
    int se = cmd.indexOf (' ', sb+1);
    String ssid = cmd.substring (sb+1, se);
    long lsid = Long.parseLong (ssid);
    // lookup session hashtable to check if it contains the key given by sid
    if (super.snHT.containsKey (lsid))
    {
        // log out
        Member m = super.snHT.remove (lsid);
        out.println ("removed: " + m);
        pw.println ("SUCCESSFUL LOGOUT JNFSP/1.0");
    }
    else
    {
        // unknown user
        out.println ("unknown user");
        pw.println ("UNSUCCESSFUL LOGOUT USER UNKNOWN JNFSP/1.0");
    }
    // logout done
  }

  /**
	* respond to a registration attempt
	*/
  protected void doRegisterResponse (String cmd) {
    // request: REGISTER UID <uid> PWD <pwd> JNFSP/1.0
    // get writer
    PrintWriter pw = new PrintWriter (os, true);
    // get user id
    int s0 = cmd.indexOf (' ');
    int ub = cmd.indexOf (' ', s0+1);
    int ue = cmd.indexOf (' ', ub+1);
    String uid = cmd.substring (ub+1, ue);
    int pb = cmd.indexOf (' ', ue+1);
    int pe = cmd.indexOf (' ', pb+1);
    String pwd = cmd.substring (pb+1, pe);
    // check if userid exists
    Member mem = register.search (uid);
    // if mem is null then its a new member
    if (mem == null)
    {
        // register new member
        // create new member
        InetAddress ia = sock.getInetAddress ();
        if (ia == null)
        {
            // no point of continuing
            pw.println ("ERROR MACHINE IP NULL JNFSP/1.0");
            return;
        }
        mem = new Member (uid, pwd, ia);
        // append to register
        register.addMember (mem);
        if (mem != null)
        {
          // successful: non null member created
          // now assign sync space to user
          String sr = super.ht.getProperty ("SYNC_ROOT");
          File sdir = null;
          // try to create a new folder with uid as name
          try
          {
          	sdir = new File (sr + "\\" + mem.getUID ());
            sdir.mkdir ();
            out.println (">>>> in ServerHandler.doRegisterResponse (): syncDir = " + sdir);
          }
          catch (Exception e)
          {
            out.println (e);
          }
          finally
          {
            if (sdir != null && sdir.exists ())
            {
              // sdir could be created
              mem.setSyncDir (sdir);
              out.println (">>>> regn success.");
              pw.println ("SUCCESSFUL REGISTRATION JNFSP/1.0");
            }
            else
            {
              // either sdir is null or sdir could not be created
              out.println (">>>> regn failure.");
              pw.println ("UNSUCCESSFUL REGISTRATION PERMISSION DENIED JNFSP/1.0");
            }
          }
          // done
        }
    }
    else
    {
        // member already exists
        // illegal overwrite attempt
        pw.println ("UNSUCCESSFUL REGISTRATION OVERWRITE ATTEMPT JNFSP/1.0");
    }
  }

  /**
	* respond to an unclassified attempt
	*/
  protected void doEchoResponse (String cmd) {
    // simply echo
    try
    {
        PrintWriter pw = new PrintWriter (os, true);
        pw.println ("I heard you say " + cmd);
    }
    catch (Exception e)
    {
      out.println (e);
    }
  }

  /**
	* respond to a http request
	*/
  protected void doHttpResponse (String cmd) {
    String[] parts = cmd.split ("\\s+");
    if (parts[1].equals ("/"))
    {
      try
      {
      	DataOutputStream dos = new DataOutputStream (os);
        FileInputStream fis = new FileInputStream (new File ("index.html"));
        byte[] buf = new byte[1024];
        for (; ; )
        {
            int x = fis.read (buf);
            if (x == -1)
            {
              break;
            }
            dos.write (buf, 0, x);
        }
        fis.close ();
        dos.close ();
        fis = null;
      }
      catch (Exception e)
      {
        System.out.println (e);
      }
    }
    else
    {
      String nm = parts[1].substring (1);
      // send respective file...
      try
      {
      	DataOutputStream dos = new DataOutputStream (os);
        FileInputStream fis = new FileInputStream (new File (nm));
        byte[] buf = new byte[1024];
        for (; ; )
        {
            int x = fis.read (buf);
            if (x == -1)
            {
              break;
            }
            dos.write (buf, 0, x);
        }
        fis.close ();
        dos.close ();
        fis = null;
      }
      catch (Exception e)
      {
        System.out.println (e);
      }
    }
  }
  
  /**
	* respond to a view attempt
	*/
  protected void doViewResponse (String cmd) {
    int space1 = cmd.indexOf (' ');
    int space2 = cmd.indexOf (' ', space1 + 1);
    String[] cmda = new String[2];
    cmda[0] = cmd.substring (0, space1);
    cmda[1] = cmd.substring (space1 + 1, space2);
    
    if (cmda[1].equals ("TREE")) {
      try {
        Object dtm = getFileSystemTree ();
        ObjectOutputStream oos = new ObjectOutputStream (os);
        oos.writeObject (dtm);
      }
      catch (Exception e) {}
    }
    else if (cmda[1].equals ("FILE")) {
      int start = cmd.indexOf ('"');
      int stop = cmd.indexOf ('"', start + 1);
      String fpath = cmd.substring (start + 1, stop);
      
      System.out.println ("file path received: " + fpath);
      sendFile (fpath);
    }
  }
  
  /**
	* respond to an upload attempt
	*/
  protected void doUploadResponse (String cmd) {
    // uploader
    System.out.println ("Servicing upload cmd: " + cmd);
    int space1 = cmd.indexOf (' ');
    int space2 = cmd.indexOf (' ', space1 + 1);
    String[] cmda = new String[2];
    cmda[0] = cmd.substring (0, space1);
    cmda[1] = cmd.substring (space1 + 1, space2);

    if (cmda[1].equals ("FILE"))
    {
      // process filepath
      PrintWriter pw = new PrintWriter (os, true);
      int start = cmd.indexOf ('"');
      int stop = cmd.indexOf ('"', start + 1);
      String fpath = cmd.substring (start + 1, stop);

      int begin = cmd.indexOf (' ', stop);
      int end = cmd.indexOf (' ', begin + 1);
      long fs = Long.parseLong (cmd.substring (begin + 1, end));
      
      System.out.println ("file path received: " + fpath);
      // generate absolute filepath
      String root = ht.getProperty ("ROOT_FOLDER");
      if (root.indexOf (File.separatorChar) == -1)
      {
        // don't change fpath
      }
      else {
        fpath = root.substring (0, root.lastIndexOf (File.separatorChar) + 1) + fpath;
        System.out.println ("fpath: " + fpath);
      }
      //
      File fu = new File (fpath);
      if (fu.exists ())
      {
        pw.println ("REWRITE???");
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        String line = "";
        try
        {
        	line = br.readLine ();
        }
        catch (Exception e)
        {}
        if (line.equals ("YES"))
        {
          System.out.println ("fu.delete () : " + fu.delete ());
          pw.println ("WAITING...");
          receiveFile (fu, fs);
        }
        else if (line.equals ("NO")) {
          pw.println ("ABORTED!!!");
        }
      }
      else {
        pw.println ("WAITING...");
        receiveFile (fu, fs);
      }
    }
  }
  
  /**
	* respond to a download attempt
	*/
  protected void doDownloadResponse (String cmd) {
    System.out.println ("Servicing download cmd: " + cmd);
    int space1 = cmd.indexOf (' ');
    int space2 = cmd.indexOf (' ', space1 + 1);
    String[] cmda = new String[2];
    cmda[0] = cmd.substring (0, space1);
    System.out.println ("cmda[0]: " + cmda[0]);
    cmda[1] = cmd.substring (space1 + 1, space2);
    System.out.println ("cmda[1]: " + cmda[1]);

    if (cmda[1].equals ("FILE"))
    {
      System.out.println ("cmda[1].equals (\"FILE\")");
      int start = cmd.indexOf ('"');
      int stop = cmd.indexOf ('"', start + 1);
      String fpath = cmd.substring (start + 1, stop);
      
      System.out.println ("file path received: " + fpath);
      System.out.println ("before sendfile ()");
      sendFile (fpath);
    }
  }
  
  void syncDownloadFile(String cmd,long sid) {
    int q1 = cmd.indexOf ('"');
    int q2 = cmd.indexOf ('"', q1 + 1);
    String flpath = cmd.substring (q1 + 1, q2);
    File file = new File (flpath);
    if (file.exists ())
    {
        // send to client, who's currently waiting.
      System.out.println ("sending file...");
      
      try
      {
      	//
        DataOutputStream dos = new DataOutputStream (os);
        FileInputStream fis = new FileInputStream (file);
      
        long total = file.length ();
        long SZMAX = 419430400;    // 2^24 * 25 (i.e. 25 * 16MB)
        long SZMIN = 25600;        // 2^10 *25
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
        long currprev = 0;
        int nb;
      
        for (;;) {
          nb = fis.read (buffer);
          curr += nb;
          if (nb == -1) break;
          dos.write (buffer, 0, (int) (curr - currprev));
          currprev = curr;
        }
        // end of for
      }
      catch (Exception e)
      {
        System.out.println ("exception:: " + e);
      }

    }
    else
    {
      System.out.println ("File " + file + " doesn't exist !");
    }
  }
  
  void syncGetTree(String cmd,long sid) {
    try
    {
      ObjectOutputStream oos = new ObjectOutputStream (os);
      // extract dirname and dirpath from cmd
      // now its just this
      String dir_path = super.snHT.get (sid).getSyncDir ().getCanonicalPath ();

      // check for existence
      File sync_dir = new File (dir_path);
      if (sync_dir.exists ())
      {
        msg (sync_dir.toString () + " exists.");
        // below is the code to be tested... uses File system tree_node objects.
        FSTreeNode root = null;
        File drive = null;
        XQ<File> fnq = null;
        XQ<FSTreeNode> tnq = null;
    
        drive = sync_dir;
        root = new FSTreeNode (drive);

        System.out.println (drive.getCanonicalPath ());

        fnq = new XQ<File> ();
        tnq = new XQ<FSTreeNode> ();

        fnq.enqueue (drive);
        tnq.enqueue (root);

        while (!fnq.isEmpty ()) {
          File fn = fnq.dequeue ();
          FSTreeNode tn = tnq.dequeue ();

          for (File i : fn.listFiles ()) {
            FSTreeNode dmtn = new FSTreeNode (i);
            tn.add (dmtn);

            if (i.isDirectory ()) {
              fnq.enqueue (i);
              tnq.enqueue (dmtn);
            }
          }
          // for ends
        }
        // the hierarchy lies in 'root'
        System.out.println (" ... in doSyncResponse (), just before writing object...");
        oos.writeObject (new DefaultTreeModel (root));
        // report status
        msg ("SUCCESS: wrote object...");
      }
      else
      {
        msg (sync_dir.toString () + " does not exist.");
        FSTreeNode dmtn = new FSTreeNode (new File ("."));
        DefaultTreeModel dtm = new DefaultTreeModel (dmtn);
        oos.writeObject (dtm);
        // report status
        msg ("WARNING: wrote dummy object...");
      }
    }
    catch (Exception e)
    {
        System.out.println ("exception:: " + e);
    }
  }
  
  void syncMkdir(String cmd,long sid) {
    // create dir
    int q1 = cmd.indexOf ('"');
    int q2 = cmd.indexOf ('"', q1 + 1);
    String dirpath = cmd.substring (q1 + 1, q2);
    File dir = new File (dirpath);
    boolean s = dir.mkdir ();
    PrintWriter pw = new PrintWriter (os, true);
    if (s)
    {
      pw.println ("TRUE");
    }
    else
    {
      pw.println ("FALSE");
    }
  }
  
  void syncMkfile(String cmd,long sid) {
    // create file
    try
    {
      int q1 = cmd.indexOf ('"');
      int q2 = cmd.indexOf ('"', q1 + 1);
      String filepath = cmd.substring (q1 + 1, q2);
      File file = new File (filepath);
      boolean s = file.createNewFile ();
      PrintWriter pw = new PrintWriter (os, true);
      if (s)
      {
        pw.println ("TRUE");
      }
      else
      {
        pw.println ("FALSE");
      }
    }
    catch (Exception e)
    {
      msg ("exception:: " + e.toString ());
    }
  }
  
  void syncUpfile(String cmd,long sid) {
    // say OK to receive file.
    try
    {
      int q1 = cmd.indexOf ('"');
      int q2 = cmd.indexOf ('"', q1 + 1);
      int q3 = cmd.indexOf ('"', q2 + 1);
      int q4 = cmd.indexOf ('"', q3 + 1);
      String filepath = cmd.substring (q1 + 1, q2);
      long filesize = Long.parseLong (cmd.substring (q3 + 1, q4));
      File file = new File (filepath);
      PrintWriter pw = new PrintWriter (os, true);
      pw.println ("OK");
      //
      receiveFile (file, filesize);
    }
    catch (Exception e)
    {
      msg ("exception:: " + e.toString ());
    }
  }
  
  /**
   * The Synchronize response. <br />
   * Sends the directory hierarchy of the folder to be synced to the requesting client.
   */
  protected void doSyncResponse (String cmd, long sid) {
    msg ("In doSyncResponse (), cmd: " + cmd);
    //
    if (cmd.startsWith ("SYNC DOWNLOAD FILE"))
    {
      syncDownloadFile(cmd,sid);
    }
    else if (cmd.startsWith ("SYNC GET TREE"))
    {
      syncGetTree(cmd,sid);
    }
    else if (cmd.startsWith ("SYNC CREATE DIRECTORY"))
    {
      syncMkdir(cmd,sid);
    }
    else if (cmd.startsWith ("SYNC CREATE FILE"))
    {
      syncMkfile(cmd,sid);
    }
    else if (cmd.startsWith ("SYNC UPLOAD FILE"))
    {
      syncUpfile(cmd,sid);
    }
  }
  
  /**
   * Gets the directory hierarchy of the server root folder <br />
   * Returns a <b> DefaultTreeModel </b> reference.
   */
  protected DefaultTreeModel getFileSystemTree () {
    DefaultMutableTreeNode root = null;
    File drive = null;
    XQ<File> fnq = null;
    XQ<DefaultMutableTreeNode> tnq = null;
    
    try {
      System.out.println ("  // in serverhandler obj: ht: " + ht);
      drive = new File (ht.getProperty ("ROOT_FOLDER"));
      root = new DefaultMutableTreeNode ((Object) new String (drive.getName ()));
      
      System.out.println (drive.getCanonicalPath ());
      
      fnq = new XQ<File> ();
      tnq = new XQ<DefaultMutableTreeNode> ();
      
      fnq.enqueue (drive);
      tnq.enqueue (root);
      
      while (!fnq.isEmpty ()) {
        File fn = fnq.dequeue ();
        DefaultMutableTreeNode tn = tnq.dequeue ();
        
        for (File i : fn.listFiles ()) {
          DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode ((Object) new String (i.getName ()));
          tn.add (dmtn);
          
          if (i.isDirectory ()) {
            fnq.enqueue (i);
            tnq.enqueue (dmtn);
          }
        }
        // the hierarchy lies in 'root'
      }
    }
    catch (Exception e) {}
    finally {
      if (fnq != null) {
        fnq.cleanup ();
        fnq = null;
      }
      if (tnq != null) {
        tnq.cleanup ();
        tnq = null;
      }
      if (drive != null) {
        drive = null;
      }
      
      return new DefaultTreeModel (root);
    }
  }
  
  /**
	* send the file specified by the relative path
	*/
  protected void sendFile (String relPath) {
    System.out.println ("in send file");

    File rootFolder = null;
    String path = null;
    File reqFile = null;
    FileInputStream fis = null;
    DataOutputStream dos = null;
    long sizeLimit = 1073741824;

    try {
      rootFolder = new File (ht.getProperty ("ROOT_FOLDER"));
      System.out.println ("relpath: " + relPath);
      String rpsub = relPath.substring (relPath.indexOf ('\\'));
      System.out.println ("rpsub: " + rpsub);
      path = rootFolder.getCanonicalPath () + rpsub;
      System.out.println ("absolute path: " + path);
      reqFile = new File (path);
      
      if (reqFile.isDirectory ()) {
        PrintWriter pw = new PrintWriter (os, true);
        pw.println ("DIRECTORY!!!");
        return;
      }
      
      if (reqFile.exists ()) {
        if (reqFile.length () > sizeLimit) {
          // too large
          PrintWriter pw = new PrintWriter (os, true);
          pw.println ("BIGFILE!!!");
        }
        else {
          PrintWriter pw = new PrintWriter (os, true);
          pw.println ("SENDING... " + reqFile.length ());
          System.out.println ("sending file...");
          
          dos = new DataOutputStream (os);
          fis = new FileInputStream (reqFile);
          
          long total = reqFile.length ();
          long SZMAX = 419430400;    // 2^24 * 25 (i.e. 25 * 16MB)
          long SZMIN = 25600;        // 2^10 *25
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
          long currprev = 0;
          int nb;
          
          for (;;) {
            nb = fis.read (buffer);
            curr += nb;
            if (nb == -1) break;
            dos.write (buffer, 0, (int) (curr - currprev));
            currprev = curr;
          }
          // end of for
        }
        // end of try
      }
      else {
        PrintWriter pw = new PrintWriter (os, true);
        pw.println ("NOSUCHFILE!!!");
      }
    }
    catch (Exception e) {
      System.out.println ("  // " + e);
    }
    finally {
      try {
        fis.close ();
        dos.close ();
        fis = null;
      }
      catch (Exception e) {}
    }
    //
  }

  /**
	* receive a file
	*/
  protected void receiveFile (File fp, long fs) {
    // file reception
    // ... open inputstream and read chunx...
    try
    {
    	DataInputStream dis = new DataInputStream (is);
        FileOutputStream fos = new FileOutputStream (fp);

        long total = fs;
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
        fos.close ();
        fos = null;
        //
    }
    catch (Exception e)
    {
        System.out.println (e);
    }
    //
  }
  
  /**
	* messager
	*/
  protected void msg (String s) {
    System.out.println ("[!_MESSAGE: " + s + " _!]");
  }

  /**
	* shout at the client
	*/
  protected void sendMsg (String s) {
    PrintWriter pw = new PrintWriter (os, true);
    pw.println (s);
  }

  /**
	* verify session id
	*/
  protected boolean verifySID (long sid) {
    return super.snHT.containsKey (sid);
  }

}
