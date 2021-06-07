package realm.jnfss.serv;

import realm.jnfss.comm.ServerConnection;
import vortex.jnfss.FSTreeNode;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import java.util.Observable;
import java.util.Observer;
import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
  * This class is Observable.
  * Observer is the Controller
*/
public class SyncService extends Service implements Observer {
  private File sfconf;
  private ServerConnection sc;
  private String syncDirPath;

  /**
    * Construct object with a server connection
    * currently empty
  */
  public SyncService (long sid) {
    // for later use
    super (sid);
  }

  /**
    * this should be of use later on...
    * to create 'syncfolder' on client and server
    * in case it is absent.
  */
  public void serviceSyncRequest () {
    // creation of "syncfolder" on client and server
    //   client-side
    //
  }
  //

  /**
    * This method assumes presence of the file parameter passed to it
    */
  public void serviceSyncRequest (File sync) {
    /* "syncfolder" already exists
    __ check syncfolders for mismatch
    __   init sf...
    */
    sfconf = sync;
    //   read sf and find out client & server side paths
    try
    {
      BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (sfconf)));
      String sync_dir_name = br.readLine ();
      String client_path = br.readLine ();
      msg ("sync_dir:: " + sync_dir_name);
      msg ("client:: " + client_path);
      br.close ();

      //String sync_req = "SID " + super.snID + " SYNC GET TREE \"" + sync_dir_name + "\" \"" + server_path + "\" JNFSP/1.1";
      String sync_req = "SID " + super.snID + " SYNC GET TREE JNFSP/1.1";
      msg (sync_req);
      sc = new ServerConnection ();
      sc.addObserver (this);

      // update client_path
      client_path = client_path + File.separator + sync_dir_name;

      // tree to be matched.
      // client to do most of the work (reduce load on server)
      // get syncdir tree from server, get syncdir from client
      // match them both, recursively, accordingly upload or download files.

      TreeModel serverTree = sc.requestTargetSubtree (sync_req);
      TreeModel clientTree = getTargetSubtree (client_path);

      msg ("serverTree: " + serverTree);
      msg ("clientTree: " + clientTree);

      // print them
      msg ("In serviceSyncRequest ()");
      msg ("serverTree = " + serverTree.toString ());
      msg ("clientTree = " + clientTree.toString ());

      // give two trees to method 'syncTrees'
      syncTrees (serverTree, clientTree, client_path);

      // updated trees are now shown.
      // notify controller
        // problem in serverconnection
      sc = new ServerConnection ();
      sc.addObserver (this);
      //msg ("sc = " + sc.toString ());
      serverTree = sc.requestTargetSubtree (sync_req);
      clientTree = getTargetSubtree (client_path);
      TreeModel[] temp_tm = new TreeModel[2];
      temp_tm[0] = serverTree;
      temp_tm[1] = clientTree;
      //
      setChanged ();
      notifyObservers (temp_tm);
      // done.
    }
    catch (Exception e)
    {
      msg ("exception:: " + e.toString ());
    }

  }

  /**
      >> Method to get the <b>tree of folder</b> to be synced on client machine <br />
      >> takes string (location) as parameter
  */
  public TreeModel getTargetSubtree (String path) {
    TreeModel tm = null;
    syncDirPath = path;
    //
      File sync_dir = new File (path);
      if (sync_dir.exists ())
      {
        msg (sync_dir.toString () + " exists.");
        // code using FSTreeNode to be tested.
        /**/
        FSTreeNode root = null;
        File drive = null;
        XQ<File> fnq = null;
        XQ<FSTreeNode> tnq = null;
    
        drive = sync_dir;
        root = new FSTreeNode (drive);

        try
        {
        	System.out.println (drive.getCanonicalPath ());
        }
        catch (Exception e)
        {}

        fnq = new XQ<File> ();
        tnq = new XQ<FSTreeNode> ();

        fnq.enqueue (drive);
        tnq.enqueue (root);

        while (!fnq.isEmpty ()) {
          File fn = fnq.dequeue ();
          FSTreeNode tn = tnq.dequeue ();

          for (File i : fn.listFiles ()) {
            FSTreeNode dmtn = new FSTreeNode (i);
            try
            {
            	tn.add (dmtn);
            }
            catch (Exception e)
            {
                System.out.println (e);
            }

            if (i.isDirectory ()) {
              fnq.enqueue (i);
              tnq.enqueue (dmtn);
            }
          }
          // for ends
        }
        // the hierarchy lies in 'root'
        /**/
        // done.
        tm = (TreeModel) new DefaultTreeModel (root);
        // report status
        msg ("SUCCESS: got client tree model.");
      }
      else
      {
        msg (sync_dir.toString () + " does not exist.");
        FSTreeNode dmtn = new FSTreeNode (new File ("."));
        DefaultTreeModel dtm = new DefaultTreeModel (dmtn);
        tm = (TreeModel) dtm;
        // report status
        msg ("WARNING: got dummy model.");
      }
    //
    return tm;
  }

  /**
      >> Method to <b>synchronize</b> the server and client subtrees
  */
	public void syncTrees (TreeModel st, TreeModel ct, String cPath) {
        System.out.println ("cPath = " + cPath);
		FSTreeNode sroot = (FSTreeNode) st.getRoot();
		FSTreeNode croot = (FSTreeNode) ct.getRoot();
		//
		FSTreeNode[] root = new FSTreeNode[2];
		root[0] = sroot;
		root[1] = croot;
		XQ<FSTreeNode[]> stateQ = new XQ<FSTreeNode[]> ();
		XQ<FSTreeNode[]> downloadQ = new XQ<FSTreeNode[]> ();
		XQ<FSTreeNode[]> uploadQ = new XQ<FSTreeNode[]> ();
        // first download server extras
        System.out.println ("in syncTrees (). ");
        System.out.println ("root: " + root);
		stateQ.enqueue(root);
		while (!stateQ.isEmpty()) {
			FSTreeNode[] temp = stateQ.dequeue();
			Enumeration se = temp[0].children();
			Enumeration ce = temp[1].children();
            FSTreeNode[] searr = new FSTreeNode [temp[0].getChildCount ()];
            FSTreeNode[] cearr = new FSTreeNode [temp[1].getChildCount ()];
            for (int i=0; se.hasMoreElements (); i++)
            {
                searr[i] = (FSTreeNode) se.nextElement ();
            }
            for (int i=0; ce.hasMoreElements (); i++)
            {
                cearr[i] = (FSTreeNode) ce.nextElement ();
            }
			for (FSTreeNode snode : searr) {
				boolean isContained = false;
                File sf = snode.getFSNode ();
				for (FSTreeNode cnode : cearr) {
					if (snode.equals(cnode)) {
                        //System.out.println (snode + " equals " + cnode);
						if (sf.isDirectory()) {
							FSTreeNode[] x = new FSTreeNode[2];
							x[0] = snode; x[1] = cnode;
							stateQ.enqueue(x);
						}
						else if (sf.isFile()) {
							// do nothing, no date check
						}
						isContained = true;
                        break;
					}
				}
				if (isContained) {
					// handled
                    System.out.println (snode + " is already present.");
				}
				else {
                    System.out.println ("going to construct node...");
                    TreePath path = new TreePath (temp[1].getPath ());
                    String sp = path.toString ();
                    System.out.println ("  // raw sp = " + sp);
                    sp = sp.substring (1, sp.length () - 1);
                    sp = sp.replaceAll (", ", "\\\\");
                    sp += "\\" + snode.toString ();
                    sp = cPath.substring (0, cPath.lastIndexOf ('\\') + 1) + sp;
                    System.out.println ("dest_addr: " + sp);
                    File nd = new File (sp);
                    FSTreeNode newCNode = new FSTreeNode (nd);
                    try
                    {
                        if (sf.isDirectory ())
                        {
                            boolean d = nd.mkdir ();
                            if (d)
                            {
                              FSTreeNode[] _f = new FSTreeNode[2];
                              _f[0] = snode;
                              _f[1] = newCNode;
                              stateQ.enqueue (_f);
                            }
                        }
                        else
                        {
                            boolean f = nd.createNewFile ();
                            String _req = "SID " + super.snID + " SYNC DOWNLOAD FILE \"" + snode.getFSNode ().getCanonicalPath () + "\" JNFSP/1.0";
                            sc = new ServerConnection ();
                            sc.pullFromServer (_req, nd);
                        }
                    	temp[1].add (newCNode);
                    }
                    catch (Exception e)
                    {
                        System.out.println ("while adding: " + e);
                    }
                    //
                    FSTreeNode[] narr = new FSTreeNode[2];
                    narr[0] = snode; narr[1] = newCNode;
					downloadQ.enqueue(narr);
                    //System.out.println ("To be downloaded: " + snode);
				}
			}
		}
		//
        System.out.println ("finished loading downloadQ.");
        System.out.println ("stateQ = " + stateQ);
        // upload client extras
		stateQ.enqueue(root);
		while (!stateQ.isEmpty()) {
			FSTreeNode[] temp = stateQ.dequeue();
			Enumeration se = temp[0].children();
			Enumeration ce = temp[1].children();
            FSTreeNode[] searr = new FSTreeNode [temp[0].getChildCount ()];
            FSTreeNode[] cearr = new FSTreeNode [temp[1].getChildCount ()];
            for (int i=0; se.hasMoreElements (); i++)
            {
                searr[i] = (FSTreeNode) se.nextElement ();
            }
            for (int i=0; ce.hasMoreElements (); i++)
            {
                cearr[i] = (FSTreeNode) ce.nextElement ();
            }
            for (FSTreeNode cnode : cearr) {
				boolean isContained = false;
                File cf = cnode.getFSNode ();
				for (FSTreeNode snode : searr) {
                    System.out.println (cnode + " vs " + snode);
					if (cnode.equals(snode)) {
                        //System.out.println (cnode + " equals " + snode);
						if (cf.isDirectory()) {
							FSTreeNode[] x = new FSTreeNode[2];
							x[0] = snode; x[1] = cnode;
							stateQ.enqueue(x);
						}
						else if (cf.isFile()) {
							// do nothing, no date check
						}
						isContained = true;
                        break;
					}
				}
				if (isContained) {
					// handled
				}
				else {
                    // it is a client extra, upload to server.
                    /*
                    TreePath path = new TreePath (temp[0].getPath ());
                    String sp = path.toString ();
                    */
                    String sp = null;
                    try
                    {
                    	sp = temp[0].getFSNode ().getCanonicalPath ();
                    }
                    catch (Exception e)
                    {
                        System.out.println ("exception: " + e);
                    }
                    System.out.println ("  // sp = " + sp);
                    sp += "\\" + cnode.toString ();
                    System.out.println ("dest_addr: " + sp);
                    File _df = new File (sp);
                    FSTreeNode newSNode = null;
                    try
                    {
                        if (cf.isDirectory ())
                        {
                            // request to create dir on server
                            sc = new ServerConnection ();
                            String _req = "SID " + super.snID + " SYNC CREATE DIRECTORY \"" + _df.getCanonicalPath () + "\" JNFSP/1.0";
                            boolean d = sc.mkdirOnServer (_req);
                            if (d)
                            {
                              newSNode = new FSTreeNode (_df);
                              temp[0].add (newSNode);
                              FSTreeNode[] _f = new FSTreeNode[2];
                              _f[0] = newSNode;
                              _f[1] = cnode;
                              stateQ.enqueue (_f);
                            }
                            else {
                                System.out.println ("[alas]: could not create directory on server. (Possible lack of permissions)");
                            }
                        }
                        else
                        {
                            // request to create a file on server
                            sc = new ServerConnection ();
                            String req = "SID " + super.snID + " SYNC CREATE FILE \"" + _df.getCanonicalPath () + "\" JNFSP/1.0";
                            boolean f = sc.createNewFileOnServer (req);
                            if (f)
                            {
                                newSNode = new FSTreeNode (_df);
                                temp[0].add (newSNode);
                                String _req = "SID " + super.snID + " SYNC UPLOAD FILE \"" + _df.getCanonicalPath () + "\" \"" + cf.length () + "\" JNFSP/1.0";
                                System.out.println (" **imp** _req = " + _req);
                                sc = new ServerConnection ();
                                sc.pushToServer (_req, cf);
                            }
                            else {
                                System.out.println ("[alas]: could not create file on server. (Possible lack of permissions)");
                            }
                        }
                    	//temp[0].add (newSNode);
                    }
                    catch (Exception e)
                    {
                        System.out.println ("while adding: " + e);
                    }
                    //
                    FSTreeNode[] narr = new FSTreeNode[2];
                    narr[0] = newSNode; narr[1] = cnode;
					uploadQ.enqueue(narr);
				}
			}
		}
		// now downloadQ and uploadQ are ready for use
		// first download then upload
		System.out.println ("downloadQ: " + downloadQ);
        System.out.println ("uploadQ: " + uploadQ);
        //
	}
  //

  /**
    * just to avoid writing s.o.p () everytime. :)
  */
  private void msg (String s) { System.out.println ("[!_MESSAGE: " + s + " _!]"); }

  /**
    * Listener
  */
  public void update (Observable o, Object arg) {
    msg (o.toString () + ", has sent, " + arg.toString ());
  }
}
