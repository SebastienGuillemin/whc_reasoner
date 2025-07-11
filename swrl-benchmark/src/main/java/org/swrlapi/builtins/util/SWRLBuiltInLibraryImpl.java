package org.swrlapi.builtins.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.swrlapi.builtins.AbstractSWRLBuiltInLibrary;
import org.swrlapi.builtins.arguments.SWRLBuiltInArgument;
import org.swrlapi.exceptions.SWRLBuiltInException;

public class SWRLBuiltInLibraryImpl extends AbstractSWRLBuiltInLibrary{
    public static final String PREFIX = "util";
    public static final String NAMESPACE_NAME = "http://www.sebastienguillemin.fr/swrl/builtins/util#";
    public static final HashSet<String> BUILT_IN_NAMES = new HashSet<>(Arrays.asList("lessThanfivePercent"));


    public SWRLBuiltInLibraryImpl() {
        super(PREFIX, NAMESPACE_NAME, BUILT_IN_NAMES);
    }

    @Override
    public void reset() {
        // Do nothing
    }

    public boolean lessThanfivePercent(List<SWRLBuiltInArgument> arguments) throws SWRLBuiltInException  {
        checkNumberOfArgumentsEqualTo(2, arguments.size());

        float val1 = getArgumentAsAFloat(0, arguments);
        float val2 = getArgumentAsAFloat(1, arguments);
        
        return (100f * Math.abs(val1 - val2) / ((val1 + val2) / 2f)) <= 5f;
    }
}
