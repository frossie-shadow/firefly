/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.ui.creator;

import com.google.gwt.user.client.ui.Widget;

import java.util.Map;

/**
 * Date: Aug 4, 2010
 *
 * @author loi
 * @version $Id: FormWorkerCreator.java,v 1.1 2010/08/13 22:14:47 roby Exp $
 */
public interface FormWorkerCreator extends UICreator {
    public Widget create(Map<String, String> params);
}
