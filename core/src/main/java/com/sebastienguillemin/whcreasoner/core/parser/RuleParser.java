package com.sebastienguillemin.whcreasoner.core.parser;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

import com.sebastienguillemin.whcreasoner.core.entities.NamespacePrefix;
import com.sebastienguillemin.whcreasoner.core.entities.OntologyWrapper;
import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.BuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.ClassAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.DataPropertyAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.DatatypeAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.ObjectPropertyAtom;
import com.sebastienguillemin.whcreasoner.core.entities.rule.Rule;
import com.sebastienguillemin.whcreasoner.core.entities.variable.DVariable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.IVariable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.exception.AtomParsingException;
import com.sebastienguillemin.whcreasoner.core.exception.AtomWeightException;
import com.sebastienguillemin.whcreasoner.core.exception.InvalidRuleException;
import com.sebastienguillemin.whcreasoner.core.exception.RuleParsingException;
import com.sebastienguillemin.whcreasoner.core.exception.URIReferenceException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableTypeException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;
import com.sebastienguillemin.whcreasoner.core.exception.VariablesCountException;

import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplBoolean;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplDouble;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplFloat;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplInteger;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplLong;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplString;

// TODO : better datatype verification (?).
// TODO : handle custom rule URI.
/**
 * 
 */
public class RuleParser {
    private OntologyWrapper ontologyWrapper;
    private IRI baseIRI;

    /**
     * Caches
     */
    private Set<IRI> classIRI;
    // private List<IRI> datatypeIRI;
    private Set<IRI> objectPropertyIRI;
    private Set<IRI> dataPropertyIRI;

    private Hashtable<IRI, Variable> variables;

    public RuleParser(OntologyWrapper ontologyWrapper) {
        this.ontologyWrapper = ontologyWrapper;
        this.classIRI = new HashSet<>();
        // this.datatypeIRI = new ArrayList<>();
        this.objectPropertyIRI = new HashSet<>();
        this.dataPropertyIRI = new HashSet<>();
        this.variables = new Hashtable<>();

        this.initChache();
    }

    /**
     * Parse the rule given as string via parameter {@code ruleString}.
     * 
     * @param ruleName   the rule name
     * @param ruleString the rule to parse.
     * @return the {@link Rule} corresponding to the {@code ruleString}.
     * 
     * @throws RuleParsingException   If an exception occurs when parsing the rule.
     * @throws InvalidRuleException   If the rule is invalid (invalid head atom type
     *                                etc.).
     * @throws AtomWeightException    If an atom weight is lower or equal than 0 or
     *                                greater than 1.
     * @throws VariableTypeException  If a variable of the wrong type is used to
     *                                instantiate an atom.
     * @throws URIReferenceException  If the a variable URI is incorrect.
     * @throws VariableValueException
          * @throws VariablesCountException 
          * 
          * @see Rule
          * @see Atom
          */
         public Rule parseRule(String ruleName, String ruleString) throws RuleParsingException,
                 InvalidRuleException, AtomWeightException, VariableTypeException, URIReferenceException,
                 VariableValueException, VariablesCountException {

        Rule rule = null;
        this.variables.clear();

        String[] ruleParts = ruleString.split("->");

        if (ruleParts.length != 2)
            throw new RuleParsingException(
                    "A rule must have exactly two parts. The input rule has " + ruleParts.length + " part(s).");

        String bodyStr = ruleParts[0].trim();

        String headStr, threshold;
        Pattern pattern = Pattern.compile("(.*)\\[(.*)\\]");
        Matcher matcher = pattern.matcher(ruleParts[1].trim());

        if (!matcher.matches())
            throw new RuleParsingException(
                    "Invalide atom format for head and threshold : " + ruleParts[1].trim() + ".");

        headStr = matcher.group(1);
        threshold = matcher.group(2);

        Set<Atom> atoms = this.parseBody(bodyStr);
        Atom atom = this.parsehead(headStr);


        rule = new Rule(IRI.create(this.ontologyWrapper.getBaseIRI().toString() + ruleName), atoms, atom,
                Float.valueOf(threshold));

        return rule;
    }

    public Rule parseRule(String ruleString) throws RuleParsingException,
            InvalidRuleException, AtomWeightException, VariableTypeException, URIReferenceException,
            VariableValueException, VariablesCountException {

        return this.parseRule(UUID.randomUUID().toString(), ruleString);
    }

    /**
     * Retrieve elements from ontology wrapper caches.
     */
    private void initChache() {
        this.baseIRI = this.ontologyWrapper.getBaseIRI();

        this.classIRI = this.ontologyWrapper.getClasses().stream().map(c -> c.getIRI()).collect(Collectors.toSet());
        // this.datatypeIRI = this.ontology.datatypesInSignature().map(c ->
        // c.getIRI()).collect(Collectors.toList());
        this.objectPropertyIRI = this.ontologyWrapper.getObjectProperties().stream().map(op -> op.getIRI())
                .collect(Collectors.toSet());
        this.dataPropertyIRI = this.ontologyWrapper.getDataProperties().stream().map(dp -> dp.getIRI())
                .collect(Collectors.toSet());
    }

    private Atom parsehead(String headStr) throws RuleParsingException, AtomWeightException,
            VariableTypeException, URIReferenceException, VariableValueException, VariablesCountException {

        headStr = headStr.replace("^^", "~");
        Atom head = this.parseAtom(headStr);
        head.isHead(true);

        return head;
    }

    private Set<Atom> parseBody(String bodyStr) throws RuleParsingException, AtomWeightException,
            VariableTypeException, URIReferenceException, VariableValueException, VariablesCountException {
        Set<Atom> atoms = new HashSet<>();

        // Rewrite "^^" sequence to avoir split on it.
        bodyStr = bodyStr.replace("^^", "~");

        String[] bodyParts = bodyStr.split("\\^");
        for (String part : bodyParts)
            atoms.add(this.parseAtom(part.trim()));

        return atoms;
    }

    private Atom parseAtom(String atomStr) throws RuleParsingException, AtomWeightException,
            VariableTypeException, URIReferenceException, VariableValueException, VariablesCountException {
        Atom atom;

        Pattern pattern = Pattern
                .compile("(([0-1](\\.[0-9]+)?) ?\\* ?)?" + "([a-zA-Z]+(\\:[a-zA-Z]+)*)" + "\\(([\\?, a-zA-Z0-9\\\"~\\:]+)\\)");
        Matcher matcher = pattern.matcher(atomStr);

        if (!matcher.matches())
            throw new RuleParsingException("Invalid atom format for atom : " + atomStr + ".");

        String weightStr = matcher.group(2);
        String IRIStr = matcher.group(4);
        String variablesStr = matcher.group(6).trim();

        String variablesPartsStr[] = variablesStr.split(",");

        IRI atomIRI = this.createAomtIRI(IRIStr);

        IRI.create(this.baseIRI + IRIStr);
        switch (variablesPartsStr.length) {
            case 0:
                throw new AtomParsingException("An atom must have at least one variable.");

            case 1:
                if (this.classIRI.contains(atomIRI)) {
                    atom = new ClassAtom(atomIRI);
                    this.createClassAtomVariable((ClassAtom) atom, variablesPartsStr[0].trim());
                } else {
                    atom = new DatatypeAtom(atomIRI);
                    this.createDatatypeAtomVariable((DatatypeAtom) atom, variablesPartsStr[0].trim());
                }
                break;

            case 2:
                if (this.objectPropertyIRI.contains(atomIRI)) {
                    atom = new ObjectPropertyAtom(atomIRI);
                    this.createObjectPropertyAtomVariables((ObjectPropertyAtom) atom, variablesPartsStr[0].trim(),
                            variablesPartsStr[1].trim());
                } else if (this.dataPropertyIRI.contains(atomIRI)) {
                    atom = new DataPropertyAtom(atomIRI);
                    this.createDataPropertyAtomVariables((DataPropertyAtom) atom, variablesPartsStr[0].trim(),
                            variablesPartsStr[1].trim());
                } else {
                    atom = BuiltInAtomFactory.createAtom(atomIRI);
                    if (atom == null)
                        throw new AtomParsingException("Binary atom " + atomIRI + " does not exist.");

                    this.createBuiltinAtomVariable((BuiltInAtom) atom, variablesPartsStr);
                }
                break;

            default:
                atom = BuiltInAtomFactory.createAtom(atomIRI);
                this.createBuiltinAtomVariable((BuiltInAtom) atom, variablesPartsStr);
                break;
        }

        if (weightStr == null)
            atom.setWeight(1);
        else
            atom.setWeight(Float.valueOf(weightStr));

        return atom;
    }

    private IRI createAomtIRI(String IRIStr) throws RuleParsingException {
        String[] IRIStrParts = IRIStr.split("\\:");

        if (IRIStrParts.length == 1)
            return IRI.create(this.baseIRI + IRIStr);
        else if (IRIStrParts.length == 2) {
            return IRI.create(NamespacePrefix.valueOf(IRIStrParts[0].toUpperCase()).getCompleteIRI() + IRIStrParts[1]);
        } else
            throw new RuleParsingException("Invalid atom IRI : " + IRIStr + ".");
    }

    private void createClassAtomVariable(ClassAtom atom, String variablesStr)
            throws RuleParsingException, VariableTypeException, URIReferenceException, VariableValueException {

        if (isAConstant(variablesStr)) {
            atom.setVariable(createIConstant(atom, variablesStr));
        }

        atom.setVariable(this.createIVariable(checkAndClearVariableSyntaxe(variablesStr.trim()), atom));
    }

    private void createDatatypeAtomVariable(DatatypeAtom atom, String variablesStr)
            throws RuleParsingException, VariableTypeException, URIReferenceException, VariableValueException {

        if (isAConstant(variablesStr)) {
            atom.setVariable(createDConstant(atom, variablesStr));
        } else
            atom.setVariable(this.createDVariable(checkAndClearVariableSyntaxe(variablesStr), atom));
    }

    private void createObjectPropertyAtomVariables(ObjectPropertyAtom atom, String firstVariableStr,
            String secondVariableStr)
            throws RuleParsingException, VariableTypeException, URIReferenceException, VariableValueException {

        if (isAConstant(firstVariableStr)) {
            atom.setFirstVariable(createIConstant(atom, secondVariableStr));
        } else
            atom.setFirstVariable(this.createIVariable(checkAndClearVariableSyntaxe(firstVariableStr), atom));

        if (isAConstant(secondVariableStr)) {
            atom.setSecondVariable(createIConstant(atom, secondVariableStr));
        } else
            atom.setSecondVariable(this.createIVariable(checkAndClearVariableSyntaxe(secondVariableStr), atom));
    }

    private void createDataPropertyAtomVariables(DataPropertyAtom atom, String firstVariableStr,
            String secondVariableStr)
            throws RuleParsingException, VariableTypeException, URIReferenceException, VariableValueException {
        
        if (isAConstant(firstVariableStr)) {
            atom.setFirstVariable(createIConstant(atom, firstVariableStr));
        } else
            atom.setFirstVariable(this.createIVariable(checkAndClearVariableSyntaxe(firstVariableStr), atom));

        if (isAConstant(secondVariableStr)) {
            atom.setSecondVariable(createDConstant(atom, secondVariableStr));
        } else
            atom.setSecondVariable(this.createDVariable(checkAndClearVariableSyntaxe(secondVariableStr), atom));
    }

    private void createBuiltinAtomVariable(BuiltInAtom atom, String variablesPartsStr[])
            throws RuleParsingException, VariableTypeException, URIReferenceException, VariableValueException, VariablesCountException {
        IRI atomIRI = atom.getIRI();

        String variable1Name = variablesPartsStr[0].trim();
        String variable2Name = variablesPartsStr[1].trim();
        if (atomIRI.equals(BuiltInReference.SAME_AS.getIri()) || atomIRI.equals(BuiltInReference.DIFFERENT_FROM.getIri())) {
            if (variablesPartsStr.length != 2)
                throw new RuleParsingException("Need exactly 2 variable to create '"+ atomIRI +"' builtIn");

            if (isAConstant(variable1Name))
                atom.addVariable(this.createIConstant(atom, variable1Name));
            else
                atom.addVariable(this.createDVariable(checkAndClearVariableSyntaxe(variable1Name.trim()), atom));
            
            if (isAConstant(variable2Name))
                atom.addVariable(this.createIConstant(atom, variable2Name));
            else
                atom.addVariable(this.createDVariable(checkAndClearVariableSyntaxe(variable2Name.trim()), atom));
        } else if (atomIRI.equals(BuiltInReference.LESS_THAN_EQUAL.getIri()) || atomIRI.equals(BuiltInReference.GREATER_THAN_EQUAL.getIri())) {
            if (variablesPartsStr.length != 2)
                throw new RuleParsingException("Need exactly 2 variable to create '" + atomIRI + "' builtIn");

            if (isAConstant(variable1Name))
                atom.addVariable(this.createDConstant(atom, variable1Name));
            else
                atom.addVariable(this.createDVariable(checkAndClearVariableSyntaxe(variable1Name.trim()), atom));
            
            if (isAConstant(variable2Name)) {
                atom.addVariable(this.createDConstant(atom, variable2Name));
            }
            else {
                atom.addVariable(this.createDVariable(checkAndClearVariableSyntaxe(variable2Name.trim()), atom));
            }
        }
    }

    private boolean isAConstant(String variable) {
        return Pattern.matches("(\".+\"~[a-zA-Z0-9]+\\:)?[a-zA-Z0-9]+", variable);
    }


    private OWLLiteral createOWLLiteral(String litteralStr) throws RuleParsingException {
        String[] litteralStrParts = litteralStr.split("~");
        String[] type = litteralStrParts[1].split("\\:");
        String constant = litteralStrParts[0].substring(1, litteralStrParts[0].length() - 1);

        if (!type[0].equals("xsd"))
            throw new RuleParsingException("Only xsd namespace is supported for literals.");

        switch (type[1]) {
            case "string":
                return new OWLLiteralImplString(constant);

            case "boolean":
                return new OWLLiteralImplBoolean(Boolean.parseBoolean(constant));

            case "double":
                return new OWLLiteralImplDouble(Double.parseDouble(constant));

            case "float":
                return new OWLLiteralImplFloat(Float.parseFloat(constant));

            case "integer":
                return new OWLLiteralImplInteger(Integer.parseInt(constant));

            case "long":
                return new OWLLiteralImplLong(Long.parseLong(constant));

            default:
                throw new RuleParsingException("Literal type : " + litteralStrParts[1] + " is not supported.");
        }

    }

    private OWLPropertyAssertionObject retrieveIndividual(String litteralStr) {
        IRI baseIRI = this.ontologyWrapper.getBaseIRI();

        return this.ontologyWrapper.getIndividual(IRI.create(baseIRI + litteralStr));
    }

    private String checkAndClearVariableSyntaxe(String variable) throws RuleParsingException {
        if (variable.length() == 1)
            throw new RuleParsingException("Error for variable : '" + variable + "', must have a name after '?'");

        variable = variable.substring(1);
        if (variable.contains("?"))
            throw new RuleParsingException(
                    "Error for variable : '" + variable + "', no '?' accepted in a variable name.");

        if (variable.contains(" "))
            throw new RuleParsingException(
                    "Error for variable : '" + variable + "', no space accepted in a variable name.");

        return variable;
    }

    private IVariable createIVariable(String variableName, Atom atom) throws RuleParsingException {
        try {
            IRI variableIRI = IRI.create(variableName.trim());

            IVariable variable = (IVariable) this.variables.get(variableIRI);

            if (variable == null) {
                variable = Variable.createIVariable(variableIRI, atom);
                this.variables.put(variableIRI, variable);
            } else
                variable.addAtom(atom);
            return variable;
        } catch (ClassCastException e) {
            throw new RuleParsingException(variableName + " is a d-variable and thus cannot be used as a i-variable. Atom: " + atom.getIRI());
        }
    }

    private DVariable createDVariable(String variableName, Atom atom) throws RuleParsingException {
        try {
            IRI variableIRI = IRI.create(variableName.trim());

            DVariable variable = (DVariable) this.variables.get(variableIRI);

            if (variable == null) {
                variable = Variable.createDVariable(variableIRI, atom);
                this.variables.put(variableIRI, variable);
            } else
                variable.addAtom(atom);
            return variable;
        } catch (ClassCastException e) {
            throw new RuleParsingException(variableName + " is a i-variable and thus cannot be used as a d-variable. Atom: " + atom.getIRI());
        }
    }

    private IVariable createIConstant(Atom atom, String constantName) throws RuleParsingException, VariableValueException {
        IVariable constant = this.createIVariable("CONSTANT_" + Variable.getNextConstantIndex(), atom);
        constant.setValue(this.retrieveIndividual(constantName));
        constant.constant(true);

        return constant;
    }

    private DVariable createDConstant(Atom atom, String constantName) throws RuleParsingException, VariableValueException {
        DVariable constant = this.createDVariable("CONSTANT_" + Variable.getNextConstantIndex(), atom);
        constant.setValue(this.createOWLLiteral(constantName));
        constant.constant(true);

        return constant;
    }
}