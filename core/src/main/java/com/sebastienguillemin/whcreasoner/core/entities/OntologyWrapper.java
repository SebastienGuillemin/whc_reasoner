package com.sebastienguillemin.whcreasoner.core.entities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;

import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;
import com.sebastienguillemin.whcreasoner.core.exception.OWLAxiomConversionException;
import com.sebastienguillemin.whcreasoner.core.util.Logger;

import lombok.Getter;

public class OntologyWrapper {
    private static final IRI TOP_OBJECT_PROPERTY_IRI = IRI.create("http://www.w3.org/2002/07/owl#topObjectProperty");
    private static final IRI TOP_DATA_PROPERTY_IRI = IRI.create("http://www.w3.org/2002/07/owl#topDataProperty");

    @Getter
    private OWLOntology ontology;

    // Caches to easely access OWL ontology elements.
    private HashMap<IRI, OWLClass> classes;
    private HashMap<IRI, OWLObjectProperty> objectProperties;
    private HashMap<IRI, OWLDataProperty> dataProperties;
    private HashMap<IRI, OWLNamedIndividual> individuals;

    @Getter
    // Class IRI -> set of individuals
    private HashMap<IRI, Set<OWLPropertyAssertionObject>> individualsByClass;
    @Getter
    // Object property IRI -> set of subject individuals
    private HashMap<IRI, Set<OWLPropertyAssertionObject>> objectPropertiesSubjectIndividuals;

    @Getter
    // Object property IRI -> set of object individuals
    private HashMap<IRI, Set<OWLPropertyAssertionObject>> objectPropertiesObjectIndividuals;

    @Getter
    // Data property IRI -> set of subject individuals
    private HashMap<IRI, Set<OWLPropertyAssertionObject>> dataPropertiesSubjectIndividuals;

    @Getter
    // Data property IRI -> set of literals
    private HashMap<IRI, Set<OWLPropertyAssertionObject>> dataPropertiesObjectValues;

    @Getter
    // Literals type -> set of literals
    private HashMap<IRI, Set<OWLPropertyAssertionObject>> literalsByType;

    @Getter
    // Object property -> <object -> set of subjects>
    private HashMap<IRI, HashMap<OWLPropertyAssertionObject, Set<OWLPropertyAssertionObject>>> objectToSubjectsOfObjectProperties;

    @Getter
    // Object property -> <subject -> set of objects>
    private HashMap<IRI, HashMap<OWLPropertyAssertionObject, Set<OWLPropertyAssertionObject>>> subjectToObjectsOfObjectProperties;

    @Getter
    // Data property -> <object -> set of subjects>
    private HashMap<IRI, HashMap<OWLPropertyAssertionObject, Set<OWLPropertyAssertionObject>>> objectToSubjectsOfDataProperties;

    @Getter
    // Data property -> <subject -> set of objects>
    private HashMap<IRI, HashMap<OWLPropertyAssertionObject, Set<OWLPropertyAssertionObject>>> subjectToObjectsOfDataProperties;

    @Getter
    private HashMap<IRI, Set<OWLObjectPropertyDomainAxiom>> objectPropertiesDomains;

    @Getter
    private HashMap<IRI, Set<OWLObjectPropertyRangeAxiom>> objectPropertiesRanges;

    @Getter
    private HashMap<IRI, Set<OWLDataPropertyDomainAxiom>> dataPropertiesDomains;

    @Getter
    private HashMap<IRI, Set<OWLDataPropertyRangeAxiom>> dataPropertiesRanges;

    private IRI baseIRI;

    public OntologyWrapper(OWLOntology ontology) {
        this.ontology = ontology;

        this.classes = new HashMap<>();
        this.individuals = new HashMap<>();
        this.dataProperties = new HashMap<>();
        this.objectProperties = new HashMap<>();
        this.individualsByClass = new HashMap<>();

        this.objectPropertiesSubjectIndividuals = new HashMap<>();
        this.objectPropertiesObjectIndividuals = new HashMap<>();

        this.dataPropertiesSubjectIndividuals = new HashMap<>();
        this.dataPropertiesObjectValues = new HashMap<>();

        this.literalsByType = new HashMap<>();

        this.objectToSubjectsOfObjectProperties = new HashMap<>();
        this.subjectToObjectsOfObjectProperties = new HashMap<>();

        this.objectToSubjectsOfDataProperties = new HashMap<>();
        this.subjectToObjectsOfDataProperties = new HashMap<>();

        this.objectPropertiesDomains = new HashMap<>();
        this.objectPropertiesRanges = new HashMap<>();
        this.dataPropertiesDomains = new HashMap<>();
        this.dataPropertiesRanges = new HashMap<>();

        this.init();
    }

    public IRI getBaseIRI() {
        if (this.baseIRI == null)
            this.baseIRI = IRI.create(this.classes.keySet().iterator().next().getNamespace());
        return this.baseIRI;
    }

    public Set<OWLPropertyAssertionObject> getIndividuals() {
        return new HashSet<>(this.individuals.values());
    }

    public Set<OWLClass> getClasses() {
        return new HashSet<>(this.classes.values());
    }

    public Set<OWLDataProperty> getDataProperties() {
        return new HashSet<>(this.dataProperties.values());
    }

    public Set<OWLObjectProperty> getObjectProperties() {
        return new HashSet<>(this.objectProperties.values());
    }

    public Set<OWLPropertyAssertionObject> getOntologyLiterals() {
        return this.objectToSubjectsOfDataProperties.values().stream().map(hashmap -> hashmap.keySet())
                .flatMap(keySet -> keySet.stream()).collect(Collectors.toSet());
    }

    public OWLNamedIndividual getIndividual(IRI iri) {
        return this.individuals.get(iri);
    }

    public OWLClass getClass(IRI iri) {
        return this.classes.get(iri);
    }

    public OWLDataProperty getDataProperty(IRI iri) {
        return this.dataProperties.get(iri);
    }

    public OWLObjectProperty getObjectProperty(IRI iri) {
        return this.objectProperties.get(iri);
    }

    public Set<OWLPropertyAssertionObject> getIndividualsOfClass(IRI classIRI) {
        return Optional.ofNullable(this.individualsByClass.get(classIRI)).orElse(new HashSet<>());
    }

    public Set<OWLPropertyAssertionObject> getSubjectOfDataProperty(IRI dataPropertyIRI) {
        return Optional.ofNullable(this.dataPropertiesSubjectIndividuals.get(dataPropertyIRI)).orElse(new HashSet<>());
    }

    public Set<OWLPropertyAssertionObject> getObjectOfDataProperty(IRI dataPropertyIRI) {
        return Optional.ofNullable(this.dataPropertiesObjectValues.get(dataPropertyIRI)).orElse(new HashSet<>());
    }

    public Set<OWLPropertyAssertionObject> getLiteralsOftype(IRI literalType) {
        return Optional.ofNullable(this.literalsByType.get(literalType)).orElse(new HashSet<>());
    }

    public Set<OWLPropertyAssertionObject> getAllLitterals() {
        return this.literalsByType.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    public Set<OWLPropertyAssertionObject> getSubjectOfObjectProperty(IRI objectPropertyIRI) {
        return Optional.ofNullable(this.objectPropertiesSubjectIndividuals.get(objectPropertyIRI)).orElse(new HashSet<>());
    }

    public Set<OWLPropertyAssertionObject> getObjectOfObjectProperty(IRI objectPropertyIRI) {
        return Optional.ofNullable(this.objectPropertiesObjectIndividuals.get(objectPropertyIRI)).orElse(new HashSet<>());
    }

    public Set<OWLPropertyAssertionObject> getSubjectForObjectOfObjectProperty(IRI atomIRI,
            OWLPropertyAssertionObject object) {
        if (object == null)
            return null;

        return this.objectToSubjectsOfObjectProperties.get(atomIRI).get(object);
    }

    public Set<OWLPropertyAssertionObject> getObjectForSubjectOfObjectProperty(IRI atomIRI,
            OWLPropertyAssertionObject subject) {
        if (subject == null)
            return null;

        return this.subjectToObjectsOfObjectProperties.get(atomIRI).get(subject);
    }

    public Set<OWLPropertyAssertionObject> getSubjectForObjectOfDataProperty(IRI atomIRI,
            OWLPropertyAssertionObject objectValue) {
        if (objectValue == null)
            return null;

        return this.objectToSubjectsOfDataProperties.get(atomIRI).get(objectValue);
    }

    public Set<OWLPropertyAssertionObject> getObjectForSubjectOfDataProperty(IRI atomIRI,
            OWLPropertyAssertionObject subject) {
        if (subject == null)
            return null;

        return this.subjectToObjectsOfDataProperties.get(atomIRI).get(subject);
    }

    public void saveOntology(String path) throws OWLOntologyStorageException, FileNotFoundException {
        this.ontology.saveOntology(new TurtleDocumentFormat(), new FileOutputStream(path));

        Logger.log("Ontology save at path: " + path);
    }

    public boolean alreadyInOntology(Atom atom) throws OWLAxiomConversionException {
        return this.ontology.containsAxiom(atom.toOWLAxiom());
    }

    public boolean addInferredAxioms(Set<OWLAxiom> axioms) {
        boolean addedAxioms = this.ontology.addAxioms(axioms).equals(ChangeApplied.SUCCESSFULLY);

        if (addedAxioms)
            this.updateCaches(axioms.stream().flatMap(a -> a.individualsInSignature()).collect(Collectors.toSet()));

        return addedAxioms;
    }

    public boolean isSymmetric(IRI iri) {
        if (this.objectProperties.containsKey(iri)) {
            OWLObjectProperty obProperty = this.objectProperties.get(iri);
            return this.ontology.getSymmetricObjectPropertyAxioms(obProperty).size() != 0;
        }

        return false;
    }

    @Override
    public String toString() {
        String str = "Classes : \n";
        for (OWLClass owlClass : this.getClasses())
            str += "    " + owlClass + "\n";
        str += "\n";

        HashMap<IRI, HashMap<OWLPropertyAssertionObject, Set<OWLPropertyAssertionObject>>> OPSubjectToObject = this
                .getSubjectToObjectsOfObjectProperties();
        HashMap<IRI, HashMap<OWLPropertyAssertionObject, Set<OWLPropertyAssertionObject>>> OPObjectToSubject = this
                .getObjectToSubjectsOfObjectProperties();
        str += "Object properties : \n";
        for (OWLObjectProperty objectProperty : this.getObjectProperties()) {
            str += "    " + objectProperty + "\n";

            str += "| (Subject to object) : " + OPSubjectToObject.get(objectProperty.getIRI()) + "\n";
            str += "| (Object to subject) : " + OPObjectToSubject.get(objectProperty.getIRI()) + "\n";
            str += "\n";
        }
        str += "\n";

        HashMap<IRI, HashMap<OWLPropertyAssertionObject, Set<OWLPropertyAssertionObject>>> DPSubjectToObject = this
                .getSubjectToObjectsOfDataProperties();
        HashMap<IRI, HashMap<OWLPropertyAssertionObject, Set<OWLPropertyAssertionObject>>> DPObjectToSubject = this
                .getObjectToSubjectsOfDataProperties();
        str += "Data properties : \n";
        for (OWLDataProperty dataProperty : this.getDataProperties()) {
            str += "    " + dataProperty + "\n";

            str += "| (Subject to object) : " + DPSubjectToObject.get(dataProperty.getIRI()) + "\n";
            str += "| (Object to subject) : " + DPObjectToSubject.get(dataProperty.getIRI()) + "\n";
            str += "\n";
        }
        str += "\n";

        str += "Individuals : \n";
        for (OWLPropertyAssertionObject individual : this.getIndividuals())
            str += "    " + individual + "\n";
        str += "\n";

        return str;
    }

    /**
     * Caches classes, datatypes and properties (i.e., doamins, ranges etc.)
     */
    private void init() {
        // Process classes
        ontology.classesInSignature().forEach(c -> this.classes.put(c.getIRI(), c));

        // Process object properties
        ontology.objectPropertiesInSignature()
                .filter(ob -> !ob.getIRI().equals(TOP_OBJECT_PROPERTY_IRI))
                .forEach(ob -> this.objectProperties.put(ob.getIRI(), ob));

        this.objectProperties.values().stream().forEach(op -> this.objectPropertiesDomains.put(op.getIRI(), this.ontology.getObjectPropertyDomainAxioms(op)));
        this.objectProperties.values().stream().forEach(op -> this.objectPropertiesRanges.put(op.getIRI(), this.ontology.getObjectPropertyRangeAxioms(op)));


        // Init subject and object caches for the current object property
        this.objectProperties.keySet().stream()
                .forEach(opIRI -> this.objectPropertiesSubjectIndividuals.put(opIRI, new HashSet<>()));
        this.objectProperties.keySet().stream()
                .forEach(opIRI -> this.objectPropertiesObjectIndividuals.put(opIRI, new HashSet<>()));

        this.objectProperties.keySet().stream()
                .forEach(opIRI -> this.subjectToObjectsOfObjectProperties.put(opIRI, new HashMap<>()));
        this.objectProperties.keySet().stream()
                .forEach(opIRI -> this.objectToSubjectsOfObjectProperties.put(opIRI, new HashMap<>()));

        // Process data properties
        ontology.dataPropertiesInSignature()
                .filter(dp -> !dp.getIRI().equals(TOP_DATA_PROPERTY_IRI))
                .forEach(dp -> this.dataProperties.put(dp.getIRI(), dp));

        this.dataProperties.values().stream().forEach(dp -> this.dataPropertiesDomains.put(dp.getIRI(), this.ontology.getDataPropertyDomainAxioms(dp)));
        this.dataProperties.values().stream().forEach(dp -> this.dataPropertiesRanges.put(dp.getIRI(), this.ontology.getDataPropertyRangeAxioms(dp)));

        // Init subject and object caches for the current data property
        this.dataProperties.keySet().stream()
                .forEach(dpIRI -> this.dataPropertiesSubjectIndividuals.put(dpIRI, new HashSet<>()));
        this.dataProperties.keySet().stream()
                .forEach(dpIRI -> this.dataPropertiesObjectValues.put(dpIRI, new HashSet<>()));

        this.dataProperties.keySet().stream()
                .forEach(dpIRI -> this.subjectToObjectsOfDataProperties.put(dpIRI, new HashMap<>()));
        this.dataProperties.keySet().stream()
                .forEach(dpIRI -> this.objectToSubjectsOfDataProperties.put(dpIRI, new HashMap<>()));

        this.updateCaches(this.ontology.getIndividualsInSignature());
    }

    private void updateCaches(Set<OWLNamedIndividual> individuals) {
        IRI iri;
        for (OWLNamedIndividual individual : individuals) {
            // Add individual to individuals cache
            this.individuals.put(individual.getIRI(), individual);

            // Process individuals classes
            for (OWLClassAssertionAxiom axiom : this.ontology.getClassAssertionAxioms(individual)) {
                iri = axiom.getClassExpression().asOWLClass().getIRI();

                // Init individuals by class cache
                if (!this.individualsByClass.containsKey(iri))
                    this.individualsByClass.put(iri, new HashSet<>());

                this.individualsByClass.get(iri).add(individual);
            }

            // Process object properties where the individuals is the subject
            for (OWLObjectPropertyAssertionAxiom axiom : this.ontology.getObjectPropertyAssertionAxioms(individual)) {
                iri = axiom.getProperty().asOWLObjectProperty().getIRI();

                if (iri.equals(TOP_OBJECT_PROPERTY_IRI))
                    continue;

                OWLPropertyAssertionObject object = axiom.getObject().asOWLNamedIndividual();

                // Subject to objects
                Set<OWLPropertyAssertionObject> objects = this.subjectToObjectsOfObjectProperties.get(iri)
                        .get(individual);
                if (objects == null) {
                    objects = new HashSet<>();
                    this.subjectToObjectsOfObjectProperties.get(iri).put(individual, objects);
                }

                // Object to subjects
                Set<OWLPropertyAssertionObject> subjects = this.objectToSubjectsOfObjectProperties.get(iri).get(object);
                if (subjects == null) {
                    subjects = new HashSet<>();
                    this.objectToSubjectsOfObjectProperties.get(iri).put(object, subjects);
                }

                // Add the individual in the object property's subject and object caches
                this.objectPropertiesSubjectIndividuals.get(iri).add(individual);
                this.objectPropertiesObjectIndividuals.get(iri).add(object);

                // Add subject<->object to caches for object properties
                this.subjectToObjectsOfObjectProperties.get(iri).get(individual).add(object);
                this.objectToSubjectsOfObjectProperties.get(iri).get(object).add(individual);
            }

            // Process data properties where the individuals is the subject
            for (OWLDataPropertyAssertionAxiom axiom : this.ontology.getDataPropertyAssertionAxioms(individual)) {
                iri = axiom.getProperty().asOWLDataProperty().getIRI();

                if (iri.equals(TOP_DATA_PROPERTY_IRI))
                    continue;

                OWLPropertyAssertionObject object = axiom.getObject();

                // Subject to objects
                Set<OWLPropertyAssertionObject> objects = this.subjectToObjectsOfDataProperties.get(iri)
                        .get(individual);
                if (objects == null) {
                    objects = new HashSet<>();
                    this.subjectToObjectsOfDataProperties.get(iri).put(individual, objects);
                }

                // Object to subjects
                Set<OWLPropertyAssertionObject> subjects = this.objectToSubjectsOfDataProperties.get(iri).get(object);
                if (subjects == null) {
                    subjects = new HashSet<>();
                    this.objectToSubjectsOfDataProperties.get(iri).put(object, subjects);
                }

                // Add the individual in the object property's subject cache
                this.dataPropertiesSubjectIndividuals.get(iri).add(individual);
                this.dataPropertiesObjectValues.get(iri).add(object);

                // Add subject<->object to caches for data properties
                this.subjectToObjectsOfDataProperties.get(iri).get(individual).add(object);
                this.objectToSubjectsOfDataProperties.get(iri).get(object).add(individual);

                // Cache the object in the literalsByType cache
                IRI owlLiteralIRI = ((OWLLiteral) object).getDatatype().getIRI();
                Set<OWLPropertyAssertionObject> literals = this.literalsByType.get(owlLiteralIRI);
                if (literals == null) {
                    literals = new HashSet<>();
                    this.literalsByType.put(owlLiteralIRI, literals);
                }
                literals.add(object);
            }
        }
    }
}
