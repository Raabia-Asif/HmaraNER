package com.company;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * This class contains a simple example, showing how a developer could create a
 * simple NIF corpus.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
//@Ignore
class CorpusGenerationExample {

	public void example(){//String[] args) {

		String text = "Japan (Japanese: 日本 Nippon or Nihon) is a stratovolcanic archipelago of 6,852 islands.";
		Document document = new DocumentImpl(text, "http://example.org/document0");

		// Add the marking for "Japan"
		Set<String> uris = new HashSet<String>();
		uris.add("http://example.org/Japan");
		Set<String> types = new HashSet<String>();
		types.add("http://example.org/Country");
		types.add("http://example.org/StratovolcanicArchipelago");
		document.addMarking(new TypedNamedEntity(0, 5, uris, types));

		// Add the marking for "stratovolcanic archipelago"
		uris = new HashSet<String>();
		uris.add("http://example.org/StratovolcanicArchipelago");
		types = new HashSet<String>();
		types.add("http://example.org/Archipelago");
		types.add("http://www.w3.org/2000/01/rdf-schema#Class");
		document.addMarking(new TypedNamedEntity(42, 26, uris, types));

		// Add a marking showing that this document has geographical content
		uris = new HashSet<String>();
		uris.add("http://example.org/Geography");
		document.addMarking(new Annotation(uris));

		List<Document> documents = new ArrayList<Document>();
		documents.add(document);

		// Writing our new list of documents to a String
		NIFWriter writer = new TurtleNIFWriter();
		String nifString = writer.writeNIF(documents);
		System.out.println(nifString);

		// After generating a NIF corpus, it can be helpful to parse the NIF using a `NIFParser` instance.
		//NIFParser parser = new TurtleNIFParser();
		//parser.parseNIF(nifString);

		// Instead of text containing the NIF information, a jena RDF `Model` can be created.
		DocumentListWriter listWriter = new DocumentListWriter();
		Model nifModel = ModelFactory.createDefaultModel();
		listWriter.writeDocumentsToModel(nifModel, documents);
	}
}
