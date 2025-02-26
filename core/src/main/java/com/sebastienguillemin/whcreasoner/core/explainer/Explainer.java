package com.sebastienguillemin.whcreasoner.core.explainer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

            explanations += atomPrettyString + " because:\n";

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

    public void writeExplanations(Hashtable<Atom, Set<Atom>> satisfiedAtomCauses, String filePath) {
        File file = new File(filePath);
        try {
            // Create the file if it does not exist
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            
            writer.write(this.explain(satisfiedAtomCauses));
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}