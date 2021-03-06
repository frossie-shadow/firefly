/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.data.fuse.provider;
/**
 * User: roby
 * Date: 7/25/14
 * Time: 12:45 PM
 */


import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.caltech.ipac.firefly.data.FinderChartRequestUtil;
import edu.caltech.ipac.firefly.data.Param;
import edu.caltech.ipac.firefly.data.ServerRequest;
import edu.caltech.ipac.firefly.data.table.TableData;
import edu.caltech.ipac.firefly.data.fuse.BaseImagePlotDefinition;
import edu.caltech.ipac.firefly.data.fuse.ImagePlotDefinition;
import edu.caltech.ipac.firefly.data.fuse.PlotData;
import edu.caltech.ipac.firefly.data.fuse.config.SelectedRowData;
import edu.caltech.ipac.firefly.ui.creator.CommonParams;
import edu.caltech.ipac.firefly.ui.creator.drawing.DatasetDrawingLayerProvider;
import edu.caltech.ipac.firefly.ui.table.EventHub;
import edu.caltech.ipac.firefly.util.Dimension;
import edu.caltech.ipac.firefly.visualize.Band;
import edu.caltech.ipac.firefly.visualize.WebPlotRequest;
import edu.caltech.ipac.firefly.visualize.draw.DrawSymbol;
import edu.caltech.ipac.util.StringUtils;
import edu.caltech.ipac.visualize.plot.WorldPt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static edu.caltech.ipac.firefly.data.FinderChartRequestUtil.ImageSet;
import static edu.caltech.ipac.firefly.data.fuse.DatasetInfoConverter.DataVisualizeMode.FITS;
import static edu.caltech.ipac.firefly.data.fuse.DatasetInfoConverter.DataVisualizeMode.FITS_3_COLOR;
import static edu.caltech.ipac.firefly.visualize.WebPlotRequest.ServiceType.DSS;
import static edu.caltech.ipac.firefly.visualize.WebPlotRequest.ServiceType.ISSA;
import static edu.caltech.ipac.firefly.visualize.WebPlotRequest.ServiceType.SDSS;
import static edu.caltech.ipac.firefly.visualize.WebPlotRequest.ServiceType.TWOMASS;
import static edu.caltech.ipac.firefly.visualize.WebPlotRequest.ServiceType.WISE;
import static edu.caltech.ipac.firefly.data.FinderChartRequestUtil.Artifact;

// convenience sharing of constants

/**
 * @author Trey Roby
 */
public class FinderChartDataSetInfoConverter extends AbstractDataSetInfoConverter {


    private BaseImagePlotDefinition imDef= null;
    private static NumberFormat _nf   = NumberFormat.getFormat("#.######");

    private enum ID { DSS1_BLUE, DSS1_RED, DSS2_BLUE, DSS2_RED, DSS2_IR,
                      SDSS_U, SDSS_G, SDSS_R, SDSS_I, SDSS_Z,
                      TWOMASS_J, TWOMASS_H,  TWOMASS_K,
                      WISE_1, WISE_2, WISE_3, WISE_4,
                      IRAS_12,IRAS_25,IRAS_60, IRAS_100 }

    private enum ID3 { DSS_3,
                       SDSS_3,
                       TWOMASS_3,
                       WISE_3C,
                       IRAS_3}

//    private static final HashMap<ID3,List<ID>> pref3Map = new HashMap<ID3, List<ID>>(7);
//    private static final HashMap<ID3,String> title3Map = new HashMap<ID3, String>(7);


    private static final String DEFAULT_SOURCES= "DSS,SDSS,TWOMASS,WISE";
    private static final float DEFAULT_SUBSIZE= .08F;
    private static List<String> idList= asIDList();
    private Dimension dimension= new Dimension(200,200);

    private List<Band> rgb= Arrays.asList(Band.RED,Band.GREEN,Band.BLUE);

    public FinderChartDataSetInfoConverter() {
        super(Arrays.asList(FITS,FITS_3_COLOR), new PlotData(new FCResolver(),true,false,false),"target");


        PlotData pd= getPlotData();

        pd.set3ColorIDOfIDs(ID3.DSS_3.name(), makeIDList(ID.DSS2_IR, ID.DSS2_RED, ID.DSS2_BLUE , ID.DSS1_BLUE, ID.DSS1_RED));
        pd.set3ColorIDOfIDs(ID3.SDSS_3.name(), makeIDList(ID.SDSS_Z, ID.SDSS_R, ID.SDSS_U, ID.SDSS_G,  ID.SDSS_I));
        pd.set3ColorIDOfIDs(ID3.TWOMASS_3.name(), makeIDList(ID.TWOMASS_K, ID.TWOMASS_H, ID.TWOMASS_J));
        pd.set3ColorIDOfIDs(ID3.WISE_3C.name(), makeIDList(ID.WISE_3, ID.WISE_2, ID.WISE_1, ID.WISE_4));
        pd.set3ColorIDOfIDs(ID3.IRAS_3.name(), makeIDList(ID.IRAS_60, ID.IRAS_25, ID.IRAS_12, ID.IRAS_100));

        pd.setTitle(ID3.DSS_3.name(), "DSS 3 Color");
        pd.setTitle(ID3.SDSS_3.name(), "SDSS 3 Color");
        pd.setTitle(ID3.TWOMASS_3.name(), "2MASS 3 Color");
        pd.setTitle(ID3.WISE_3C.name(), "WISE 3 Color");
        pd.setTitle(ID3.IRAS_3.name(), "IRAS 3 Color");


        pd.setTitle(ID.DSS1_BLUE.name(), "DSS 1 Blue");
        pd.setTitle(ID.DSS1_RED.name(),  "DSS 1 Red");
        pd.setTitle(ID.DSS2_BLUE.name(), "DSS 2 Blue");
        pd.setTitle(ID.DSS2_RED.name(),  "DSS 2 Red");
        pd.setTitle(ID.DSS2_IR.name(),   "DSS 2 IR");

        pd.setTitle(ID.SDSS_U.name(), "SDSS U");
        pd.setTitle(ID.SDSS_G.name(), "SDSS G");
        pd.setTitle(ID.SDSS_R.name(), "SDSS R");
        pd.setTitle(ID.SDSS_I.name(), "SDSS I");
        pd.setTitle(ID.SDSS_Z.name(), "SDSS Z");

        pd.setTitle(ID.TWOMASS_J.name(),  "2MASS J");
        pd.setTitle(ID.TWOMASS_H.name(),  "2MASS H");
        pd.setTitle(ID.TWOMASS_K.name(),  "2MASS K");

        pd.setTitle(ID.WISE_1.name(),   "WISE 1");
        pd.setTitle(ID.WISE_2.name(),   "WISE 2");
        pd.setTitle(ID.WISE_3.name(),   "WISE 3");
        pd.setTitle(ID.WISE_4.name(),   "WISE 4");

        pd.setTitle(ID.IRAS_12.name(),  "IRAS 12");
        pd.setTitle(ID.IRAS_25.name(),  "IRAS 25");
        pd.setTitle(ID.IRAS_60.name(),  "IRAS 60");
        pd.setTitle(ID.IRAS_100.name(), "IRAS 100");



        imDef= new FCImagePlotDefinition(idList,
                                         as3ColorIDList(),
                                         makeViewerToLayerMap());
        imDef.setBandOptions(ID3.DSS_3.toString(), make3CMap(Arrays.asList(ID.DSS2_IR, ID.DSS2_RED, ID.DSS2_BLUE)));
        imDef.setBandOptions(ID3.SDSS_3.toString(), make3CMap(Arrays.asList(ID.SDSS_U, ID.SDSS_G, ID.SDSS_R)));
        imDef.setBandOptions(ID3.TWOMASS_3.toString(), make3CMap(Arrays.asList(ID.TWOMASS_J, ID.TWOMASS_H, ID.TWOMASS_K)));
        imDef.setBandOptions(ID3.WISE_3C.toString(), make3CMap(Arrays.asList(ID.WISE_1, ID.WISE_2, ID.WISE_4)));
        imDef.setBandOptions(ID3.IRAS_3.toString(), make3CMap(Arrays.asList(ID.IRAS_100, ID.IRAS_25, ID.IRAS_60)));

    }

    private List<String> makeIDList(ID... idAry) {
        List<String> retList= new ArrayList<String>(idAry.length);
        for(ID id : idAry) {
            retList.add(id.name());
        }
        return retList;
    }


    public ImagePlotDefinition getImagePlotDefinition() {
        return imDef;
    }

    private Map<Band,String> make3CMap(List<ID> list) {
        Map<Band,String> retMap= new HashMap<Band, String>(7);
        int len= Math.min(list.size(), 3);
        for(int i=0; (i<len ); i++) {
            retMap.put(rgb.get(i), list.get(i).toString());
        }
        return retMap;
    }

    @Override
    public void update(SelectedRowData selRowData, AsyncCallback<String> callback) {
        int width= FinderChartRequestUtil.getPlotWidth(selRowData.getRequest().getParam("thumbnail_size"));
        dimension= new Dimension(width,width); // make width & height the same
        super.update(selRowData,callback);
    }




    private static List<WebPlotRequest.ServiceType> getServices(ServerRequest req) {
        List<WebPlotRequest.ServiceType> retList= new ArrayList<WebPlotRequest.ServiceType>(5);
        String sources= req.getParam("sources");
        sources = StringUtils.isEmpty(sources) ? DEFAULT_SOURCES : sources;
        for (String serviceStr: sources.split(",")) {
            serviceStr = serviceStr.trim().equalsIgnoreCase("2mass") ? WebPlotRequest.ServiceType.TWOMASS.name() : serviceStr.toUpperCase();
            WebPlotRequest.ServiceType service = WebPlotRequest.ServiceType.valueOf(serviceStr.toUpperCase());
            retList.add(service);
        }
        return retList;
    }

    private static WorldPt getWorldPt(TableData.Row<String> row) {
        double ra= Double.parseDouble(row.getValue("ra"));
        double dec= Double.parseDouble(row.getValue("dec"));
        return new WorldPt(ra,dec);
    }


    private static String getBandKey(String key, ServerRequest r) {
        String retval= r!=null ? r.getParam(key) : null;
        if (retval==null) {
            if (key.equals(ImageSet.DSS.band)) {
                retval= "poss1_blue,poss1_red,poss2ukstu_blue,poss2ukstu_red,poss2ukstu_ir";
            }
            else if (key.equals(ImageSet.IRIS.band)) {
                retval= "12,25,60,100";
            }
            else if (key.equals(ImageSet.TWOMASS.band)) {
                retval= "j,h,k";
            }
            else if (key.equals(ImageSet.WISE.band)) {
                retval= "1,2,3,4";
            }
            else if (key.equals(ImageSet.SDSS.band)) {
                retval= "u,g,r,i,z";
            }
        }
        return retval;
    }





    private static List<String> asIDList() {
        List<String> retList= new ArrayList<String>(ID.values().length);
        for(ID id : ID.values()) {
            retList.add(id.toString());
        }
        return retList;
    }

    private static List<String> as3ColorIDList() {
        List<String> retList= new ArrayList<String>(ID3.values().length);
        for(ID3 id : ID3.values()) {
            retList.add(id.toString());
        }
        return retList;
    }

    private static Map<String, List<String>> makeViewerToLayerMap() {
        Map<String, List<String>> map= new HashMap<String, List<String>>(31);
        for(ID id : ID.values()) {
            List<String> list= new ArrayList<String>(5);
            list.add("target");
            map.put(id.toString(), list);
        }

        map.get(ID.WISE_1.toString()).addAll(Arrays.asList("diff_spikes_3_1", "halos_1", "ghosts_1", "latents_1" ));
        map.get(ID.WISE_2.toString()).addAll(Arrays.asList("diff_spikes_3_2", "halos_2", "ghosts_2", "latents_2" ));
        map.get(ID.WISE_3.toString()).addAll(Arrays.asList("diff_spikes_3_3", "halos_3", "ghosts_3", "latents_3" ));
        map.get(ID.WISE_4.toString()).addAll(Arrays.asList("diff_spikes_3_4", "halos_4", "ghosts_4", "latents_4" ));

        map.get(ID.TWOMASS_J.toString()).addAll(Arrays.asList("pers_arti", "glint_arti" ));
        map.get(ID.TWOMASS_H.toString()).addAll(Arrays.asList("pers_arti", "glint_arti" ));
        map.get(ID.TWOMASS_K.toString()).addAll(Arrays.asList("pers_arti", "glint_arti" ));


         map.put(ID3.DSS_3.toString(),Arrays.asList("target"));
         map.put(ID3.SDSS_3.toString(),Arrays.asList("target"));
         map.put(ID3.TWOMASS_3.toString(),Arrays.asList("target"));
         map.put(ID3.WISE_3C.toString(),Arrays.asList("target"));
         map.put(ID3.IRAS_3.toString(),Arrays.asList("target"));

        return map;
    }


    private static String getComboPair(WebPlotRequest.ServiceType service, String key) {
        if (service.equals(WebPlotRequest.ServiceType.WISE) && key!= null) key = "3a."+key;
        for (String combo: ImageSet.lookup(service).comboAry) {
            if (key!= null && key.equals(FinderChartRequestUtil.getComboValue(combo))) return combo;
        }
        return "";
    }

    private static String getComboPair(ID id) {
        String retval= null;
        switch (id) {
            case DSS1_BLUE: retval= getComboPair(DSS, "poss1_blue"); break;
            case DSS1_RED:  retval= getComboPair(DSS, "poss1_red"); break;
            case DSS2_BLUE: retval= getComboPair(DSS, "poss2ukstu_blue"); break;
            case DSS2_RED:  retval= getComboPair(DSS, "poss2ukstu_red"); break;
            case DSS2_IR:   retval= getComboPair(DSS, "poss2ukstu_ir"); break;
            case SDSS_U: retval= getComboPair(SDSS, "u"); break;
            case SDSS_G: retval= getComboPair(SDSS, "g"); break;
            case SDSS_R: retval= getComboPair(SDSS, "r"); break;
            case SDSS_I: retval= getComboPair(SDSS, "i"); break;
            case SDSS_Z: retval= getComboPair(SDSS, "z"); break;
            case TWOMASS_J: retval= getComboPair(TWOMASS, "j"); break;
            case TWOMASS_H: retval= getComboPair(TWOMASS, "h"); break;
            case TWOMASS_K: retval= getComboPair(TWOMASS, "k"); break;
            case WISE_1:  retval= getComboPair(WISE, "1"); break;
            case WISE_2:  retval= getComboPair(WISE, "2"); break;
            case WISE_3:  retval= getComboPair(WISE, "3"); break;
            case WISE_4:  retval= getComboPair(WISE, "4"); break;
            case IRAS_12:  retval= getComboPair(ISSA, "12"); break;
            case IRAS_25:  retval= getComboPair(ISSA, "25"); break;
            case IRAS_60:  retval= getComboPair(ISSA, "60"); break;
            case IRAS_100: retval= getComboPair(ISSA, "100"); break;
        }
        return retval;
    }





    private static String getID(WebPlotRequest.ServiceType service, String band) {
        String retID= "";
        switch (service) {
            case IRIS:
            case ISSA :
                if      (band.equals("12"))  retID= ID.IRAS_12.name();
                else if (band.equals("25"))  retID= ID.IRAS_25.name();
                else if (band.equals("60"))  retID= ID.IRAS_60.name();
                else if (band.equals("100")) retID= ID.IRAS_100.name();
                break;
            case DSS:
                if      (band.equals("poss1_blue"))      retID= ID.DSS1_BLUE.name();
                else if (band.equals("poss1_red"))       retID= ID.DSS1_RED.name();
                else if (band.equals("poss2ukstu_blue")) retID= ID.DSS2_BLUE.name();
                else if (band.equals("poss2ukstu_red"))  retID= ID.DSS2_RED.name();
                else if (band.equals("poss2ukstu_ir"))   retID= ID.DSS2_IR.name();
                break;
            case SDSS:
                if      (band.equals("u")) retID= ID.SDSS_U.name();
                else if (band.equals("g")) retID= ID.SDSS_G.name();
                else if (band.equals("r")) retID= ID.SDSS_R.name();
                else if (band.equals("i")) retID= ID.SDSS_I.name();
                else if (band.equals("z")) retID= ID.SDSS_Z.name();
                break;
            case TWOMASS:
                if      (band.equals("j")) retID= ID.TWOMASS_J.name();
                else if (band.equals("h")) retID= ID.TWOMASS_H.name();
                else if (band.equals("k")) retID= ID.TWOMASS_K.name();
                break;
            case WISE:
                if      (band.endsWith("1")) retID= ID.WISE_1.name();
                else if (band.endsWith("2")) retID= ID.WISE_2.name();
                else if (band.endsWith("3")) retID= ID.WISE_3.name();
                else if (band.endsWith("4")) retID= ID.WISE_4.name();
                break;
            case MSX:
            case NONE:
            default:
                retID= ID.TWOMASS_J.name();
                break;
        }
        return retID;
    }



    private class FCImagePlotDefinition extends BaseImagePlotDefinition {

        public FCImagePlotDefinition(List<String> viewerIDList,
                                     List<String> threeColorViewerIDList,
                                     Map<String, List<String>> viewerToDrawingLayerMap) {
            super(10, viewerIDList, threeColorViewerIDList, viewerToDrawingLayerMap,
                  FINDER_CHART_GRID_LAYOUT );
        }

        @Override
        public List<String> getAllBandOptions(String viewerID) {
            return idList;
        }


        public Dimension getImagePlotDimension() {
            return dimension;
        }

        public List<String> getViewerIDs(SelectedRowData selData) {
            return getPlotData().getResolver().getIDsForMode(PlotData.GroupMode.TABLE_ROW_ONLY, selData);
        }

        @Override
        public List<String> get3ColorViewerIDs(SelectedRowData selData) {
            if (selData==null) return  super.get3ColorViewerIDs(selData);
            else return getPlotData().getResolver().getIDsForMode(PlotData.GroupMode.TABLE_ROW_ONLY, selData);
        }
    }

    private static WebPlotRequest.ServiceType getService(ID id) {
        WebPlotRequest.ServiceType service= null;
        switch (id) {
            case DSS1_BLUE:
            case DSS1_RED:
            case DSS2_BLUE:
            case DSS2_RED:
            case DSS2_IR:
                service= WebPlotRequest.ServiceType.DSS;
                break;
            case SDSS_U:
            case SDSS_G:
            case SDSS_R:
            case SDSS_I:
            case SDSS_Z:
                service= WebPlotRequest.ServiceType.SDSS;
                break;
            case TWOMASS_J:
            case TWOMASS_H:
            case TWOMASS_K:
                service= WebPlotRequest.ServiceType.TWOMASS;
                break;
            case WISE_1:
            case WISE_2:
            case WISE_3:
            case WISE_4:
                service= WebPlotRequest.ServiceType.WISE;
                break;
            case IRAS_12:
            case IRAS_25:
            case IRAS_60:
            case IRAS_100:
                service= WebPlotRequest.ServiceType.IRIS;
                break;
        }
        return service;
    }


    @Override
    public List<DatasetDrawingLayerProvider> initArtifactLayers(EventHub hub) {

        addWise(hub);
        add2Mass(hub);

        return null;
    }

    private void add2Mass(EventHub hub) {
        String desc= FinderChartRequestUtil.Artifact.pers_arti.desc;
        String color= "orange";
        DrawSymbol symbol= DrawSymbol.CROSS;
        String enablePref= Artifact.pers_arti.enablePref;
        String type= FinderChartRequestUtil.Artifact.pers_arti.name();

        add2massLayerAllBands(hub, FinderChartRequestUtil.Artifact.pers_arti.name(), desc, color, symbol, enablePref, type);


        desc= FinderChartRequestUtil.Artifact.glint_arti.desc;
        color= "purple";
        symbol= DrawSymbol.DIAMOND;
        enablePref= FinderChartRequestUtil.Artifact.glint_arti.enablePref;
        type= FinderChartRequestUtil.Artifact.glint_arti.name();

        add2massLayerAllBands(hub, Artifact.glint_arti.name(), desc, color, symbol, enablePref, type);

    }


    private void add2massLayerAllBands(EventHub hub, String layer, String desc, String color, DrawSymbol symbol, String pref, String type) {
//        for(String s : Arrays.asList("j", "h", "k")) {
            addLayer(hub, layer,desc, color, symbol,
                     Arrays.asList(new Param("service", "2mass"), new Param("type", type), new Param("band","j")),
                     pref);

//        }
    }

    private void addWise(EventHub hub) {
        String desc= Artifact.diff_spikes_3.desc;
        String color= "orange";
        DrawSymbol symbol= DrawSymbol.DOT;
        String enablePref= Artifact.diff_spikes_3.enablePref;
        String type= "D";
        addWiseLayerAllBands(hub, Artifact.diff_spikes_3 + "_", desc, color, symbol, enablePref, type);

        desc= Artifact.halos.desc;
        color= "yellow";
        symbol= DrawSymbol.SQUARE;
        enablePref= Artifact.halos.enablePref;
        type= "H";
        addWiseLayerAllBands(hub, Artifact.halos + "_", desc, color, symbol, enablePref, type);


        desc= Artifact.ghost.desc;
        color= "pink";
        symbol= DrawSymbol.DIAMOND;
        enablePref= Artifact.ghost.enablePref;
        type= "O";
        addWiseLayerAllBands(hub, Artifact.ghost + "_", desc, color, symbol, enablePref, type);


        desc= Artifact.latents.desc;
        color= "green";
        symbol= DrawSymbol.X;
        enablePref= Artifact.latents.enablePref;
        type= "P";
        addWiseLayerAllBands(hub, Artifact.latents + "_", desc, color, symbol, enablePref, type);
    }


    private void addWiseLayerAllBands(EventHub hub, String layer, String desc, String color, DrawSymbol symbol, String pref, String type) {
        for(int i=1; (i<5); i++) {
            addLayer(hub, layer+i,desc, color,symbol,
                     Arrays.asList(new Param("service", "wise"), new Param("type", type), new Param("band", i+"")),
                     pref);

        }
    }

    private DatasetDrawingLayerProvider addLayer(EventHub hub,
                                                 String id,
                                                 String title,
                                                 String color,
                                                 DrawSymbol symbol,
                                                 List<Param> extraParams,
                                                 String enablingPreference
                                                 ) {

        DatasetDrawingLayerProvider p= new DatasetDrawingLayerProvider();
        p.setQuerySources(Arrays.asList("finderChart"));
        p.setEnabled(false);   //todo - we want this off by default, make sure that works
        p.setID(id);
        p.setParam(CommonParams.SEARCH_PROCESSOR_ID, "FinderChartQueryArtifact");

        p.setDesc(title);
        p.setColor(color);
        p.setSymbol(symbol);
        p.setExtraParams(extraParams);
        p.setEnablingPreferenceKey(enablingPreference);

        p.setEventsByName(Arrays.asList(EventHub.ON_ROWHIGHLIGHT_CHANGE,EventHub.ON_TABLE_SHOW) );
        p.setArgCols(Arrays.asList("ra", "dec"));
        p.setArgsFromOriginalRequest(Arrays.asList("subsize"));

        p.bind(hub);

        return p;
    }





    private static class FCResolver implements PlotData.Resolver {
        public WebPlotRequest getRequestForID(String id, SelectedRowData selData, boolean useWithThreeColor) {

            Map<String,WebPlotRequest> map= new LinkedHashMap<String, WebPlotRequest>();
            ServerRequest req= selData.getRequest();
            // use default if not given
            Float subSize= req.getFloatParam("subsize");
            if (subSize.isNaN()) subSize= DEFAULT_SUBSIZE;
            int width= FinderChartRequestUtil.getPlotWidth(req.getParam("thumbnail_size"));
//            List<WebPlotRequest.ServiceType> services= getServices(req);
            WorldPt wp= getWorldPt(selData.getSelectedRow());
            WebPlotRequest.ServiceType st= getService(ID.valueOf(id));
            String bandComboPair=getComboPair(ID.valueOf(id));
            String expPrefix;
            switch (st) {
                case IRIS:
                case ISSA:
                case DSS:
                case SDSS:
                case MSX:
                    expPrefix= st.name()+":";
                    break;
                case WISE:
                    expPrefix= "AllWISE:";
                    break;
                case TWOMASS:
                    expPrefix= "2MASS:";
                    break;
                default:
                    expPrefix= null;
                    break;
            }
            WebPlotRequest wpReq= FinderChartRequestUtil.makeWebPlotRequest(wp, subSize, width, bandComboPair, expPrefix, st);

            if (useWithThreeColor) {
                wpReq.setTitle("3 Color");
                wpReq.setTitleOptions(WebPlotRequest.TitleOptions.NONE);
            }
            return wpReq;
        }

        public List<String> getIDsForMode(PlotData.GroupMode mode, SelectedRowData selData) {
            List<String> retList= new ArrayList<String>(30);
            Map<String,WebPlotRequest> map= new LinkedHashMap<String, WebPlotRequest>();
            ServerRequest req= selData.getRequest();
            // use default if not given
            List<WebPlotRequest.ServiceType> services= getServices(req);


            String bandStr;
            String bands[]=null;

            for(String idStr : idList) map.put(idStr,null);

            for (WebPlotRequest.ServiceType service : services) {
                String bandKey = ImageSet.lookup(service).band;
                if (bandKey!=null) {
                    bandStr = getBandKey(bandKey,req);
                    if (bandStr !=null) {
                        bands = bandStr.split(",");
                        for (int i=0;i<bands.length;i++) {
                            bands[i]=getComboPair(service, bands[i]);
                        }
                    } else {
                        bands = ImageSet.lookup(service).comboAry;
                    }
                }


                for (String band: bands) {
                    if (service.equals(WebPlotRequest.ServiceType.WISE)) {
                        if (!band.startsWith("3a.")) band = "3a."+band;
                    }
                    String idStr= getID(service, FinderChartRequestUtil.getComboValue(band));
                    retList.add(idStr);
                }

            }
            return retList;
        }

        public List<String> get3ColorIDsForMode(SelectedRowData selData) {
            List<String> retList= new ArrayList<String>(10);
            ServerRequest req= selData.getRequest();
            List<WebPlotRequest.ServiceType> services= getServices(req);
            for (WebPlotRequest.ServiceType service : services) {
                switch (service) {
                    case IRIS:
                    case ISSA:
                        retList.add(ID3.IRAS_3.name());
                        break;
                    case DSS:
                        retList.add(ID3.DSS_3.name());
                        break;
                    case SDSS:
                        retList.add(ID3.SDSS_3.name());
                        break;
                    case TWOMASS:
                        retList.add(ID3.TWOMASS_3.name());
                        break;
                    case WISE:
                        retList.add(ID3.WISE_3C.name());
                        break;
                    case DSS_OR_IRIS:
                    case MSX:
                    case NONE:
                    default:
                        break;
                }

            }
            return retList;
        }
    }
}


/*
 * THIS SOFTWARE AND ANY RELATED MATERIALS WERE CREATED BY THE CALIFORNIA 
 * INSTITUTE OF TECHNOLOGY (CALTECH) UNDER A U.S. GOVERNMENT CONTRACT WITH 
 * THE NATIONAL AERONAUTICS AND SPACE ADMINISTRATION (NASA). THE SOFTWARE 
 * IS TECHNOLOGY AND SOFTWARE PUBLICLY AVAILABLE UNDER U.S. EXPORT LAWS 
 * AND IS PROVIDED AS-IS TO THE RECIPIENT WITHOUT WARRANTY OF ANY KIND, 
 * INCLUDING ANY WARRANTIES OF PERFORMANCE OR MERCHANTABILITY OR FITNESS FOR 
 * A PARTICULAR USE OR PURPOSE (AS SET FORTH IN UNITED STATES UCC 2312- 2313) 
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
