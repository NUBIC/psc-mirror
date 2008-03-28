package edu.northwestern.bioinformatics.studycalendar.grid.client;

import edu.northwestern.bioinformatics.studycalendar.grid.common.StudyServiceI;
import edu.northwestern.bioinformatics.studycalendar.grid.stubs.StudyServicePortType;
import edu.northwestern.bioinformatics.studycalendar.grid.stubs.service.StudyServiceAddressingLocator;
import gov.nih.nci.cagrid.introduce.security.client.ServiceSecurityClient;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.axis.utils.ClassUtils;
import org.globus.gsi.GlobusCredential;
import org.oasis.wsrf.properties.GetResourcePropertyResponse;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * This class is autogenerated, DO NOT EDIT GENERATED GRID SERVICE METHODS.
 *
 * This client is generated automatically by Introduce to provide a clean unwrapped API to the
 * service.
 *
 * On construction the class instance will contact the remote service and retrieve it's security
 * metadata description which it will use to configure the Stub specifically for each method call.
 * 
 * @created by Introduce Toolkit version 1.0
 */
public class StudyServiceClient extends ServiceSecurityClient implements StudyServiceI {	
	protected StudyServicePortType portType;
	private Object portTypeMutex;

	public StudyServiceClient(String url) throws MalformedURIException, RemoteException {
		this(url,null);	
	}

	public StudyServiceClient(String url, GlobusCredential proxy) throws MalformedURIException, RemoteException {
	   	super(url,proxy);
	   	initialize();
	}
	
	public StudyServiceClient(EndpointReferenceType epr) throws MalformedURIException, RemoteException {
	   	this(epr,null);
	}
	
	public StudyServiceClient(EndpointReferenceType epr, GlobusCredential proxy) throws MalformedURIException, RemoteException {
	   	super(epr,proxy);
		initialize();
	}
	
	private void initialize() throws RemoteException {
	    this.portTypeMutex = new Object();
		this.portType = createPortType();
	}

	private StudyServicePortType createPortType() throws RemoteException {

		StudyServiceAddressingLocator locator = new StudyServiceAddressingLocator();
		// attempt to load our context sensitive wsdd file
		InputStream resourceAsStream = ClassUtils.getResourceAsStream(getClass(), "client-config.wsdd");
		if (resourceAsStream != null) {
			// we found it, so tell axis to configure an engine to use it
			EngineConfiguration engineConfig = new FileProvider(resourceAsStream);
			// set the engine of the locator
			locator.setEngine(new AxisClient(engineConfig));
		}
		StudyServicePortType port = null;
		try {
			port = locator.getStudyServicePortTypePort(getEndpointReference());
		} catch (Exception e) {
			throw new RemoteException("Unable to locate portType:" + e.getMessage(), e);
		}

		return port;
	}
	
	public GetResourcePropertyResponse getResourceProperty(QName resourcePropertyQName) throws RemoteException {
		return portType.getResourceProperty(resourcePropertyQName);
	}

	public static void usage(){
		System.out.println(StudyServiceClient.class.getName() + " -url <service url>");
	}
	
	public static void main(String [] args){
	    System.out.println("Running the Grid Service Client");
		try{
		if(!(args.length < 2)){
			if(args[0].equals("-url")){
			  StudyServiceClient client = new StudyServiceClient(args[1]);
			  // place client calls here if you want to use this main as a
			  // test....
			} else {
				usage();
				System.exit(1);
			}
		} else {
			usage();
			System.exit(1);
		}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata getServiceSecurityMetadata() throws RemoteException {
      synchronized(portTypeMutex){
        configureStubSecurity((Stub)portType,"getServiceSecurityMetadata");
        gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataRequest params = new gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataRequest();
        gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataResponse boxedResult = portType.getServiceSecurityMetadata(params);
        return boxedResult.getServiceSecurityMetadata();
      }
    }
	public edu.northwestern.bioinformatics.studycalendar.grid.Study retrieveStudyByAssignedIdentifier(java.lang.String assignedIdentifier) throws RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyDoesNotExistsException {
      synchronized(portTypeMutex){
        configureStubSecurity((Stub)portType,"retrieveStudyByAssignedIdentifier");
        edu.northwestern.bioinformatics.studycalendar.grid.stubs.RetrieveStudyByAssignedIdentifierRequest params = new edu.northwestern.bioinformatics.studycalendar.grid.stubs.RetrieveStudyByAssignedIdentifierRequest();
        params.setAssignedIdentifier(assignedIdentifier);
        edu.northwestern.bioinformatics.studycalendar.grid.stubs.RetrieveStudyByAssignedIdentifierResponse boxedResult = portType.retrieveStudyByAssignedIdentifier(params);
        return boxedResult.getStudy();
      }
    }
	public edu.northwestern.bioinformatics.studycalendar.grid.Study createStudy(edu.northwestern.bioinformatics.studycalendar.grid.Study study) throws RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyCreationException {
      synchronized(portTypeMutex){
        configureStubSecurity((Stub)portType,"createStudy");
        edu.northwestern.bioinformatics.studycalendar.grid.stubs.CreateStudyRequest params = new edu.northwestern.bioinformatics.studycalendar.grid.stubs.CreateStudyRequest();
        edu.northwestern.bioinformatics.studycalendar.grid.stubs.CreateStudyRequestStudy studyContainer = new edu.northwestern.bioinformatics.studycalendar.grid.stubs.CreateStudyRequestStudy();
        studyContainer.setStudy(study);
        params.setStudy(studyContainer);
        edu.northwestern.bioinformatics.studycalendar.grid.stubs.CreateStudyResponse boxedResult = portType.createStudy(params);
        return boxedResult.getStudy();
      }
    }

}
