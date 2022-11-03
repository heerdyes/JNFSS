package vortex.jnfss;

import java.io.Serializable;
import java.net.*;
import java.io.*;

/**
  * a member of the network file sharing system software
  */
public class Member implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String uid;
	private String pwd;
    private InetAddress inetAddress = null;
    private File syncDir = null;
	
	/**
	  * takes userid and passwd as parameters
	  */
	public Member (String u, String p) {
		uid = u;
		pwd = p;
	}

	/**
	  * takes uid, pwd, and ip addr as parameters
	  */
    public Member (String u, String p, InetAddress ia) {
		uid = u;
		pwd = p;
        inetAddress = ia;
	}

	/**
	  * takes uid, pwd, ip addr and file as parameters
	  */
    public Member (String u, String p, InetAddress ia, File f) {
		uid = u;
		pwd = p;
        inetAddress = ia;
        syncDir = f;
	}
	
	/**
	  * string representation
	  */
	public String toString () {
		return "[uid: " + uid + "; inetAddress: " + inetAddress + "; syncDir: " + syncDir + "]";
	}
	
	/**
	  * gets the uid
	  */
	public String getUID () {
		return uid;
	}

	/**
	  * checks if password matches
	  */
    public boolean passwordMatches (String p) {
      return pwd.equals (p);
    }

	/**
	  * set the ip address
	  */
    public boolean setInetAddress (InetAddress ia) {
      // if ia null only then write else return boolean
      if (inetAddress != null)
      {
          // compare parameter ia with existing ia
          // if not same warn user that sync will not be possible
          return inetAddress.equals (ia);
      }
      if (ia == null)
      {
          return false;
      }
      inetAddress = ia;
      return true;
    }

	/**
	  * get the ip address
	  */
    public InetAddress getInetAddress () {
      return inetAddress;
    }

	/**
	  * get the sync directory
	  */
    public File getSyncDir () {
      return syncDir;
    }

	/**
	  * set the sync directory
	  */
    public boolean setSyncDir (File sd) {
      // check if this member's syncDir is already set
      // if so then return false
      // else i.e. if syncDir is null then set it to sd (which is assumed to be !null)
      if (sd != null)
      {
        if (syncDir == null)
        {
          syncDir = sd;
          return true;
        }
        // else come out and return false
      }
      return false;
    }

}
