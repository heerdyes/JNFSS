package realm.jnfss.serv;

import java.util.Observable;

/**
  * Extends this class to build your service
  */
public abstract class Service extends Observable
{

    /**
     * Long variable that holds the current session ID.
     */
    protected long snID;

    /**
     * Constructor that sets the session ID.
     */
    protected Service (long snID) {
      this.snID = snID;
    }

}
