package com.sebastienguillemin.whcreasoner.core.parser;

import org.semanticweb.owlapi.model.IRI;

import com.sebastienguillemin.whcreasoner.core.entities.atom.BuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin.DateDiffGreaterThanSixMonths;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin.DateDiffLessEqualThanSixMonths;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin.DiffValuesIn;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin.DifferentFrom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin.LessThanfivePerCent;
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

        else if (atomIRI.equals(BuiltInReference.LESS_THAN_FIVE_PERCENT.getIri()))
            return new LessThanfivePerCent();

        else if (atomIRI.equals(BuiltInReference.DATE_DIFF_LESS_EQUAL_THAN_SIX_MONTHS.getIri()))
            return new DateDiffLessEqualThanSixMonths();

        else if (atomIRI.equals(BuiltInReference.DATE_DIFF_GREATER_THAN_SIX_MONTHS.getIri()))
            return new DateDiffGreaterThanSixMonths();

        else if (atomIRI.equals(BuiltInReference.DIFF_VALUES_IN.getIri()))
            return new DiffValuesIn();

        return null;
    }
}
