package gov.nih.nci.ccts.grid.service.globus;

import gov.nih.nci.ccts.grid.service.StudyImportExportImpl;

import java.rmi.RemoteException;

/** 
 * DO NOT EDIT:  This class is autogenerated!
 *
 * This class implements each method in the portType of the service.  Each method call represented
 * in the port type will be then mapped into the unwrapped implementation which the user provides
 * in the StudyImportExportImpl class.  This class handles the boxing and unboxing of each method call
 * so that it can be correclty mapped in the unboxed interface that the developer has designed and 
 * has implemented.  Authorization callbacks are automatically made for each method based
 * on each methods authorization requirements.
 * 
 * @created by Introduce Toolkit version 1.0
 * 
 */
public class StudyImportExportProviderImpl{
	
	StudyImportExportImpl impl;
	
	public StudyImportExportProviderImpl() throws RemoteException {
		impl = new StudyImportExportImpl();
	}
	

	public gov.nih.nci.ccts.grid.stubs.ExportStudyByCoordinatingCenterIdentifierResponse exportStudyByCoordinatingCenterIdentifier(gov.nih.nci.ccts.grid.stubs.ExportStudyByCoordinatingCenterIdentifierRequest params) throws RemoteException {
		StudyImportExportAuthorization.authorizeExportStudyByCoordinatingCenterIdentifier();
		gov.nih.nci.ccts.grid.stubs.ExportStudyByCoordinatingCenterIdentifierResponse boxedResult = new gov.nih.nci.ccts.grid.stubs.ExportStudyByCoordinatingCenterIdentifierResponse();
		boxedResult.setResponse(impl.exportStudyByCoordinatingCenterIdentifier(params.getString()));
		return boxedResult;
	}

}
