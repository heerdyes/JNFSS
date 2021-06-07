package realm.jnfss.serv;

import java.util.LinkedList;

/**
  * This is the template for a queue
  */
public class XQ<E> {
  private LinkedList<E> q = null;
  
  /**
    * Create an empty linked list
    */
  public XQ () {
    q = new LinkedList<E> ();
  }
  
  /**
    * enqueue an element
	*/
  public void enqueue (E x) {
    q.addLast (x);
  }
  
  /**
    * dequeue an element
	*/
  public E dequeue () {
    return q.removeFirst ();
  }
  
  /**
    * check if Q is empty
	*/
  public boolean isEmpty () {
    if (q.size () == 0) return true;
    return false;
  }
  
  /**
    * clear the queue and nullify the queue reference
	*/
  public void cleanup () {
    q.clear ();
    q = null;
  }

  /**
    * string representation of the queue
	*/
  public String toString () {
    return q.toString ();
  }
}
