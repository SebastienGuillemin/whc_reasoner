package com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.semanticweb.owlapi.model.OWLLiteral;

import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.AbstractBuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.parser.BuiltInReference;

public class DateDiffGreaterThanSixMonths extends AbstractBuiltInAtom {

    public DateDiffGreaterThanSixMonths() {
        super(BuiltInReference.DATE_DIFF_GREATER_THAN_SIX_MONTHS.getIri());
        this.arity = 2;
        // this.dateTimeFormatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.FRANCE);
    }

    @Override
    protected DateDiffGreaterThanSixMonths copyBuiltin() {
        return new DateDiffGreaterThanSixMonths();
    }

    @Override
    public boolean isSatisfied() {
        OWLLiteral date1Str = (OWLLiteral) this.variables.get(0).getValue();
        OWLLiteral date2Str = (OWLLiteral) this.variables.get(1).getValue();

        LocalDate date1 = LocalDate.parse(date1Str.getLiteral().replace("Z", ""));
        LocalDate date2 = LocalDate.parse(date2Str.getLiteral().replace("Z", ""));

       return Math.abs(ChronoUnit.DAYS.between(date1, date2)) > 182.5;
    }

    @Override
    public String toPrettyString() {
        OWLLiteral date1Str = (OWLLiteral) this.variables.get(0).getValue();
        OWLLiteral date2Str = (OWLLiteral) this.variables.get(1).getValue();

        return 
            date1Str + " and " + date2Str + " are more than 6 months apart ";
    }
}