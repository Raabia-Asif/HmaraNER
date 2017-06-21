package com.company;

/**
 * Created by Raabia on 5/13/2017.
 */

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DBPediaQueryClient {


    /**
     * Query an Endpoint using the given SPARQl query
     * @param szQuery
     * @param szEndpoint
     * @throws Exception
     */
    public List<String> queryEndpoint(String szQuery, String szEndpoint)   throws Exception
    {
        // Create a Query with the given String
        Query query = QueryFactory.create(szQuery);

        // Create the Execution Factory using the given Endpoint
        QueryExecution qexec = QueryExecutionFactory.sparqlService(szEndpoint, query);

        // Set Timeout
        ((QueryEngineHTTP)qexec).addParam("timeout", "1000000");


        // Execute Query
        int iCount = 0, i=0;
        ResultSet rs = qexec.execSelect();
        List<String> uris = new ArrayList<String>();
        while (rs.hasNext()) {
            // Get Result
            QuerySolution qs = rs.next();

            // Get Variable Names
            Iterator<String> itVars = qs.varNames();

            // Count
            iCount++;
            System.out.println("Result " + iCount + ": ");

            // Display Result
            while (itVars.hasNext()) {
                String szVar = itVars.next().toString();
                String szVal = qs.get(szVar).toString();

                uris.add(szVal);
                i++;//i is no more needed
                System.out.println("[" + szVar + "]: " + szVal);
            }
        }
        return uris;
    } // End of Method: queryEndpoint()


    public List<String> qExample(String fileName, String label) throws IOException {
        List<String> uris = new ArrayList<String>();
        // SPARQL Query
        String szQuery = "select * where {?Subject ?Predicate ?Object} LIMIT 5";

        // Arguments
        if (!fileName.isEmpty()) {
            byte[] bytes = Files.readAllBytes(Paths.get("src\\com\\company\\queryBase",fileName));
            szQuery = new String(bytes, Charset.defaultCharset());
        }
        //String label = "Florence Nightingale";
        szQuery = szQuery.replace("LABEL_TO_QUERY", label);
        System.out.println("The label to query on DBpedia is : "+label);

        // DBPedia Endpoint
        String szEndpoint = "http://dbpedia.org/sparql";

        // Query DBPedia
        try {
            DBPediaQueryClient q = new DBPediaQueryClient();
            uris.addAll(q.queryEndpoint(szQuery, szEndpoint));
        }
        catch (Exception ex) {
            System.err.println(ex);
        }
        return uris;
    }
}