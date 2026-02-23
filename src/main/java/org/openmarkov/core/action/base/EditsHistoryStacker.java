/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jrico
 */
public class EditsHistoryStacker {
    
    private final EditsHistory mainEditsHistory;
    private final ArrayDeque<EditsHistory> uncommitedHistories;
    
    public EditsHistoryStacker() {
        this.mainEditsHistory = new EditsHistory();
        this.uncommitedHistories = new ArrayDeque<>();
    }
    
    public void openNewSubEditHistory() {
        this.uncommitedHistories.addLast(new EditsHistory());
    }
    
    public void closeSubEditHistory(List<CloseEditStackOptions> closeOperations) {
        if (this.uncommitedHistories.isEmpty()) {
            return;
        }
        ;
        var lastUsedStack = this.uncommitedHistories.removeLast();
        ArrayList<PNEdit> doneEdits = lastUsedStack.getDoneEdits();
        PNEdit stackAsEdit = switch (doneEdits.size()) {
            case 0 -> null;
            case 1 -> doneEdits.getFirst();
            default -> new ListPNEdit(doneEdits.getFirst().getProbNet(), doneEdits);
        };
        if (stackAsEdit == null) {
            return;
        }
        if (!closeOperations.contains(CloseEditStackOptions.FORGET)) {
            var nextLastStack = this.uncommitedHistories.isEmpty() ? this.mainEditsHistory : this.uncommitedHistories.getLast();
            nextLastStack.addEdit(stackAsEdit);
        }
        if (closeOperations.contains(CloseEditStackOptions.UNDO)) {
            stackAsEdit.undo();
        }
    }
    
    public EditsHistory getCurrentUndoManager() {
        if (!this.uncommitedHistories.isEmpty()) {
            return this.uncommitedHistories.getLast();
        }
        return this.mainEditsHistory;
    }
    
    
}
