package realm.jnfss;

import java.awt.EventQueue;
import javax.swing.JFrame;

import realm.jnfss.ctrl.Controller;
import realm.jnfss.ui.UserInterface;

import static java.lang.System.out;

/**
  * This is the template for the program launcher
  */
public class ApplicationLauncher {
  public static void main (String... args) {
    // start app process
    
    EventQueue.invokeLater (
      new Runnable () {
        public void run () {
          // add some extra initialization procedures...
          Initializer ini = new Initializer ();
          AppConfig ac = ini.attemptLogin ();
          out.println (">>>> in ApplicationLauncher.main (): sid = " + ac.getSID ());

          JFrame ui = new UserInterface (ac.getIsSyncable ());
          Controller c = new Controller (ui, ac.getSID ());
          
          ui.addPropertyChangeListener (c);
          
          ui.setVisible (true);
          ui.pack ();
        }
      }
    );
    
    System.out.println ("started");
  }
  //
}
