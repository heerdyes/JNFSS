package vortex.jnfss;

import java.io.File;
import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * FSTreeNode is a viewable tree node as well as a file.
 */
public class FSTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;
	
	private File fsNode;
	
    /**
     * Constructor takes a file as an argument. <br />
     * There is no default constructor
     */
	public FSTreeNode (File f) {
		super (f.getName());
		fsNode = f;
	}
	
	public String toString () {
		String sup = super.toString();
		
		return sup;
	}

	public boolean equals (Object x) {
		FSTreeNode fstn = (FSTreeNode) x;
		boolean p = this.getFSNode().getName().equals(fstn.getFSNode().getName());
		boolean q = !(this.getFSNode().isDirectory() ^ fstn.getFSNode().isDirectory());
		return p && q;
	}
	
	public void add (FSTreeNode fstn) throws IOException {
		String xname = fstn.getFSNode().getParent();
		String pname = fsNode.getCanonicalPath();
		if (pname.equals(xname)) {
			super.add(fstn);
		}
		else {
			// ideally throw some appropriate exception, below is QNDI
			System.out.println ("Illegal addition !!");
		}
	}
	
	public File getFSNode () {
		return fsNode;
	}

    /**
     * dumps the contents of the object. <br />
     * a little better than the toString () method.
     */
    public void dump () {
        String sup = null;
		try {
			sup += "MTNode: " + sup + ", FSNode: " + fsNode.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println (sup);
    }
}
