/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.visualize.graph;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gchart.client.GChart;
import com.googlecode.gchart.client.HoverParameterInterpreter;
import edu.caltech.ipac.firefly.core.HelpManager;
import edu.caltech.ipac.firefly.data.SpecificPoints;
import edu.caltech.ipac.firefly.data.table.DataSet;
import edu.caltech.ipac.firefly.data.table.TableDataView;
import edu.caltech.ipac.firefly.resbundle.css.CssData;
import edu.caltech.ipac.firefly.resbundle.css.FireflyCss;
import edu.caltech.ipac.firefly.resbundle.images.VisIconCreator;
import edu.caltech.ipac.firefly.ui.GwtUtil;
import edu.caltech.ipac.firefly.ui.MaskMessgeWidget;
import edu.caltech.ipac.firefly.ui.MaskPane;
import edu.caltech.ipac.firefly.ui.PopoutWidget;
import edu.caltech.ipac.firefly.ui.PopupUtil;
import edu.caltech.ipac.firefly.util.MinMax;
import edu.caltech.ipac.firefly.visualize.AllPlots;
import edu.caltech.ipac.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tatianag
 *         $Id: $
 */
public class XYPlotBasicWidget extends PopoutWidget {

    // colors that color blind people can distinguish
    // http://safecolours.rigdenage.com/Comp10.jpg
    // plus the colors that are 3 stops darker
    // see http://www.w3schools.com/tags/ref_colorpicker.asp
    private static String [] colors = {"#333333", "#ff3333", "#00ccff","#336600",
              "#9900cc", "#ff9933", "#009999", "#66ff33", "#cc9999",
            "#333333", "#b22424", "#008fb2", "#244700",
        "#6b008f", "#b26b24", "#006b6b", "#47b224", "8F6B6B"};

    // CSS light colors
    private static String [] lightcolors = {"MediumPurple", "LightCoral", "LightBlue", "Olive",
              "Plum", "LightSalmon", "SandyBrown", "PaleTurquoise", "YellowGreen",
              "LightPink", "CornflowerBlue", "Khaki", "PaleGreen", "LightSteelBlue"};

    private static String [] sedcolors = {"gray", "#00b8e6", "#9900cc", "#336600"}; // [0] b  upper limit orange #ff9933

    protected static final String ZOOM_OUT_HELP = "&nbsp;Zoom out with original size button.&nbsp;";
    protected static final String ZOOM_IN_HELP = "&nbsp;Rubber band zoom &mdash; click and drag an area to zoom in.&nbsp;";

    protected static int MIN_SIZE_FOR_DOCKED_OPTIONS = 650;
    protected static int OPTIONS_PANEL_WIDTH = 350;

    private static final int RESIZE_DELAY= 100;
    protected DockLayoutPanel _dockPanel = new DockLayoutPanel(Style.Unit.PX);
    private final MaskMessgeWidget _maskMessge = new MaskMessgeWidget(false);
    protected final MaskPane _maskPane=
            new MaskPane(_dockPanel, _maskMessge);

    ScrollPanel _panel= new ScrollPanel();
    SimplePanel _cpanel= new SimplePanel(); // for chart
    //HTML _statusMessage;  // was used for data info
    protected GChart _chart = null;
    protected DataSet _dataSet = null;
    protected XYPlotData _data = null;
    protected XYPlotMeta _meta = null;
    private Widget _legend = null;
    private boolean _showLegend = false;
    private boolean _popoutWidgetSet;
    private int _xResizeFactor = 1;
    private int _yResizeFactor = 1;
    private int TICKS = 6; // 5 intervals
    protected Scale _xScale;
    protected Scale _yScale;
    protected boolean resizeNow = false;

    ArrayList<GChart.Curve> _mainCurves;
    ArrayList<SpecificPointUI> _specificPoints;
    String specificPointsDesc;
    GChart.Curve _selectionCurve;
    boolean _selecting = false;
    protected Selection _savedZoomSelection = null;
    //boolean preserveOutOfBoundPoints = false;
    HTML _actionHelp;
    HTML _chartTitle = new HTML("");
    protected XYPlotOptionsPanel optionsPanel;
    protected XYPlotOptionsDialog optionsDialog;
    private ResizeTimer _resizeTimer= new ResizeTimer();
    int titleSize = 5;

    private int defaultChartW=0, defaultChartH=0;

    private List<NewDataListener> _listeners = new ArrayList<NewDataListener>();

    private static final FireflyCss _ffCss = CssData.Creator.getInstance().getFireflyCss();


    public XYPlotBasicWidget(XYPlotMeta meta) {
        super(300,180);
//        super(300, 180);
        _meta = meta;
        GChart.setCanvasFactory(ChartingFactory.getInstance());
        _popoutWidgetSet = false;

        _actionHelp = new HTML();
        _actionHelp.setWidth("100%");
        _actionHelp.addStyleName(_ffCss.fadedText());

        /**
        final CheckBox outOfBoundCheck = GwtUtil.makeCheckBox("Connect Out of Bounds Points",
                "Take into account out of bounds points that are reasonably close", false);
        outOfBoundCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            public void onValueChange(ValueChangeEvent valueChangeEvent) {
                if (_chart != null && _data != null) {
                    preserveOutOfBoundPoints = outOfBoundCheck.getValue();
                    if (preserveOutOfBoundPoints) {
                        _chart.getYAxis().setOutOfBoundsMultiplier(Double.NaN);
                        _chart.getY2Axis().setAxisMin(_chart.getY2Axis().getAxisMin());
                        _chart.update();
                    } else {
                        _chart.getYAxis().setOutOfBoundsMultiplier(0);
                        _chart.getY2Axis().setAxisMin(_chart.getY2Axis().getAxisMin());
                        _chart.update();
                    }
                }
            }
        });
         */

    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (_chart != null) {_chart.update();}
    }

    void showOptionsDialog() {
        if (optionsDialog == null) {
            optionsDialog = new XYPlotOptionsDialog(XYPlotBasicWidget.this);
        }
        optionsDialog.setVisible(true);

    }


    protected void setupNewChart(String title) {
        _selecting = false;
        _savedZoomSelection = null;

        if (!_popoutWidgetSet) {
            //_vertPanel.add(_cpanel);
            //_vertPanel.setWidth("100%");
            _cpanel.setWidth("100%");
            _panel.setWidth("100%");
//            _dockPanel.setSize("100%", "100%");
            _dockPanel.addStyleName("component-background");
            _dockPanel.addNorth(getMenuBar(), 37);
            _dockPanel.addWest(getOptionsPanel(), OPTIONS_PANEL_WIDTH);
            //_statusMessage = GwtUtil.makeFaddedHelp("&nbsp;");
            //GwtUtil.setStyles(_statusMessage, "textAlign", "left", "paddingTop", "2px", "borderTop", "1px solid #bbbbbb");
            //ScrollPanel statusPanel = new ScrollPanel();
            //statusPanel.setSize("100%", "100%");
            //statusPanel.add(_statusMessage);
            //_dockPanel.addSouth(statusPanel, 20);
            _dockPanel.add(_panel);
            GwtUtil.DockLayout.hideWidget(_dockPanel, optionsPanel);
            setPopoutWidget(_dockPanel);
            _popoutWidgetSet = true;
        }
        setTitle(title);
        _chartTitle.setHTML("");
        //removeCurrentChart();
        if (_chart == null) {
            _chart = new GChart(_meta.getXSize(), _meta.getYSize());
            _chart.setOptimizeForMemory(true);
            _chart.setPadding("5px");
            _chart.setLegendBorderWidth(0); // no border
            _chart.setBackgroundColor("white");
            _chart.setGridColor("#999999");
            _chart.setHoverParameterInterpreter(new XYHoverParameterInterpreter());
            _chart.setClipToPlotArea(true);
            _chart.setClipToDecoratedChart(false);
            Widget footnotes = GwtUtil.leftRightAlign(new Widget[]{_actionHelp}, new Widget[]{new HTML("&nbsp;"), HelpManager.makeHelpIcon("visualization.xyplotViewer")});
            footnotes.setWidth("100%");
            _chart.setChartFootnotes(footnotes);
            _chart.setChartFootnotesLeftJustified(true);
            _chart.setChartFootnotesThickness(20);
            addMouseListeners();
            _cpanel.setWidget(_chart);
        }

        // if we are not showing legend, inform the chart
        _chart.setLegendVisible(_showLegend || _meta.alwaysShowLegend());

    }

    protected Widget getMenuBar() {
        FlowPanel menuBar = new FlowPanel();
        //GwtUtil.setStyle(menuBar, "borderBottom", "1px solid #bbbbbb");
        menuBar.setWidth("100%");

        HorizontalPanel left = new HorizontalPanel();
        left.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
        left.setSpacing(10);
        GwtUtil.setStyle(left, "align", "left");

        HorizontalPanel right = new HorizontalPanel();
        right.setSpacing(10);
        GwtUtil.setStyle(right, "align", "center");
        GwtUtil.setStyle(right, "paddingRight", "20px");

        VisIconCreator ic= VisIconCreator.Creator.getInstance();
        right.add(GwtUtil.makeImageButton(new Image(ic.getZoomOriginalSmall()), "Zoom out to original chart", new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                if (_data != null) {
                    _savedZoomSelection = null;
                    _actionHelp.setHTML(ZOOM_IN_HELP);
                    if (XYPlotData.shouldSample(_dataSet.getSize())) {
                        _meta.userMeta.setXLimits(null);
                        _meta.userMeta.setYLimits(null);
                        updateMeta(_meta,false);
                    } else {
                        setChartAxes();
                        _chart.update();
                    }
                }
            }
         }));

        left.add(GwtUtil.makeImageButton(new Image(ic.getSettings()), "Plot options and tools", new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                showOptions();
            }
        }));

        left.add(_chartTitle);

        menuBar.add(GwtUtil.leftRightAlign(new Widget[]{left}, new Widget[]{right}));

        return menuBar;

    }

    public void makeNewChart(DataSet dataSet, String title) {
        removeCurrentChart();
        setupNewChart(title);
        try {
            _dataSet = dataSet;
            addData(new XYPlotData(dataSet, _meta));
            _selectionCurve = getSelectionCurve();
            if (optionsDialog != null && (optionsDialog.isVisible() || _meta.hasUserMeta())) {
                if (optionsDialog.setupError()) {
                    if (!optionsDialog.isVisible()) showOptionsDialog();
                }
            }
            if (_chart != null) {
                _chart.update();
            }

        } catch (Throwable e) {
            if (!StringUtils.isEmpty(e.getMessage()) && e.getMessage().indexOf("column is not found") > 0) {
                if (_chart != null) _chart.clearCurves();
                showOptionsDialog();
            } else {
                showMask(e.getMessage());
            }
        } finally {
            _panel.setWidget(_cpanel);
        }
    }

    private XYPlotOptionsPanel getOptionsPanel() {
        if (optionsPanel == null) {
            optionsPanel = new XYPlotOptionsPanel(this);
            GwtUtil.setStyle(optionsPanel, "paddingTop", "10px");
        }
        return optionsPanel;
    }

    protected void showOptions() {

        boolean show = !(optionsDialog!=null && optionsDialog.isVisible()) && GwtUtil.DockLayout.isHidden(optionsPanel);
        if (show) {
            if (_panel.asWidget().getOffsetWidth()>MIN_SIZE_FOR_DOCKED_OPTIONS) {
                GwtUtil.DockLayout.showWidget(_dockPanel, optionsPanel);
                onResize();
            } else {
                showOptionsDialog();
            }
        } else {
            if (!GwtUtil.DockLayout.isHidden(optionsPanel)) {
                GwtUtil.DockLayout.hideWidget(_dockPanel, optionsPanel);
                onResize();
            }
            if (optionsDialog != null && optionsDialog.isVisible()) {
                optionsDialog.setVisible(false);
            }
        }
    }

    public void removeCurrentChart() {
        if (_chart != null) {
            _chart.clearCurves();
            _mainCurves = new ArrayList<GChart.Curve>();
            _data = null;
            _panel.remove(_cpanel);
            //_chart = null;
            // back to default zoom mode
            _actionHelp.setHTML(ZOOM_IN_HELP);
            //_statusMessage.setHTML("");
        }
        _savedZoomSelection = null; // do not preserve zoomed selection
    }

    public XYPlotMeta getPlotMeta() {
        return _meta;
    }


    public XYPlotData getPlotData() {
        return _data;
    }

    public void addListener(NewDataListener l) {
        _listeners.add(l);
    }

    protected void addMouseListeners() {


        _chart.addMouseDownHandler(new MouseDownHandler() {
            public void onMouseDown(MouseDownEvent event) {

                AllPlots.getInstance().setSelectedPopoutWidget(XYPlotBasicWidget.this);
                if (_chart == null || _data==null) { return; }
                /*
                  * Most browsers, by default, support the ability to
                  * to "drag-copy" any web page image to the desktop.
                  * But GChart's rendering makes extensive use of
                  * images, so we need to override this default.
                  *
                  */
                event.preventDefault();
                event.stopPropagation();

                double x = _chart.getXAxis().getMouseCoordinate();
                double y = _chart.getYAxis().getMouseCoordinate();
                //if (_data.getXMinMax().isIn(x) && _data.getYMinMax().isIn(y)) {
                _selecting = true;
                _selectionCurve.clearPoints();
                _selectionCurve.addPoint(x, y);
                enableHover(false);
                //}
            }
        });

        _chart.addMouseMoveHandler(new MouseMoveHandler() {
            public void onMouseMove(MouseMoveEvent event) {
                if (_selecting) {
                    event.preventDefault();
                    event.stopPropagation();

                    double x = _chart.getXAxis().getMouseCoordinate();
                    double y = _chart.getYAxis().getMouseCoordinate();

                    //if (_data.getXMinMax().isIn(x) && _data.getYMinMax().isIn(y)) {
                        GChart.Curve.Point p0 = _selectionCurve.getPoint(0);
                        double x0 = p0.getX();
                        double y0 = p0.getY();

                        _selectionCurve.clearPoints();
                        _selectionCurve.addPoint(x0, y0);
                        _selectionCurve.addPoint(x, y0);
                        _selectionCurve.addPoint(x, y);
                        _selectionCurve.addPoint(x0, y);
                        _selectionCurve.addPoint(x0,y0);
                        _selectionCurve.setVisible(true);
                        _chart.update();
                    //}
                }
            }
        });

        _chart.addMouseUpHandler(new MouseUpHandler() {
            public void onMouseUp(MouseUpEvent event) {
                if (_selecting) {
                    event.preventDefault();
                    event.stopPropagation();
                    _selectionCurve.setVisible(false);
                    enableHover(true);
                    if (_selectionCurve.getNPoints() == 5 && _data != null) {
                        // diagonal points of the selection rectangle
                        GChart.Curve.Point p0 = _selectionCurve.getPoint(0);
                        GChart.Curve.Point p2 = _selectionCurve.getPoint(2);
                        double xMin = Math.min(_xScale.getUnscaled(p0.getX()), _xScale.getUnscaled(p2.getX()));
                        double xMax = Math.max(_xScale.getUnscaled(p0.getX()), _xScale.getUnscaled(p2.getX()));
                        double yMin = Math.min(_yScale.getUnscaled(p0.getY()), _yScale.getUnscaled(p2.getY()));
                        double yMax = Math.max(_yScale.getUnscaled(p0.getY()), _yScale.getUnscaled(p2.getY()));
                        MinMax xMinMax = new MinMax(xMin, xMax);
                        MinMax yMinMax = new MinMax(yMin, yMax);
                        onSelection(xMinMax, yMinMax);
                    } else {
                        _chart.update();
                    }
                    _selecting = false;
                }
            }
        });
    }

    protected void enableHover(boolean enable) {
        for (GChart.Curve mainCurve : _mainCurves) {
            mainCurve.getSymbol().setHoverSelectionEnabled(enable);
            mainCurve.getSymbol().setHoverAnnotationEnabled(enable);
        }
    }

    protected void onSelection(MinMax xMinMax, MinMax yMinMax) {
        if (_data.isSampled()) {
           _meta.userMeta.setXLimits(xMinMax);
           _meta.userMeta.setYLimits(yMinMax);
           updateMeta(_meta, false);
        } else {
            // clear previous limits, if any
            _meta.userMeta.setXLimits(null);
            _meta.userMeta.setYLimits(null);
        }
        setChartAxesForSelection(xMinMax, yMinMax);
        _chart.update();
    }

    protected GChart.Curve getSelectionCurve() {
        _chart.addCurve();
        GChart.Curve selectionCurve = _chart.getCurve();
        GChart.Symbol symbol= selectionCurve.getSymbol();
        symbol.setBorderColor("black");
        symbol.setSymbolType(GChart.SymbolType.LINE);
        symbol.setFillThickness(2);
        symbol.setWidth(0);
        symbol.setHeight(0);
        symbol.setHoverSelectionEnabled(false);
        symbol.setHoverAnnotationEnabled(false);
        selectionCurve.setVisible(false);
        return selectionCurve;
    }

    private Widget createLegend() {
        int nCurves = _mainCurves.size();
        int nPoints = _specificPoints.size();
        if  (_data == null || (nCurves<2 && nPoints<1)) return null;
        Grid result = new Grid(nCurves+(nPoints>0 ? (nPoints+1) : 0), 1);
        int cIdx = 0;
        if (nCurves > 1) {
            for (final GChart.Curve c : _mainCurves) {
                c.getSymbol().getBorderColor();
                final CheckBox ch = GwtUtil.makeCheckBox(c.getLegendLabel(), "Deselect to hide", true);
                ch.getElement().getStyle().setProperty("color", c.getSymbol().getBackgroundColor());
                ch.addClickHandler(new ClickHandler() {

                    public void onClick(ClickEvent event) {
                        boolean visible = ch.getValue();
                        c.setVisible(visible);
                        // 2 error curves are added for each main curve
                        // error curves are added before main curves
                        if (_meta.plotError() && _data.hasError()) {
                            int cIdx = _mainCurves.indexOf(c);
                            XYPlotData.Curve current = _data.getCurveData().get(cIdx);
                            int lowerErrIdx = current.getErrorLowerCurveIdx();
                            int upperErrIdx = current.getErrorUpperCurveIdx();

                            try {
                                for (int i=lowerErrIdx; i<=upperErrIdx; i++) {
                                    _chart.getCurve(i).setVisible(visible);
                                }
                            } catch (Exception e) { _meta.setPlotError(false); }
                        }
                        _chart.update();
                    }
                });
                result.setWidget(cIdx, 0, ch);
                cIdx++;
            }
        }
        int pIdx = 0;

        if (_meta.plotSpecificPoints() && nPoints>0) {
            Label desc = new HTML(cIdx>0?"<br>":""+"<b>"+specificPointsDesc.replaceAll(" ", "<br>")+"</b>");
            result.setWidget(cIdx, 0, desc);   //"&nbsp;"

            for (final SpecificPointUI pointUI : _specificPoints) {
                final CheckBox ch = GwtUtil.makeCheckBox(pointUI.p.getLabel(), "Deselect to hide", true);
                ch.getElement().getStyle().setProperty("color", pointUI.spCurve.getSymbol().getBackgroundColor());
                ch.addClickHandler(new ClickHandler() {

                    public void onClick(ClickEvent event) {
                        pointUI.setVisible(ch.getValue());
                        _chart.update();
                    }
                });
                result.setWidget(cIdx+pIdx+1, 0, ch);
                pIdx++;
            }
        }
        return result;
    }

    protected void showMask(String text) {
        _maskMessge.setHTML(text == null ? "Failure" : text);
        _maskPane.show();
    }

    public void updateMeta(final XYPlotMeta meta, final boolean preserveZoomSelection) {
        try {
            _meta = meta;
            if (_chart != null) {
                _chart.clearCurves();

                // force to reevaluate chart size
                reevaluateChartSize(true);
                //update chart
                addData(new XYPlotData(_dataSet, _meta));

                _selectionCurve = getSelectionCurve();
                if (_savedZoomSelection != null && preserveZoomSelection) {
                    setChartAxesForSelection(_savedZoomSelection.xMinMax, _savedZoomSelection.yMinMax);
                } else {
                    _savedZoomSelection = null;
                }

                _chart.update();
            }
        } catch (Throwable e) {
            if (_chart != null) {
                _chart.clearCurves();
            }
            PopupUtil.showError("Error",e.getMessage());
        }

    }

    protected void addData(XYPlotData data) {
        _data = data;

        _xScale = _meta.getXScale();
        _yScale = _meta.getYScale();

        String errorTitle = null;
        if (_xScale instanceof LogScale && _data.getXMinMax().getMin()<=0.0) {
            errorTitle = "Using linear scale for X";
            _xScale = XYPlotMeta.LINEAR_SCALE;
            _meta.setXScale(_xScale);
        }
        if (_yScale instanceof LogScale &&
                (_meta.plotError() && _data.hasError()?_data.getWithErrorMinMax():_data.getYMinMax()).getMin()<=0.0) {
            errorTitle = (StringUtils.isEmpty(errorTitle) ? "Using linear scale for Y" : errorTitle+" and Y");
            _yScale = XYPlotMeta.LINEAR_SCALE;
            _meta.setYScale(_yScale);
        }
        if (!StringUtils.isEmpty(errorTitle)) {
            PopupUtil.showError(errorTitle, "Data set contains negative values or zero.");
        }

        // call listeners
        for (NewDataListener l : _listeners) {
            l.newData(_data);
        }

        // chart title;
        if (_meta.getTitle() != null) {
            if (!_meta.getTitle().equalsIgnoreCase("none")) {
               _chartTitle.setHTML("<b>"+_meta.getTitle()+"</b>");
            }
        } else {
            _chartTitle.setHTML("<b>"+_meta.getYName(_data)+" vs. "+_meta.getXName(_data)+"</b>");
        }
        _chart.setChartTitle("&nbsp");
        _chart.setChartTitleThickness(titleSize);

        // make sure we start with clean chart
        _chart.clearCurves();

        // error curves - should be plotted first,
        // so that main curves are plotted on top of them
        if (_meta.plotError() && _data.hasError()) {
            addErrorCurves();
        }

        // main curves
        addMainCurves();

        // add specific points
        addSpecificPoints();

        // set axes
        setChartAxes();
        setGridlines();

        // set legend
        _legend = createLegend();
        _chart.setLegend(_legend);
        updateLegendVisibility();
        _chart.update();
    }

    private void updateLegendVisibility() {
        boolean showLegend = _legend != null && (_showLegend || _meta.alwaysShowLegend());
        if (_legend != null ) { _legend.setVisible(showLegend); }
        _chart.setLegendVisible(showLegend);
        _chart.update();
    }

    public void setGridlines() {
        if (_chart == null) { return; }
        if (_meta.noGrid()) {
            _chart.getXAxis().setHasGridlines(false);
            _chart.getYAxis().setHasGridlines(false);
        } else {
            _chart.getXAxis().setHasGridlines(true);
            _chart.getYAxis().setHasGridlines(true);
        }
        _chart.update();
    }

    protected void setDefaultActionHelp() {
        _actionHelp.setHTML(ZOOM_IN_HELP);
    }

    private void setHoverLocation(GChart.Symbol symbol) {
        /*
        // make sure popup is visible in the upper left corner
        symbol.setHoverAnnotationSymbolType(GChart.SymbolType.ANCHOR_NORTHWEST);
        symbol.setHoverLocation(GChart.AnnotationLocation.SOUTHEAST);
        symbol.setHoverXShift(-3);
        symbol.setHoverYShift(titleSize+3);
        */
        if (_meta.userMeta != null && _meta.userMeta.stretchToFill) {
            symbol.setHoverLocation(GChart.AnnotationLocation.SOUTH);
        } else {
            // make sure popup is visible in the lower right corner
            symbol.setHoverAnnotationSymbolType(GChart.SymbolType.ANCHOR_SOUTHEAST);
            symbol.setHoverLocation(GChart.AnnotationLocation.SOUTHWEST);
        }
        symbol.setHoverYShift(-20);
    }

    private void addMainCurves() {
        // if data are sampled, make sure the style is unconnected points
        if (_data.isSampled()) { _meta.setPlotStyle(XYPlotMeta.PlotStyle.POINTS); }

        _mainCurves = new ArrayList<GChart.Curve>(_data.getCurveData().size());
        GChart.Curve curve;

        // weight based order - colors and map string  (Color.makeSimpleColorMap return hex without "#")
        // 6 colors (use http://colorbrewer2.org)
        String[] WBO_COLS = {"#d9d9d9", "#BDBDBD", "#969696", "#737373", "#525252", "#252525"};

        String WBO_MAP_STR = "ABCDEF";

        for (XYPlotData.Curve cd : _data.getCurveData() ) {
            _chart.addCurve();
            curve = _chart.getCurve();
            _mainCurves.add(curve);

            if (_data.hasOrder()) {
                curve.setLegendLabel("Order " + cd.getOrder());
            } else if (_data.hasWeightBasedOrder()) {
                curve.setLegendLabel(cd.getOrder().substring(2)); // to omit the key
            }
            GChart.Symbol symbol= curve.getSymbol();
            symbol.setBorderColor(colors[cd.getCurveId() % colors.length]);
            if (_meta.plotStyle().equals(XYPlotMeta.PlotStyle.POINTS)) {
                symbol.setWidth(3);
                symbol.setHeight(3);
            } else if (_meta.plotStyle().equals(XYPlotMeta.PlotStyle.LINE_POINTS)) {
                symbol.setSymbolType(GChart.SymbolType.LINE);
                symbol.setFillSpacing(0);
                symbol.setFillThickness(1);
                symbol.setWidth(3);
                symbol.setHeight(3);
            } else {
                symbol.setSymbolType(GChart.SymbolType.LINE);
                symbol.setFillSpacing(0);
                symbol.setFillThickness(2);
                symbol.setWidth(0);
                symbol.setHeight(0);
            }
            if (_data.isSampled() && !symbol.getSymbolType().equals(GChart.SymbolType.LINE)) {
                int w,h;
                w = 5; h = 5;

                boolean found = false;
                String bgColor = "black";
                for (int i= 0; i<6; i++){
                    if (cd.getOrder().charAt(0) == WBO_MAP_STR.charAt(i)) {
                        bgColor = WBO_COLS[i];
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    w = 7; h = 7; // should not see
                }
                symbol.setBackgroundColor(bgColor);
                symbol.setBorderColor(bgColor);
                //symbol.setBorderColor("#f7f7f7"); // light colored border

                symbol.setWidth(w);
                symbol.setHeight(h);
            }  else {
                symbol.setBackgroundColor(symbol.getBorderColor());
            }
            symbol.setHoverSelectionEnabled(true);
            //symbol.setBrushHeight(2*_meta.getYSize());
            symbol.setBrushHeight(5);  // to facilitate selection
            symbol.setBrushWidth(5);
            symbol.setHoverSelectionWidth(4);
            symbol.setHoverSelectionHeight(4);
            symbol.setHoverSelectionBackgroundColor("yellow");
            symbol.setHoverSelectionBorderColor(_data.hasWeightBasedOrder() ? "black" : symbol.getBorderColor());
            setHoverLocation(symbol);

            String xColUnits = getXColUnits();
            String yColUnits = getYColUnits();
            String template = _meta.getXName(_data)+" = ${x}" +
                    (xColUnits != null ? " "+xColUnits : "") +
                    "<br>"+_meta.getYName(_data)+" = ${y}" +
                    (yColUnits != null ?  " "+yColUnits : "");

            if (_data.hasError()) {
                template += "<br>"+_data.getErrorCol()+" = +/- ${err}";
                String errorColUnits = _data.getErrorColUnits();
                if (errorColUnits != null) template += " "+errorColUnits;
            }
            if (_data.isSampled()) {
                template += "${pts}";
            }
            symbol.setHovertextTemplate(GChart.formatAsHovertext(template));

            cd.setCurveIdx(_chart.getCurveIndex(curve));
            for (XYPlotData.Point p : cd.getPoints()) {
                curve.addPoint(_xScale.getScaled(p.getX()),_yScale.getScaled(p.getY()));
            }
        }
    }

    private void addErrorCurves() {
        GChart.Curve  errCurveLower, errCurveUpper, errBarCurve;
        for (XYPlotData.Curve cd : _data.getCurveData() ) {

            //lower error curve
            _chart.addCurve();
            errCurveLower = _chart.getCurve();

            GChart.Symbol errSymbolLower= errCurveLower.getSymbol();
            errSymbolLower.setBorderColor("lightgray");
            errSymbolLower.setBackgroundColor("lightgray");
            if (_meta.plotStyle().equals(XYPlotMeta.PlotStyle.POINTS)) {
                errSymbolLower.setWidth(3);
                errSymbolLower.setHeight(1);
            } else {
                errSymbolLower.setSymbolType(GChart.SymbolType.LINE);
                errSymbolLower.setFillThickness(1);
                errSymbolLower.setFillSpacing(0);
                errSymbolLower.setWidth(0);
                errSymbolLower.setHeight(0);
            }

            errSymbolLower.setHoverAnnotationEnabled(false);
            double err;
            for (XYPlotData.Point p : cd.getPoints()) {
                err = p.getError();
                errCurveLower.addPoint(_xScale.getScaled(p.getX()), Double.isNaN(err) ? Double.NaN : _yScale.getScaled(p.getY()-err));
            }

            // add error bars
            if (_meta.plotStyle().equals(XYPlotMeta.PlotStyle.POINTS)) {
                if (_yScale instanceof LinearScale) {
                    // one point is enough for linear scale
                    for (XYPlotData.Point p : cd.getPoints()) {
                        err = _yScale.getScaled(p.getError());
                        if (!Double.isNaN(err)) {
                            _chart.addCurve();
                            errBarCurve = _chart.getCurve();
                            GChart.Symbol errSymbol= errBarCurve.getSymbol();
                            errSymbol.setBorderColor("lightgray");
                            errSymbol.setBackgroundColor("lightgray");
                            errSymbol.setWidth(1);
                            errSymbol.setModelHeight(2*err);
                            errBarCurve.addPoint(_xScale.getScaled(p.getX()), _yScale.getScaled(p.getY()));
                        }
                    }
                } else {
                    // need two points for bar curve
                    for (XYPlotData.Point p : cd.getPoints()) {
                        err = p.getError(); // should not be scaled here
                        if (!Double.isNaN(err)) {
                            _chart.addCurve();
                            errBarCurve = _chart.getCurve();
                            GChart.Symbol errSymbol= errBarCurve.getSymbol();
                            errSymbol.setSymbolType(GChart.SymbolType.LINE);
                            errSymbol.setFillThickness(1);
                            errSymbol.setFillSpacing(0);
                            errSymbol.setWidth(0);
                            errSymbol.setHeight(0);
                            errSymbol.setBorderColor("lightgray");
                            errSymbol.setBackgroundColor("lightgray");
                            errBarCurve.addPoint(_xScale.getScaled(p.getX()), _yScale.getScaled(p.getY()-err));
                            errBarCurve.addPoint(_xScale.getScaled(p.getX()), _yScale.getScaled(p.getY()+err));
                        }
                    }
                }
            }

            //upper error curve
            _chart.addCurve();
            errCurveUpper = _chart.getCurve();

            GChart.Symbol errSymbolUpper= errCurveUpper.getSymbol();
            errSymbolUpper.setBorderColor("lightgray");
            errSymbolUpper.setBackgroundColor("lightgray");
            if (_meta.plotStyle().equals(XYPlotMeta.PlotStyle.POINTS)) {
                errSymbolUpper.setWidth(3);
                errSymbolUpper.setHeight(1);
            } else {
                errSymbolUpper.setSymbolType(GChart.SymbolType.LINE);
                errSymbolUpper.setFillThickness(1);
                errSymbolUpper.setFillSpacing(0);
                errSymbolUpper.setWidth(0);
                errSymbolUpper.setHeight(0);
            }
            errSymbolUpper.setHoverAnnotationEnabled(false);
            for (XYPlotData.Point p : cd.getPoints()) {
                err = p.getError();
                errCurveUpper.addPoint(_xScale.getScaled(p.getX()), Double.isNaN(err) ? Double.NaN : (_yScale.getScaled(p.getY()+err)));
            }

            cd.setErrorIdx(_chart.getCurveIndex(errCurveLower), _chart.getCurveIndex(errCurveUpper));
        }
    }

    private void addSpecificPoints() {
        _specificPoints = new ArrayList<SpecificPointUI>();
        String borderColor;
        String backgroundColor;
        if (_meta.plotSpecificPoints() && _data.hasSpecificPoints()) {
            MinMax xMinMax = _data.getXMinMax();
            MinMax yMinMax;
            if (_meta.plotError() && _data.hasError()) {
                yMinMax = _data.getWithErrorMinMax();
            }  else {
                yMinMax = _data.getYMinMax();
            }

            SpecificPoints specificPoints = _data.getSpecificPoints();
            specificPointsDesc = specificPoints.getDescription();
            boolean isSED = specificPointsDesc.startsWith("SED");
            boolean isUpperLimit = false;
            for (int i=0; i<specificPoints.getNumPoints(); i++) {
                SpecificPoints.Point p = specificPoints.getPoint(i);
                MinMax x =  p.getXMinMax();
                MinMax y = p.getYMinMax();
                if (xMinMax.isIn(x.getReference()) &&
                        yMinMax.isIn(y.getReference())) {

                    if (isSED) {
                        int colorIdx = p.getId() % sedcolors.length;
                        borderColor = sedcolors[colorIdx];
                        backgroundColor = sedcolors[colorIdx];
                        isUpperLimit = (p.getId() == 0);
                    } else {
                        int colorIdx = p.getId() % lightcolors.length;
                        borderColor = lightcolors[colorIdx];
                        backgroundColor = lightcolors[colorIdx];
                    }

                    // simulate point with two lines vertical and horizontal
                    //dotted x-line
                    _chart.addCurve();
                    GChart.Curve xCurve = _chart.getCurve();
                    xCurve.setLegendLabel(p.getLabel());

                    GChart.Symbol symbol= xCurve.getSymbol();
                    symbol.setBorderColor(borderColor);
                    symbol.setBackgroundColor(backgroundColor);
                    symbol.setSymbolType(GChart.SymbolType.LINE);
                    symbol.setFillThickness(1);
                    symbol.setFillSpacing(2);
                    symbol.setWidth(0);
                    symbol.setHeight(0);
                    symbol.setHoverAnnotationEnabled(false);

                    xCurve.addPoint(_xScale.getScaled(x.getMin()), _yScale.getScaled(y.getReference()));
                    xCurve.addPoint(_xScale.getScaled(x.getMax()), _yScale.getScaled(y.getReference()));

                    // dotted y-line
                    _chart.addCurve();
                    GChart.Curve yCurve = _chart.getCurve();
                    symbol= yCurve.getSymbol();
                    symbol.setBorderColor("black");
                    symbol.setBackgroundColor("black");
                    symbol.setHoverAnnotationEnabled(false);


                    if (isSED && isUpperLimit) {
                        // show upper limits by down arrow
                        yCurve.addPoint(_xScale.getScaled(x.getReference()), _yScale.getScaled(y.getReference()));

                        yCurve.getPoint().setAnnotationLocation(GChart.AnnotationLocation.SOUTH);
                        yCurve.getPoint().setAnnotationFontColor("black");
                        yCurve.getPoint().setAnnotationXShift(0);
                        yCurve.getPoint().setAnnotationYShift(3);
                        //yCurve.getPoint().setAnnotationWidget(new HTML("<b>&darr;</b>"));
                        //yCurve.getPoint().setAnnotationFontSize(12);
                        yCurve.getPoint().setAnnotationText("<html><b>&darr;</b></html>");
                    } else {
                        yCurve.addPoint(_xScale.getScaled(x.getReference()), _yScale.getScaled(y.getMin()));
                        yCurve.addPoint(_xScale.getScaled(x.getReference()), _yScale.getScaled(y.getMax()));

                        symbol.setSymbolType(GChart.SymbolType.LINE);
                        symbol.setFillThickness(1);
                        symbol.setFillSpacing(1);
                        symbol.setWidth(0);
                        symbol.setHeight(0);
                    }


                    _chart.addCurve();
                    GChart.Curve spCurve = _chart.getCurve();
                    spCurve.addPoint(_xScale.getScaled(x.getReference()), _yScale.getScaled(y.getReference()));
                    spCurve.getPoint().setAnnotationLocation(GChart.AnnotationLocation.NORTHEAST);
                    spCurve.getPoint().setAnnotationFontColor(borderColor);
                    //spCurve.getPoint().setAnnotationFontSize(8);
                    spCurve.getPoint().setAnnotationXShift(-3);
                    spCurve.getPoint().setAnnotationYShift(3);
                    spCurve.getPoint().setAnnotationText(p.getLabel());

                    symbol= spCurve.getSymbol();
                    symbol.setBorderColor("Black");
                    symbol.setBackgroundColor(backgroundColor);
                    symbol.setSymbolType(GChart.SymbolType.BOX_CENTER);
                    symbol.setHoverSelectionEnabled(true);
                    symbol.setHoverSelectionBackgroundColor("black");
                    symbol.setHoverSelectionBorderColor(borderColor);
                    setHoverLocation(symbol);

                    String template = p.getDesc();
                    symbol.setHovertextTemplate(GChart.formatAsHovertext(template));

                    _specificPoints.add(new SpecificPointUI(p, spCurve, xCurve, yCurve));

                }
            }
            _chart.getXAxis().setOutOfBoundsMultiplier(Double.NaN);
            _chart.getYAxis().setOutOfBoundsMultiplier(Double.NaN);
        }
    }

    protected void setChartAxes() {
        MinMax xMinMax = _data.getXMinMax();
        MinMax yMinMax;
        if (_meta.plotError() && _data.hasError()) {
            yMinMax = _data.getWithErrorMinMax();
        }  else {
            yMinMax = _data.getYMinMax();
        }

        xMinMax = MinMax.ensureNonZeroRange(xMinMax);
        yMinMax = MinMax.ensureNonZeroRange(yMinMax);
        setChartAxes(xMinMax, yMinMax);

        // do not check for out of bounds points
        _chart.getXAxis().setOutOfBoundsMultiplier(Double.NaN);
        _chart.getYAxis().setOutOfBoundsMultiplier(Double.NaN);
        setDefaultActionHelp();
    }

    protected void setChartAxesForSelection(MinMax xMinMax, MinMax yMinMax) {
        int numPoints = _data.getNPoints(xMinMax, yMinMax);
        if (numPoints > 0) {
            setChartAxes(xMinMax, yMinMax);
            _savedZoomSelection = new Selection(xMinMax, yMinMax);
            // do not render points that are out of bounds
            //_chart.getXAxis().setOutOfBoundsMultiplier(0);
            //if (preserveOutOfBoundPoints || numPoints == 1) {
            //    _chart.getYAxis().setOutOfBoundsMultiplier(Double.NaN);
            //} else {
            //    _chart.getYAxis().setOutOfBoundsMultiplier(0);
            //}
            _actionHelp.setHTML(ZOOM_OUT_HELP);
        }
    }

    private void setChartAxes(MinMax xMinMax, MinMax yMinMax) {
        // set axes min/max and ticks
        GChart.Axis xAxis= _chart.getXAxis();
        GChart.Axis yAxis= _chart.getYAxis();
        String xUnits = getXColUnits();
        xAxis.setAxisLabel(_meta.getXName(_data) + (StringUtils.isEmpty(xUnits) ? "" : " (" + xUnits + ")"));
        if (_xScale instanceof LogScale) {
            setLogScaleAxis(xAxis, xMinMax, TICKS * _xResizeFactor);
        } else {
            setLinearScaleAxis(xAxis, xMinMax, TICKS * _xResizeFactor);
        }

        String yName = _meta.getYName(_data);
        Widget yLabel;
        int yLabelLines = 1;
        if (getYColUnits().length() > 0) {
            if  (yName.length()+getYColUnits().length() > 20)  yLabelLines++;
            yLabel =  new HTML(yName + (yLabelLines>1 ? "<br>" : " ") + "(" + getYColUnits() +")");
        } else {
            yLabel =  new HTML(yName);
        }
        yLabel.addStyleName(_ffCss.rotateLeft());
        yAxis.setAxisLabel(yLabel);
        yAxis.setAxisLabelThickness(yLabelLines*20);
        if (_yScale instanceof LogScale) {
            setLogScaleAxis(yAxis, yMinMax, TICKS * _yResizeFactor);
        } else {
            setLinearScaleAxis(yAxis, yMinMax, TICKS * _yResizeFactor);
        }

        // adjust symbol size for sampled data
        // comment it if you'd like same size symbols
        adjustSymbolSize();
    }


    private void adjustSymbolSize() {
        if (_data.isSampled()) {
            double xSampleBinSize = _data.getXSampleBinSize();
            double ySampleBinSize = _data.getYSampleBinSize();
            if (xSampleBinSize > 0 && ySampleBinSize > 0) {
                // data minMax are slightly less than axes minMax
                double xMin = _chart.getXAxis().getAxisMin();
                double xMax = _chart.getXAxis().getAxisMax();
                double yMin = _chart.getYAxis().getAxisMin();
                double yMax = _chart.getYAxis().getAxisMax();
                _chart.update();
                int xPixelSize = (_xScale instanceof LogScale) ? 5 : (int)Math.ceil(xSampleBinSize*_chart.getXChartSize()/(xMax-xMin));
                int yPixelSize = (_yScale instanceof LogScale) ? 5 : (int)Math.ceil(ySampleBinSize*_chart.getYChartSize()/(yMax-yMin));
                // pad with 1px, to avoid empty horizontal or vertical lines
                // not sure padding is a good idea, because it obscures symbol size
                // if (xPixelSize <= 8) { xPixelSize += 1; }
                // if (yPixelSize <= 8) { yPixelSize += 1; }
                GChart.Symbol s;
                for (GChart.Curve curve : _mainCurves) {
                    s = curve.getSymbol();
                    if (xPixelSize > 8 && yPixelSize > 8) {
                        s.setBorderColor("#ABABAB"); // between 2nd and 3rd
                    } else {
                        s.setBorderColor(s.getBackgroundColor());
                    }
                    if (curve.getLegendLabel().endsWith("pt")) {
                        // single points
                        int size = Math.min(xPixelSize, yPixelSize);
                        if (size > 3) size -= 1; // make the size a bit smaller than for aggregated points
                        size = Math.min(size, 5);
                        s.setWidth(size);
                        s.setHeight(size);
                        continue;
                    }

                    // use model size to set symbol width and height
                    s.setModelWidth(xSampleBinSize);
                    s.setModelHeight(ySampleBinSize);
                }
                _chart.update();
            }
        }
    }


    private void setLinearScaleAxis(GChart.Axis axis, MinMax minMax, int maxTicks) {
        NiceScale numScale = new NiceScale(minMax, maxTicks);
        double min = numScale.getNiceMin();
        double max = numScale.getNiceMax();
        int tickCount = (int)Math.round(Math.abs((max-min)/numScale.getTickSpacing()))+1;
        axis.setAxisMin(min);
        axis.setAxisMax(max);

        if (tickCount > 0) { axis.setTickCount(tickCount); }
        String tickLabelFormat = numScale.getFormatString();
        axis.setTickLabelFormat(tickLabelFormat);
        axis.setTickLabelFontSize(10);
        if (axis.equals(_chart.getXAxis())) {
            // sparse tick labels, if they are too long
            int minLen = tickLabelFormat.length();
            int extraLen = (int)Math.floor(Math.log10(Math.abs(min)));
            if (extraLen > 0) minLen += extraLen;
            if (min < 0) minLen += 1;
            if (minLen > 8) {
                axis.setTicksPerLabel(3);
            } else if (minLen > 5) {
                axis.setTicksPerLabel(2);
            } else {
                axis.setTicksPerLabel(1);
            }
        }
    }

    private void setLogScaleAxis(GChart.Axis axis, MinMax minMax, int maxTicks) {
        axis.clearTicks();
        axis.setTickLabelFormat("=10^#.##########");
        axis.setTickLabelFontSize(10);

        if (minMax.getMin() <= 0.0) {
            // this should not happen,
            // but if it does lmin will be -Infinity,
            // we don't want to do any calculations
            // at this point
            return;
        }
        double lmin = Math.floor(Math.log10(minMax.getMin()));
        double lmax = Math.ceil(Math.log10(minMax.getMax()));
        axis.setAxisMin(lmin);
        axis.setAxisMax(lmax);


        if (Math.abs(lmax-lmin) <= maxTicks) {
            //show conventional log scale ticks
            axis.addTick(lmin);
            for (double x=Math.pow(10,lmin); x < Math.pow(10,lmax); x*=10)  {
                for (int y = 2; y <= 10; y++) {
                    if (y==10) { axis.addTick(Math.log10(x*y)); }
                    else { axis.addTick(Math.log10(x*y), ""); }
                }
            }
        } else {
            int scale = (Math.abs(lmax-lmin)<=maxTicks) ? 1 : (int)Math.ceil(Math.abs(lmax-lmin)/maxTicks);
            for (double x = lmin; x<=lmax; x+=scale)  {
                axis.addTick(x);
            }
        }
    }


    private String getXColUnits() {
        if (_data == null) {
            return "";
        } else if (_meta.userMeta != null && !StringUtils.isEmpty(_meta.userMeta.xUnit)) {
            return _meta.userMeta.xUnit;
        }
        String xUnits = _data.getXUnits();
        if (xUnits == null || xUnits.trim().length()<1 || xUnits.equals("null")) {
            xUnits = _meta.getDefaultXUnits(_data);
        }
        return xUnits;
    }

    private String getYColUnits() {
        if (_data == null) {
            return "";
        } else if (_meta.userMeta != null && !StringUtils.isEmpty(_meta.userMeta.yUnit)) {
            return _meta.userMeta.yUnit;
        }
        String yUnits = _data.getYUnits();
        if (yUnits == null || yUnits.trim().length()<1 || yUnits.equals("null")) {
            yUnits = _meta.getDefaultYUnits(_data);
        }
        return yUnits;
    }


    private double getError(GChart.Curve.Point hoveredOver) {
        double error = Double.MIN_VALUE;
        if (_data.hasError()) {
            try {
                XYPlotData.Point point = getDataPoint(hoveredOver);
                if (point != null) {
                    error = point.getError();
                }
            } catch (Throwable ignored) {}
        }
        return error;
    }

    public List<TableDataView.Column> getColumns() {
        if (_dataSet != null) {
            return _dataSet.getColumns();
        } else {
            return new ArrayList<TableDataView.Column>(0);
        }
    }

    @Override
    public void widgetResized(int width, int height) {
        if (resizeNow) {
            resize(width,height);
        } else {
            _resizeTimer.cancel();
            _resizeTimer.setupCall(width, height);
            _resizeTimer.schedule(RESIZE_DELAY);
        }
    }

    public void onPostExpandCollapse(boolean expanded) {
        if (_chart != null && !_meta.alwaysShowLegend() && _showLegend != expanded) {
            _showLegend = expanded;
            updateLegendVisibility();
            //_chart.update();
        }
    }

    /*
     * Reevaluate chart size based chart area width and height from the last resize and meta
     * @param forceUpdate - update chart size even for slight changes
     * @return true if chart needs to be updated because of size change
     */
    protected boolean reevaluateChartSize(boolean forceUpdate) {
        int w = defaultChartW;
        int h = defaultChartH;
        int MIN_H = 90;
        int MIN_W = 150;
        if (w < MIN_W) w = MIN_W;
        if (h < MIN_H) h = MIN_H;
        if (_meta.userMeta == null || _meta.userMeta.aspectRatio <= 0) {
            h = Math.min((int)(0.6*w), h);
        } else {
            double xyRatio = _meta.userMeta.aspectRatio;
            if (_meta.userMeta.stretchToFill) {
                // fill
                h = (int)(w/xyRatio);
                if (h < MIN_H) { h = MIN_H; w=(int)(xyRatio*h); }
            } else {
                // fit
                double currentXYRatio = (double)w/(double)h;
                if (currentXYRatio > xyRatio) {
                    w = (int)(h * xyRatio);
                    if (w < MIN_W) { w = MIN_W; h = (int)(w/xyRatio); }
                } else {
                    h = (int)(w/xyRatio);
                    if (h < MIN_H) { h = MIN_H; w=(int)(xyRatio*h); }
                }
            }
        }


        // check if size of the chart changed significantly
        double widthChangePercent = 100*Math.abs(w-_meta.getXSize())/((double)_meta.getXSize());
        double heightChangePercent = 100*Math.abs(h-_meta.getYSize())/((double)_meta.getYSize());

        if (!forceUpdate && widthChangePercent < 10 && heightChangePercent < 10) {
            return false;
        }

        _meta.setChartSize(w, h);
        if (_chart != null) _chart.setChartSize(w, h);

        _xResizeFactor = (int)Math.ceil(w/330.0);
        _yResizeFactor = (int)Math.ceil(h/300.0);

        return true;
    }

    protected void resize(int width, int height) {
        if (_meta != null) {
            if (width == 0 || height == 0) return;

            //width = _dockPanel.getOffsetWidth();
            //height = _dockPanel.getOffsetHeight();
            if (!GwtUtil.DockLayout.isHidden(optionsPanel)) {
                if (width < MIN_SIZE_FOR_DOCKED_OPTIONS) {
                    //hide options
                    GwtUtil.DockLayout.hideWidget(_dockPanel, optionsPanel);
                } else {
                    width = width-OPTIONS_PANEL_WIDTH;
                }
            }

            int w = width - 10; // chart padding
            int h = height - 37 - 10;  // menu bar, chart padding

            if (_chart == null) {
                return;
            }

            h -= _chart.getYChartSizeDecorated()-_chart.getYChartSize();
            h *= .90F;

            w -= _chart.getXChartSizeDecorated()-_chart.getXChartSize();
            w *= .93F;

            defaultChartW = w;
            defaultChartH = h;
            // reevaluate chart size, if needed
            if (!reevaluateChartSize(false)) {
                // slight size change, no need to update
                return;
            }

            if (_data != null) {
                //if (_data.isSampled()) {
                //    updateMeta(_meta, true);
                //} else {
                if (_savedZoomSelection != null) {
                    setChartAxesForSelection(_savedZoomSelection.xMinMax, _savedZoomSelection.yMinMax);
                } else {
                    setChartAxes();
                }
                _chart.update();
                //_panel.setVerticalScrollPosition((_panel.getMaximumVerticalScrollPosition()-_panel.getMinimumVerticalScrollPosition())/2);
                _panel.scrollToTop();
                //}
            }
        }
    }

    protected XYPlotData.Point getDataPoint(GChart.Curve.Point p) {
        if (_data!=null && _mainCurves.size()>0) {
            int curveIdx = p.getParent().getParent().getCurveIndex(p.getParent());
            int pointIdx = p.getParent().getPointIndex(p);

            if (isMainCurve(curveIdx)) {
                return _data.getPoint(curveIdx, pointIdx);
            }
        }
        return null;
    }

    protected boolean isMainCurve(int curveIdx) {
        for (GChart.Curve curve : _mainCurves) {
            if (_chart.getCurveIndex(curve) == curveIdx) {
                return true;
            }
        }
        return false;
    }

    class Selection {
        MinMax xMinMax;
        MinMax yMinMax;
        Selection(MinMax xMinMax, MinMax yMinMax) {
            this.xMinMax = xMinMax;
            this.yMinMax = yMinMax;
        }
    }

    class XYHoverParameterInterpreter implements
            HoverParameterInterpreter {
        public String getHoverParameter(String paramName,
                                        GChart.Curve.Point hoveredOver) {
            String result = null;

            XYPlotData.Point point = getDataPoint(hoveredOver);
            if (point != null) {
                if ("x".equals(paramName))
                    result = point.getXStr();
                else if ("y".equals(paramName))
                    result = point.getYStr();
                else if ("err".equals(paramName)) {
                    result = point.getErrorStr();
                } else if ("pts".equals(paramName)) {
                    int numRepresented = point.getWeight();
                    result = numRepresented>1 ? ("<br><i>point represents "+numRepresented+" rows&nbsp;</i>") : "";
                }
            } else {
                if ("x".equals(paramName))
                    result = _chart.getXAxis().formatAsTickLabel(hoveredOver.getX());
                else if ("y".equals(paramName))
                    result = _chart.getYAxis().formatAsTickLabel(hoveredOver.getY());
                else if ("err".equals(paramName)) {
                    result = _chart.getYAxis().formatAsTickLabel(getError(hoveredOver));
                }
            }
            return result;

        }
    }

    class SpecificPointUI {
        SpecificPoints.Point p;
        GChart.Curve spCurve;
        GChart.Curve xCurve;
        GChart.Curve yCurve;

        SpecificPointUI(SpecificPoints.Point p, GChart.Curve spCurve, GChart.Curve xCurve, GChart.Curve yCurve) {
            this.p = p;
            this.spCurve = spCurve;
            this.xCurve = xCurve;
            this.yCurve = yCurve;
        }

        void setVisible(boolean visible) {
            this.xCurve.setVisible(visible);
            this.yCurve.setVisible(visible);
            this.spCurve.setVisible(visible);
        }

    }

    private class ResizeTimer extends Timer {
        private int w= 0;
        private int h= 0;

        public void setupCall(int w, int h) {
            this.w= w;
            this.h= h;
        }

        @Override
        public void run() { resize(w,h); }
    }

    public static interface NewDataListener {
        public void newData(XYPlotData data);
    }

}
