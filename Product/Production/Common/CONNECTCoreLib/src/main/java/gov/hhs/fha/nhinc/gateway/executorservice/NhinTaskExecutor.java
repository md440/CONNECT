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
package gov.hhs.fha.nhinc.gateway.executorservice;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import gov.hhs.fha.nhinc.orchestration.OutboundResponseProcessor;
import gov.hhs.fha.nhinc.orchestration.OutboundOrchestratableMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Main unit of execution Executes a DQ or PD request currently, but could be used to execute any of the Nhin
 * transaction requests (such as DR or DeferredPD)
 * 
 * Uses generics for CumulativeResponse (which represents final object that is returned). Each IndividualResponse
 * returned from executed NhinCallableRequest contains the OutboundResponseProcessor for the IndividualResponse
 * 
 * Constructs with the java.util.concurrent.ExecutorService to use to execute the requests and a List of
 * NhinCallableRequest to be submitted to ExecutorService
 * 
 * Uses an ExecutorCompletionService, and executeTask will return only when all CallableRequest have completed/returned.
 * Once executeTask has returned, call getFinalResponse to get the final cumulative/aggregated/processed response which
 * contains all the responses from the individual NhinCallableRequest
 * 
 * @author paul.eftis
 */
public class NhinTaskExecutor<CumulativeResponse extends OutboundOrchestratableMessage, IndividualResponse extends OutboundOrchestratableMessage> {

    private Log log = LogFactory.getLog(getClass());

    private CumulativeResponse cumulativeResponse = null;

    private Executor executor = null;
    private String transactionId = null;
    private List<NhinCallableRequest<IndividualResponse>> callableList = new ArrayList<NhinCallableRequest<IndividualResponse>>();

    /**
     *
     */
    public NhinTaskExecutor(Executor e, List<NhinCallableRequest<IndividualResponse>> list, String id) {
        transactionId = id;
        executor = e;
        callableList = list;
    }

    /**
     * Called when TaskExecutor is complete to retrieve the final result
     * 
     * @return Response which contains all the responses from the individual CallableRequest aggregated into a single
     *         response
     */
    public CumulativeResponse getFinalResponse() {
        return cumulativeResponse;
    }

    @SuppressWarnings("static-access")
    public void executeTask() throws InterruptedException, ExecutionException {

        log.debug("NhinTaskExecutor::executeTask begin");

        try {
            CompletionService<IndividualResponse> executorCompletionService = new ExecutorCompletionService<IndividualResponse>(
                    executor);
            // loop through the callableList and submit the callable requests for execution
            for (NhinCallableRequest<IndividualResponse> c : callableList) {
                executorCompletionService.submit(c);
            }

            // the executor completion service puts the callable responses on a
            // blocking queue where you retrieve <Future> responses off queue using
            // take(), when they become available
            int count = 0;
            for (NhinCallableRequest<IndividualResponse> c : callableList) {
                Future<IndividualResponse> future = executorCompletionService.take();
                // for debug
                count++;
                log.debug("NhinTaskExecutor::executeTask::take received response count=" + count);

                if (future != null) {
                    try {
                        IndividualResponse r = (IndividualResponse) future.get();
                        if (r != null) {
                            // process response
                            OutboundResponseProcessor processor = r.getResponseProcessor();
                            cumulativeResponse = (CumulativeResponse) processor.processNhinResponse(r,
                                    cumulativeResponse);
                        } else {
                            // shouldn't ever get here, but if we do all we can do is log and skip it
                            log.error("NhinTaskExecutor::executeTask (count=" + count + ") received null response!!!!!");
                        }
                    } catch (Exception e) {
                        // shouldn't ever get here
                        log.error("NhinTaskExecutor processResponse EXCEPTION!!!");
                        ExecutorServiceHelper.getInstance().outputCompleteException(e);
                    }
                } else {
                    // shouldn't ever get here
                    log.error("NhinTaskExecutor::executeTask received null future from queue (i.e. take)!!!!!");
                }
            }
            log.debug("NhinTaskExecutor::executeTask done");
        } catch (Exception e) {
            // shouldn't ever get here
            log.error("NhinTaskExecutor EXCEPTION!!!");
            ExecutorServiceHelper.getInstance().outputCompleteException(e);
        }
    }

}
