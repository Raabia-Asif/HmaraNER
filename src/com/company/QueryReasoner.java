package com.company;

/**
 * Created by Raabia on 5/22/2017.
 */

//this class only does entity linking

import java.io.*;
import java.util.*;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import org.aksw.gerbil.io.nif.DocumentListWriter;
import org.aksw.gerbil.io.nif.NIFParser;
import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.Annotation;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.TypedNamedEntity;
//import org.junit.Ignore;

import java.io.IOException;


public class QueryReasoner {
    public Document makeQuerries(List<MyEntity> E,String text, String docUri) throws IOException {
        DBPediaQueryClient dc = new DBPediaQueryClient();
        String uri = "", type="", label="";
        int startPos = 0, length = 0;
        List<String> uris = new ArrayList<String>();
        boolean rsSet=false;
        //String text = "Japan (Japanese: 日本 Nippon or Nihon) is a stratovolcanic archipelago of 6,852 islands.";
        Document document = new DocumentImpl(text, docUri);//http://example.org/document0");
        String t1="http://aksw.org/notInWiki/", t2="";


        for(int i=0; i<E.size(); i++){
            rsSet=false;
            MyEntity e = E.get(i);
            uri="";
            uris.clear();

            //query e on dbpedia
            if(e.getIsEntity()==1) {

                for(int j=2; j>-1 && rsSet==false; j--) {
                    type = e.getNerType(j);
                    if (!type.equals("O") && e.getIdentifiedByNer(j)) {
                        rsSet = true;
                        startPos = Math.toIntExact(e.getBeginOffset(j));
                        length =  Math.toIntExact(e.getEndOffset(j)) - startPos;
                        label = e.getOriginalText(j);
                        if (type.equals("PERSON")) {//for person entities
                            uris.addAll(0,dc.qExample("qPerson.sparql", label));
                        } else if (type.equals("LOCATION") || type.equals("STATE") || type.equals("PROVINCE") || type.equals("CITY") || type.equals("COUNTRY")) {//for place entities
                            uris.addAll(0,dc.qExample("qPlace.sparql", label));
                        } else if (type.equals("ORGANIZATION")) {//for organization entities
                            uris.addAll(0,dc.qExample("qOrganization.sparql", label));
                        }
                        else {
                            uris.addAll(0,dc.qExample("query.sparql", label));
                        }
                        //for writing e as NIF
                        // Add the marking for the entity
                        if(uris.size()>0) {
                            uri = uris.get(0);
                            System.out.println(label+" resource found on DBpedia! "+uri);
                        }else if(e.getIdentifiedBySpot()){
                            uri = e.getUri();
                            System.out.println(label+" resource NOT found on DBpedia, got uri from Spotlight instead!"+uri);
                        }else {//not found
                            //create new uri
                            t2 = label.replaceAll(" ","_");
                            uri = t1 + t2;
                            System.out.println(label+" resource NOT found on DBpedia, generating new uri.."+uri);
                        }
                        document.addMarking(new NamedEntity(startPos, length, uri));
                        //documents.add(document);
                    }
                }
                if(rsSet==false && e.getIdentifiedBySpot()){
                    //type = e.getSpotTypes().split(",")[0];
                    startPos = Math.toIntExact(e.getSpotOffset());
                    length = e.getSurfaceForm().length();
                    label = e.getSurfaceForm();
                    uris.addAll(0,dc.qExample("query.sparql", label));
                    //for writing e as NIF
                    // Add the marking for the entity
                    if(uris.size()>0) {
                        uri = uris.get(0);
                        System.out.println(label+" resource found on DBpedia! "+uri);
                    }else if(e.getIdentifiedBySpot()){
                        uri = e.getUri();
                        System.out.println(label+" resource NOT found on DBpedia, got uri from Spotlight instead!"+uri);
                    }else {//not found
                        //create new uri
                        t2 = label.replaceAll(" ","_");
                        uri = t1 + t2;
                        System.out.println(label+" resource NOT found on DBpedia, generating new uri.."+uri);
                    }
                    document.addMarking(new NamedEntity(startPos, length, uri));
                }

            }
        }
        return document;
    }
}
