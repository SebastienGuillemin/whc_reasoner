package com.sebastienguillemin.whcreasoner.core.entities;

import org.semanticweb.owlapi.model.IRI;

public class BaseEntity implements NamedEntity {
    protected IRI iri;

    public BaseEntity(IRI iri) {
        this.iri = iri;
    }

    @Override
    public IRI getIRI() {
        return this.iri;
    }
}
