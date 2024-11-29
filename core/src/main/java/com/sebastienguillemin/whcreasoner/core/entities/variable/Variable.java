package com.sebastienguillemin.whcreasoner.core.entities.variable;

import java.util.HashMap;
import java.util.UUID;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

import com.sebastienguillemin.whcreasoner.core.entities.BaseEntity;
import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;

import lombok.Getter;

public class Variable extends BaseEntity implements Comparable<Variable> {
    private static int CONSTANT_COUNTER = 0;

    public static IVariable createIVariable(IRI iri, Atom atom) {
        return new IVariable(iri, atom);
    }

    public static DVariable createDVariable(IRI iri, Atom atom) {
        return new DVariable(iri, atom);
    }

    public static Variable createGenericVariable(IRI iri, Atom atom) {
        return new Variable(iri, null, atom);
    }

    public static int getNextConstantIndex() {
        return ++CONSTANT_COUNTER;
    }

    private UUID ID = UUID.randomUUID();

    @Getter
    protected VariableType type;

    @Getter
    protected OWLPropertyAssertionObject value;

    @Getter
    protected boolean constant;

    @Getter
    protected HashMap<UUID, Atom> atoms;

    @Getter
    protected HashMap<Variable, Atom> dependsOfSubjects;

    @Getter
    protected HashMap<Variable, Atom> dependsOfObjects;

    @Getter
    protected boolean boundToNothing;

    protected Variable(IRI iri, VariableType type, Atom atom) {
        super(iri);
        this.type = type;
        this.constant = false;
        this.atoms = new HashMap<>();
        this.dependsOfSubjects = new HashMap<>();
        this.dependsOfObjects = new HashMap<>();
        this.boundToNothing = false;
        
        this.addAtom(atom);
    }

    public void setValue(OWLPropertyAssertionObject value) throws VariableValueException {
        if (this.constant)
            throw new VariableValueException(
                    "Cannot update value because " + this.iri.getFragment() + " is a constant.");

        if (this.boundToNothing)
            throw new VariableValueException(
                    "Cannot update value because " + this.iri.getFragment() + " is currently bound to nothing. Call 'clear()' first.");

        this.value = value;
    }

    public void constant(boolean constant) {
        this.constant = constant;
    }

    public void clear() {
        if (!this.constant)
            this.value = null;
            this.boundToNothing = false;
    }

    public void addAtom(Atom atom) {
        UUID atomID = atom.getUniqueID();

        if (!this.atoms.containsKey(atomID)) {
            this.atoms.put(atomID, atom);
        }
    }

    public boolean hasValue() {
        return !this.isBoundToNothing() && this.value != null;
    }

    /**
     * Return the ordered set of atoms in which the variable appears. The list is
     * ordered using the atoms precedence.
     * 
     * @see Atom
     * 
     * @return a list of ordered atom.
     * 
     */
    // public TreeSet<Atom> getOrderedAtoms() {
    //     return new TreeSet<>(this.atoms.values());
    // }

    public Variable copyVariable(Atom atom) {
        Variable newVariable = new Variable(this.iri, this.type, atom);

        this.copyAttributes(newVariable);

        return newVariable;
    }

    public void bindToNothing() {
        this.clear();
        this.boundToNothing = true;
    }

    @Override
    public String toString() {
        return this.iri.getFragment() + "_" + this.ID.toString().substring(this.ID.toString().length() - 6) + " (" + this.type + ")" + ((this.value != null) ? " = " + this.value : "");
    }

    public String toShortString() {
        if (this.type == VariableType.I_VARIABLE)
            return "?" + this.iri.getFragment() + "_" + this.ID.toString().substring(this.ID.toString().length() - 6) + ((this.value != null) ? "=" + ((OWLNamedIndividual) this.value).getIRI().getFragment() : "");

        return "?" + this.iri.getFragment() + "_" + this.ID.toString().substring(this.ID.toString().length() - 6) + ((this.value != null || this.isBoundToNothing()) ? "=" + this.value : "");
    }

    @Override
    public int compareTo(Variable otherVar) {
        if (this.iri.equals(otherVar.getIRI()) && this.ID.equals(otherVar.ID))
            return 0;

        if (this.value == null && !this.boundToNothing)
            return 1;

        if (otherVar.getValue() == null && !otherVar.isBoundToNothing())
            return -1;

        if (!otherVar.getIRI().equals(this.iri))
            return this.iri.compareTo(otherVar.getIRI());

        return this.ID.compareTo(otherVar.ID);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Variable))
            return false;

        return this.compareTo((Variable) other) == 0;
    }

    protected void copyAttributes(Variable newVariable) {
        newVariable.constant = this.constant;
        newVariable.value = this.value;
    }
}
