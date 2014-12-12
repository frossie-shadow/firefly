package edu.caltech.ipac.planner.io;

import edu.caltech.ipac.util.Assert;
import edu.caltech.ipac.util.action.ClassProperties;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
* Maintain a list of targets from a target text file.
*
* @author Su Potts
* @version $Id: TextTargetList.java,v 1.4 2006/01/19 17:50:43 roby Exp $
*
*/ 

public class TextTargetList extends AbstractTableModel {

    private final static ClassProperties _prop = 
            new ClassProperties(TextTargetList.class);

// private constants for table column names

    private final static String TARGET_TYPE_COL =
                                _prop.getColumnName("target_type");
    private final static String TARGET_NAME_COL =
                                _prop.getColumnName("target_name");

// constants for the table column index

    static final int TARGET_TYPE_IDX = 0;
    static final int TARGET_NAME_IDX = 1;

// private / protected variables

    static private final int NUM_COLUMNS = 2;

    private final String _colNames[] = new String[NUM_COLUMNS];
    private List<TextTarget>  _targetList = new ArrayList<TextTarget>();

// constructors

    public TextTargetList() {
        _colNames[TARGET_TYPE_IDX] = TARGET_TYPE_COL;
        _colNames[TARGET_NAME_IDX] = TARGET_NAME_COL;
    }

// methods from AbstractTableModel class

    public int getRowCount() { return _targetList.size(); }

    public int getColumnCount() { return NUM_COLUMNS; }

    public String getColumnName(int column) {
           return _colNames[column];
    }

    public Object getValueAt(int row, int column) {
        Object retval  = null; 
        TextTarget ttarget = _targetList.get(row);
        Assert.tst(ttarget);

        switch (column) {
            case TARGET_TYPE_IDX:  retval = ttarget.getTargetType();
                                   break;
            case TARGET_NAME_IDX:  retval = ttarget.getTargetName();
                                   break;
            default:               Assert.tst(false);
                                   break;
        }
        return retval;
    }

    public void addTextTarget(TextTarget tt) {
        _targetList.add(tt);
    }
}
/*
 * THIS SOFTWARE AND ANY RELATED MATERIALS WERE CREATED BY THE CALIFORNIA 
 * INSTITUTE OF TECHNOLOGY (CALTECH) UNDER A U.S. GOVERNMENT CONTRACT WITH 
 * THE NATIONAL AERONAUTICS AND SPACE ADMINISTRATION (NASA). THE SOFTWARE 
 * IS TECHNOLOGY AND SOFTWARE PUBLICLY AVAILABLE UNDER U.S. EXPORT LAWS 
 * AND IS PROVIDED AS-IS TO THE RECIPIENT WITHOUT WARRANTY OF ANY KIND, 
 * INCLUDING ANY WARRANTIES OF PERFORMANCE OR MERCHANTABILITY OR FITNESS FOR 
 * A PARTICULAR USE OR PURPOSE (AS SET FORTH IN UNITED STATES UCC 2312-2313) 
 * OR FOR ANY PURPOSE WHATSOEVER, FOR THE SOFTWARE AND RELATED MATERIALS, 
 * HOWEVER USED.
 * 
 * IN NO EVENT SHALL CALTECH, ITS JET PROPULSION LABORATORY, OR NASA BE LIABLE 
 * FOR ANY DAMAGES AND/OR COSTS, INCLUDING, BUT NOT LIMITED TO, INCIDENTAL 
 * OR CONSEQUENTIAL DAMAGES OF ANY KIND, INCLUDING ECONOMIC DAMAGE OR INJURY TO 
 * PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER CALTECH, JPL, OR NASA BE 
 * ADVISED, HAVE REASON TO KNOW, OR, IN FACT, SHALL KNOW OF THE POSSIBILITY.
 * 
 * RECIPIENT BEARS ALL RISK RELATING TO QUALITY AND PERFORMANCE OF THE SOFTWARE 
 * AND ANY RELATED MATERIALS, AND AGREES TO INDEMNIFY CALTECH AND NASA FOR 
 * ALL THIRD-PARTY CLAIMS RESULTING FROM THE ACTIONS OF RECIPIENT IN THE USE 
 * OF THE SOFTWARE. 
 */