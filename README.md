<a href="http://data.cervantesvirtual.com/"><img src=http://data.cervantesvirtual.com/blog/wp-content/uploads/2017/05/fbvmc.png></a> 

# rda2res
Exporting from RDA (Resource Description and Access) to RES (Research and Education Space) from BBC.

## Introduction
Since 2013, the <a href="https://bbcarchdev.github.io/res/" target="_blank">RES project</a> has been working with teachers, students and academics, along with curators of world class digital collections and product developers to improve the resources and tools available in all levels of education. The project was a partnership initiative between the <a href="http://www.bbc.co.uk/" target="_blank">BBC</a>, <a href="http://bufvc.ac.uk/" target="_blank">Learning on Screen</a> and <a href="https://www.jisc.ac.uk/" target="_blank">Jisc</a>. 

<a href="https://bbcarchdev.github.io/inside-acropolis/#res-intro" target="_blank><img src=https://bbcarchdev.github.io/inside-acropolis/powered-by-res-icon-green-text.jpeg></a> 

RES provides an open platform built by the BBC. The platform indexes and organises the digital collections of libraries, museums, broadcasters and galleries to make their content more discoverable, accessible and usable to those in UK education and research. Images, TV and radio programmes, documents and text from world class organisations such as The British Museum, British Library, The National Archives, Europeana, Wellcome Trust and the BBC are all being indexed by RES. RES also enables developers to create digital educational products that will inspire learners, teachers and researchers by using applications powered by the RES platform.

The catalogue of the Biblioteca Virtual Miguel de Cervantes contains more than 200,000 records which were originally created in compliance with the MARC21 standard. The entries in the catalogue have been recently migrated to a new relational database whose data model adheres to the conceptual models promoted by the International Federation of Library Associations and Institutions (IFLA), in particular, to the FRBR and FRAD specifications. The database content has been later mapped, by means of an automated procedure, to RDF triples which employ basically the <a href="www.rdaregistry.info/" target="_blank">RDA vocabulary (Resource Description and Access)</a> to describe the entities, as well as their properties and relationships. This RDF-based semantic description of the catalogue is now accessible online through an <a href="http://data.cervantesvirtual.com">interface</a> which supports browsing and searching the information. Due to their open nature, these public data can be easily linked and used for new applications created by external developers and institutions. More information can be found at <a href="http://www.semantic-web-journal.net/content/migration-library-catalogue-rda-linked-open-data-0" target="_blank">Semantic Web Journal</a>.

## Publishing data for RES
Publishers wishing to make their data visible in the Acropolis index and useable by RES applications must conform to a small set of basic requirements. These are:

* The data must be expressed as RDF and published as Linked Open Data;
* the data must be licensed under permissive terms (in particular, it must allow re-use in both commercial and non-commercial applications);
* the licensing terms must be included in the data itself so that consumers can perform automated due diligence before using it;
* the data should use the vocabularies described in this book for best results (although you are free to use other vocabularies too).

## Rda2Res tool
This Java code prepares your data for RES. In order to make the transformation, the data provided must be defined using the RDA vocabulary, and automatically the data is transformed and curated for RES.

The [Rda2res.java class](src/main/java/com/cervantesvirtual/rdf/rda2res/Rda2res.java) downloads the original RDA documents and stores them in a folder called {home.user}/data/origin. At the beginning, the code defines the DOMAIN and DOWNLOADURL which should be set with the correct values for your institution. Then, the new files created by the transformation process will be stored at {home.user}/data/. The transformed files should be ingested in a Triple store such as Virtuoso or RDF4J.



