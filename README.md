#Overview
TermGenie is a web application, which uses patterns and reasoning to create new classes for an ontology.

Originally this was devolped for the GeneOntology, but TermGenie uses a generic approach applicable to many ontologies.

#Publication

**TermGenie – a web-application for pattern-based ontology class generation**   
Heiko Dietze, Tanya Z. Berardini, Rebecca E. Foulger, David P. Hill, Jane Lomax, David Osumi-Sutherland3, Paola Roncaglia, and Christopher J. Mungall   
*Journal of Biomedical Semantics* 2014, **5**:48  doi:10.1186/2041-1480-5-48 Published: 11 December 2014 

[Link to JBMS open access article](http://www.jbiomedsem.com/content/5/1/48)

##Abstract
*Background*

Biological ontologies are continually growing and improving from requests for new classes (terms) by biocurators. These ontology requests can frequently create bottlenecks in the biocuration process, as ontology developers struggle to keep up, while manually processing these requests and create classes.

*Results*

TermGenie allows biocurators to generate new classes based on formally specified design patterns or templates. The system is web-based and can be accessed by any authorized curator through a web browser. Automated rules and reasoning engines are used to ensure validity, uniqueness and relationship to pre-existing classes. In the last 4 years the Gene Ontology TermGenie generated 4715 new classes, about 51.4% of all new classes created. The immediate generation of permanent identifiers proved not to be an issue with only 70 (1.4%) obsoleted classes.

*Conclusion*

TermGenie is a web-based class-generation system that complements traditional ontology development tools. All classes added through pre-defined templates are guaranteed to have OWL equivalence axioms that are used for automatic classification and in some cases inter-ontology linkage. At the same time, the system is simple and intuitive and can be used by most biocurators without extensive training. 

#Technical descriptions

For details on how to build TermGenie using Maven, run TermGenie in Eclipse for debugging, or a description of TermGenie and its modules, please check the wiki pages of the project.

#TermGenie Instances
Instances exist for the following ontologies:

* [GeneOntology TermGenie](http://go.termgenie.org)
* [OBA TermGenie](http://oba.termgenie.org)
* [CL TermGenie](http://cl.termgenie.org)
* [HP TermGenie](http://hp.termgenie.org)
* [MP TermGenie](http://mp.termgenie.org)
* [ENVO TermGenie](http://envo.termgenie.org)

To get write access to any of these TG instances, contact the administrators of the ontology (list available from obofoundry.org)
