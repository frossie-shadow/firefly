/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.core.layout;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import edu.caltech.ipac.firefly.ui.GwtUtil;
import edu.caltech.ipac.firefly.util.Constants;
import edu.caltech.ipac.firefly.util.event.Name;
import edu.caltech.ipac.firefly.util.event.WebEvent;
import edu.caltech.ipac.firefly.util.event.WebEventManager;

/**
 * Date: Nov 15, 2007
 *
 * @author loi
 * @version $Id: BaseRegion.java,v 1.24 2011/10/12 17:28:53 loi Exp $
 */
public class BaseRegion implements Region, RequiresResize {

    public static final String ALIGN_MIDDLE = Constants.ALIGN_MIDDLE;
    public static final String ALIGN_LEFT = Constants.ALIGN_LEFT;
    public static final String ALIGN_RIGHT = Constants.ALIGN_RIGHT;

    private SimplePanel holder;
    private Widget currChild;
    private String id;
    private String title;
    private SimplePanel mainPanel;
    private int minHeight = 0;
    private boolean inlineBlock= false;


    public BaseRegion(String id) {
        this(id, null);
    }

    public BaseRegion(String id, Widget widget) {
        this(id, id, widget);
    }

    public BaseRegion(String id, String title, Widget widget) {
        this(id, title, widget, "100%", "100%");
    }

    public BaseRegion(String id, String title, Widget widget, String width, String height) {
        this.id = id;
        this.title = title;
        holder = new SimplePanel();
        holder.setSize("100%", "100%");
        if (widget != null) {
            setContent(widget);
        }
        mainPanel = new SimplePanel(holder);
        mainPanel.setSize(width, height);
        mainPanel.getElement().setId("region-" + id);
        setAlign(ALIGN_LEFT);
        adjust(mainPanel, holder);
    }

    protected void adjust(SimplePanel main, SimplePanel holder) {}

    public void setInlineBlock(boolean inlineBlock) {
        this.inlineBlock= inlineBlock;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setCollapsedTitle(String title) {
    }

    public void setExpandedTitle(String title) {
    }

    public void collapse() {
//        Application.getInstance().resize();
    }

    public void expand() {
//        Application.getInstance().resize();
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public Widget getContent() {
        return holder.getWidget();
    }

    public void clear() {
        if (currChild != null) {
            holder.remove(currChild);
        }
        currChild = null;
    }

    public int getMinHeight() {
        return minHeight;
    }

    private void setContent(Widget content) {
        if (currChild != null) {
            holder.remove(currChild);
        }
        if (content != null) {
            holder.setWidget(content);
        }
        currChild = content;
    }

    public Widget getDisplay() {
        return mainPanel;
    }

    public void setAlign(String align) {
        if (align == null) return;
        String v = align.equals(ALIGN_MIDDLE) ? "center" : align.equals(ALIGN_RIGHT) ? "right" : "left";
        DOM.setElementAttribute(holder.getElement(), "align", v);
    }

    public void setDisplay(Widget display) {

        if ((currChild == null && display == null) || (currChild != null && currChild.equals(display))) {
            show();
            return;  // already set.. do nothing.
        }

        if (display == null && currChild != null) {
            setContent(null);
            WebEventManager.getAppEvManager().fireEvent( new WebEvent(this, Name.REGION_REMOVED, currChild) );
        } else {
//            expand();
            WebEventManager.getAppEvManager().fireEvent( new WebEvent(this, Name.REGION_REMOVED, currChild) );
            setContent(display);
            WebEventManager.getAppEvManager().fireEvent( new WebEvent(this, Name.REGION_ADDED, display) );
        }
        WebEvent ev= new WebEvent(this, Name.REGION_CHANGE, id);
        WebEventManager.getAppEvManager().fireEvent(ev);
        show();

    }

    public void show() {
        if (inlineBlock) {
            GwtUtil.setStyle(mainPanel, "display", "inline-block");

        }
        else {
            mainPanel.setVisible(true);
        }
        WebEventManager.getAppEvManager().fireEvent( new WebEvent(this, Name.REGION_SHOW) );
    }

    public void hide() {
        mainPanel.setVisible(false);
        WebEventManager.getAppEvManager().fireEvent( new WebEvent(this, Name.REGION_HIDE) );
    }

    public boolean isCollapsible() {
        return false;
    }

    public void onResize() {
        if (currChild instanceof RequiresResize) {
            ((RequiresResize)currChild).onResize();
        }
    }
}
