package com.sebastienguillemin.whcreasoner.core.entities;

import org.semanticweb.owlapi.model.IRI;

import lombok.Getter;

public enum NamespacePrefix {
    STUPS(IRI.create("http://www.stups.fr/ontologies/2023/stups/#")),
    OWL(IRI.create("http://www.w3.org/2002/07/owl#")),
    RDF(IRI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#")),
    RDFS(IRI.create("http://www.w3.org/2000/01/rdf-schema#")),
    XML(IRI.create("http://www.w3.org/XML/1998/namespace#")),
    XSD(IRI.create("http://www.w3.org/2001/XMLSchema#")),
    SWRLB(IRI.create("https://www.w3.org/2003/11/swrlb#"));

    @Getter
    public final IRI completeIRI;

    private NamespacePrefix(IRI completeIRI) {
        this.completeIRI = completeIRI;
    }
}


