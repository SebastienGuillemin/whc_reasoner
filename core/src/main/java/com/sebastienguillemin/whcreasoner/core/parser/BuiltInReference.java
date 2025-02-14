package com.sebastienguillemin.whcreasoner.core.parser;

import org.semanticweb.owlapi.model.IRI;

import com.sebastienguillemin.whcreasoner.core.entities.NamespacePrefix;

import lombok.Getter;

@Getter
public enum BuiltInReference {
    SAME_AS(IRI.create(NamespacePrefix.OWL.getCompleteIRI() + "sameAs")),
    DIFFERENT_FROM(IRI.create(NamespacePrefix.OWL.getCompleteIRI() + "differentFrom")),
    LESS_THAN_EQUAL(IRI.create(NamespacePrefix.SWRLB.getCompleteIRI() + "lessThanOrEqual")),
    GREATER_THAN_EQUAL(IRI.create(NamespacePrefix.SWRLB.getCompleteIRI() + "greaterThanOrEqual")),
    LESS_THAN_FIVE_PERCENT(IRI.create(NamespacePrefix.STUPS.getCompleteIRI() + "lessThanfivePercent")),
    DIFF_VALUES_IN(IRI.create(NamespacePrefix.STUPS.getCompleteIRI() + "diffValuesIn")),
    DATE_DIFF_LESS_EQUAL_THAN_SIX_MONTHS(IRI.create(NamespacePrefix.STUPS.getCompleteIRI() + "dateDiffLessEqualThanSixMonths")),
    DATE_DIFF_GREATER_THAN_SIX_MONTHS(IRI.create(NamespacePrefix.STUPS.getCompleteIRI() + "dateDiffGreaterThanSixMonths"));

    private IRI iri;

    private BuiltInReference(IRI iri) {
        this.iri = iri;
    }

    public String IRIAsString() {
        return this.iri.toString();
    }
}
