package com.company;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.coref.CorefCoreAnnotations;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

/** This class demonstrates building and using a Stanford CoreNLP pipeline. */
public class StanfordCoreNlpClient {

  /**---old--- Usage: java -cp "*" StanfordCoreNlpDemo [inputFile [outputTextFile [outputJSONFile]]] */
  public void annotate(String inputText, String outNlpFile, String outNlp3File, String outNlp7File) throws IOException {
    PrintWriter jsonOut3 = null; //for writing output of ner 3 class model
    PrintWriter jsonOut7 = null; //for writing output of ner 7 class model
    PrintWriter jsonOut = null; //for writing output of corenlp.run ner

    jsonOut3 = new PrintWriter(outNlp3File);
    jsonOut7 = new PrintWriter(outNlp7File);
    jsonOut = new PrintWriter(outNlpFile);


    // Create a CoreNLP pipeline. To build the default pipeline, you can just use:
    //   StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    // Here's a more complex setup example:
    //   Properties props = new Properties();
    //   props.put("annotators", "tokenize, ssplit, pos, lemma, ner, depparse");
    //   props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
    //   props.put("ner.applyNumericClassifiers", "false");
    //   StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    Properties props = new Properties();


    // ------------- For ner 3-class ------------------
    System.out.println("Starting 3-class ner annotation");
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
    props.put("ner.model","edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
    props.put("ner.applyNumericClassifiers", "false");

    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    // Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.
    //annotation = new Annotation(inputText);
    Annotation annotation = new Annotation(inputText);
    // run all the selected Annotators on this text
    pipeline.annotate(annotation);

    // this prints out the results of sentence analysis to file(s) in good formats
    if (jsonOut3 != null) {
      pipeline.jsonPrint(annotation, jsonOut3);
    }

    // ------------- For corenlp.run ner ------------------
    System.out.println("Starting corenlp.run ner annotation");
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");//ner,

    pipeline = new StanfordCoreNLP(props);

    // Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.

    //annotation = new Annotation(IOUtils.slurpFileNoExceptions(inputFile);


    // run all the selected Annotators on this text
    pipeline.annotate(annotation);

    // this prints out the results of sentence analysis to file(s) in good formats
    if (jsonOut != null) {
      pipeline.jsonPrint(annotation, jsonOut);
    }

    // ------------- For ner 7-class ------------------
    System.out.println("Starting 7-class ner annotation");
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
    props.put("ner.model","edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz");
    props.put("ner.applyNumericClassifiers", "false");

    pipeline = new StanfordCoreNLP(props);

    // run all the selected Annotators on this text
    pipeline.annotate(annotation);

    // this prints out the results of sentence analysis to file(s) in good formats
    if (jsonOut7 != null) {
      pipeline.jsonPrint(annotation, jsonOut7);
    }


    // An Annotation is a Map with Class keys for the linguistic analysis types.
    // You can get and use the various analyses individually.
    // For instance, this gets the parse tree of the first sentence in the text.
    //List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

    IOUtils.closeIgnoringExceptions(jsonOut3);
    IOUtils.closeIgnoringExceptions(jsonOut7);
    IOUtils.closeIgnoringExceptions(jsonOut);
  }

}
