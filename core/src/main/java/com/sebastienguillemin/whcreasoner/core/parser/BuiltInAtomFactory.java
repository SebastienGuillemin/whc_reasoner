package com.sebastienguillemin.whcreasoner.core.parser;

import org.semanticweb.owlapi.model.IRI;

import com.sebastienguillemin.whcreasoner.core.entities.atom.BuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin.DifferentFrom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin.FivePerCentDifferent;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin.GreaterThanOrEqual;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin.LessThanOrEqual;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin.SameAs;

public class BuiltInAtomFactory {
    public static BuiltInAtom createAtom(IRI atomIRI) {
        if (atomIRI.equals(BuiltInReference.SAME_AS.getIri()))
            return new SameAs();

        else if (atomIRI.equals(BuiltInReference.DIFFERENT_FROM.getIri()))
            return new DifferentFrom();

        else if (atomIRI.equals(BuiltInReference.LESS_THAN_EQUAL.getIri()))
            return new LessThanOrEqual();

        else if (atomIRI.equals(BuiltInReference.GREATER_THAN_EQUAL.getIri()))
            return new GreaterThanOrEqual();

        else if (atomIRI.equals(BuiltInReference.FIVE_PER_CENT_DIFFERENT.getIri()))
            return new FivePerCentDifferent();

        return null;
    }
}
