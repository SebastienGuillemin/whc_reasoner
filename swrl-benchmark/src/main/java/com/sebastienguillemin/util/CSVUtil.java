package com.sebastienguillemin.util;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Loads CSV to populate a KB.
 */
public class CSVUtil {
    public static void addToCSV(String fileName, String row) throws IOException {
        File file = new File(fileName);
        boolean exists = file.exists();
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            if (!exists)
                pw.println("KB,Inferring time (ms), Inferred axioms");

            pw.println(row);
            pw.close();
        }
    }
}
