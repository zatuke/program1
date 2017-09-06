/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;



public class WebWorker implements Runnable
{

int cate = 0;
private Socket socket;

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{  
   String temp;
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      
            
      temp = readHTTPRequest(is);
     
    
      writeHTTPHeader(os,"text/html");
      writeContentFile(os,temp);
    
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/
private String readHTTPRequest(InputStream is)
{
   String line;
   String [] splitstuff;
   String realline;
   String spit = " ";
   
  
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
     
   while (true) {
      try {
         while (!r.ready()) Thread.sleep(1);
         line = r.readLine();
            splitstuff = line.split(" "); //split first line
            realline = splitstuff[1]; //grab file path
           File a = new File(realline.substring(1));
                                          
            BufferedReader mk = new BufferedReader(new FileReader(a)); //add path to new reader
            
            while(mk.ready()){
              String b = mk.readLine();
            if(b.contains("<cs371date>")){
                Date d = new Date();
               DateFormat df = DateFormat.getDateTimeInstance();
               df.setTimeZone(TimeZone.getTimeZone("GMT"));
               line = df.format(d); 
               spit = spit + line + "\n";
               }
           else if(b.contains("<cs371server>")){
            line = "you have reached Server: WhooHoo\n";
            spit = spit + line + "\n";
            }
            else
               spit = spit + b + "\n";

            }
            
            
         
         System.err.println("Request line: ("+line+")");
         if (line.length()==0) break;
      } catch (Exception e) {
         cate = 404;     
         System.err.println("Request error: "+e);
         return "404";

      }
      return spit;
   }
   
      
   return spit;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
{
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   if(cate == 404){
   String error = "HTTP/1.1 " + cate + "ERROR\n";
   os.write(error.getBytes());
   }
   else {
   os.write("HTTP/1.1 200 OK\n".getBytes());
   }
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Jon's very own server\n".getBytes());
   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes()); 
   os.write("Connection: close\n".getBytes());
   os.write("Content-Type: ".getBytes());
   os.write(contentType.getBytes());
   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   return;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContentError(OutputStream os, String e ) throws Exception
{
   
   os.write(e.getBytes());
   os.write(" error: file not found\n".getBytes());
}

private void writeContentFile(OutputStream os , String s) throws Exception
{  if(s.equals("404")){
      os.write(s.getBytes());
   os.write(" error: file not found\n".getBytes());
                     }
                     
   else{
  // os.write("this is the content of the file\n".getBytes());
   os.write(s.getBytes());
   //os.write("yay\n".getBytes());
   }
}

} // end class
