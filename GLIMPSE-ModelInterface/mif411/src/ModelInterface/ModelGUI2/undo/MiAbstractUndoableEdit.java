/*
* LEGAL NOTICE
* This computer software was prepared by Battelle Memorial Institute,
* hereinafter the Contractor, under Contract No. DE-AC05-76RL0 1830
* with the Department of Energy (DOE). NEITHER THE GOVERNMENT NOR THE
* CONTRACTOR MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
* LIABILITY FOR THE USE OF THIS SOFTWARE. This notice including this
* sentence must appear on any copies of this computer software.
* 
* Copyright 2012 Battelle Memorial Institute.  All Rights Reserved.
* Distributed as open-source under the terms of the Educational Community 
* License version 2.0 (ECL 2.0). http://www.opensource.org/licenses/ecl2.php
* 
* EXPORT CONTROL
* User agrees that the Software will not be shipped, transferred or
* exported into any country or used in any manner prohibited by the
* United States Export Administration Act or any other applicable
* export laws, restrictions or regulations (collectively the "Export Laws").
* Export of the Software may require some form of license or other
* authority from the U.S. Government, and failure to obtain such
* export control license may result in criminal liability under
* U.S. laws. In addition, if the Software is identified as export controlled
* items under the Export Laws, User represents and warrants that User
* is not a citizen, or otherwise located within, an embargoed nation
* (including without limitation Iran, Syria, Sudan, Cuba, and North Korea)
*     and that User is not otherwise prohibited
* under the Export Laws from receiving the Software.
* 
*/
package ModelInterface.ModelGUI2.undo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

/**
 * Provide UndoableEdits with the ability to notify MiUndoableEditListeners
 * of undos and redo.
 * @author Pralit Patel
 * @see ModelInterface.ModelGUI2.undo.MiUndoableEditListener
 */ 
public abstract class MiAbstractUndoableEdit extends AbstractUndoableEdit {
	/**
	 * List of registered listeners
	 */
	protected List<MiUndoableEditListener> editListeners = new ArrayList<MiUndoableEditListener>();

	/**
	 * Registers a listener with this Edit.
	 * @param listener The listener that will be registered.
	 */
	public void addListener(MiUndoableEditListener listener) {
		editListeners.add(listener);
	}

	/**
	 * Unregisters a listener with this Edit.
	 * @param listener The listener that should be removed.
	 */
	public void removeListener(MiUndoableEditListener listener) {
		editListeners.remove(listener);
	}

	/**
	 * Notifies all registerd listeners that an undo has occured.
	 * @param source The source of the undo.
	 * @param edit The edit that did the undo.
	 */ 
	public void fireUndoPerformed(Object source, UndoableEdit edit) {
		UndoableEditEvent e = new UndoableEditEvent(source, edit);
		for(Iterator<MiUndoableEditListener> it = editListeners.iterator(); it.hasNext(); ) {
			it.next().undoPerformed(e);
		}
	}

	/**
	 * Notifies all registerd listeners that an redo has occured.
	 * @param source The source of the redo.
	 * @param edit The edit that did the redo.
	 */ 
	public void fireRedoPerformed(Object source, UndoableEdit edit) {
		UndoableEditEvent e = new UndoableEditEvent(source, edit);
		for(Iterator<MiUndoableEditListener> it = editListeners.iterator(); it.hasNext(); ) {
			it.next().redoPerformed(e);
		}
	}

	/**
	 * If we are going to die might as well remove all listeners.
	 * @see javax.swing.undo.UndoableEdit
	 */
	public void die() {
		editListeners.clear();
	}
}
