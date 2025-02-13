package com.sebastienguillemin.whcreasoner.core.explainer;

import java.util.Hashtable;
import java.util.Set;
import java.util.Map.Entry;

import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;

public class Explainer {
    public String explain(Hashtable<Atom, Set<Atom>> satisfiedAtomCauses) {
        String explanations = "";
        for (Entry<Atom, Set<Atom>> entry : satisfiedAtomCauses.entrySet()) {
            Atom atom = entry.getKey();
            Set<Atom> causes = entry.getValue();

            String atomPrettyString = atom.toPrettyString();

            explanations += atomPrettyString + "because:\n";

            for (Atom cause : causes) {
                String causePrettyString = cause.toPrettyString();
                if (atomPrettyString.equals(causePrettyString))
                    continue;

                explanations += "   - " + causePrettyString + "\n";
            }

            explanations += "\n";
        }

        return explanations;
    }    
}