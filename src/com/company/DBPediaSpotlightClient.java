package com.company;

import java.io.File;
import java.util.Map;
import org.apache.commons.io.IOUtils;
 
public class DBPediaSpotlightClient {

 public void annotate (String inputText, String spotlightOutFile) {

try {
    System.out.println("Starting Spotlight client ... ");
    System.out.println("Input text: "+inputText);
    //String nlpOutFile = "outcorenlp.txt";
    //spotlightOutFile = "outspotlight.json";
    //String command1 = "start java -mx4g -cp \"*\" \"edu.stanford.nlp.pipeline.StanfordCoreNLPServer\" -port 9000> nul 2>&1";
    String command2 = "curl http://model.dbpedia-spotlight.org/en/annotate --data-urlencode \"text="+inputText+"\"  --data \"confidence=0.0\" --data \"types=Person,Organisation,Place,Location\" -H \"Accept: application/json\"";
    //String command3 = "curl --data \"The quick brown fox jumped over the lazy dog.\" \"http://localhost:9000/?properties={%22annotators%22%3A%22tokenize%2Cssplit%2Cpos%22%2C%22outputFormat%22%3A%22json%22}\" -o -";
    //GoodWindowsExec g1 = new GoodWindowsExec(command1, null, null);
    GoodWindowsExec g2 = new GoodWindowsExec(command2, spotlightOutFile, null);
    //GoodWindowsExec g3 = new GoodWindowsExec(command3, nlpOutFile, null);
 /*
    //------------For Shutting down corenlp server---------------------------------
    String tmpDir = System.getProperty("java.io.tmpdir");
    //File tmpFile = new File(tmpDir + File.separator + "corenlp.shutdown");
    //String path = tmpFile.getPath();
    //System.out.println("Path of corenlp.shutdown file: "+ path);
    //System.out.println("Working Directory: "+System.getProperty("user.dir"));
    //Process process4 = runtime4.exec("cmd.exe /c "+"type \"corenlp.shutdown\"", null, new File(tmpDir));//"C:\\Users\\Raabia\\AppData\\Local\\Temp"));
    String command4 = "type \"corenlp.shutdown\"";
    GoodWindowsExec g4 = new GoodWindowsExec(command4, null, new File(tmpDir));
    //String key = IOUtils.toString(process4.getInputStream());
    //System.out.println("Key: " + key);        
    //process4 = runtime4.exec("curl localhost:9000/shutdown?key="+key+" -o -");
  */
} catch (Throwable cause) {
    // process cause
    cause.printStackTrace();
}
  }

}
