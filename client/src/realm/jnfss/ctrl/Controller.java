package realm.jnfss.ctrl;

import realm.jnfss.serv.ViewService;
import realm.jnfss.serv.UploadService;
import realm.jnfss.serv.DownloadService;
import realm.jnfss.serv.SyncService;

import realm.jnfss.ui.UserInterface;
import realm.jnfss.ui.FileSaver;
import realm.jnfss.ui.FileOpener;

import realm.jnfss.comm.ServerConnection;

import java.util.Observer;
import java.util.Observable;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JFrame;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.JOptionPane;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import static java.lang.System.out;
import static java.lang.System.exit;

/**
  * Template for the controller
  */
public class Controller implements Observer, PropertyChangeListener {
  // attributes
  private ViewService viewService;
  private UploadService uploadService;
  private DownloadService downloadService;
  private SyncService syncService;
  
  private UserInterface ui;
  private String downloadFilePathRel;
  private String previewFilePath;
  private volatile File fileToBeUploaded;
  
  private long sID;
  
  // ctor
  /**
    * Only one constructor for now
    */
  public Controller (JFrame fui, long sessionID) {
    // session info
    this.sID = sessionID;
    // others
    ui = (UserInterface) fui;
    viewService = new ViewService (sID);
    viewService.addObserver (this);
    downloadService = new DownloadService (sID);
    downloadService.addObserver (this);
    uploadService = new UploadService (sID);
    uploadService.addObserver (this);
    syncService = new SyncService (sID);
    syncService.addObserver (this);
  }
  
  // behavior
  /**
    * Initiates the view service
    */
  public void requestViewService () {
    msg ("Initiating View Service ...");
    ui.freeze ();
    
    Object tmp = viewService.serviceViewRequest ();
    if (tmp != null) {
      DefaultTreeModel dtm = (DefaultTreeModel) viewService.serviceViewRequest ();
      ui.setViewPanel (dtm);
      ui.unfreeze ();
      msg ("Obtained Directory Hierarchy from Server");
    }
    else {
      ui.unfreeze ();
      msg ("Unable to connect to Server ! Contact Administrator.");
    }
  }
  
  /**
    * Initiates the preview service
    */
  public void requestPreviewService (Object x) {
    msg ("Initiating Preview Service ...");
    ui.freeze ();
    
    String pathObj = x.toString ();
    String temp = pathObj.substring (1, pathObj.length () - 1);
    System.out.println (temp);
    String reqPath = temp.replaceAll (", ", "/");
    System.out.println (reqPath);
    String flnm = reqPath.substring (reqPath.lastIndexOf ('/') + 1);

    System.out.println ("filename: " + flnm);
    File config = new File ("downloadfolder.txt");
    BufferedReader br = null;
    String filePath = null;
    try
    {
      br = new BufferedReader (new InputStreamReader (new FileInputStream (config)));
      filePath = new String (br.readLine () + File.separator + flnm);
      previewFilePath = filePath;
      System.out.println ("client machine filepath: " + filePath);
      br.close ();
      br = null;
    }
    catch (Exception e)
    {
      System.out.println (e);
    }
    
    boolean done = viewService.servicePreviewRequest (reqPath);
    if (done) {
      // nothing
    }
    else msg ("Unable to Preview");
    
    ui.unfreeze ();
    msg ("Previewing ...");
  }

  /**
    * Requests the upload service taking the upload path as param
    */
  public void requestUploadService (Object uploadPath) {
    // uploadPath is the required path
    if (fileToBeUploaded == null)
    {
      msg ("No File Chosen to Upload!");
      return;
    }

    msg ("Initiating Upload... ");
    ui.freeze ();
    ui.swapCursors ();

    String uPath = uploadPath.toString ();
    System.out.println ("  // upload path = " + uPath);
    String path = uPath.substring (1, uPath.length () - 1);
    path += File.separator + fileToBeUploaded.getName ();
    System.out.println ("path to be sent: " + path);

    // use recorded variable 'fileToBeUploaded'
    boolean done = uploadService.serviceUploadRequest (fileToBeUploaded, path);
    if (!done)
    {
      msg ("Cancelled!");
      ui.swapCursors ();
      ui.unfreeze ();
      // unfreeze everything ... back to normal
    }
    else {
      msg ("Uploading... ");
    }
    //
  }
  
  /**
    * Initiates the upload service (default overload)
    */
  public void requestUploadService () {
    msg ("Initiating Upload Service ... ");
    ui.freeze ();

    Object tmp = viewService.serviceViewRequest ();
    if (tmp != null) {
      DefaultTreeModel dtm = (DefaultTreeModel) viewService.serviceViewRequest ();
      ui.setUploadPanel (dtm);
      //msg ("Root: " + dtm.toString ());
      msg ("Connected to Server.");
      
      FileOpener fo = new FileOpener (ui);
      // save file to be uploaded
      fileToBeUploaded = fo.openFile ();
      System.out.println ("In Controller... [fileToBeUploaded = " + fileToBeUploaded + "]");
      if (fileToBeUploaded == null)
      {
        ui.setNoPanel ();
        msg ("No file chosen to upload!");
      }
      msg ("Select the Destination Folder.");
      ui.unfreeze ();
      //
    }
    else {
      ui.unfreeze ();
      msg ("Unable to connect to Server ! Contact Administrator.");
    }
    //
  }

  /**
    * Requests the download service taking download path as param
    */
  public void requestDownloadService (Object downloadPath) {
    // downloadPath is the reqpath
    msg ("Initiating Download... ");
    ui.freeze ();
    ui.swapCursors ();

    String dPath = downloadPath.toString ();
    System.out.println ("  // download path = " + dPath);
    String path = dPath.substring (1, dPath.length () - 1);

    // write path to variable "downloadFilePathRel"
    downloadFilePathRel = path;
    
    boolean success = downloadService.serviceDownloadRequest (path);
    System.out.println ("serviceDownloadRequest (fPath) returned " + success);
    //
    msg ("Downloading... ");
  }
  
  /**
    * Requests the download service (default overload)
    */
  public void requestDownloadService () {
    msg ("Initiating Download Service ... ");
    ui.freeze ();
    
    Object tmp = viewService.serviceViewRequest ();
    if (tmp != null) {
      DefaultTreeModel dtm = (DefaultTreeModel) viewService.serviceViewRequest ();
      ui.setDownloadPanel (dtm);
      ui.unfreeze ();
      //msg ("Root: " + dtm.toString ());
      msg ("Select File to Download...");
    }
    else {
      ui.unfreeze ();
      msg ("Unable to connect to Server ! Contact Administrator.");
    }
    //
  }
  
  /**
    * Initiates the sync service
    */
  public void requestSyncService () {
    msg ("Initiating Sync Service ... ");
    ui.swapCursors ();
    ui.freeze ();

    ui.setNoPanel ();
    // check for presence of a file "syncflag"
    //   if present : server client share specified folder already
    //   else : first time press
    File sf;
    if ((sf = new File ("syncflag")).exists ())
    {
      msg ("syncflag exists");
      // folder already shared
      // check for mismatch
      msg ("Syncing...");
      syncService.serviceSyncRequest (sf);
    }
    else
    {
      msg ("syncflag does not exist");
      // first time entry
      // create shared folder
      //
      // create a sync folder on client and server machines
      // similar to dropbox.
      syncService.serviceSyncRequest ();
    }
    //

    msg ("Synchronized");
  }
  
  // private stuff
  private void msg (String s) {
    System.out.println (s);
    ui.displayStatus (s);
  }

  // saves a file
  private void doFileSave () {
    //
      msg ("Saving File to Local Machine...");
      String path = downloadFilePathRel;
      FileSaver fs = new FileSaver (ui);
      if (fs.saveFile (path, path.substring (path.lastIndexOf ('\\') + 1)))
      {
        System.out.println ("File Saved");
      }
      fs = null;
      //
      ui.swapCursors ();
      ui.unfreeze ();
      msg ("Ready");
  }
  
  // observer ...
  /**
    * Called when observed object notifies
    */
  public void update (Observable o, Object arg) {
    // observations ...
    if (arg.equals ("download_complete"))
    {
      // launch file chooser dialog
      doFileSave ();
    }
    else if (arg.equals ("upload_complete"))
    {
      // tell user upload is complete
      msg ("Upload Complete");
      ui.swapCursors ();
      ui.unfreeze ();
      //
    }
    else if (arg.equals ("preview_download_complete"))
    {
      // start explorer process
      System.out.println ("in update () of controller");
      msg ("launching viewer application program...");
      // highly non-recommended, just QNDI
      try {
        new ProcessBuilder ("explorer", previewFilePath).start ();
      }
      catch (Exception e) {}
      msg ("Ready");
    }
    else
    {
      msg ("arg: " + arg.getClass ().getName ());
      // TreeModel arrays
      TreeModel[] tm = (TreeModel[]) arg;
      ui.setSyncPanel ((DefaultTreeModel) tm[0], (DefaultTreeModel) tm[1]);
      ui.swapCursors ();
      ui.unfreeze ();
    }
  }

  // handle shutdown
  /**
    * Handles clean shutdown of application by logging out.
    */
  protected void handleShutdown (Object ob) {
    boolean b = (Boolean) ob;
    if (b)
    {
        // try to logout first
        try
        {
          ServerConnection sc = new ServerConnection ();
          sc.doLogout (sID);
          sc = null;
          // then alert about exit application
          JOptionPane.showMessageDialog (ui, "Successfully logged out", "Exiting...", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception e)
        {
            out.println (e);
        }
        finally
        {
            // exit application
            exit (0);
        }
    }
  }
  
  // listener ...
  /**
    * The property change handler...
    */
  public void propertyChange (PropertyChangeEvent pce) {
    String propertyName = pce.getPropertyName ();
    System.out.println ("event fired: " + propertyName);
    
    if (propertyName.equals ("viewButtonPressed")) {
      requestViewService ();
    }
    else if (propertyName.equals ("uploadButtonPressed")) {
      requestUploadService ();
    }
    else if (propertyName.equals ("downloadButtonPressed")) {
      requestDownloadService ();
    }
    else if (propertyName.equals ("syncButtonPressed")) {
      requestSyncService ();
    }
    else if (propertyName.equals ("selectedViewTreePath")) {
      requestPreviewService (pce.getNewValue ());
    }
    else if (propertyName.equals ("selectedDownloadTreePath")) {
      requestDownloadService (pce.getNewValue ());
    }
    else if (propertyName.equals ("selectedUploadTreePath")) {
      requestUploadService (pce.getNewValue ());
    }
    else if (propertyName.equals ("shutdownInitiated"))
    {
      handleShutdown (pce.getNewValue ());
    }
    //
  }
}
