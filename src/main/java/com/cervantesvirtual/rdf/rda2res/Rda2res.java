package com.cervantesvirtual.rdf.rda2res;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DCTypes;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.cervantesvirtual.rdf.rda2res.NS.Formats;

public class Rda2res {
	
	public static boolean DEBUG_MODE = false;
	public static String DOWNLOAD_FOLDER = System.getProperty("user.home") + "/data/origin";
	public static String NEW_FILES_FOLDER = System.getProperty("user.home") + "/data/";
	
	public static String DOMAIN = "http://data.cervantesvirtual.com/";
	public static String DOWNLOAD_URL = "http://data.cervantesvirtual.com/getRDF?uri=";
	public static String INSTITUTION = "http://fundacion.cervantesvirtual.com";
	private String uri;
	private String identifier;
	
	private Model personModel;
	
	public Rda2res(String identifier, String uri){
		this.setUri(uri);
    	this.setIdentifier(identifier);
	}
	
	public static void main(String[] args) {
		
		String csvFile = "data/wikidataauthors.csv";
		FileManager.get().addLocatorClassLoader(Rda2res.class.getClassLoader());
		
        BufferedReader br = null;
        String line = "";
        String csvSplitter = ",";

        try {
            br = new BufferedReader(new InputStreamReader(FileManager.get().open(csvFile)));
            while ((line = br.readLine()) != null) {
                
                String[] person = line.split(csvSplitter);
                System.out.println("line:" + person[0] + person[1]);
                Rda2res rda2res = new Rda2res(person[0], person[1]);
                try{
                    rda2res.createRESFile();
                }catch(Exception e){
                	System.out.println("Error parsing :" + person[1] + ". " + e.getLocalizedMessage());
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
            	try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
	}
	
    public void createRESFile() {
    	    	
    	File rdfFile = new File( DOWNLOAD_FOLDER, "person-" + getIdentifier() + ".rdf");
    	
    	try {
			FileUtils.copyURLToFile(new URL(DOWNLOAD_URL + getUri()), rdfFile);
			
			setPersonModel(FileManager.get().loadModel(rdfFile.getPath(), null, "RDF/XML"));
	    	addNSPrefixes(getPersonModel());
	    	removeDCTermsProperties(getPersonModel(), getPersonModel().getResource(uri));
	    		        
	    	ResourceUtils.renameResource(getPersonModel().getResource(getUri()), getUri()+"#id");
	        addDocument(getPersonModel(), getUri());
	        addRDFFormat(getPersonModel(), getUri());
	        addHTMLFormat(getPersonModel(), getUri());
	        
	        ArrayList<String> subjectUris = addWorks(DC.subject);
	        ArrayList<String> authorUris = addWorks(getPersonModel().createProperty("http://rdaregistry.info/Elements/w/author"));
	        
            for(String s: subjectUris){
	    		ResourceUtils.renameResource(getPersonModel().getResource(s), s+"#id");
	    	}
	    	
	    	for(String s: authorUris){
	    	    ResourceUtils.renameResource(getPersonModel().getResource(s), s+"#id");
	    	}
	    	
	        String fileName = "new-model-person-" + getIdentifier() + ".rdf";
			writeRDF(getPersonModel(), fileName);
	        
		} catch (MalformedURLException e) {
			System.out.println("Error parsing file");
		} catch (IOException e) {
			System.out.println("Error parsing file");
		}
    }
    
    // remove original DCTerms properties which are not suitable por RES
    public void removeDCTermsProperties(Model model, Resource resource){
    	System.out.println("resource to delete:" + resource.getURI());
    	NodeIterator nodeIterator = model.listObjectsOfProperty(DCTerms.created);
        RDFNode foundToDelete = null;
        while (nodeIterator.hasNext()) {
            RDFNode next = nodeIterator.next();
            foundToDelete = next;
            break;
        }
        System.out.println("foundToDelete:" + foundToDelete.toString());
        model.remove(resource, DCTerms.created, foundToDelete);
        
        nodeIterator = model.listObjectsOfProperty(DCTerms.creator);
        foundToDelete = null;
        while (nodeIterator.hasNext()) {
            RDFNode next = nodeIterator.next();
            foundToDelete = next;
            break;
        }
        System.out.println("foundToDelete:" + foundToDelete.toString());
        model.remove(resource, DCTerms.creator, foundToDelete);
    }
    
    public void addNSPrefixes(Model model){
    	String DCTERMS = "http://purl.org/dc/terms/";
    	model.setNsPrefix("dcmi-terms",DCTERMS );
    	
    	String DC = "http://purl.org/dc/elements/1.1/";
    	model.setNsPrefix("dc",DC );
    	
    	String DCMI = "http://purl.org/dc/dcmitype/";
    	model.setNsPrefix("dcmitype",DCMI );
    	
    	String FOAF = "http://xmlns.com/foaf/0.1/";
    	model.setNsPrefix("foaf",FOAF );
    	
    	String FORMATS = "http://www.w3.org/ns/formats/";
    	model.setNsPrefix("formats",FORMATS );
    	
    	String ISNI = "http://isni.org/ontology#";
    	model.setNsPrefix("isni", ISNI);
    	
    	String OWL = "http://www.w3.org/2002/07/owl#";
    	model.setNsPrefix("owl", OWL);
    	
    	String RDAA = "http://rdaregistry.info/Elements/a/";
    	model.setNsPrefix("rdaa", RDAA);
    	
    	String RDAC = "http://rdaregistry.info/Elements/c/";
    	model.setNsPrefix("rdac", RDAC);
    	
    	String RDAW = "http://rdaregistry.info/Elements/w/";
    	model.setNsPrefix("rdaw", RDAW);
    	
    	String RDAE = "http://rdaregistry.info/Elements/e/";
    	model.setNsPrefix("rdae", RDAE);
    	
    	String RDAM = "http://rdaregistry.info/Elements/m/";
    	model.setNsPrefix("rdam", RDAM);
    	
    	String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    	model.setNsPrefix("rdfs", RDFS);
    	
    	String SCHEMA = "http://schema.org/";
    	model.setNsPrefix("schema", SCHEMA);

    	String XSD = "http://www.w3.org/2001/XMLSchema#";
    	model.setNsPrefix("xsd", XSD);
    	
    	if (DEBUG_MODE){
	    	for (Map.Entry<String, String> entry : model.getNsPrefixMap().entrySet()){
	            System.out.println(entry.getKey() + "/" + entry.getValue());
	        }
    	}
    }
    
    public void writeRDF(Model model, String fileName) throws IOException{
    	FileWriter out = new FileWriter(NEW_FILES_FOLDER + fileName );
    	try {
    	    model.write( out, "RDF/XML" ); //TTL, "RDF/XML-ABBREV"
    	}
    	finally {
    	   try {
    	       out.close();
    	   }
    	   catch (IOException closeException) {
    	       // ignore
    	   }
    	}
    }
    public void addDocument(Model model, String uri){
    	
    	System.out.println("#### addDocument : " + uri + "#id ####");
    	Literal label = model.createLiteral( "Data about '" + model.getResource(uri+"#id").getProperty(RDFS.label).getLiteral() + "'", "en-GB" );
        
        Calendar cal = GregorianCalendar.getInstance();
        Literal created = model.createTypedLiteral(cal);
        
    	model.createResource(uri)
        .addProperty(RDF.type, FOAF.Document)
        .addLiteral(RDFS.label, label)
        .addProperty(FOAF.primaryTopic, model.createResource(uri+"#id"))
        .addProperty(DCTerms.created, created)
        .addProperty(DCTerms.creator, model.createResource(INSTITUTION))
        .addProperty(DCTerms.license, model.createResource("http://creativecommons.org/publicdomain/zero/1.0/"))
        .addProperty(DCTerms.hasFormat, model.createResource(uri + ".rdf"));
    	
    	System.out.println("#### FINISH addDocument : " + uri + "#id ####");
    }
    
    public void addRDFFormat(Model model, String uri){
    	
    	System.out.println("#### addRDFFormat : " + uri + "#id ####");
    	Literal label = model.createLiteral( "Data about '" + model.getResource(uri+"#id").getProperty(RDFS.label).getLiteral() + "' as RDF/XML", "en-GB" );
               
    	model.createResource(uri+".rdf")
        .addProperty(RDF.type, DCTypes.Text)
        .addProperty(RDF.type, model.createResource(Formats.RDF_XML))
        .addLiteral(RDFS.label, label)
        .addProperty(DCTerms.license, model.createResource("http://creativecommons.org/publicdomain/zero/1.0/"))
        .addProperty(DCTerms.format, model.createResource("http://purl.org/NET/mediatypes/application/rdf+xml"));
    	
    	System.out.println("#### FINISH addRDFFormat : " + uri + "#id ####");
    }
    
    public void addHTMLFormat(Model model, String uri){
    	
    	System.out.println("#### addHTMLFormat : " + uri + "#id ####");
    	Literal label = model.createLiteral( "Data about '" + model.getResource(uri+"#id").getProperty(RDFS.label).getLiteral() + "' as HTML", "en-GB" );
               
    	model.createResource(uri+".html")
        .addProperty(RDF.type, DCTypes.Text)
        .addLiteral(RDFS.label, label)
        .addProperty(DCTerms.license, model.createResource("http://creativecommons.org/publicdomain/zero/1.0/"))
        .addProperty(DCTerms.format, model.createResource("http://purl.org/NET/mediatypes/text/html"));
    	
    	System.out.println("#### FINISH addHTMLFormat : " + uri + "#id ####");
    }
    
    public ArrayList<String> addWorks(Property property){
    	ArrayList<String> subjectUris = new ArrayList<String>();
    	System.out.println("#### addWorks uri: " + uri + "#id property:" + property.toString() + "####");
    	
    	StmtIterator stmtIterator = personModel.listStatements(null, property, personModel.createResource(uri+"#id"));
    	
    	while (stmtIterator.hasNext()){
    	    Statement s = stmtIterator.nextStatement();
    	    
    	    if(s.getSubject().isResource() /*&& s.getSubject().asResource().toString().equals("http://data.cervantesvirtual.com/work/5372")*/){
    	    	subjectUris.add(s.getSubject().getURI());
    	    	String workIdentifier = s.getSubject().asResource().toString().replace("http://", "").replaceAll("/", "-");
    	    	
    	    	String newModelFileName = "new-model-work-" + workIdentifier + ".rdf";
    	    	File newModelFile = new File( NEW_FILES_FOLDER, newModelFileName);
    	    	
    	    	File rdfWorkFile = new File( DOWNLOAD_FOLDER, workIdentifier + ".rdf");
    	    	
    	    	// possibility of several authors for the same work
    	    	Model workModel = null;
    	    	if(!newModelFile.exists()){
	    	    	try {
	    	    		System.out.println("### Work file does not exists: " + s.getSubject().toString() + " ###");
	    				FileUtils.copyURLToFile(new URL(DOWNLOAD_URL + s.getSubject().toString()), rdfWorkFile);
	    				workModel = FileManager.get().loadModel(rdfWorkFile.getPath(), null, "RDF/XML");
	    				addNSPrefixes(workModel);
	    				removeDCTermsProperties(workModel, workModel.createResource(s.getSubject().getURI()));
	    				
	    				// change #id work
	        	        ResourceUtils.renameResource(workModel.getResource(s.getSubject().toString()), s.getSubject().toString()+"#id");
	        	        // change #id person in personModel already has #id, but not in workModel
	        	        ResourceUtils.renameResource(workModel.getResource(s.getObject().toString().replace("#id", "")), s.getObject().toString());
	        	        
	        	        addDocument(workModel, s.getSubject().asResource().toString());
	        	        addRDFFormat(workModel, s.getSubject().asResource().toString());
	        	        addHTMLFormat(workModel, s.getSubject().asResource().toString());
	        	        
	        	    } catch (MalformedURLException e) {
	    				System.out.println("MalformedURLException: " + e.getMessage());
	    			} catch (IOException e) {
	    				System.out.println("IOException: " + e.getMessage());
	    			}
    	    	}
    	    	else{
    	    		System.out.println("### Work file exists: " + s.getSubject().toString() + " ###" + s.getObject().toString());
    	    		workModel = FileManager.get().loadModel(newModelFile.getPath(), null, "RDF/XML");
    	    		
    	    		// change #id work
    	    		if(s.getSubject().toString().endsWith("#id"))
        	            ResourceUtils.renameResource(workModel.getResource(s.getSubject().toString()), s.getSubject().toString()+"#id");
        	        
    	    		// change #id person in personModel already has #id, but not in workModel
        	        ResourceUtils.renameResource(workModel.getResource(s.getObject().toString().replace("#id", "")), s.getObject().toString());
        	    }
    	    	
    	    	try {
    	        	writeRDF(workModel, newModelFileName);
    			} catch (IOException e) {
    				System.out.println("Error parsing workModel: " + e.getMessage());
    			}
    	    	
    	    	System.out.println("#### FINISH addWorks : " + uri + "#id ####");
    	    }
    	}
    	
    	return subjectUris;
    }

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Model getPersonModel() {
		return personModel;
	}

	public void setPersonModel(Model personModel) {
		this.personModel = personModel;
	} 

}
