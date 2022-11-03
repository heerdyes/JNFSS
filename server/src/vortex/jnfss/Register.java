package vortex.jnfss;

import java.io.Serializable;
import java.util.ArrayList;

/**
  * the register of members
  */
public class Register implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Member> members;
	
	/**
	  * create an empty member list
	  */
	public Register () {
		members = new ArrayList<Member> ();
		members.clear();
	}
	
	/**
	  * gets the members
	  */
	public ArrayList<Member> getMembers () {
		return members;
	}
	
	/**
	  * add a member to the register
	  */
	public void addMember (Member m) {
		members.add(m);
	}

	/**
	  * remove a member from the register
	  */
    public boolean removeMember (String uid) {
        // if uid exists then remove
        // if successful removal return true
        Member m = null;
        if ((m = search (uid)) != null)
        {
            // uid exists
            return members.remove (m);
        }
        // uid does not exist
        return false;
    }
	
	/**
	  * search for a member by userid
	  */
	public Member search (String usrnm) {
		Member m = null;
		for (int i=0;i<members.size();i++) {
			if (members.get(i).getUID().equals(usrnm)) {
                // member exists
                m = members.get(i);
				break;
			}
		}
		return m;
	}
	
	/**
	  * string representation of register
	  */
	public String toString () {
		return "contents: { " + members.toString() + " };";
	}

	/**
	  * wipe the register clear
	  */
    public void wipe () {
        members.clear ();
    }

	/**
	  * check if register is empty
	  */
    public boolean isEmpty () {
        return members.isEmpty ();
    }

}
