package edu.caltech.ipac.firefly.server.visualize;
/**
 * User: roby
 * Date: 2/28/13
 * Time: 12:53 PM
 */


import edu.caltech.ipac.firefly.visualize.VisUtil;
import edu.caltech.ipac.target.TargetUtil;
import edu.caltech.ipac.util.dd.Region;
import edu.caltech.ipac.util.dd.RegionAnnulus;
import edu.caltech.ipac.util.dd.RegionBox;
import edu.caltech.ipac.util.dd.RegionBoxAnnulus;
import edu.caltech.ipac.util.dd.RegionDimension;
import edu.caltech.ipac.util.dd.RegionEllipse;
import edu.caltech.ipac.util.dd.RegionEllipseAnnulus;
import edu.caltech.ipac.util.dd.RegionLines;
import edu.caltech.ipac.util.dd.RegionPoint;
import edu.caltech.ipac.util.dd.RegionText;
import edu.caltech.ipac.util.dd.RegionValue;
import edu.caltech.ipac.visualize.draw.FixedObject;
import edu.caltech.ipac.visualize.draw.FixedObjectGroup;
import edu.caltech.ipac.visualize.draw.ScalableObject;
import edu.caltech.ipac.visualize.draw.ScalableObjectPosition;
import edu.caltech.ipac.visualize.draw.ShapeInfo;
import edu.caltech.ipac.visualize.draw.SkyShape;
import edu.caltech.ipac.visualize.draw.SkyShapeFactory;
import edu.caltech.ipac.visualize.draw.StringShape;
import edu.caltech.ipac.visualize.draw.VectorObject;
import edu.caltech.ipac.visualize.plot.CoordinateSys;
import edu.caltech.ipac.visualize.plot.ImagePlot;
import edu.caltech.ipac.visualize.plot.ImageWorkSpacePt;
import edu.caltech.ipac.visualize.plot.PlotView;
import edu.caltech.ipac.visualize.plot.ProjectionException;
import edu.caltech.ipac.visualize.plot.WorldPt;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * @author Trey Roby
 */
public class RegionPng {


    private final List<Region> regList;
    private final List<FixedObjectGroup> fgList;
    private final List<VectorObject> vectorList;
    private final List<ScalableObjectPosition> scaleList;
    private final ImagePlot plot;
    private final FixedObjectGroup fg= new FixedObjectGroup();

    public RegionPng(List<Region> regList,
                     ImagePlot plot,
                     List<FixedObjectGroup> fgList,
                     List<VectorObject> vectorList,
                     List<ScalableObjectPosition> scaleList) {
        this.regList = regList;
        this.fgList = fgList;
        this.vectorList = vectorList;
        this.scaleList = scaleList;
        this.plot = plot;
        if (plot.getPlotView()==null)  new PlotView().addPlot(plot);
    }

//======================================================================
//------------------ Private / Protected Methods -----------------------
//======================================================================

    public void drawRegions() {
        FixedObjectGroup fg= new FixedObjectGroup();

        for(Region r : regList) {
            if (r.getOptions().isInclude()) {
                try {
                    if (r instanceof RegionAnnulus) {
                        RegionAnnulus ra= (RegionAnnulus)r;
                        if (ra.isCircle())  makeCircle(ra);
                        else                makeAnnulus(ra);
                    }
                    else if (r instanceof RegionBox) {
                        makeBox((RegionBox) r);
                    }
                    else if (r instanceof RegionBoxAnnulus) {
                        makeBoxAnnulus((RegionBoxAnnulus)r);
                    }
                    else if (r instanceof RegionEllipse) {
                        throw new IllegalArgumentException("not implemented");

                    }
                    else if (r instanceof RegionEllipseAnnulus) {
                        throw new IllegalArgumentException("not implemented");
                    }
                    else if (r instanceof RegionLines) {
                        RegionLines rl= (RegionLines)r;
                        if (rl.isPolygon())  makePolygon(rl);
                        else                 makeLine(rl);

                    }
                    else if (r instanceof RegionPoint) {
                        makePoint((RegionPoint)r);

                    }
                    else if (r instanceof RegionText) {
                        makeText((RegionText)r);
                    }
                } catch (NoninvertibleTransformException e) {
                    // ignore - just don't plot region
                } catch (ProjectionException e) {
                    // ignore - just don't plot region
                }
            }
        }

        if (fg.size()>0)  fgList.add(fg);



    }


    private void makeCircle(RegionAnnulus ra) throws NoninvertibleTransformException, ProjectionException {
        WorldPt wp= confirmJ2000(ra.getPt());
        double radius= convertToDegree(ra.getRadii()[0]);
        Shape shape= new Ellipse2D.Double(-radius,-radius, radius*2, radius*2); //radius set on the circle creation
        Color c= PlotPngCreator.convertColor(ra.getColor());
        ShapeInfo si[]= new ShapeInfo[] { new ShapeInfo(shape,c)};
        ScalableObject scaleObj= new ScalableObject(si);
        ScalableObjectPosition pos= new ScalableObjectPosition(scaleObj);
        PlotView pv= plot.getPlotView();
        scaleObj.addPlotView(pv);
        pos.addPlotView(pv);
        pos.setPosition(wp.getLon(),wp.getLat()); // world pt is set here
        scaleList.add(pos);
    }

    private void makeAnnulus(RegionAnnulus ra) throws NoninvertibleTransformException, ProjectionException {
        WorldPt wp= confirmJ2000(ra.getPt());
        RegionValue radiusAry[]= ra.getRadii();
        ShapeInfo si[]= new ShapeInfo[radiusAry.length];
        Color c= PlotPngCreator.convertColor(ra.getColor());
        for(int i=0; (i<radiusAry.length); i++) {
            double radius= convertToDegree(radiusAry[i]);
            Shape shape= new Ellipse2D.Double(-radius,-radius, radius*2, radius*2); //radius set on the circle creation
            si[i]= new ShapeInfo(shape,c);
        }
        ScalableObject scaleObj= new ScalableObject(si);
        ScalableObjectPosition pos= new ScalableObjectPosition(scaleObj);
        PlotView pv= plot.getPlotView();
        scaleObj.addPlotView(pv);
        pos.addPlotView(pv);
        pos.setPosition(wp.getLon(),wp.getLat()); // world pt is set here
        scaleList.add(pos);
    }

    private void makeBox(RegionBox rb) throws NoninvertibleTransformException, ProjectionException {
        RegionDimension dim= rb.getDim();
        WorldPt wp= confirmJ2000(rb.getPt());
        Color c= PlotPngCreator.convertColor(rb.getColor());
        double w= convertToDegree(dim.getWidth());
        double h= convertToDegree(dim.getHeight());
        Shape shape= new Rectangle2D.Double( 0,0, w,h);
        ShapeInfo si= new ShapeInfo(shape, c);
        ScalableObject so= new ScalableObject(new ShapeInfo[] {si});
        ScalableObjectPosition pos= new ScalableObjectPosition(so);
        PlotView pv= plot.getPlotView();
        so.addPlotView(pv);
        pos.addPlotView(pv);
        pos.setPosition(wp.getLon(),wp.getLat()); // world pt is set here
        scaleList.add(pos);
        updatePositionAngle(wp,pos);
    }

    private void makeBoxAnnulus (RegionBoxAnnulus rba) throws NoninvertibleTransformException, ProjectionException {
        RegionDimension dim[]= rba.getDim();
        WorldPt wp= confirmJ2000(rba.getPt());
        Color c= PlotPngCreator.convertColor(rba.getColor());
        ShapeInfo siAry[]= new ShapeInfo[dim.length];
        for(int i=0; (i<dim.length); i++) {
            double w= convertToDegree(dim[i].getWidth());
            double h= convertToDegree(dim[i].getHeight());
            Shape shape= new Rectangle2D.Double( 0, 0, w,h);
            siAry[i]= new ShapeInfo(shape, c);
        }
        ScalableObject so= new ScalableObject(siAry);
        ScalableObjectPosition pos= new ScalableObjectPosition(so);
        PlotView pv= plot.getPlotView();
        so.addPlotView(pv);
        pos.addPlotView(pv);
        pos.setPosition(wp.getLon(),wp.getLat()); // world pt is set here
        scaleList.add(pos);
    }




    private void makePolygon(RegionLines rl) {
        WorldPt originalAry[]= rl.getPtAry();
        WorldPt wptAry[]= new WorldPt[originalAry.length+1];
        System.arraycopy(originalAry,0,wptAry,0,originalAry.length);
        wptAry[wptAry.length-1]= wptAry[0];
        if (wptAry.length>2) {
            VectorObject vo= new VectorObject(wptAry);
            Color c= PlotPngCreator.convertColor(rl.getColor());
            if (c!=null) vo.getLineShape().setColor(c);
            vectorList.add(vo);
        }
    }

    private void makeLine(RegionLines rl) {
        WorldPt wptAry[]= rl.getPtAry();
        if (wptAry.length==2) {
            VectorObject vo= new VectorObject(wptAry);
            Color c= PlotPngCreator.convertColor(rl.getColor());
            if (c!=null) vo.getLineShape().setColor(c);
            vectorList.add(vo);
        }
    }

    private void makePoint(RegionPoint rp) {
        FixedObjectGroup pointFG= new FixedObjectGroup();
        SkyShapeFactory factory= SkyShapeFactory.getInstance();
        FixedObject fo= pointFG.makeFixedObject(rp.getPt());
        pointFG.add(fo);
        SkyShape shape;
        switch (rp.getPointType()) {
            case X:
                shape = makeX(rp);
                break;
            case Box:
            case BoxCircle:
                Rectangle2D rec= new Rectangle2D.Double(0,0, rp.getPointSize(),
                                                        rp.getPointSize());
                shape= new SkyShape(rec);
                break;
            case Cross:
            case Arrow:
                shape = makeCross(rp);
                break;
            case Diamond:
                shape = makeDiamond(rp);
                break;
            case Circle:
                Ellipse2D circle= new Ellipse2D.Double(0,0, rp.getPointSize()*2,rp.getPointSize()*2);
                shape= new SkyShape(circle);
                break;
            default:
                shape = factory.getSkyShape("x");
                break;
        }
        fo.getDrawer().setSkyShape(shape);
        Color c= PlotPngCreator.convertColor(rp.getColor());
        fo.getDrawer().setStandardColor(c);
        fgList.add(pointFG);
    }

    private static SkyShape makeX(RegionPoint rp) {
        GeneralPath gp= new GeneralPath();
        int size= rp.getPointSize();
        gp.moveTo(0,0);
        gp.lineTo(size,size);
        gp.moveTo(0,size);
        gp.lineTo(size,0);
        return new SkyShape(gp);
    }

    private static SkyShape makeDiamond(RegionPoint rp) {
        GeneralPath gp= new GeneralPath();
        int h= rp.getPointSize();
        int size= h*2;
        gp.moveTo(h,0);
        gp.lineTo(size,h);
        gp.lineTo(h,size);
        gp.lineTo(0,h);
        gp.lineTo(h,0);
        return new SkyShape(gp);
    }

    private static SkyShape makeCross(RegionPoint rp) {
        GeneralPath gp= new GeneralPath();
        int h= rp.getPointSize();
        int size= h*2;
        gp.moveTo(0,h);
        gp.lineTo(size,h);
        gp.moveTo(h,0);
        gp.lineTo(h,size);
        return new SkyShape(gp);
    }

    private void makeText(RegionText rt) throws ProjectionException {
        FixedObjectGroup textFg= new FixedObjectGroup();
        Point2D p= plot.getScreenCoords(rt.getPt());
        int offX= rt.getOptions().getOffsetX();
        int offY= rt.getOptions().getOffsetY();
        WorldPt wp= new WorldPt(p.getX()+offX, p.getY()+offY, CoordinateSys.SCREEN_PIXEL);
        FixedObject fo= textFg.makeFixedObject(wp);
        Color c= PlotPngCreator.convertColor(rt.getColor());
        fo.getDrawer().setStandardColor(c);
        fo.setShowName(true);
        fo.setShowPoint(false);
        fo.setTargetName(rt.getOptions().getText());
        StringShape ss= fo.getDrawer().getStringShape();
        ss.setDrawWithBackground(false);
        ss.setUseRegionCalc(true);
        ss.setOffsetDirection(StringShape.CENTER);
        textFg.add(fo);
        fgList.add(textFg);
    }

    private WorldPt confirmJ2000(WorldPt wp) throws NoninvertibleTransformException, ProjectionException {
        CoordinateSys csys= wp.getCoordSys();
        if (csys.equals(CoordinateSys.SCREEN_PIXEL)) {
            wp= plot.getWorldCoords(new Point2D.Double(wp.getX(), wp.getY()));
        }
        else if (csys.equals(CoordinateSys.PIXEL)) {
            wp= plot.getWorldCoords(new ImageWorkSpacePt(wp.getX(),wp.getY()));
        }
        wp= VisUtil.convertToJ2000(wp);
        return wp;

    }

    private double convertToDegree(RegionValue v) {
        double retval;
        if (v.isWorldCoords()) {
            retval= v.toDegree();
        }
        else {
            double scaleDeg= plot.getPixelScale()/3600;
            switch (v.getType()) {
                case IMAGE_PIXEL:
                    retval= v.getValue()*scaleDeg;
                    break;
                case CONTEXT:
                case SCREEN_PIXEL:
                case UNKNOWN:
                default:
                    retval= v.getValue()*(1/plot.getPlotGroup().getZoomFact())*scaleDeg;
                    break;
            }
        }
        return retval;
    }

    void updatePositionAngle(WorldPt wp, ScalableObjectPosition pos) {
        float posAngle;
        try {
            ImageWorkSpacePt iwpt = plot.getImageCoords(wp);
            ImageWorkSpacePt iwpt2 = new ImageWorkSpacePt(iwpt.getX(), iwpt.getY() + 1);
            WorldPt wpt = plot.getWorldCoords(iwpt2, CoordinateSys.EQ_J2000);
            posAngle = (float) TargetUtil.getPositionAngle(wp.getLon(), wpt.getLat(),
                                                            wpt.getLon(), wpt.getLat());
            float rotation_angle = posAngle - 270.0F;
            pos.setRotation(plot, new ScalableObject.RotationInfo(rotation_angle, null));
        }
        catch (ProjectionException pe) {
           // do nothing
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