/**
 * This file is part of NIF transfer library for the General Entity Annotator Benchmark.
 *
 * NIF transfer library for the General Entity Annotator Benchmark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NIF transfer library for the General Entity Annotator Benchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NIF transfer library for the General Entity Annotator Benchmark.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.gerbil.io.nif.impl;

import java.io.InputStream;
import java.io.Reader;

import org.aksw.gerbil.io.nif.AbstractNIFParser;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFReaderRIOT_TTL;
import org.apache.jena.riot.adapters.RDFReaderRIOT;

import com.hp.hpl.jena.rdf.model.Model;

public class TurtleNIFParser extends AbstractNIFParser {

    private static final String HTTP_CONTENT_TYPE = "application/x-turtle";

    public TurtleNIFParser() {
        super(HTTP_CONTENT_TYPE);
    }

    @Override
    protected Model parseNIFModel(InputStream is, Model nifModel) {
        RDFReaderRIOT rdfReader = new RDFReaderRIOT_TTL();
        rdfReader.read(nifModel, is, "");
        return nifModel;
    }

    @Override
    protected Model parseNIFModel(Reader reader, Model nifModel) {
        RDFReaderRIOT rdfReader = new RDFReaderRIOT_TTL();
        rdfReader.read( nifModel, reader, "");
        return nifModel;
    }

}
