package com.company;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raabia on 5/7/2017.
 */
public class Reasoner {
    private static final int NERINDEX = 0;//regex ner
    private static final int NER3INDEX = 1;
    private static final int NER7INDEX = 2;

    private JSONArray tokensJArray = new JSONArray();

    public Reasoner(String outNlpFile) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Object obj_read = parser.parse(new FileReader(outNlpFile));
        JSONObject jsonObject = (JSONObject) obj_read;
        JSONArray sentencesArray = (JSONArray) jsonObject.get("sentences");
        for (int i=0; i<sentencesArray.size(); i++){
            Object so = sentencesArray.get(i);//get one sentence
            JSONObject sJO = (JSONObject) so;
            Object sTokens = sJO.get("tokens");//get tokens of a sentence
            tokensJArray.add(sTokens);
            //tokensJArray.addAll((Collection) sTokens);
        }
    }

    public List<MyEntity> recognizeEntities(String inputText, String outNlp3File, String outNlp7File, String outSpotlightFile){
        System.out.println("Starting reasoner ... ");
        List<MyEntity> E = new ArrayList<MyEntity>();
        List<MyEntity> S = new ArrayList<MyEntity>();
        try {
            S = new ArrayList<MyEntity>(identifiedBySPOTLIGHT(outSpotlightFile));
            E.addAll(0, identifiedByNERs(S, outNlp3File, outNlp7File));//E = identifiedByNERs(S, outNlp3File, outNlp7File);
            E = new ArrayList<MyEntity>(applyRules(E,outNlp7File));//E = applyRules(E,outNlp7File);
            System.out.println("Exiting Reasoner ....");
            return E;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return E;
    }

    private List<MyEntity> applyRules(List<MyEntity> E, String outNlp7File) throws IOException, ParseException {//3 class labels:	Location, Person, Organization,...... 4 class labels:	Location, Person, Organization, Misc
        // 7 class labels:	Location, Person, Organization, Money, Percent, Date, Time
        System.out.println("Inside class: Reasoner, function: applyRules");
        DBPediaQueryClient dc = new DBPediaQueryClient();
        List<String> uris = new ArrayList<String>();
        for (int i = 0; i < E.size(); i++)//this loop runs for all the entities
        {
            uris.clear();
            JSONArray sentence = (JSONArray) tokensJArray.get(Math.toIntExact(E.get(i).getSentenceIndex()));
            //can give null pointer exception?
            String ner = E.get(i).getNerType(NERINDEX);//regex ner type
            String ner3 = E.get(i).getNerType(NER3INDEX);
            String ner7 = E.get(i).getNerType(NER7INDEX);
            String originalText="";
            String pos="", type="";
            int tokenIndex = -1;
            int notPPO = 0;//if other than person/place/organization, then set it to 1. notPPO=-1 if partially other than PPO
            int hasNoun = 0;
            boolean hasNextToken=false, hasPrevToken=false;
            JSONObject nextToken = null;
            if(E.get(i).getIdentifiedByNer(NERINDEX)){
                type = ner;
                originalText = E.get(i).getOriginalText(NERINDEX);
                pos = E.get(i).getPos(NERINDEX);
                tokenIndex = Math.toIntExact(E.get(i).getIndex(NERINDEX));
            }else if(E.get(i).getIdentifiedByNer(NER3INDEX)){
                type = ner3;
                originalText = E.get(i).getOriginalText(NER3INDEX);
                pos = E.get(i).getPos(NER3INDEX);
                tokenIndex = Math.toIntExact(E.get(i).getIndex(NER3INDEX));
            }else if (E.get(i).getIdentifiedByNer(NER7INDEX)){
                type = ner7;
                originalText = E.get(i).getOriginalText(NER7INDEX);
                pos = E.get(i).getPos(NER7INDEX);
                tokenIndex = Math.toIntExact(E.get(i).getIndex(NER7INDEX));
            }else if(E.get(i).getIdentifiedBySpot()){
                String[] tempTypes = E.get(i).getSpotTypes().split(",");
                if(tempTypes.length>1) {
                    type = tempTypes[1];
                    tempTypes = type.split(":");
                    type = tempTypes[1];
                }
                originalText = E.get(i).getSurfaceForm();
                tokenIndex = Math.toIntExact(E.get(i).getTokenIndex());
                String[] words = originalText.split(" ");
                int tempIndex = tokenIndex;
                for (int temp=0; temp<words.length; temp++) {
                    pos += (String) ((JSONObject) sentence.get(tempIndex)).get("pos");
                    tempIndex++;
                }
            }
            E.get(i).setText(originalText);
            if (tokenIndex!=-1 && tokenIndex < sentence.size()-1) {
                nextToken = (JSONObject) sentence.get(tokenIndex);//next token
                hasNextToken = true;
            }
            String[] words = originalText.split(" ");
            String[] posArray = pos.split(",");
            boolean allSmall = false;
            if(!originalText.isEmpty())
                allSmall = preProcess1(originalText);
            if(allSmall==true){//do not add to list S if all small letters
                E.remove(i);
                continue;
            }

            //START --- RULE# 2  ---
            if (words.length < 2) // single word
            {
                if (pos.startsWith("VB") || pos.startsWith("DT") || pos.startsWith("PRP") || pos.startsWith("IN") || pos.startsWith("CC") || pos.startsWith("JJ"))// word is verb / determinent / pronoun / conjunction / adjective
                {
                    E.remove(i);
                    continue;
                }
            } else {
                if (pos.startsWith("IN") || pos.startsWith("CC"))//multi-word entity starts with a conjunction
                {
                    E.remove(i);
                    continue;
                }
            }
            //END --- RULE# 2  ---


            //START --- RULE# 1  --- Check if an entity has type other than person/place/organization
            if ((!ner.equals("PERSON") && !ner.equals("LOCATION") && !ner.equals("ORGANIZATION") && !ner.equals("STATE") && !ner.equals("PROVINCE") && !ner.equals("CITY") && !ner.equals("COUNTRY") && !ner.equals("TITLE") && !ner.equals("NATIONALITY"))
                    || (ner7.equalsIgnoreCase("MONEY") || ner7.equalsIgnoreCase("PERCENT") || ner7.equalsIgnoreCase("DATE") || ner7.equalsIgnoreCase("TIME"))) {
                notPPO = 1;
                E.get(i).setIsEntity(0);
                System.out.println("Regex ner Type is other than p/p/o: " + ner);

                //START --- RULE# 9  --- if partially not PPO
                if(E.get(i).getIdentifiedBySpot()==true){//if identified by spotlight
                    if(E.get(i).getOriginalText(NERINDEX).length() < E.get(i).getOriginalText(NER3INDEX).length()){ //this is probably not a good way to check
                        notPPO = -1;
                    }
                    for(int k=0; k<posArray.length;k++){
                        if(posArray[k].startsWith("NN")){//if contains a noun
                            hasNoun = 1;
                            break;
                        }
                    }
                    if(hasNoun>0){//if contains a noun
                        if(notPPO==-1){//and is partially not PPO
                            E.get(i).setIsEntity(1);
                        }
                    }
                }
                //END --- RULE# 9  --- if partially not PPO

                else {
                    E.get(i).setIsEntity(0);
                }
                continue;
            }
            //END --- RULE# 1  --- Check if an entity has type other than person/place/organization


            //START --- RULE# 3  --- if entity identified by both, isEntity=true
            if (E.get(i).getIdentifiedBySpot() &&
                    (E.get(i).getIdentifiedByNer(NERINDEX) || E.get(i).getIdentifiedByNer(NER3INDEX) || E.get(i).getIdentifiedByNer(NER7INDEX))) {
                E.get(i).setIsEntity(1);
            }
            //END --- RULE# 3  ---

            tokenIndex = tokenIndex + words.length - 1;

            //START --- RULE# 4  --- if entity followed by acronym in parenthesis, then acronym is also entity
            if(tokenIndex+1<sentence.size()) {
                JSONObject token3 = (JSONObject) sentence.get(tokenIndex + 1);//next next token
                if (E.get(i).getIsEntity() == 1) {
                    if(hasNextToken) {
                        if (nextToken.get("originalText").equals("(")) {//if token next to entity is (
                            String t3Text = (String) token3.get("originalText");
                            boolean isAcronym = true;
                            int k = 0, l = 0;
                            for (; k < t3Text.length() && l < words.length; ) {
                                if (Character.isUpperCase(t3Text.charAt(k))) {
                                    if (t3Text.charAt(k) != words[l].charAt(0)) {
                                        isAcronym = false;
                                        break;
                                    } else {
                                        k++;
                                    }
                                    l++;
                                } else {
                                    l++;
                                    continue;
                                }
                            }
                            if (k == t3Text.length() && isAcronym) {//if after ( is an acronym
                                MyEntity m = new MyEntity();
                                m.setEntityNer(NERINDEX, E.get(i).getSentenceIndex(), (Long) token3.get("index"), (String) token3.get("pos"), (Long) token3.get("characterOffsetBegin"), (Long) token3.get("characterOffsetEnd"), false, (String) token3.get("ner"), (String) token3.get("originalText"));
                                //m.setSentenceIndex(E.get(i).getSentenceIndex());
                                m.setIsEntity(1);
                                m.setTokenIndex((long) tokenIndex);
                                E.add(i + 1, m);
                            }
                        }
                    }
                }
            }
            //END --- RULE# 4  ---

            //START --- RULE# 5  --- if ner type is title
            if(ner.equals("TITLE")){
                int k1=1,k2=3;
                JSONObject prevToken = null;
                String temp1="", temp2="";
                if(tokenIndex-2 >=0) {
                    prevToken = (JSONObject) sentence.get(tokenIndex - 2);//previous token
                    temp2 = (String) prevToken.get("pos");//pos of previous token
                    hasPrevToken = true;
                }
                if(tokenIndex<sentence.size()-1 && hasNextToken) {
                    temp1 = (String) nextToken.get("pos");//pos of next token
                }
                JSONObject tokenTemp = nextToken;
                if(temp1.equals(",")){//if next token is comma, ignore it and update next token
                    if(tokenIndex+k1<sentence.size()) {
                        tokenTemp = (JSONObject) sentence.get(tokenIndex + k1);//token next to ,
                        temp1 = (String) tokenTemp.get("pos");
                        k1++;
                    }
                }
                if(tokenIndex+k1<sentence.size()&& hasNextToken && notPPO==0 && temp1.startsWith("NNP") && Character.isUpperCase(temp1.charAt(0)))//if next token is proper noun and starts with a capital letter
                {
                    MyEntity m = new MyEntity();
                    m.setEntityNer(NERINDEX,E.get(i).getSentenceIndex(),(Long) tokenTemp.get("index"),temp1,(Long)tokenTemp.get("characterOffsetBegin"),(Long)tokenTemp.get("characterOffsetEnd"),false,(String) tokenTemp.get("ner"),(String) tokenTemp.get("originalText"));
                    m.setIdentifiedBySpot(false);
                    //m.setSentenceIndex(E.get(i).getSentenceIndex());
                    m.setIsEntity(1);
                    m.setText((String) tokenTemp.get("originalText"));
                    do {
                        tokenTemp = (JSONObject) sentence.get(tokenIndex + k1);//token next
                        temp1 = (String) tokenTemp.get("pos");
                        m.expandEntityNer(NERINDEX,temp1,(Long)tokenTemp.get("characterOffsetEnd"),(String)tokenTemp.get("originalText"));
                        k1++;
                    }while(temp1.startsWith("NNP") && Character.isUpperCase(temp1.charAt(0)));
                    m.setTokenIndex((long) tokenIndex);
                    E.add(i+1,m);
                }
                else if(hasPrevToken){
                    tokenIndex = tokenIndex - words.length +1;
                    if(tokenIndex-k2 >=0 && temp2.equals(",")){//if previous token is comma, ignore it and update previous token
                        if(tokenIndex-k2 >=0) {
                            tokenTemp = (JSONObject) sentence.get(tokenIndex - k2);// token before ,
                            temp2 = (String) tokenTemp.get("pos");
                            k2++;
                        }
                    }
                    if(tokenIndex-k2 >=0 && hasPrevToken && notPPO==0 && temp2.startsWith("NNP") && Character.isUpperCase(temp2.charAt(0)))//if previous token is proper noun and starts with a capital letter
                    {
                        MyEntity m = new MyEntity();
                        m.setEntityNer(NERINDEX,E.get(i).getSentenceIndex(), (Long) tokenTemp.get("index"),temp2,(Long)tokenTemp.get("characterOffsetBegin"),(Long)tokenTemp.get("characterOffsetEnd"),false,(String) tokenTemp.get("ner"),(String) tokenTemp.get("originalText"));
                        m.setIdentifiedBySpot(false);
                        //m.setSentenceIndex(E.get(i).getSentenceIndex());
                        m.setIsEntity(1);
                        m.setText((String) tokenTemp.get("originalText"));
                        do {
                            tokenTemp = (JSONObject) sentence.get(tokenIndex - k2);//token prev
                            temp2 = (String) tokenTemp.get("pos");
                            m.expandEntityNer(NERINDEX,temp2,(Long)tokenTemp.get("characterOffsetEnd"),(String)tokenTemp.get("originalText"));
                            m.setBeginOffset((Long)tokenTemp.get("characterOffsetBegin"), NERINDEX);
                            k2++;
                        }while(tokenIndex-k2 >=0 && temp2.startsWith("NNP") && Character.isUpperCase(temp2.charAt(0)));
                        m.setTokenIndex((long) tokenIndex);
                        E.add(i+1,m);
                    }
                }
                E.remove(i);// remove the title entity
            }
            //END --- RULE# 5  ---

            //START --- RULE# 6  --- if not yet decided and identified by NER, decide it to be entity
            if(E.get(i).getIsEntity()==-1 &&
                    (E.get(i).getIdentifiedByNer(NERINDEX) || E.get(i).getIdentifiedByNer(NER3INDEX) || E.get(i).getIdentifiedByNer(NER7INDEX))) {
                E.get(i).setIsEntity(1);
            }
            //END --- RULE# 6  ---

            //START --- RULE# 8  ---
            if(E.get(i).getIsEntity()==-1){//if isEntity not set yet
                if(E.get(i).getIdentifiedBySpot()==true){//if identified by spotlight
                    //if dbpedia resource found
                    uris.addAll(0,dc.qExample("query.sparql", E.get(i).getSurfaceForm()));
                    if (uris.size()>0) {
                        E.get(i).setIsEntity(1);
                    }
                    else E.get(i).setIsEntity(0);
                }
            }
            //END --- RULE# 8  ---

//START --- RULE# 10 and 11  --- merge same level consecutive place entities, Nationality Entities
            boolean isPlace = ner7.equals("LOCATION") || ner3.equals("LOCATION") || ner.equals("LOCATION") || ner.equals("STATE") || ner.equals("PROVINCE") || ner.equals("CITY") || ner.equals("COUNTRY");
            boolean isNationality = ner.equals("NATIONALITY");
            if(isPlace || isNationality) {
                JSONParser parser = new JSONParser();
                Object obj_read = parser.parse(new FileReader(outNlp7File));
                JSONObject jsonObject = (JSONObject) obj_read;
                JSONArray sentencesArray = (JSONArray) jsonObject.get("sentences");
                JSONObject thisSentence = (JSONObject) sentencesArray.get(Math.toIntExact(E.get(i).getSentenceIndex()));
                String parseTree = (String) thisSentence.get("parse");
                //System.out.println(parseTree);
                String reg = "\\)\\s+";
                String[] parseArray = parseTree.split(reg);

                //START --- RULE# 10  --- merge same level consecutive place entities
                if (isPlace) {
                    if (i < E.size() - 1) {//if its not the last entity
                        boolean isConsecutivePlace = false, isSameLevel = false;
                        MyEntity nextEntity = E.get(i + 1);
                        String nextNer = nextEntity.getNerType(NERINDEX);
                        boolean isNextPlace = nextEntity.getNerType(NER7INDEX).equals("LOCATION") || nextEntity.getNerType(NER3INDEX).equals("LOCATION") || nextNer.equals("LOCATION") || nextNer.equals("STATE") || nextNer.equals("PROVINCE") || nextNer.equals("CITY") || nextNer.equals("COUNTRY");
                        if (isNextPlace) {
                            if (nextEntity.getIndex(NERINDEX) == tokenIndex + 2) {//if next entity is the next to next word
                                if (nextToken.get("originalText").equals(",")) {
                                    isConsecutivePlace = true;
                                    //check if both places are at same level of parse tree
                                    isSameLevel = !parseArray[tokenIndex].contains("\n");//if in between the two entities is a newline character, i.e. next entity is on next line or next level
                                    if (isSameLevel) {
                                        int tempIndex = 0;
                                        if (nextEntity.getIdentifiedByNer(NERINDEX))
                                            tempIndex = NERINDEX;
                                        else if (nextEntity.getIdentifiedByNer(NER7INDEX))
                                            tempIndex = NER7INDEX;
                                        else if (nextEntity.getIdentifiedByNer(NER3INDEX))
                                            tempIndex = NER3INDEX;
                                        E.get(i).expandEntityNer(tempIndex, nextEntity.getPos(tempIndex), nextEntity.getEndOffset(tempIndex), ", " + nextEntity.getOriginalText(tempIndex));
                                        int x = E.get(i).getOriginalText(tempIndex).indexOf(",");
                                        String t = E.get(i).getOriginalText(tempIndex).substring(0, x);
                                        E.get(i).setText(t);
                                        E.remove(i + 1);//removing next entity from list E
                                        System.out.println("");
                                    }
                                }
                            }
                        }
                    }
                }
                //END --- RULE# 10  ---

                //START --- RULE# 11  --- for nationality entities
                if(isNationality && E.get(i).getIsEntity()!=1){
                    int k;
                    for (k=2; k<tokenIndex; k++) {//loop backwards in parseArray
                        if(parseArray[tokenIndex - k].contains("\r\n"))
                            break;
                    }
                    if(parseArray[tokenIndex - k + 1].startsWith("(NP")){
                        String textToAppend = "";
                        String posToAppend = "";
                        Long endOffset = Long.valueOf(0);
                        int l=0;
                        int start = 0;
                        if (parseArray[tokenIndex].startsWith("(NN")) {
                            start = 1;
                        }else if (parseArray[tokenIndex].startsWith("(JJ")) {
                            start = 2;
                        }
                        boolean phraseEnded = parseArray[tokenIndex].contains(")");
                        int phraseEndIndex = 0;//set this when phrase ends
                        int nextEntityIndex = Math.toIntExact(E.get(i + 1).getIndex(NERINDEX));
                            JSONObject tempToken;
                        boolean hasNounAfterIt=false;
                            if(start==1) {
                                while (!phraseEnded && parseArray[tokenIndex + l].startsWith("(NN") && ((tokenIndex+l)<nextEntityIndex) ){
                                    hasNounAfterIt = true;
                                    tempToken = (JSONObject) tokensJArray.get(tokenIndex+l+1);
                                    textToAppend += tempToken.get("originalText");
                                    posToAppend += tempToken.get("pos");
                                    endOffset = (Long) tempToken.get("characterOffsetEnd");
                                    l++;
                                    phraseEnded = parseArray[tokenIndex + l].contains("\r\n");
                                }
                            }else if(start==2) {
                                while (!phraseEnded && parseArray[tokenIndex + l].startsWith("(JJ") && ((tokenIndex+l)<nextEntityIndex)) {
                                    tempToken = (JSONObject) tokensJArray.get(tokenIndex+l+1);
                                    textToAppend += tempToken.get("originalText");
                                    posToAppend += tempToken.get("pos");
                                    endOffset = (Long) tempToken.get("characterOffsetEnd");
                                    l++;
                                    phraseEnded = parseArray[tokenIndex + l].contains("\r\n");
                                }
                                if(!phraseEnded && parseArray[tokenIndex + l].startsWith("(NN") && ((tokenIndex+l)<nextEntityIndex)){//if there is a noun after adjectives
                                    hasNounAfterIt = true;//need to append only when last is noun, else not
                                    tempToken = (JSONObject) tokensJArray.get(tokenIndex+l+1);
                                    textToAppend += tempToken.get("originalText");
                                    posToAppend += tempToken.get("pos");
                                    endOffset = (Long) tempToken.get("characterOffsetEnd");
                                }else {//if no noun after adjectives
                                    textToAppend="";
                                    posToAppend ="";
                                    hasNounAfterIt = false;
                                }
                            }
                            if(hasNounAfterIt) {
                                E.get(i).expandEntityNer(NERINDEX, posToAppend, endOffset, textToAppend);
                            }
                    }
                    //if dbpedia resource found
                    uris.addAll(0,dc.qExample("query.sparql", E.get(i).getSurfaceForm()));
                    if (uris.size()>0) {
                        E.get(i).setIsEntity(1);
                    }
                    else E.get(i).setIsEntity(0);
                }
                //END --- RULE# 11  ---
            }
            //END --- RULE# 10 and 11  --- merge same level consecutive place entities, Nationality Entities

        }
        System.out.println("Exiting class: Reasoner, function: applyRules");
        return E;
    }

    private List<MyEntity> identifiedBySPOTLIGHT(String outSpotlightFile) throws IOException, ParseException {
        System.out.println("Inside class: Reasoner, function: identifiedBySPOTLIGHT");
        List<MyEntity> S = new ArrayList<MyEntity>();
        JSONParser parser = new JSONParser();
        try {
            Object obj_read = parser.parse(new FileReader(outSpotlightFile));
            JSONObject jsonObject = (JSONObject) obj_read;
            // get array resources
            if(jsonObject.containsKey("Resources")) {
                Object resourcesO = jsonObject.get("Resources");
                JSONArray resourcesJA = (JSONArray) resourcesO;
                for (int i = 0; i < resourcesJA.size(); i++) {
                    Object tObject = resourcesJA.get(i);
                    JSONObject tJO = (JSONObject) tObject;
                    boolean allSmall = preProcess1((String) tJO.get("@surfaceForm"));
                    if (allSmall == true) {//do not add to list S if all small letters
                        continue;
                    }
                    MyEntity m = new MyEntity();
                    //set all Spotlight properties of entity
                    m.setEntitySpot(Long.parseLong((String) tJO.get("@offset")), (String) tJO.get("@URI"), true, (String) tJO.get("@types"), (String) tJO.get("@surfaceForm"));
                    m.setTokenIndex((long) i);
                    S.add(m);
                }
            }
            System.out.println("Exiting class: Reasoner, function: identifiedBySPOTLIGHT");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return S;
    }

    private boolean preProcess1(String originalText){//START --- Preprocessing# 1  --- all small case words
        System.out.println("Inside class: Reasoner, function: preProcess1");
        boolean allSmallCase = true;
        String[] words = originalText.split(" ");
        for(int wi = 0; wi<words.length; wi++) {
            if (Character.isUpperCase(words[wi].charAt(0))) {
                allSmallCase = false;
                break;
            }
        }
        System.out.println("Exiting class: Reasoner, function: preProcess1");
        return allSmallCase;
    }//END --- Preprocessing# 1  --- all small case words

    private List<MyEntity> identifiedByNERs (List<MyEntity> S, String outNlp3File, String outNlp7File) throws IOException, ParseException{
        System.out.println("Inside class: Reasoner, function: identifiedByNERs");
        int indexS = 0;
        JSONParser parser3 = new JSONParser();
        JSONParser parser7 = new JSONParser();
        List<MyEntity> E = new ArrayList<MyEntity>();

        Object obj_read3 = parser3.parse(new FileReader(outNlp3File));
        JSONObject jsonObject3 = (JSONObject) obj_read3;
        Object obj_read7 = parser7.parse(new FileReader(outNlp7File));
        JSONObject jsonObject7 = (JSONObject) obj_read7;

        // get array of sentence's tokens for 3-class ner
        JSONArray sentencesArray3 = (JSONArray) jsonObject3.get("sentences");
        // get array of sentence's tokens for 7-class ner
        JSONArray sentencesArray7 = (JSONArray) jsonObject7.get("sentences");
        for (int sentenceNo=0; sentenceNo<sentencesArray3.size();sentenceNo++) {
            Object sTokens = tokensJArray.get(sentenceNo);
            JSONArray sTokensJA = (JSONArray) sTokens;

            Object so3 = sentencesArray3.get(sentenceNo);
            JSONObject sJO3 = (JSONObject) so3;
            Object sTokens3 = sJO3.get("tokens");
            JSONArray sTokensJA3 = (JSONArray) sTokens3;

            Object so7 = sentencesArray7.get(sentenceNo);
            JSONObject sJO7 = (JSONObject) so7;
            Object sTokens7 = sJO7.get("tokens");
            JSONArray sTokensJA7 = (JSONArray) sTokens7;
            String prevNer = "", prevNer3 = "", prevNer7 = "";

            for (int i = 0; i < sTokensJA3.size(); i++)//this loop runs for all the tokens and creates list N and also E = N U S
            {
                Object tObject = sTokensJA.get(i);
                JSONObject tJO = (JSONObject) tObject;
                Object tObject3 = sTokensJA3.get(i);
                JSONObject tJO3 = (JSONObject) tObject3;
                Object tObject7 = sTokensJA7.get(i);
                JSONObject tJO7 = (JSONObject) tObject7;
                //Getting entity values
                Long index = (Long) tJO.get("index");
                String pos = (String) tJO.get("pos");
                Long characterOffsetBegin = (Long) tJO.get("characterOffsetBegin");
                Long characterOffsetEnd = (Long) tJO.get("characterOffsetEnd");
                String ner="O";
                if(tJO.containsKey("ner")) {
                    ner = (String) tJO.get("ner");//ner type
                }
                String ner3 = (String) tJO3.get("ner");
                String ner7 = (String) tJO7.get("ner");
                String originalText = (String) tJO.get("originalText");

                if (indexS<S.size() && S.get(indexS).getSpotOffset().equals(characterOffsetBegin)) {//if recognized by spotlight
                    E.add(S.get(indexS));
                    E.get(E.size()-1).setSentenceIndex(Long.valueOf(sentenceNo));
                    if(indexS < S.size()-1) {
                        indexS++;
                    }
                } else if (i>0 && E.size()>0 && (!ner.equalsIgnoreCase("O") || !ner7.equalsIgnoreCase("O") || !ner3.equalsIgnoreCase("O"))) {//if recognized by any ner
                    if (!((isSameEntity(ner, prevNer)) || (isSameEntity(ner3, prevNer3)) || (isSameEntity(ner7, prevNer7)))) {//if its a new entity
                        MyEntity m = new MyEntity();
                        m.setIdentifiedBySpot(false);
                        //E.get(E.size()-1).setSentenceIndex(Long.valueOf(sentenceNo));
                        if(!ner.equalsIgnoreCase("O")) {
                            m.setEntityNer(NERINDEX,(Long.valueOf(sentenceNo)),index,pos,characterOffsetBegin, characterOffsetEnd, true,ner,originalText);
                        }
                        if(!ner3.equalsIgnoreCase("O")) {
                            m.setEntityNer(NER3INDEX,(Long.valueOf(sentenceNo)),index,pos,characterOffsetBegin, characterOffsetEnd, true,ner3,originalText);
                        }
                        if(!ner7.equalsIgnoreCase("O")) {
                            m.setEntityNer(NER7INDEX,(Long.valueOf(sentenceNo)),index,pos,characterOffsetBegin, characterOffsetEnd, true,ner7,originalText);
                        }
                        m.setTokenIndex((long) i);
                        E.add(m);
                    } else {
                        if (isSameEntity(ner, prevNer)) {//if corenlp ner type is same as previous corenlp ner type, then expand the entity till this word
                            E.get(E.size() - 1).expandEntityNer(NERINDEX, pos, characterOffsetEnd, originalText);
                        } else if (E.size()>0 && !ner.equalsIgnoreCase("O")) {//if corenlp ner type is not empty, then set entity's ner[0] attributes
                            E.get(E.size() - 1).setEntityNer(NERINDEX, Long.valueOf(sentenceNo), index, pos, characterOffsetBegin, characterOffsetEnd, true, ner, originalText);
                        }
                        if (isSameEntity(ner3, prevNer3)) {//if 3-class ner type is same as previous 3-class ner type, then expand the entity till this word
                            E.get(E.size() - 1).expandEntityNer(NER3INDEX, pos, characterOffsetEnd, originalText);
                        } else if (E.size()>0 && !ner3.equalsIgnoreCase("O")) {//if 3-class ner type is not empty, then set entity's ner[1] attributes
                            E.get(E.size() - 1).setEntityNer(NER3INDEX, Long.valueOf(sentenceNo), index, pos, characterOffsetBegin, characterOffsetEnd, true, ner3, originalText);
                        }
                        if (isSameEntity(ner7, prevNer7)) {//if 7-class ner type is same as previous 7-class ner type, then expand the entity till this word
                            E.get(E.size() - 1).expandEntityNer(NER7INDEX, pos, characterOffsetEnd, originalText);
                        } else if (E.size()>0 && !ner7.equalsIgnoreCase("O")) {//if 7-class ner type is not empty, then set entity's ner[2] attributes
                            E.get(E.size() - 1).setEntityNer(NER7INDEX, Long.valueOf(sentenceNo), index, pos, characterOffsetBegin, characterOffsetEnd, true, ner7, originalText);
                        }
                        prevNer = ner;
                        prevNer3 = ner3;
                        prevNer7 = ner7;
                        continue;
                    }
                } else {//if neither recognized by ner nor by spotlight

                    //START --- RULE# 7  --- if an entity re-appears, add it to E again
                   /* for (int k=0; k<E.size(); k++){
                        if(E.get(k).getIsEntity()==1) {
                            boolean p = false, p3 = false, p7 = false;
                            if (!E.get(k).getNerType(NERINDEX).isEmpty()) {
                                p = (E.get(k).getNerType(NERINDEX)).equals("PERSON");
                            }
                            if (!(E.get(k).getNerType(NER3INDEX)).isEmpty()) {
                                p3 = (E.get(k).getNerType(NER3INDEX)).equals("PERSON");
                            }
                            if (!(E.get(k).getNerType(NER7INDEX)).isEmpty()) {
                                p7 = (E.get(k).getNerType(NER7INDEX)).equals("PERSON");
                            }
                            boolean isPerson = (p || p3 || p7);
                            boolean isReappear = false;
                            if (isPerson) {
                                if (E.get(k).getOriginalText(NERINDEX).contains(originalText) || E.get(k).getSurfaceForm().contains(originalText)) {
                                    isReappear = true;
                                }
                            } else {
                                if (E.get(k).getOriginalText(NERINDEX).equalsIgnoreCase(originalText) || E.get(k).getSurfaceForm().equalsIgnoreCase(originalText)) {
                                    isReappear = true;
                                }
                            }
                            if (isReappear) {
                                MyEntity m = new MyEntity();
                                m.setIdentifiedBySpot(false);
                                String text = E.get(k).getOriginalText(NERINDEX);
                                if (text.isEmpty()) {
                                    text = E.get(k).getSurfaceForm();
                                }
                                m.setEntityNer(NERINDEX, Long.valueOf(sentenceNo), index, pos, characterOffsetBegin, characterOffsetEnd, false, ner, text);
                                //E.get(E.size() - 1).setSentenceIndex(Long.valueOf(sentenceNo));
                                m.setTokenIndex((long) i);
                                E.add(m);
                            }
                        }
                    }*/
                    //END --- RULE# 7  --- if an entity re-appears, add it to E again

                    prevNer = ner;
                    prevNer3 = ner3;
                    prevNer7 = ner7;
                    continue;//if entity not recognized by spotlight and ner, then skip this iteration
                }

                //set all NER properties of entity
                if (!ner.equalsIgnoreCase("O")) {//if corenlp ner type is not empty, then set entity's ner[0] attributes
                    E.get(E.size() - 1).setEntityNer(NERINDEX, Long.valueOf(sentenceNo), index, pos, characterOffsetBegin, characterOffsetEnd, true, ner, originalText);
                }
                if (!ner3.equalsIgnoreCase("O")) {//if 3-class ner type is not empty, then set entity's ner[1] attributes
                    E.get(E.size() - 1).setEntityNer(NER3INDEX, Long.valueOf(sentenceNo), index, pos, characterOffsetBegin, characterOffsetEnd, true, ner3, originalText);
                }
                if (!ner7.equalsIgnoreCase("O")) {//if 7-class ner type is not empty, then set entity's ner[2] attributes
                    E.get(E.size() - 1).setEntityNer(NER7INDEX, Long.valueOf(sentenceNo), index, pos, characterOffsetBegin, characterOffsetEnd, true, ner7, originalText);
                }
                //end of setting all NER properties of entity

                prevNer = ner;
                prevNer3 = ner3;
                prevNer7 = ner7;
            }
        }
        System.out.println("Exiting class: Reasoner, function: identifiedByNERs");
        return E;
    }

    private boolean isSameEntity(String nerType, String prevNerType){
        boolean isSame = false;
        if(nerType.equalsIgnoreCase(prevNerType)){
            if(nerType.equalsIgnoreCase("O")){
                isSame = false;
            }
            else {
                isSame = true;
            }
        }else {
            isSame = false;
        }
        return isSame;
    }

}