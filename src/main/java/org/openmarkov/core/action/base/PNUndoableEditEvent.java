/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.model.network.ProbNet;

@SuppressWarnings("serial")

/*
  The different between {@code PNUndoableEditEvent} and
  {@code UndoableEditEvent} is that a <code>PNUndoableEditEvent</code>
  use a {@code ProbNet}.
 */
public class PNUndoableEditEvent {
    
    private final Object source;
    private final PNEdit edit;
    // Attributes
	private ProbNet probNet;

	// Constructor

	/**
	 * @param source  The {@code Object} that originated the event.
	 * @param edit    An {@code UndoableEdit} object.
	 * @param probNet The {@code ProbNet} on witch the event will operate
	 */
    public PNUndoableEditEvent(Object source, PNEdit edit, ProbNet probNet) {
        this.source = source;
        this.edit = edit;
        this.probNet = probNet;
	}
    
    /**
     * The object on which the Event initially occurred.
     *
     * @return the object on which the Event initially occurred
     */
    public Object getSource() {
        return source;
    }
    
    /**
     * Returns the edit value.
     *
     * @return the UndoableEdit object encapsulating the edit
     */
    public PNEdit getEdit() {
        return this.edit;
    }
    
	/**
	 * @return probNet. {@code ProbNet}
	 */
	public ProbNet getProbNet() {
		return probNet;
	}

}
