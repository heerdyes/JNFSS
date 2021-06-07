package realm.jnfss.ui;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.Cursor;

import java.io.File;

/**
  * the user interface
  */
public class UserInterface extends JFrame implements ActionListener, MouseListener {
  private int height = 369;
  private int width = 656;
  private int deflen = 30;
  private double fraction = 0.25;
  private TreePath oldViewPath;
  private TreePath oldDownloadPath;
  private TreePath oldUploadPath;
  private TreeModel viewTreeModel;
  private TreeModel downloadTreeModel;
  private TreeModel uploadTreeModel;
  private TreeModel syncServerTreeModel;
  private TreeModel syncClientTreeModel;
  private Component glassPane;
  private PanelType state;
  private SelectType select;
  private Cursor oldCur;
  private Cursor newCur;
  
  // components
  private JPanel masterPanel;
    private JPanel controlPanel;
      private JButton viewButton;
      private JButton uploadButton;
      private JButton downloadButton;
      private JButton syncButton;
      
    private JPanel dashboardPanel;
      private JPanel viewPanel;
        private JScrollPane viewScrollPane;
        private JTree viewTree;
        
      private JPanel uploadPanel;
        private JScrollPane uploadScrollPane;
        private JTree uploadTree;
        private JSplitPane uploadSplitPane;
        private JPanel uploadInfoPanel;
        private JScrollPane uploadPathScrollPane;
        private JTextField uploadPathTextField;
        private JButton uploadStartButton;

      private JPanel downloadPanel;
        private JScrollPane downloadScrollPane;
        private JTree downloadTree;
        private JSplitPane downloadSplitPane;
        private JPanel downloadInfoPanel;
        private JScrollPane downloadPathScrollPane;
        private JTextField downloadPathTextField;
        private JButton downloadStartButton;

      private JPanel syncPanel;
        private JScrollPane syncServerScrollPane;
          private JTree syncServerTree;
        private JScrollPane syncClientScrollPane;
          private JTree syncClientTree;
      
    private JSplitPane splitter;
    
    private JPanel statusPanel;
      private JTextField statusTextField;
  
  /**
    * parameter is boolean. <br />
	* true if syncable false otherwise
	*/
  public UserInterface (boolean isSync) {
    super ("JNetShare");
    
    setLocationByPlatform (true);
    setPreferredSize (new Dimension (width, height));
    //setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    // add a window listener...
    /**/
    addWindowListener (
      new WindowAdapter () {
        public void windowClosing (WindowEvent we) {
          firePropertyChange ("shutdownInitiated", false, true);
        }
      }
    );
    /**/
    
    glassPane = getGlassPane ();
    glassPane.addMouseListener (
      new MouseAdapter () {
        public void mouseClicked (MouseEvent me) {
          // no need to handle
          System.out.println ("application busy !");
        }
      }
    );

    oldCur = getCursor ();
    newCur = new Cursor (Cursor.WAIT_CURSOR);
    
    // creation
    masterPanel = new JPanel ();
      controlPanel = new JPanel ();
        viewButton = new JButton ("View");
        uploadButton = new JButton ("Upload");
        downloadButton = new JButton ("Download");
        syncButton = new JButton ("Sync");
        
      dashboardPanel = new JPanel ();
        viewPanel = new JPanel ();
          viewTree = new JTree ();
          viewScrollPane = new JScrollPane (viewTree);
          
        uploadPanel = new JPanel ();
          uploadTree = new JTree ();
          uploadScrollPane = new JScrollPane (uploadTree);
          uploadSplitPane = new JSplitPane (JSplitPane.VERTICAL_SPLIT);
          uploadInfoPanel = new JPanel ();
          uploadPathTextField = new JTextField (20);
          uploadPathScrollPane = new JScrollPane (uploadPathTextField);
          uploadStartButton = new JButton ("Start");

        downloadPanel = new JPanel ();
          downloadTree = new JTree ();
          downloadScrollPane = new JScrollPane (downloadTree);
          downloadSplitPane = new JSplitPane (JSplitPane.VERTICAL_SPLIT);
          downloadInfoPanel = new JPanel ();
          downloadPathTextField = new JTextField (20);
          downloadPathScrollPane = new JScrollPane (downloadPathTextField);
          downloadStartButton = new JButton ("Start");

        syncPanel = new JPanel ();
            syncServerTree = new JTree ();
          syncServerScrollPane = new JScrollPane (syncServerTree);
            syncClientTree = new JTree ();
          syncClientScrollPane = new JScrollPane (syncClientTree);
        
      splitter = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT);
      
      statusPanel = new JPanel ();
        statusTextField = new JTextField (deflen);
        
    // layouts
    controlPanel.setLayout (new GridLayout (4, 1));
    dashboardPanel.setLayout (new BorderLayout ());
    masterPanel.setLayout (new BorderLayout ());
    statusPanel.setLayout (new GridLayout (1, 1));
    downloadPathScrollPane.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    downloadPathScrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    uploadPathScrollPane.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    uploadPathScrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    
    // configuration ... 
      // adding listeners
    viewButton.addActionListener (this);
    uploadButton.addActionListener (this);
    downloadButton.addActionListener (this);
    syncButton.addActionListener (this);
    
    viewTree.addMouseListener (this);
    downloadTree.addMouseListener (this);
    uploadTree.addMouseListener (this);
      // others
    statusTextField.setEditable (false);
    downloadStartButton.addActionListener (this);
    downloadStartButton.setEnabled (false);
    uploadStartButton.addActionListener (this);
    uploadStartButton.setEnabled (false);

    // enable/disable sync depending on ctor parameters
    syncButton.setEnabled (isSync);
    
    // manifestation
    controlPanel.add (viewButton);
    controlPanel.add (uploadButton);
    controlPanel.add (downloadButton);
    controlPanel.add (syncButton);
    
    splitter.setLeftComponent (controlPanel);
    splitter.setRightComponent (dashboardPanel);
    splitter.setDividerLocation ((int) (fraction * (double) width));
    
    statusPanel.add (statusTextField);
    
    masterPanel.add (splitter, BorderLayout.CENTER);
    masterPanel.add (statusPanel, BorderLayout.SOUTH);
    
    add (masterPanel);
  }
  //
  
  // behavior
  /**
    * print on status bar
	*/
  public void displayStatus (String msg) {
    statusTextField.setText (msg);
    //
  }

  /**
    * swap the cursors to change to waiting mode and back
	*/
  public void swapCursors () {
    Component c = (Component) this;
    c.setCursor (newCur);
    Cursor tmp = newCur;
    newCur = oldCur;
    oldCur = tmp;
    tmp = null;
  }
  
  // set the view panel
  /**
    * takes a <code>DefaultTreeModel</code> as parameter and sets the view panel
	*/
  public void setViewPanel (DefaultTreeModel model) {
    dashboardPanel.removeAll ();
    state = PanelType.VIEW;
    viewTreeModel = model;
    viewTree.setModel (viewTreeModel);
    viewPanel.setLayout (new GridLayout (1, 1));
    viewPanel.add (viewScrollPane);
    dashboardPanel.add (viewPanel, BorderLayout.CENTER);
    dashboardPanel.validate ();
  }

  // set the upload panel
  /**
    * takes a <code>DefaultTreeModel</code> as parameter and sets the upload panel
	*/
  public void setUploadPanel (DefaultTreeModel model) {
    dashboardPanel.removeAll ();
    state = PanelType.UPLOAD;
    uploadTreeModel = model;
    uploadTree.setModel (uploadTreeModel);
    uploadPanel.setLayout (new BorderLayout ());
    uploadPanel.add (uploadScrollPane, BorderLayout.CENTER);

    uploadInfoPanel.setLayout (new GridBagLayout ());
    GridBagConstraints gbc = new GridBagConstraints ();
    gbc.gridheight = 1;
    gbc.weightx = 0.8;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    uploadInfoPanel.add (uploadPathScrollPane, gbc);
    gbc.weightx = 0.2;
    uploadInfoPanel.add (uploadStartButton, gbc);

    uploadPanel.add (uploadInfoPanel, BorderLayout.SOUTH);
    dashboardPanel.add (uploadPanel, BorderLayout.CENTER);
    dashboardPanel.validate ();
  }

  // set the download panel
  /**
    * takes a <code>DefaultTreeModel</code> as parameter and sets the download panel
	*/
  public void setDownloadPanel (DefaultTreeModel model) {
    dashboardPanel.removeAll ();
    state = PanelType.DOWNLOAD;
    downloadTreeModel = model;
    downloadTree.setModel (downloadTreeModel);
    downloadPanel.setLayout (new BorderLayout ());
    downloadPanel.add (downloadScrollPane, BorderLayout.CENTER);

    downloadInfoPanel.setLayout (new GridBagLayout ());
    GridBagConstraints gbc = new GridBagConstraints ();
    gbc.gridheight = 1;
    gbc.weightx = 0.8;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    downloadInfoPanel.add (downloadPathScrollPane, gbc);
    gbc.weightx = 0.2;
    downloadInfoPanel.add (downloadStartButton, gbc);

    downloadPanel.add (downloadInfoPanel, BorderLayout.SOUTH);
    dashboardPanel.add (downloadPanel, BorderLayout.CENTER);
    dashboardPanel.validate ();
  }

  // set the sync panel
  /**
    * takes a <code>DefaultTreeModel</code> as parameter and sets the sync panel
	*/
  public void setSyncPanel (DefaultTreeModel serv, DefaultTreeModel clie) {
    dashboardPanel.removeAll ();
    state = PanelType.SYNC;
    syncServerTreeModel = (TreeModel) serv;
    syncServerTree.setModel (syncServerTreeModel);
    syncClientTreeModel = (TreeModel) clie;
    syncClientTree.setModel (syncClientTreeModel);
    syncPanel.setLayout (new GridLayout (1, 2));
    // add components
    syncPanel.add (syncServerScrollPane);
    syncPanel.add (syncClientScrollPane);
    dashboardPanel.add (syncPanel, BorderLayout.CENTER);
    dashboardPanel.validate ();
  }

  /**
    * sets an empty panel
	*/
  public void setNoPanel () {
    dashboardPanel.removeAll ();
    state = PanelType.NONE;
    dashboardPanel.validate ();
  }
  
  // freezing and thawing (unfreezing)
  /**
    * freeze the ui... block all ui events
	*/
  public void freeze () {
    glassPane.setVisible (true);
  }
  
  /**
    * revert ui back to normal
	*/
  public void unfreeze () {
    glassPane.setVisible (false);
  }
  
  // cleanup
  /**
    * remove panels <br />
	* nullify components
	*/
  public void cleanup () {
    remove (masterPanel);
    
    masterPanel = null;
      controlPanel = null;
        viewButton = null;
        uploadButton = null;
        downloadButton = null;
        syncButton = null;
        
      dashboardPanel = null;
        viewPanel = null;
        uploadPanel = null;
        downloadPanel = null;
        syncPanel = null;
        
      statusPanel = null;
        statusTextField = null;
      
      splitter = null;
  }
  
  // listen to events ...
  /**
    * the event handling method
	*/
  public void actionPerformed (ActionEvent ae) {
    final String cmd = ae.getActionCommand ();
    new Thread (
      new Runnable () {
        public void run () {
          System.out.println (cmd);
          if (cmd.equals ("View")) {
            firePropertyChange ("viewButtonPressed", false, true);
          }
          else if (cmd.equals ("Upload")) {
            firePropertyChange ("uploadButtonPressed", false, true);
          }
          else if (cmd.equals ("Download")) {
            firePropertyChange ("downloadButtonPressed", false, true);
          }
          else if (cmd.equals ("Sync")) {
            firePropertyChange ("syncButtonPressed", false, true);
          }
          else if (cmd.equals ("Start"))
          {
            System.out.println ("Start Type: " + select);
            if (select.equals (SelectType.DOWNLOAD))
            {
              // the path in downloadPathTextField is a leaf, hence send it to downloadservice
              firePropertyChange ("selectedDownloadTreePath", "", downloadPathTextField.getText ());
            }
            else if (select.equals (SelectType.UPLOAD))
            {
              // text of uploadPathTextField is folder, send to uploadservice
              firePropertyChange ("selectedUploadTreePath", "", uploadPathTextField.getText ());
            }
          }
        }
      }
    ).start ();
  }
  
  // mice events
  public void mouseClicked (MouseEvent me) {}
  
  public void mouseEntered (MouseEvent me) {}
  
  public void mouseExited (MouseEvent me) {}
  
  /**
    * mouse press handler
	*/
  public void mousePressed (MouseEvent me) {
    //
    System.out.println ("state: " + state);
    System.out.println ("mouseevent : " + me);
    if (state.equals (PanelType.VIEW))
    {
      int selRow = viewTree.getRowForLocation(me.getX(), me.getY());
      TreePath selPath = viewTree.getPathForLocation(me.getX(), me.getY());
      if(selRow != -1) {
        if(me.getClickCount() == 1) {
          handleViewSingleClick(selRow, selPath);
        }
        else if(me.getClickCount() == 2) {
          handleViewDoubleClick(selRow, selPath);
        }
      }
    }
    else if (state.equals (PanelType.DOWNLOAD))
    {
      // handle download
      int selRow = downloadTree.getRowForLocation(me.getX(), me.getY());
      TreePath selPath = downloadTree.getPathForLocation(me.getX(), me.getY());
      if(selRow != -1) {
        if(me.getClickCount() == 1) {
          handleDownloadSingleClick(selRow, selPath);
        }
        else if(me.getClickCount() == 2) {
          handleDownloadDoubleClick(selRow, selPath);
        }
      }
    }
    else if (state.equals (PanelType.UPLOAD))
    {
      // handle upload
      System.out.println ("uploadTree: " + uploadTree);
      int selRow = uploadTree.getRowForLocation(me.getX(), me.getY());
      System.out.println ("selRow: " + selRow);
      TreePath selPath = uploadTree.getPathForLocation(me.getX(), me.getY());
      System.out.println ("selPath: " + selPath);
      if(selRow != -1) {
        if(me.getClickCount() == 1) {
          System.out.println ("clickcount = " + me.getClickCount ());
          handleUploadSingleClick(selRow, selPath);
          System.out.println ("  // after returning from handleUploadSingleClick ()");
        }
        else if(me.getClickCount() == 2) {
          handleUploadDoubleClick(selRow, selPath);
        }
      }
    }
    else if (state.equals (PanelType.SYNC))
    {
      // handle sync
    }
    //
  }
  
  public void mouseReleased (MouseEvent me) {}
  
  // mouse responses
  /**
    * handle single clicks on view panel
	*/
  private void handleViewSingleClick (int row, TreePath path) {
    displayStatus ("path: " + path.toString ());
    select = SelectType.VIEW;
  }
  
  /**
    * handle double clicks on view panel
	*/
  private void handleViewDoubleClick (int row, TreePath newViewPath) {
    TreeNode selNode = (TreeNode) newViewPath.getLastPathComponent ();
    if (viewTreeModel.isLeaf (selNode)) {
      firePropertyChange ("selectedViewTreePath", oldViewPath, newViewPath);
    }
    oldViewPath = newViewPath;
  }
  
  /**
    * handle single clicks on download panel
	*/
  private void handleDownloadSingleClick (int row, TreePath newDownloadPath) {
    displayStatus ("path: " + newDownloadPath.toString ());
    System.out.println ("before settext...");
    downloadPathTextField.setText (newDownloadPath.toString ().replaceAll (", ", "\\\\"));
    select = SelectType.DOWNLOAD;

    System.out.println ("after selecting download type ...");

    TreeNode selNode = (TreeNode) newDownloadPath.getLastPathComponent ();
    System.out.println ("after getting selnode...");
    if (downloadTreeModel.isLeaf (selNode)) {
      downloadStartButton.setEnabled (true);
    }
    else {
      downloadStartButton.setEnabled (false);
    }
    oldDownloadPath = newDownloadPath;
  }
  
  /**
    * handle double clicks on download panel
	*/
  private void handleDownloadDoubleClick (int row, TreePath newDownloadPath) {
    // do nothing
  }

  /**
    * handle single clicks on upload panel
	*/
  private void handleUploadSingleClick (int row, TreePath newUploadPath) {
    displayStatus ("path: " + newUploadPath.toString ());
    System.out.println ("  // uploadPathTextField: " + uploadPathTextField);
    System.out.println ("  // new upload path: " + uploadPathTextField);
    String nup = newUploadPath.toString ();
    System.out.println ("nup = " + nup);
    System.out.println ("nup.indexOf (,): " + nup.indexOf (", ") + "; nup.length () = " + nup.length ());
    String _nup = null;
    try
    {
        _nup = nup.replaceAll (",\\s", "\\\\");
        //_nup = nup.replaceAll ("[", "{");
    }
    catch (Exception e)
    {
        System.out.println (e);
        System.out.println ("init_cause: " + e.initCause (e.getCause ()));
    }

    System.out.println ("_nup = " + _nup);
    uploadPathTextField.setText (_nup);
    System.out.println ("  // after setting textfield.");
    select = SelectType.UPLOAD;

    TreeNode selNode = (TreeNode) newUploadPath.getLastPathComponent ();
    if (uploadTreeModel.isLeaf (selNode)) {
      uploadStartButton.setEnabled (false);
    }
    else {
      uploadStartButton.setEnabled (true);
    }
    oldUploadPath = newUploadPath;
  }
  
  private void handleUploadDoubleClick (int row, TreePath newUploadPath) {
    // do nothing
  }
  
   // repainting ...
  //
  /**
    * overriding paint
	*/
  public void paint (Graphics g) {
    super.paint (g);
    Rectangle screen = g.getClipBounds ();
    
    height = (int) screen.getHeight ();
    width = (int) screen.getWidth ();
    
    splitter.setDividerLocation ((int) (fraction * (double) width));
    //System.out.println ("\t// window changed");
  }
}
