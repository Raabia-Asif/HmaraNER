package com.company;

import java.util.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

class StreamGobbler extends Thread
{
    InputStream is;
    String type;
    OutputStream os;
    
    StreamGobbler(InputStream is, String type)
    {
        this(is, type, null);
    }
    StreamGobbler(InputStream is, String type, String fileName)
    {
        this.is = is;
        this.type = type;
        FileOutputStream fos = null;
            if (fileName != null)
                try {
                    fos = new FileOutputStream(fileName);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StreamGobbler.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.os = fos;
    }
    
    public void run()
    {
        try
        {
            PrintWriter pw = null;
            if (os != null)
                pw = new PrintWriter(os);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null){
                if (pw != null)
                    pw.println(line);
                System.out.println(type + ">" + line);    
            }
            if (pw != null)
                pw.flush();
            if (os != null){
                os.flush();
                os.close(); 
            }
            } catch (IOException ioe)
              {
                ioe.printStackTrace();  
              }
    }
}
public class GoodWindowsExec
{
    String command;
    String fileName;
    
    GoodWindowsExec(String command, String fileName, File dir){
        this.command = command;
        this.fileName = fileName;
        
        try
        {      
            //String osName = System.getProperty("os.name" );
            String[] cmd = new String[3];
            //System.out.println(osName);
            cmd[0] = "cmd.exe" ;
            cmd[1] = "/C" ;
            cmd[2] = command;//"java -mx4g -cp \"*\" \"edu.stanford.nlp.pipeline.StanfordCoreNLPServer\" -port 9000";
            
            //Runtime rt = Runtime.getRuntime();
            System.out.println("Executing " + cmd[0] + " " + cmd[1] 
                               + " " + cmd[2]);
            Process proc; //Process p = new ProcessBuilder("myCommand", "myArg").start();
            ProcessBuilder pb = new ProcessBuilder(cmd);
            if (dir == null) //its normal command
            {
                proc = pb.start();//proc = rt.exec(cmd);
            }
            else //its shutdown command
            {
                //proc = rt.exec(cmd, null, dir);
                pb.directory(dir);
                proc = pb.start();
                InputStream is = proc.getInputStream();
                String key = IOUtils.toString(is);
                System.out.println("Key: " + key);
                StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");            
                StreamGobbler outputGobbler = new StreamGobbler(is, "OUTPUT", fileName);
                
                errorGobbler.start();
                outputGobbler.start();

                boolean exitVal = proc.waitFor(5, TimeUnit.SECONDS);
                System.out.println("ExitValue: " + exitVal);
                  
                cmd[2] = "curl localhost:9000/shutdown?key="+key+" -o -";
                proc = new ProcessBuilder(cmd).start();//proc = rt.exec(cmd);
            }
            // any error message?
            StreamGobbler errorGobbler = new 
                StreamGobbler(proc.getErrorStream(), "ERROR");    
            
            // any output?
            StreamGobbler outputGobbler = new 
                StreamGobbler(proc.getInputStream(), "OUTPUT", fileName);
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();
             
            // any error???
            boolean exitVal = proc.waitFor(5, TimeUnit.SECONDS);
            
            System.out.println("ExitValue: " + exitVal); 
            
            //added by Raabia
            //proc.getErrorStream().close();
            //proc.getInputStream().close();
            //System.out.println("Process streams closed..");
            
        } catch (Throwable t)
          {
            t.printStackTrace();
          }
    }
}