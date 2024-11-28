package com.sebastienguillemin.whcreasoner.core.parser;

import org.semanticweb.owlapi.model.IRI;

import com.sebastienguillemin.whcreasoner.core.entities.NamespacePrefix;

import lombok.Getter;

@Getter
public enum BuiltInReference {
    SAME_AS(IRI.create(NamespacePrefix.OWL.getCompleteIRI() + "sameAs")),
    DIFFERENT_FROM(IRI.create(NamespacePrefix.OWL.getCompleteIRI() + "differentFrom")),
    LESS_THAN_EQUAL(IRI.create(NamespacePrefix.SWRLB.getCompleteIRI() + "lessThanOrEqual")),
    GREATER_THAN_EQUAL(IRI.create(NamespacePrefix.SWRLB.getCompleteIRI() + "greaterThanOrEqual"));

    private IRI iri;

    private BuiltInReference(IRI iri) {
        this.iri = iri;
    }

    public String IRIAsString() {
        return this.iri.toString();
    }
}
