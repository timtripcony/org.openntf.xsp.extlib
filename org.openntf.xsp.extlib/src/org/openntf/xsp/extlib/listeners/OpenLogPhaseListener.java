package org.openntf.xsp.extlib.listeners;

/*

 <!--
 Copyright 2013 Paul Withers
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and limitations under the License
 -->

 */

import java.util.Map;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import com.ibm.jscript.InterpretException;
import com.ibm.xsp.exception.EvaluationExceptionEx;

import org.openntf.xsp.extlib.beans.OpenLogErrorHolder;
import org.openntf.xsp.extlib.beans.OpenLogErrorHolder.EventError;
import org.openntf.xsp.extlib.util.*;

/**
 * @author withersp
 * @since 1.0.0
 * 
 */
public class OpenLogPhaseListener implements PhaseListener {
	private static final long serialVersionUID = 1L;
	private static final int RENDER_RESPONSE = 6;

	public void beforePhase(PhaseEvent event) {

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
	 */
	@SuppressWarnings("unchecked")
	public void afterPhase(PhaseEvent event) {
		try {
			if (RENDER_RESPONSE == event.getPhaseId().getOrdinal()) {
				Map<String, Object> r = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
				if (null != r.get("error")) {
					// requestScope.error is not null, we're on the custom error page.
					Object error = r.get("error");

					// Set the agent (page we're on) to the *previous* page
					OpenLogItem.setThisAgent(false);

					if ("com.ibm.xsp.exception.EvaluationExceptionEx".equals(error.getClass().getName())) {
						// EvaluationExceptionEx, so error is on a component property
						EvaluationExceptionEx ee = (EvaluationExceptionEx) error;
						InterpretException ie = (InterpretException) ee.getCause();
						String msg = "";
						msg = "Error on " + ee.getErrorComponentId() + " " + ee.getErrorPropertyId()
								+ " property/value, line " + Integer.toString(ie.getErrorLine() + 1) + ":\n\n"
								+ ie.getLocalizedMessage() + "\n\n" + ie.getExpressionText();
						OpenLogItem.logErrorEx(ee, msg, null, null);

					} else if ("javax.faces.FacesException".equals(error.getClass().getName())) {
						// FacesException, so error is on event
						FacesException fe = (FacesException) error;
						EvaluationExceptionEx ee = (EvaluationExceptionEx) fe.getCause();
						String msg = "";
						msg = "Error on " + ee.getErrorComponentId() + " " + ee.getErrorPropertyId()
								+ " property/value:\n\n" + ee.getErrorText();
						OpenLogItem.logErrorEx(ee, msg, null, null);
					}

				} else if (null != r.get("openLogBean")) {
					// requestScope.openLogBean is not null, the developer has called openLogBean.addError(e,this)
					OpenLogErrorHolder errList = (OpenLogErrorHolder) r.get("openLogBean");
					// loop through the ArrayList of InnerError objects
					for (EventError error : errList.getErrors()) {
						String msg = "Error on ";
						if (null != error.getControl()) {
							msg = msg + error.getControl().getId();
						}
						msg = msg + ":\n\n" + error.getError().getLocalizedMessage() + "\n\n"
								+ error.getError().getExpressionText();
						OpenLogItem.logErrorEx(error.getError(), msg, null, null);
					}
				}
			}
		} catch (Throwable e) {
			// We've hit an error in our code here, log the error
			OpenLogItem.logError(e);
		}
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
