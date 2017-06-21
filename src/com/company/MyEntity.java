package com.company;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maadi on 21/04/2017.
 */
public class MyEntity {
    String type = "O";
    int isEntity = -1;
    String text = "";
    Long sentenceIndex;

    public Long getTokenIndex() {
        return tokenIndex;
    }

    public void setTokenIndex(Long tokenIndex) {
        this.tokenIndex = tokenIndex;
    }

    Long tokenIndex;//index of first token of this entity


    //NER fields
    Long[] index = new Long[3];//this is word index //[0] is for corenlp's, [1] is for ner 3-class, [2] is for ner 7-class
    String[] pos = new String[3];//ArrayList<String>();// List<String> posArray3 = new ArrayList<String>();// List<String> posArray7 = new ArrayList<String>();
    Long[] beginOffset = new Long[3];//, beginOffset3, beginOffset7;//for ner
    Long[] endOffset = new Long[3];//, endOffset3, endOffset7;
    boolean[] identifiedByNer = new boolean[3];// boolean identifiedByNer3 = false;//    boolean identifiedByNer7 = false;
    String[] nerType = new String[3];//  String ner3Type = "O";//  String ner7Type = "O";
    String[] originalText = new String[3];//for corenlp.run ner text of entity//   String originalText3 = "";//for ner3 text of entity//  String originalText7 = "";//for ner7 text of entity

    //Spotlight fields
    Long spotOffset;//spotlight begin offset
    String uri = "none";
    boolean identifiedBySpot = false;
    String spotTypes = "O";
    String surfaceForm = ""; //for spotlight text of entity

    //Constructor
    public MyEntity(){
        sentenceIndex = Long.valueOf(-1);
        for(int i=0; i<3; i++){
            index[i] = Long.valueOf(-1);
            pos[i] = "";
            beginOffset[i] = Long.valueOf(-1);;
            endOffset[i] = Long.valueOf(-1);
            identifiedByNer[i] = false;
            nerType[i] = "";
            originalText[i] = "";
        }
        spotOffset = Long.valueOf(-1);
    }
    //Setters, Getters
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public int getIsEntity() {
        return isEntity;
    }
    public void setIsEntity(int isEntity) {this.isEntity = isEntity;}
    public String getText() {        return text;    }
    public void setText(String text) {        this.text = text;    }
    public Long getSentenceIndex( ) { return sentenceIndex; }
    public void setSentenceIndex(Long sentenceNo) {this.sentenceIndex = sentenceNo;}

    public Long getIndex(int nerIndex) { return index[nerIndex]; }
    public void setIndex(Long index, int nerIndex) {this.index[nerIndex] = index;}
    public String getPos(int nerIndex) {return pos[nerIndex];}
    public void setPos(String pos, int nerIndex) {this.pos[nerIndex] = pos;}
    public Long getBeginOffset(int nerIndex) {        return beginOffset[nerIndex];    }
    public void setBeginOffset(Long beginOffset, int nerIndex) {this.beginOffset[nerIndex] = beginOffset;}
    public Long getEndOffset(int nerIndex) {return endOffset[nerIndex];}
    public void setEndOffset(Long endOffset, int nerIndex) {this.endOffset[nerIndex] = endOffset;}
    public boolean getIdentifiedByNer(int nerIndex) {return identifiedByNer[nerIndex];}
    public void setIdentifiedByNer(boolean identifiedByNer, int nerIndex) {this.identifiedByNer[nerIndex] = identifiedByNer;}
    public String getNerType(int nerIndex) {return nerType[nerIndex];}
    public void setNerType(String nerType, int nerIndex) {
        this.nerType[nerIndex] = nerType;
    }
    public String getOriginalText(int nerIndex) {
        return originalText[nerIndex];
    }
    public void setOriginalText(String originalText, int nerIndex) {this.originalText[nerIndex] = originalText;}

    public Long getSpotOffset() {
        return spotOffset;
    }
    public void setSpotOffset(Long spotOffset) {this.spotOffset = spotOffset;}
    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    public boolean getIdentifiedBySpot() {
        return identifiedBySpot;
    }
    public void setIdentifiedBySpot(boolean identifiedBySpot) {
        this.identifiedBySpot = identifiedBySpot;
    }
    public String getSpotTypes() {
        return spotTypes;
    }
    public void setSpotTypes(String spotTypes) {
        this.spotTypes = spotTypes;
    }
    public String getSurfaceForm() {
        return surfaceForm;
    }
    public void setSurfaceForm(String surfaceForm) {
        this.surfaceForm = surfaceForm;
    }

    //-----------------update functions-----------------------

    //this function expands an entity's NER fields to include next word
    public void expandEntityNer(int nerIndex, String pos, Long endOffset, String originalText){
        this.pos[nerIndex] = this.pos[nerIndex]+","+pos;//appends the pos of entity passed at the end of this entity
        this.endOffset[nerIndex] = endOffset;
        this.originalText[nerIndex] = this.originalText[nerIndex]+" "+originalText;//appends the text of entity passed at the end of this entity
    }

    //this function sets an entity's NER attributes to the passed attributes
    public void setEntityNer(int nerIndex, Long sentenceIndex, Long tokenIndex, String pos, Long beginOffset, Long endOffset, boolean identifiedByNer, String nerType, String originalText){//not updating identifiedByNER here, updating all other NER fields of entity
        this.sentenceIndex = sentenceIndex;
        this.index[nerIndex] = tokenIndex;
        this.pos[nerIndex] = pos;
        this.beginOffset[nerIndex] = beginOffset;
        this.endOffset[nerIndex] =endOffset;
        this.identifiedByNer[nerIndex] = identifiedByNer;
        this.nerType[nerIndex] = nerType;
        this.originalText[nerIndex] = originalText;
    }

    //this function sets an entity's Spotlight attributes to the passed values
    public void setEntitySpot(Long spotOffset, String uri, boolean identifiedBySpot, String spotTypes, String surfaceForm){
        this.spotOffset = spotOffset;
        this.uri = uri;
        this.identifiedBySpot = identifiedBySpot;
        this.spotTypes = spotTypes;
        this.surfaceForm = surfaceForm;
    }
}
