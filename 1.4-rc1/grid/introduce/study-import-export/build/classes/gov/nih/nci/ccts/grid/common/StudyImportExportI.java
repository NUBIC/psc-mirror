package gov.nih.nci.ccts.grid.common;

import java.rmi.RemoteException;

/** 
 * This class is autogenerated, DO NOT EDIT.
 * 
 * This interface represents the API which is accessable on the grid service from the client. 
 * 
 * @created by Introduce Toolkit version 1.0
 * 
 */
public interface StudyImportExportI {

    public gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata getServiceSecurityMetadata() throws RemoteException ;

    public java.lang.String exportStudyByCoordinatingCenterIdentifier(java.lang.String string) throws RemoteException ;

}

