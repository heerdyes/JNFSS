package realm.jnfss.sh;

import java.io.*;
import java.net.*;
import realm.jnfss.comm.ServerConnection;

public class Rsh {
  static void repl(ServerConnection sc, BufferedReader br) {
    for(;;) {
      try {
        System.out.print("jnfsh-> ");
        String cli=br.readLine();
        if(cli.equals("q")) {
          break;
        }
        String rsp=jnfsend(sc.getSock(), cli);
        System.out.println(rsp);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  static String jnfsend(Socket s, String rq) throws IOException {
    OutputStream os=s.getOutputStream();
    InputStream is=s.getInputStream();
    PrintWriter pw=new PrintWriter(os, true);
    BufferedReader nbr=new BufferedReader(new InputStreamReader(is));
    pw.println(rq);
    String rsp=nbr.readLine();
    return rsp;
  }
  
  public static void main(String... args) {
    try {
      InputStreamReader isr=new InputStreamReader(System.in);
      BufferedReader br=new BufferedReader(isr);
      ServerConnection sc=new ServerConnection();
      repl(sc,br);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

