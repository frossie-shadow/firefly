/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.ui.searchui;
/**
 * User: roby
 * Date: 6/20/14
 * Time: 11:22 AM
 */


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import edu.caltech.ipac.firefly.core.background.MonitorItem;
import edu.caltech.ipac.firefly.ui.GwtUtil;
import edu.caltech.ipac.firefly.util.WebClassProperties;


/**
 * @author Trey Roby
 */
public class SearchItemMonitorUI implements MonitorItem.UpdateListener {

    private static WebClassProperties _prop= new WebClassProperties(SearchItemMonitorUI.class);
//    private final MonitorItem monitorItem;
    private final FlowPanel panel;
    private final HTML title= new HTML();
    private final FlowPanel options= new FlowPanel();
    private final HTML status= new HTML();
    private final SimplePanel iconHolder= new SimplePanel();
    private final ActiveSearchMonitorUI parent;
    private final Image workingIcon= new Image(GwtUtil.LOADING_ICON_URL);
    private final FuseSearchPanel searchPanel;
    private boolean active= false;

    SearchItemMonitorUI(ActiveSearchMonitorUI parent, FuseSearchPanel searchPanel, MonitorItem monitorItem) {
//        this.monitorItem= monitorItem;
        this.parent= parent;
        this.searchPanel= searchPanel;
        monitorItem.addUpdateListener(this);
        panel= GwtUtil.makePanel(true, false, title, options, status, iconHolder);
        parent.getWidget().add(panel);
        init();
    }


    private void init() {
        title.setWidth("250px");
        GwtUtil.setStyle(title, "textAlign", "left");

        options.setWidth("200px");
        status.setWidth("100px");
        GwtUtil.setStyle(status, "textAlign", "left");
        GwtUtil.setStyles(panel, "lineHeight", "17px",
                                 "textAlign", "left",
                                 "marginLeft", "10px");
        workingIcon.setWidth("12px");
    }


    public void update(MonitorItem item) {
        title.setHTML(item.getTitle());
        String stateStr= "not set";
        iconHolder.clear();
        if (item.getStatus().isActive()) iconHolder.setWidget(workingIcon);

        switch (item.getState()) {
            case WAITING:
            case STARTING:
            case WORKING:      stateStr= "working...";
                break;
            case USER_ABORTED: stateStr= "aborted";
                break;
            case UNKNOWN_PACKAGE_ID:
            case FAIL:         stateStr= "Failed";
                break;
            case SUCCESS:      stateStr= "Completed";
                break;
            case CANCELED:     stateStr= "Canceled";
                break;
        }
        status.setHTML(stateStr);
        updateOptions(item);
    }


    private void updateOptions(final MonitorItem item) {
        if (active!=item.isActive() || options.getWidgetCount()==0) {
            options.clear();
            active= item.isActive();

            Widget copy= makeCopy(item);

            if (active) {
                GwtUtil.setStyles(copy, "display", "inline-block", "paddingRight", "8px");

                Widget modify= makeModify(item);
                GwtUtil.setStyles(modify, "display", "inline-block", "paddingRight", "8px");

                Widget cancel= makeCancel(item);
                GwtUtil.setStyles(cancel, "display", "inline-block");

                options.add(copy);
                options.add(modify);
                options.add(cancel);
            }
            else {
                GwtUtil.setStyles(copy, "display", "inline-block");
                options.add(copy);

            }
        }
    }


    private void copySearch(MonitorItem item) {
        searchPanel.clear();
        searchPanel.populateFields(item.getRequest());
    }

    private void modifySearch(MonitorItem item) {
        item.cancel();
        searchPanel.clear();
        searchPanel.populateFields(item.getRequest());
        parent.getWidget().remove(panel);
    }

    private void cancelSearch(MonitorItem item) {
        item.cancel();
        parent.getWidget().remove(panel);
    }


    private Widget makeCopy(final MonitorItem item) {
        return GwtUtil.makeLinkButton(_prop.makeBase("copy"),new ClickHandler() {
            public void onClick(ClickEvent event) {
                copySearch(item);
            }
        });
    }

    private Widget makeModify(final MonitorItem item) {
        return GwtUtil.makeLinkButton(_prop.makeBase("modify"),new ClickHandler() {
            public void onClick(ClickEvent event) {
                modifySearch(item);
            }
        });
    }

    private Widget makeCancel(final MonitorItem item) {
        return GwtUtil.makeLinkButton(_prop.makeBase("cancel"),new ClickHandler() {
            public void onClick(ClickEvent event) {
                cancelSearch(item);
            }
        });
    }
}

