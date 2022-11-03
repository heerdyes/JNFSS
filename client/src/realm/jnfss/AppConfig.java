package realm.jnfss;

/**
  * Objects of this class contain configuration settings for the program
  */
public class AppConfig
{
    private long sid;
    private boolean isSyncable;

    /**
      * setter for sid
      */
    public void setSID (long sid) { this.sid = sid; }

    /**
      * setter for isSyncable
      */
    public void setIsSyncable (boolean isSyncable) { this.isSyncable = isSyncable; }

    /**
      * getter for sid
      */
    public long getSID () { return sid; }

    /**
      * getter for isSyncable
      */
    public boolean getIsSyncable () { return isSyncable; }
}
