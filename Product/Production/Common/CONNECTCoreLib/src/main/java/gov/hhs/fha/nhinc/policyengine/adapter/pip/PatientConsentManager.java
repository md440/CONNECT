/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services. 
 * All rights reserved. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met: 
 *     * Redistributions of source code must retain the above 
 *       copyright notice, this list of conditions and the following disclaimer. 
 *     * Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the documentation 
 *       and/or other materials provided with the distribution. 
 *     * Neither the name of the United States Government nor the 
 *       names of its contributors may be used to endorse or promote products 
 *       derived from this software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY 
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package gov.hhs.fha.nhinc.policyengine.adapter.pip;

import gov.hhs.fha.nhinc.common.nhinccommonadapter.FineGrainedPolicyMetadataType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.BinaryDocumentPolicyCriteriaType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.BinaryDocumentPolicyCriterionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.BinaryDocumentStoreActionType;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import org.hl7.v3.POCDMT000040ClinicalDocument;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.PatientPreferencesType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import ihe.iti.xds_b._2007.DocumentRegistryService;
import ihe.iti.xds_b._2007.DocumentRegistryPortType;
import ihe.iti.xds_b._2007.DocumentRepositoryPortType;
import ihe.iti.xds_b._2007.DocumentRepositoryService;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType.DocumentRequest;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.DocumentResponse;
import java.util.ArrayList;
import oasis.names.tc.ebxml_regrep.xsd.lcm._3.SubmitObjectsRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExternalIdentifierType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.IdentifiableType;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyType;
import org.hl7.v3.II;

/**
 * This class manages the patient consent form. It stores or retrieves the patient consent document from the repository.
 * 
 * @author Les Westberg
 */
public class PatientConsentManager {

    private static Log log = LogFactory.getLog(PatientConsentManager.class);
    private static DocumentRegistryService oDocRegService = null;
    private static DocumentRepositoryService oDocRepService = null;
    private static final String ADAPTER_PROPFILE_NAME = "adapter";
    private static final String XDS_HC_VALUE = "XDSbHomeCommunityId";
    private static final String XACML_MIME_TYPE = "text/xml";
    private static final String PDF_MIME_TYPE = "application/pdf";

    /**
     * Return a handle to the document registry port.
     * 
     * @return The handle to the document registry port web service.
     */
    public DocumentRegistryPortType getDocumentRegistryPort() throws AdapterPIPException {
        DocumentRegistryPortType oDocRegistryPort = null;

        try {
            if (oDocRegService == null) {
                oDocRegService = new DocumentRegistryService();
            }

            oDocRegistryPort = oDocRegService.getDocumentRegistryPortSoap();

            // Get the real endpoint URL for this service.
            // --------------------------------------------
            String sEndpointURL = "";
            String xdsHomeCommunityId = PropertyAccessor.getInstance().getProperty(ADAPTER_PROPFILE_NAME, XDS_HC_VALUE);
            if (xdsHomeCommunityId != null && !xdsHomeCommunityId.equals("")) {
                sEndpointURL = ConnectionManagerCache.getInstance().getDefaultEndpointURLByServiceName(xdsHomeCommunityId,
                        CDAConstants.DOC_REGISTRY_SERVICE_NAME);
            } else {
                sEndpointURL = ConnectionManagerCache.getInstance().getInternalEndpointURLByServiceName(
                        CDAConstants.DOC_REGISTRY_SERVICE_NAME);
            }

            if ((sEndpointURL == null) || (sEndpointURL.length() <= 0)) {
                String sErrorMessage = "Failed to retrieve the Endpoint URL for service: '"
                        + CDAConstants.DOC_REGISTRY_SERVICE_NAME + "'.  " + "Setting this to: '" + sEndpointURL + "'";
                log.warn(sErrorMessage);
            }
            ((javax.xml.ws.BindingProvider) oDocRegistryPort).getRequestContext().put(
                    javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, sEndpointURL);
        } catch (Exception e) {
            String sErrorMessage = "Failed to retrieve a handle to the Document Registry web service.  Error: "
                    + e.getMessage();
            log.error(sErrorMessage, e);
            throw new AdapterPIPException(sErrorMessage, e);
        }

        return oDocRegistryPort;
    }

    /**
     * Return a handle to the document repository port.
     * 
     * @return The handle to the document registry port web service.
     */
    private DocumentRepositoryPortType getDocumentRepositoryPort() throws AdapterPIPException {
        DocumentRepositoryPortType oDocRepositoryPort = null;

        try {
            if (oDocRepService == null) {
                oDocRepService = new DocumentRepositoryService();
            }

            // oDocRepositoryPort = oDocRepService.getDocumentRepositoryPortSoap();
            oDocRepositoryPort = oDocRepService.getDocumentRepositoryPortSoap();

            // Get the real endpoint URL for this service.
            // --------------------------------------------
            String sEndpointURL = "";
            String xdsHomeCommunityId = PropertyAccessor.getInstance().getProperty(ADAPTER_PROPFILE_NAME, XDS_HC_VALUE);
            if (xdsHomeCommunityId != null && !xdsHomeCommunityId.equals("")) {
                sEndpointURL = ConnectionManagerCache.getInstance().getDefaultEndpointURLByServiceName(xdsHomeCommunityId,
                        CDAConstants.DOC_REPOSITORY_SERVICE_NAME);
            } else {
                sEndpointURL = ConnectionManagerCache.getInstance().getInternalEndpointURLByServiceName(
                        CDAConstants.DOC_REPOSITORY_SERVICE_NAME);
            }

            if ((sEndpointURL == null) || (sEndpointURL.length() <= 0)) {
                String sErrorMessage = "Failed to retrieve the Endpoint URL for service: '"
                        + CDAConstants.DOC_REPOSITORY_SERVICE_NAME + "'.  " + "Setting this to: '" + sEndpointURL + "'";
                log.warn(sErrorMessage);
            }
            ((javax.xml.ws.BindingProvider) oDocRepositoryPort).getRequestContext().put(
                    javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, sEndpointURL);
        } catch (Exception e) {
            String sErrorMessage = "Failed to retrieve a handle to the Document Repository web service.  Error: "
                    + e.getMessage();
            log.error(sErrorMessage, e);
            throw new AdapterPIPException(sErrorMessage, e);
        }

        return oDocRepositoryPort;
    }

    /**
     * This method saves the patient consent information to the document repository. It will overwrite anything that is
     * currently there.
     * 
     * @param oPtPref The patient consent preferences.
     * @throws gov.hhs.fha.nhinc.policyengine.adapterpip.AdapterPIPException This exception is thrown if there is an
     *             error storing.
     */
    public void storePatientConsent(PatientPreferencesType oPtPref) throws AdapterPIPException, PropertyAccessException {

        log.info("------ Begin PatientConsentManager.storePatientConsent() ------");
        // PatientPreferences contains a FineGrainedPolicyCriteria and possibly
        // multiple BinaryDocumentPolicyCriteria.
        // ---------------------------------------------------------------------
        if (oPtPref != null) {

            // Create the document
            // This also takes care of the case where there is only the
            // OptIn\OptOut setting and no fine grained policy
            PolicyType oConsentXACML = null;
            XACMLCreator oCreator = new XACMLCreator();
            oConsentXACML = oCreator.createConsentXACMLDoc(oPtPref);
            log.info("Created XACML Doc with policy OID: " + oConsentXACML.getPolicyId());

            String sDocOID = null;
            if ((oPtPref != null) && (oPtPref.getAssigningAuthority() != null) && (oPtPref.getPatientId() != null)) {
                sDocOID = retrievePatientConsentDocumentIdByPatientId(oPtPref.getPatientId(),
                        oPtPref.getAssigningAuthority());
                log.info("storePatientConsent - sDocOID: " + sDocOID);
            }

            if (sDocOID == null) {
                sDocOID = oConsentXACML.getPolicyId();
            }

            XACMLSerializer oSerializer = new XACMLSerializer();
            String sConsentXACML = oSerializer.serializeConsentXACMLDoc(oConsentXACML);

            // Uses Repository Services
            storeCPPToRepositoryUsingXDSb(oPtPref, sConsentXACML, sDocOID, XACML_MIME_TYPE);

            // Next handle the storage of the BinaryDocumentPolicyCriteria docs
            // if any have a store action set to add or update
            if (oPtPref.getBinaryDocumentPolicyCriteria() != null
                    && oPtPref.getBinaryDocumentPolicyCriteria().getBinaryDocumentPolicyCriterion() != null
                    && !oPtPref.getBinaryDocumentPolicyCriteria().getBinaryDocumentPolicyCriterion().isEmpty()) {

                List<String> olDocIds2Store = new ArrayList<String>();
                List<BinaryDocumentPolicyCriterionType> olBinPolicyCriteria = oPtPref.getBinaryDocumentPolicyCriteria()
                        .getBinaryDocumentPolicyCriterion();
                for (BinaryDocumentPolicyCriterionType oBinPolicyCriterion : olBinPolicyCriteria) {
                    if (oBinPolicyCriterion != null && oBinPolicyCriterion.getStoreAction() != null) {
                        if (oBinPolicyCriterion.getStoreAction().compareTo(BinaryDocumentStoreActionType.ADD) == 0
                                || oBinPolicyCriterion.getStoreAction().compareTo(BinaryDocumentStoreActionType.UPDATE) == 0) {
                            log.info(oBinPolicyCriterion.getStoreAction() + " requested for document: "
                                    + oBinPolicyCriterion.getDocumentUniqueId());
                            olDocIds2Store.add(oBinPolicyCriterion.getDocumentUniqueId());
                        }
                        // TODO how do we handle the delete request ?
                    }
                }

                if (olDocIds2Store != null && !olDocIds2Store.isEmpty()) {
                    CdaPdfCreator oPdfCreator = new CdaPdfCreator();
                    CdaPdfSerializer oPdfSerializer = new CdaPdfSerializer();
                    List<POCDMT000040ClinicalDocument> olPdfs = oPdfCreator.createCDA(oPtPref);
                    if (olPdfs != null && !olPdfs.isEmpty()) {
                        for (POCDMT000040ClinicalDocument oCda : olPdfs) {
                            if (oCda != null && oCda.getId() != null && oCda.getId().getExtension() != null
                                    && !oCda.getId().getExtension().isEmpty()) {
                                // The CDA ID will reflect the document unique id in the extension part of the II
                                // This can be used to determine which documents to store
                                String sDocUniqueID = oCda.getId().getExtension();
                                if (olDocIds2Store.contains(sDocUniqueID)) {
                                    String sCda = oPdfSerializer.serialize(oCda);
                                    storeCPPToRepositoryUsingXDSb(oPtPref, sCda, sDocUniqueID, PDF_MIME_TYPE);
                                }
                            }
                        }
                    }
                } else {
                    log.info("No Binary Policy Documents required updating or adding.");
                }
            } else {
                log.info("No Binary Policy Documents are contained in the patient preferences.");
            }

        } else {
            String sErrorMessage = "failed to store patient consent.  The patient preference was null.";
            log.error(sErrorMessage);
            throw new AdapterPIPException(sErrorMessage);
        }
        log.info("------ End PatientConsentManager.storePatientConsent() ------");
    }

    /**
     * This method stores the patient preference document to the repository.
     * 
     * @param oPtPref The patient preference information.
     * @param sPrefDoc The consent document.
     * @throws gov.hhs.fha.nhinc.policyengine.adapterpip.AdapterPIPException This exception is thrown if there is an
     *             error.
     */
    private void storeCPPToRepositoryUsingXDSb(PatientPreferencesType oPtPref, String sPrefDoc,
            String sUniqueDocumentId, String sMimeType) throws AdapterPIPException, PropertyAccessException {
        log.info("------ Begin PatientConsentManager.storeCPPToRepositoryUsingXDSb() ------");
        log.info("Request to store Document: " + sUniqueDocumentId);
        if ((oPtPref == null) || (oPtPref.getPatientId() == null) || (oPtPref.getPatientId().trim().length() <= 0)) {
            String sErrorMessage = "failed to store patient consent.  The patient ID was null or blank.";
            log.error(sErrorMessage);
            throw new AdapterPIPException(sErrorMessage);
        } else {
            saveCPPDoc(sPrefDoc, oPtPref, sUniqueDocumentId, sMimeType);
        }

        log.info("------ End PatientConsentManager.storeCPPToRepositoryUsingXDSb() ------");
    }

    /**
     * 
     * @param sPrefDoc
     * @param oPtPref
     * @return boolean
     * @throws gov.hhs.fha.nhinc.policyengine.adapterpip.AdapterPIPException
     */
    private void saveCPPDoc(String sPrefDoc, PatientPreferencesType oPtPref, String sUniqueDocumentId, String sMimeType)
            throws AdapterPIPException, PropertyAccessException {
        log.info("------ Begin PatientConsentManager.saveCPPDoc ------");

        // Create a raw data document to be stored
        ProvideAndRegisterDocumentSetRequestType oRequest = new ProvideAndRegisterDocumentSetRequestType();
        ProvideAndRegisterDocumentSetRequestType.Document oDoc = createDocumentRawData(sPrefDoc, sUniqueDocumentId);
        log.info("Raw document created with ID: " + sUniqueDocumentId);
        oRequest.getDocument().add(oDoc);

        if ((oDoc != null) && (oDoc.getValue() != null) & (oPtPref != null)) {
            if (oPtPref.getFineGrainedPolicyMetadata() == null) {
                oPtPref.setFineGrainedPolicyMetadata(new FineGrainedPolicyMetadataType());
            }
            oPtPref.getFineGrainedPolicyMetadata().setSize(String.valueOf(oDoc.getValue().length));
        }

        // If a matching document id is found the target will not be empty
        String sTargetObject = checkCPPMetaFromRepositoryUsingXDSb(oPtPref.getPatientId(),
                oPtPref.getAssigningAuthority(), sUniqueDocumentId, sMimeType);
        String sHomeCommunityId = PropertyAccessor.getInstance().getProperty("gateway", "localHomeCommunityId");
        PatientConsentDocumentBuilderHelper oPatConsentDocBuilderHelper = new PatientConsentDocumentBuilderHelper();
        SubmitObjectsRequest oSubmitObjectRequest = oPatConsentDocBuilderHelper.createSubmitObjectRequest(
                sTargetObject, sHomeCommunityId, sUniqueDocumentId, sMimeType, oPtPref);
        oRequest.setSubmitObjectsRequest(oSubmitObjectRequest);
        DocumentRepositoryPortType oDocRepositoryPort = getDocumentRepositoryPort();
        RegistryResponseType oRegistryResponse = oDocRepositoryPort
                .documentRepositoryProvideAndRegisterDocumentSetB(oRequest);
        if (oRegistryResponse != null && oRegistryResponse.getStatus() != null
                && !oRegistryResponse.getStatus().equals("")) {
            log.info("Patient Consent Document saved to repository Successfully");
        } else {
            throw new AdapterPIPException("Unable to Save patient Consent Document"
                    + oRegistryResponse.getRegistryErrorList().getRegistryError().get(0).getValue());
        }

        log.info("------ End PatientConsentManager.saveCPPDoc ------");
    }

    /**
     * This method is used internal by saveCPPDoc creates Document with rawData and document unique id
     * 
     * @param sPrefDoc
     * @param sDocUniqueId
     * @return ProvideAndRegisterDocumentSetRequestType.Document
     */
    private ProvideAndRegisterDocumentSetRequestType.Document createDocumentRawData(String sPrefDoc, String sDocUniqueId) {
        log.info("------ Begin PatientConsentManager.createDocumentRawData ------");
        ProvideAndRegisterDocumentSetRequestType.Document oDoc = new ProvideAndRegisterDocumentSetRequestType.Document();
        oDoc.setId(sDocUniqueId);
        oDoc.setValue(sPrefDoc.getBytes());
        log.info("------ End PatientConsentManager.createDocumentRawData ------");
        return oDoc;
    }

    /**
     * To verify a XAML Document already persists in database
     * 
     * @param sPatientId The Patient identifier
     * @param sAssigningAuthority The assigning authority
     * @return boolean
     * @throws gov.hhs.fha.nhinc.policyengine.adapterpip.AdapterPIPException
     */
    private String checkCPPMetaFromRepositoryUsingXDSb(String sPatientId, String sAssigningAuthority,
            String sUniqueDocumentId, String sMimeType) throws AdapterPIPException {
        log.info("--------------- Begin checkCPPMetaFromRepositoryUsingXDSb ---------------");
        String sTargetObject = "";
        DocumentRegistryPortType oDocRegistryPort = getDocumentRegistryPort();

        AdhocQueryResponse oResponse = null;
        AdhocQueryRequest oRequest = new QueryUtil().createAdhocQueryRequest(sPatientId, sAssigningAuthority);
        oResponse = oDocRegistryPort.documentRegistryRegistryStoredQuery(oRequest);

        if (oResponse != null && oResponse.getRegistryObjectList() != null
                && oResponse.getRegistryObjectList().getIdentifiable() != null
                && oResponse.getRegistryObjectList().getIdentifiable().size() > 0) {
            List<JAXBElement<? extends IdentifiableType>> olRegObjs = oResponse.getRegistryObjectList()
                    .getIdentifiable();
            String sFoundXACMLDoc = "";
            foundLabel: for (JAXBElement<? extends IdentifiableType> oJAXBObj : olRegObjs) {
                if ((oJAXBObj != null)
                        && (oJAXBObj.getDeclaredType() != null)
                        && (oJAXBObj.getDeclaredType().getCanonicalName() != null)
                        && (oJAXBObj.getDeclaredType().getCanonicalName()
                                .equals("oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType"))
                        && (oJAXBObj.getValue() != null)) {
                    ExtrinsicObjectType oExtObj = (ExtrinsicObjectType) oJAXBObj.getValue();
                    String sRepoObjMimeType = oExtObj.getMimeType();
                    log.info("Evaluate id: " + oExtObj.getId() + " of type: " + sRepoObjMimeType);

                    // If we are looking for a XACML type and this is of that type
                    // save it as a possiblity
                    if (XACML_MIME_TYPE.equals(sRepoObjMimeType) && XACML_MIME_TYPE.equals(sMimeType)) {
                        sFoundXACMLDoc = oExtObj.getId();
                    }

                    // Get found Document Unique ID
                    // -----------------------------
                    if ((oExtObj.getExternalIdentifier() != null) && (oExtObj.getExternalIdentifier().size() > 0)) {
                        List<ExternalIdentifierType> olExtId = oExtObj.getExternalIdentifier();
                        for (ExternalIdentifierType oExtId : olExtId) {
                            if ((oExtId.getIdentificationScheme() != null)
                                    && (oExtId.getIdentificationScheme().equals(CDAConstants.DOCUMENT_ID_IDENT_SCHEME))
                                    && (oExtId.getValue() != null) && (oExtId.getValue().length() > 0)) {
                                String sDocumentId = oExtId.getValue().trim();

                                // If this matches the document id that we are
                                // looking for save the target id and exit
                                // ---------------------------------------------
                                if (sDocumentId.equals(sUniqueDocumentId)) {
                                    sTargetObject = oExtObj.getId();
                                    log.info("Matching document id: " + sDocumentId + " sets target: " + sTargetObject);
                                    break foundLabel;
                                } else {
                                    log.info("Non-matching document id: " + sDocumentId);
                                }
                            }
                        } // for (ExternalIdentifierType oExtid : olExtId)
                    } // if ((oExtObj.getExternalIdentifier() != null) &&
                }
            }

            if (sTargetObject.isEmpty() && !sFoundXACMLDoc.isEmpty()) {
                // XACML documents may be created with a generated UID
                // It may still replace a stored document if one is found of that type
                sTargetObject = sFoundXACMLDoc;
            }
        }
        log.info("--------------- End checkCPPMetaFromRepositoryUsingXDSb ---------------");
        return sTargetObject;
    }

    /**
     * This method retrieves the patient consent information from the repository based on a document Id. It does this by
     * first using the document ID to retrieve the patient ID. Then it uses the patient ID to retrieve the consent
     * information.
     * 
     * @param sPatientId The ID of the patient.
     * @param sAssigningAuthority The assigning authority associated with the patient ID. Currently it is not really
     *            used, but here if it is needed.
     * @return The patient consent preferences.
     * @throws gov.hhs.fha.nhinc.policyengine.adapterpip.AdapterPIPException This error is thrown if there are any
     *             issues retrieving the document.
     */
    public PatientPreferencesType retrievePatientConsentByDocId(String sHomeCommunityId, String sRepositoryId,
            String sDocumentUniqueId) throws AdapterPIPException {
        log.info("--------------- Begin retrievePatientConsentByDocId ---------------");
        PatientPreferencesType oPtPref = new PatientPreferencesType();

        if ((sDocumentUniqueId == null) || (sDocumentUniqueId.trim().length() <= 0)) {
            String sErrorMessage = "Failed to retrieve patient consent.  The document unique ID was either null or an empty string.";
            log.error(sErrorMessage);
            throw new AdapterPIPException(sErrorMessage);
        }

        try {
            // Patient from the document is now the fully qualified Unique Patient Id; "PID^^^&AAID&ISO"
            String sPatientId = retrievePtIdFromDocumentId(sDocumentUniqueId, sRepositoryId);
            log.info("Given DocId: " + sDocumentUniqueId + " in Repository: " + sRepositoryId
                    + " patientId retrieved is: " + sPatientId);

            // Extract the patient and assigning authority id into an II
            II patII = extractUniquePatientIdToII(sPatientId);

            if (patII != null && patII.getExtension() != null && !patII.getExtension().isEmpty()
                    && patII.getRoot() != null && !patII.getRoot().isEmpty()) {
                oPtPref = retrievePatientConsentByPatientId(patII.getExtension(), patII.getRoot());
            }
        } catch (Exception e) {
            log.error("Exception processing retrievePatientConsentByPatientId: ", e);
        }

        log.info("--------------- End retrievePatientConsentByDocId ---------------");
        return oPtPref;
    }

    /**
     * This method takes the given Document ID and does a query against the repository to find out the patient ID for
     * this patient. It returns that patient ID.
     * 
     * @param sDocumentUniqueId The document ID of an existing document in the repository.
     * @param sRepositoryId The repository ID of the repository where the document is stored.
     * @return The patient ID of the patient.
     * @throws AdapterPIPException - This is thrown if there is an error.
     */
    private String retrievePtIdFromDocumentId(String sDocumentUniqueId, String sRepositoryId)
            throws AdapterPIPException {
        log.debug("Begin PatientConsentManager.retrievePtIdFromDocumentId()..");
        String sPatientId = "";

        DocumentRegistryPortType oDocRegistryPort = getDocumentRegistryPort();

        AdhocQueryResponse oResponse = null;
        AdhocQueryRequest oRequest = new QueryUtil().createPatientIdQuery(sDocumentUniqueId, sRepositoryId);

        oResponse = oDocRegistryPort.documentRegistryRegistryStoredQuery(oRequest);

        sPatientId = new QueryUtil().extractPatientId(oResponse);

        log.debug("retrievePtIdFromDocumentId() - PatientId:" + sPatientId);

        return sPatientId;
    }

    private II extractUniquePatientIdToII(String uniquePatientId) {
        log.debug("Begin extractUniquePatientIdToII - uniquePatientId is " + uniquePatientId);

        II patII = new II();

        String patientId = "";
        String aaId = "";

        if (uniquePatientId != null && !uniquePatientId.isEmpty()) {
            if (uniquePatientId.startsWith("'")) {
                uniquePatientId = uniquePatientId.substring(1);
            }
            int pos = 0;
            String[] tokens = uniquePatientId.split("\\&");
            log.debug("extractUniquePatientIdToII - tokens length is " + tokens.length);
            if (tokens.length > 0) {
                for (String token : tokens) {
                    log.debug("token: " + token);
                }
            }

            if (tokens.length == 3) {
                patientId = tokens[0];
                pos = patientId.indexOf("^");
                if (pos > 0) {
                    patientId = patientId.substring(0, pos);
                }
                aaId = tokens[1];
            }

            if (patientId != null && !patientId.isEmpty()) {
                patII.setExtension(patientId);
                log.debug("extracted patient id is " + patientId);
            }
            if (aaId != null && !aaId.isEmpty()) {
                patII.setRoot(aaId);
                log.debug("extracted aa id is " + aaId);
            }
        }

        return patII;
    }

    /**
     * This method retrieves the patient consent information from the repository based on patient Id.
     * 
     * @param sPatientId The ID of the patient.
     * @param sAssigningAuthority The assigning authority associated with the patient ID. Currently it is not really
     *            used, but here if it is needed.
     * @return The patient consent preferences.
     * @throws gov.hhs.fha.nhinc.policyengine.adapterpip.AdapterPIPException This error is thrown if there are any
     *             issues retrieving the document.
     */
    public PatientPreferencesType retrievePatientConsentByPatientId(String sPatientId, String sAssigningAuthority)
            throws AdapterPIPException {
        log.info("--------------- Begin retrievePatientConsentByPatientId ---------------");
        PatientPreferencesType oPtPref = new PatientPreferencesType();

        if ((sPatientId == null) || (sPatientId.trim().length() <= 0)) {
            String sErrorMessage = "Failed to retrieve patient consent.  The patient ID was either null or an empty string.";
            log.error(sErrorMessage);
            throw new AdapterPIPException(sErrorMessage);
        }

        List<CPPDocumentInfo> olDocInfo = retrieveCPPFromRepositoryUsingXDSb(sPatientId, sAssigningAuthority);

        if (olDocInfo != null && !olDocInfo.isEmpty()) {
            log.info(olDocInfo.size() + " CPP documents were retrieved from the repository");
            oPtPref = populateConsentInfo(olDocInfo);
        }
        log.info("--------------- End retrievePatientConsentByPatientId ---------------");
        return oPtPref;
    }

    public String retrievePatientConsentDocumentIdByPatientId(String sPatientId, String sAssigningAuthority)
            throws AdapterPIPException {
        log.info("--------------- Begin retrievePatientConsentDocumentIdByPatientId ---------------");
        String patientConsentDocumentId = null;

        if ((sPatientId == null) || (sPatientId.trim().length() <= 0)) {
            String sErrorMessage = "Failed to retrieve patient consent.  The patient ID was either null or an empty string.";
            log.error(sErrorMessage);
            throw new AdapterPIPException(sErrorMessage);
        }

        List<CPPDocumentInfo> olDocInfo = retrieveCPPFromRepositoryUsingXDSb(sPatientId, sAssigningAuthority);

        if (olDocInfo != null && (olDocInfo.size() > 0)) {
            log.info(olDocInfo.size() + " CPP documents were retrieved from the repository");
            patientConsentDocumentId = olDocInfo.get(0).sDocumentUniqueId;
        }

        log.info("retrievePatientConsentDocumentIdByPatientId - patientConsentDocumentId: " + patientConsentDocumentId);
        log.info("--------------- End retrievePatientConsentDocumentIdByPatientId ---------------");
        return patientConsentDocumentId;
    }

    /**
     * This operation retrieves the currently stored patient consent information from the repository.
     * 
     * @param sPatientId The ID of the patient.
     * @param sAssigningAuthority The Assigning authority
     * @return The document information.
     * @throws gov.hhs.fha.nhinc.policyengine.adapterpip.AdapterPIPException The error if one occurs.
     */
    private List<CPPDocumentInfo> retrieveCPPFromRepositoryUsingXDSb(String sPatientId, String sAssigningAuthority)
            throws AdapterPIPException {

        List<CPPDocumentInfo> olCPPDocInfo = new ArrayList<CPPDocumentInfo>();

        List<DocumentRequest> olDocReq = retrieveCPPDocIdentifiers(sPatientId, sAssigningAuthority);
        for (DocumentRequest oDocRequest : olDocReq) {
            if (oDocRequest != null) {
                CPPDocumentInfo oCPPDocInfo = new CPPDocumentInfo();
                if (oDocRequest.getHomeCommunityId() != null) {
                    oCPPDocInfo.sHomeCommunityId = oDocRequest.getHomeCommunityId();
                }

                if (oDocRequest.getRepositoryUniqueId() != null) {
                    oCPPDocInfo.sRepositoryId = oDocRequest.getRepositoryUniqueId();
                }

                if (oDocRequest.getDocumentUniqueId() != null) {
                    oCPPDocInfo.sDocumentUniqueId = oDocRequest.getDocumentUniqueId();
                }
                log.info("Document Request has community: " + oCPPDocInfo.sHomeCommunityId + " repository: "
                        + oCPPDocInfo.sRepositoryId + " docId: " + oCPPDocInfo.sDocumentUniqueId);
                retrieveCPPDoc(oDocRequest, oCPPDocInfo);
                olCPPDocInfo.add(oCPPDocInfo);
            }

        }

        return olCPPDocInfo;
    }

    /**
     * This method retrieves the document identifier information from the repository for the Consumer Preferences
     * document for this patient.
     * 
     * @param sPatientId The patient ID of the patient.
     * @param sAssigningAuthority The assigning authority
     * @return The document identifiers for the CPP document
     * @throws gov.hhs.fha.nhinc.policyengine.adapterpip.AdapterPIPException This error is thrown if any problem occurs
     *             getting the data.
     */
    private List<DocumentRequest> retrieveCPPDocIdentifiers(String sPatientId, String sAssigningAuthority)
            throws AdapterPIPException {
        QueryUtil queryUtil = new QueryUtil();

        DocumentRegistryPortType oDocRegistryPort = getDocumentRegistryPort();

        AdhocQueryResponse oResponse = null;
        AdhocQueryRequest oRequest = queryUtil.createAdhocQueryRequest(sPatientId, sAssigningAuthority);

        oResponse = oDocRegistryPort.documentRegistryRegistryStoredQuery(oRequest);

        List<DocumentRequest> olDocReq = queryUtil.createDocumentRequest(oResponse);

        return olDocReq;
    }

    /**
     * This method takes the document identifiers for the CPP document and retrieves the document from the repository.
     * 
     * @param oDocRequest The document identifiers.
     * @param oCPPDocInfo Document object to hold results
     * @return void.
     * @throws gov.hhs.fha.nhinc.policyengine.adapterpip.AdapterPIPException This is thrown if there are any errors.
     */
    private void retrieveCPPDoc(DocumentRequest oDocRequest, CPPDocumentInfo oCPPDocInfo) throws AdapterPIPException {
        String sPrefDoc = "";

        DocumentRepositoryPortType oDocRepositoryPort = getDocumentRepositoryPort();

        RetrieveDocumentSetRequestType oRequest = new RetrieveDocumentSetRequestType();
        oRequest.getDocumentRequest().add(oDocRequest);

        RetrieveDocumentSetResponseType oResponse = null;
        oResponse = oDocRepositoryPort.documentRepositoryRetrieveDocumentSet(oRequest);

        if (oResponse != null) {
            sPrefDoc = extractFineGrainedPrefDoc(oDocRequest, oResponse);
            if (NullChecker.isNotNullish(sPrefDoc)) {
                oCPPDocInfo.sConsentXACML = sPrefDoc;
            }

            List<String> olBinPrefDoc = extractBinPrefDoc(oDocRequest, oResponse);
            if (olBinPrefDoc != null && !olBinPrefDoc.isEmpty()) {
                oCPPDocInfo.olConsentPdf = olBinPrefDoc;
            }

        }
    }

    /**
     * This operation returns the Patient CPP document in XML form that it extracts from the Response.
     * 
     * @param oResponse The response that was recieved from the repository.
     * @return The XML form of the patient CPP.
     * @throws gov.hhs.fha.nhinc.policyengine.adapterpip.AdapterPIPException This is thrown if there are any errors.
     */
    private String extractFineGrainedPrefDoc(DocumentRequest oDocRequest, RetrieveDocumentSetResponseType oResponse)
            throws AdapterPIPException {
        String sPrefDoc = "";

        if ((oResponse != null) && (oResponse.getDocumentResponse() != null)
                && (oResponse.getDocumentResponse().size() > 0)) {
            List<DocumentResponse> olDocResponse = oResponse.getDocumentResponse();
            for (DocumentResponse oDocResponse : olDocResponse) {
                log.info("Doc: " + oDocResponse.getDocumentUniqueId() + " Mime type: " + oDocResponse.getMimeType());
                if (oDocRequest.getDocumentUniqueId().equals(oDocResponse.getDocumentUniqueId())) {
                    if (XACML_MIME_TYPE.equals(oDocResponse.getMimeType())) {
                        if ((oDocResponse.getDocument() != null) && (oDocResponse.getDocument().length > 0)) {
                            log.info("Matching XACML document found");
                            sPrefDoc = new String(oDocResponse.getDocument());
                            break;

                        }
                    }
                }
            }
        } // if ((oResponse != null) &&

        return sPrefDoc;
    }

    private List<String> extractBinPrefDoc(DocumentRequest oDocRequest, RetrieveDocumentSetResponseType oResponse) {

        log.info("--------------- Begin extractBinPrefDoc ---------------");
        List<String> olBinPrefDoc = new ArrayList<String>();

        if ((oResponse != null) && (oResponse.getDocumentResponse() != null)
                && (oResponse.getDocumentResponse().size() > 0)) {
            List<DocumentResponse> olDocResponse = oResponse.getDocumentResponse();
            log.info(olDocResponse.size() + " documents have been found");
            for (DocumentResponse oDocResponse : olDocResponse) {
                log.info("Doc: " + oDocResponse.getDocumentUniqueId() + " Mime type: " + oDocResponse.getMimeType());
                if (oDocRequest.getDocumentUniqueId().equals(oDocResponse.getDocumentUniqueId())) {
                    if (PDF_MIME_TYPE.equals(oDocResponse.getMimeType())) {
                        if ((oDocResponse.getDocument() != null) && (oDocResponse.getDocument().length > 0)) {
                            String sPrefDoc = new String(oDocResponse.getDocument());
                            log.info("Matching PDF document found: " + sPrefDoc);
                            olBinPrefDoc.add(sPrefDoc);
                        }
                    }
                }
            }
        }
        log.info("--------------- End extractBinPrefDoc ---------------");
        return olBinPrefDoc;
    }

    /**
     * This method takes the XML Patient preference CDA documents and it populates the PtPref with the information it
     * finds in it.
     * 
     * @param olDocInfo The list of patient preference documents
     * @return The patient preferences from the document.
     * @throws gov.hhs.fha.nhinc.policyengine.adapterpip.AdapterPIPException
     */
    private PatientPreferencesType populateConsentInfo(List<CPPDocumentInfo> olDocInfo) throws AdapterPIPException {
        log.info("--------------- Begin populateConsentInfo ---------------");
        PatientPreferencesType oPtPref = new PatientPreferencesType();

        // Default to Opt-Out, but will be set by state found in XACML document
        oPtPref.setOptIn(false);

        boolean bHasFineGrained = false;
        for (CPPDocumentInfo oDocInfo : olDocInfo) {
            if (oDocInfo != null && oDocInfo.sConsentXACML != null && !oDocInfo.sConsentXACML.isEmpty()) {
                bHasFineGrained = true;

                String sConsentXACML = oDocInfo.sConsentXACML;
                XACMLSerializer oSerializer = new XACMLSerializer();
                PolicyType oConsentXACML = oSerializer.deserializeConsentXACMLDoc(sConsentXACML);
                if (oConsentXACML != null) {
                    XACMLExtractor oExtractor = new XACMLExtractor();
                    oPtPref = oExtractor.extractPatientPreferences(oConsentXACML);
                    log.info("Extracted XACML document has patient opt-in as: " + oPtPref.isOptIn());
                }
                break;
            }

        }
        // patient is required to have a fine-grained policy to continue with binary document
        if (bHasFineGrained && oPtPref != null) {
            log.info("Begin extraction for Binary documents");
            BinaryDocumentPolicyCriteriaType oBinDocPolicyCriteriaType = new BinaryDocumentPolicyCriteriaType();
            List<BinaryDocumentPolicyCriterionType> olBinDocPolicyCriteria = oBinDocPolicyCriteriaType
                    .getBinaryDocumentPolicyCriterion();

            // Add Binary extraction for each document that contains a pdf
            for (CPPDocumentInfo oDocInfo : olDocInfo) {
                log.info(oDocInfo.lDocumentId + " has " + oDocInfo.olConsentPdf.size() + " pdf");
                if (oDocInfo != null && oDocInfo.olConsentPdf != null && !oDocInfo.olConsentPdf.isEmpty()) {
                    List<String> olConsentPdf = oDocInfo.olConsentPdf;
                    for (String sConsentPdf : olConsentPdf) {
                        CdaPdfSerializer oSerializer = new CdaPdfSerializer();
                        log.info("Deserialize: " + sConsentPdf);
                        POCDMT000040ClinicalDocument oCda = oSerializer.deserialize(sConsentPdf);
                        log.info("Returned CDA: " + oCda);
                        if (oCda != null) {
                            CdaPdfExtractor oExtractor = new CdaPdfExtractor();
                            BinaryDocumentPolicyCriterionType oBinDocPolicyCriterion = oExtractor
                                    .extractBinaryDocumentPolicyCriterion(oCda);
                            log.info("Returned Binary Criterion: " + oCda);
                            if (oBinDocPolicyCriterion != null) {
                                olBinDocPolicyCriteria.add(oBinDocPolicyCriterion);
                                log.info("Extracted Binary document has id: "
                                        + oBinDocPolicyCriterion.getDocumentUniqueId());
                            }
                        }
                    }
                }
            }
            if (olBinDocPolicyCriteria != null && !olBinDocPolicyCriteria.isEmpty()) {
                oPtPref.setBinaryDocumentPolicyCriteria(oBinDocPolicyCriteriaType);
            } else {
                log.info("No Binary documents are processed");
            }
        } else {
            log.info("Fine Grained Policy Criterion is not present.");
        }

        log.info("--------------- End populateConsentInfo ---------------");
        return oPtPref;
    }

    /**
     * This class is an inner class used to hold the document along with its set of identifiers. For internal purposes.
     */
    private class CPPDocumentInfo {

        long lDocumentId = 0; // note this is only used by our internal document repository service
        String sHomeCommunityId = "";
        String sRepositoryId = "";
        String sDocumentUniqueId = "";
        String sConsentXACML = "";
        List<String> olConsentPdf = new ArrayList<String>();
    }

}
