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
package gov.hhs.fha.nhinc.policyengine.adapter.pap;

import gov.hhs.fha.nhinc.docrepository.adapter.model.Document;
import gov.hhs.fha.nhinc.docrepository.adapter.model.DocumentQueryParams;
import gov.hhs.fha.nhinc.docrepository.adapter.service.DocumentService;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements the policy engine PAP (Policy Access Point).
 * 
 * @author mastan.ketha
 */
public class AdapterPAPImpl {

    private static Log log = LogFactory.getLog(AdapterPAPImpl.class);

    /**
     * Get the Access Consent Policy (ACP) document for the patient.
     * 
     * @param patientId
     * @return <code>Document</code>
     */
    public Document getPolicyDocumentByPatientId(String patientId) {
        log.info("Begin AdapterPAPImpl.getPolicyDocument(...)");
        Document document = new Document();
        try {
            DocumentQueryParams params = new DocumentQueryParams();
            log.debug("patientid:" + patientId);
            params.setPatientId(patientId);
            List<String> classCodeValues = new ArrayList<String>();
            classCodeValues.add(AdapterPAPConstants.DOCUMENT_CLASS_CODE);
            params.setClassCodes(classCodeValues);
            DocumentService service = new DocumentService();
            List<Document> docs = service.documentQuery(params);
            int docsSize = 0;
            if (docs != null) {
                docsSize = docs.size();
            }
            log.debug("Document size:" + String.valueOf(docsSize));
            if (docsSize > 0) {
                document = docs.get(0);
            }
        } catch (Exception ex) {
            log.error("Exception occured while retrieving documents");
            log.error(ex.getMessage());
        }
        log.info("End AdapterPAPImpl.getPolicyDocument(...)");
        return document;
    }

    /**
     * Get the Access Consent Policy (ACP) document from the document repository.
     * 
     * @param documentId
     * @return <code>Document</code>
     */
    public Document getPolicyDocumentByDocId(Long documentId) {
        log.info("Begin AdapterPAPImpl.getPolicyDocumentByDocId(...)");
        Document document = new Document();
        try {
            DocumentService service = new DocumentService();
            document = service.getDocument(documentId);
        } catch (Exception ex) {
            log.error("Exception occured while retrieving documents");
            log.error(ex.getMessage());
        }
        log.info("End AdapterPAPImpl.getPolicyDocumentByDocId(...)");
        return document;
    }

    /**
     * Saves the document to the repository. If the document id (PK) is null then the document is inserted, else the
     * document is updated
     * 
     * @param document
     * @return true - success; false - failure
     */
    public boolean savePolicyDocument(Document document) {
        log.info("Begin AdapterPAPImpl.savePolicyDocument(...)");
        boolean isDocSaved = false;
        try {
            if (document == null) {
                log.warn("AdapterPAPImpl - Document is null");
            } else {
                DocumentService service = new DocumentService();
                service.saveDocument(document);
                isDocSaved = true;
            }

        } catch (Exception ex) {
            log.error("Exception occured while saving document");
            log.error(ex.getMessage());
        }
        log.info("End AdapterPAPImpl.savePolicyDocument(...)");
        return isDocSaved;
    }

    /**
     * Deletes the document from the repository.
     * 
     * @param document
     * @return true - success; false - failure
     */
    public boolean deletePolicyDocument(Document document) {
        log.info("Begin AdapterPAPImpl.deletePolicyDocument(...)");
        boolean isDocSaved = false;

        try {
            DocumentService service = new DocumentService();
            service.deleteDocument(document);
            isDocSaved = true;
        } catch (Exception ex) {
            log.error("Exception occured while deleting document");
            log.error(ex.getMessage());
        }
        log.info("End AdapterPAPImpl.deletePolicyDocument(...)");
        return isDocSaved;

    }

}
