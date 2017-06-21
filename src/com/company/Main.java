package com.company;

import edu.stanford.nlp.io.IOUtils;
import org.aksw.gerbil.io.nif.NIFParser;
import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args)  {

        System.out.println("Starting ... ");
        String inputFile = "oke17task1Input.ttl";//"task1.ttl";//"input.txt";
        String outputFile = "outNIF.txt";
        String outNlpFile = "outNLP.json"; //for corenlp ner output
        String outNlp3File = "outNLP3.json"; //for ner 3-class output
        String outNlp7File = "outNLP7.json"; //for ner 7-class output ---- parse is also in this file -------
        String outSpotlightFile = "outSpotlight.json"; //for spotlight output
        String inputText = "";// = IOUtils.slurpFileNoExceptions(inputFile);

        NIFParser parser = new TurtleNIFParser();
        Reader r = null;
        try {
            r = new FileReader(inputFile);
            List<Document> documents = new ArrayList<>(parser.parseNIF(r));
            String[] texts = new String[documents.size()];

            FileWriter write = new FileWriter(outputFile );
            PrintWriter printer = new PrintWriter( write );

        DBPediaSpotlightClient spotlight = new DBPediaSpotlightClient();
        StanfordCoreNlpClient nlp = new StanfordCoreNlpClient();
            for(int i=0; i<documents.size(); i++) {
                //if(i<52)continue;
                System.out.println("Document no, i.e. i = "+i);
                System.out.println("Sentence no = "+documents.get(i).getDocumentURI());
                texts[i] = documents.get(i).getText();
                inputText = texts[i];
                nlp.annotate(inputText, outNlpFile, outNlp3File, outNlp7File);
                spotlight.annotate(inputText, outSpotlightFile);

                Reasoner reasoner = new Reasoner(outNlpFile);
                List<MyEntity> E = new ArrayList<>(reasoner.recognizeEntities(inputText, outNlp3File, outNlp7File, outSpotlightFile));

            QueryReasoner qr = new QueryReasoner();
            Document d = qr.makeQuerries(E, inputText, documents.get(i).getDocumentURI());
            documents.set(i,d);
             }

            NIFWriter writer = new TurtleNIFWriter();
            String nifString = writer.writeNIF(documents);
            System.out.println(nifString);
            printer.print(nifString);
            System.out.println("Printer error: "+printer.checkError());
            printer.close();

        System.out.println("Shutting down ... ");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}


