/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */

import React from 'react';
import TargetPanel from '../../ui/TargetPanel.jsx';
import InputGroup from '../../ui/InputGroup.jsx';
import CompleteButton from '../../ui/CompleteButton.jsx';
import ValidationField from '../../ui/ValidationField.jsx';
import Validate from '../../util/Validate.js';
import FieldGroup from '../../ui/FieldGroup.jsx';
import FieldGroupUtils from '../../fieldGroup/FieldGroupUtils.js';
import {showExampleDialog} from '../../ui/ExampleDialog.jsx';

import WebPlotRequest, {ServiceType} from '../WebPlotRequest.js';
import ImagePlotCntlr, {visRoot} from '../ImagePlotCntlr.js';
import DrawLayerCntlr, {getDlAry, dispatchAttachLayerToPlot, dispatchDetachLayerFromPlot} from '../DrawLayerCntlr.js';
import PlotViewUtils, {getDrawLayerByType} from '../PlotViewUtil.js';
import AppDataCntlr from '../../core/AppDataCntlr.js';
import {makeWorldPt, parseWorldPt} from '../Point.js';
import ImageViewer from './ImageViewer.jsx';
import ZoomUtil from '../ZoomUtil.js';
import {showFitsDownloadDialog} from '../../ui/FitsDownloadDialog.jsx';
import SelectArea from '../../drawingLayers/SelectArea.js';
import DistanceTool from '../../drawingLayers/DistanceTool.js';
import {showDrawingLayerPopup} from './DrawLayerPanel.jsx';
import {flux} from '../../Firefly.js';

//
//import {showFitsDownloadDialogNew} from '../../ui/FitsDownloadDialog.jsx';



/**
 *
 * @param {object} inFields
 * @param {object} action
 * @return {object}
 */
var ipReducer= function(inFields, action) {
    if (!inFields)  {
        return {
            zoom: {
                fieldKey: 'zoom',
                value: '3',
                validator: Validate.floatRange.bind(null, .1, 10, 'my zoom field'),
                tooltip: 'this is a tip for zoom',
                label: 'Zoom:'
            }
        };
    }
    else {
        return inFields;
    }
};


function showResults(success, request) {
    console.log(request);
    //====temp
    //var wp= makeWorldPt(1,1);
    var wp= parseWorldPt(request.UserTargetWorldPt);
    var wpr1= WebPlotRequest.makePlotServiceReq(ServiceType.TWOMASS, wp,'h',.1 );
    var wpr2= WebPlotRequest.makePlotServiceReq(ServiceType.TWOMASS, wp,'k',.1 );
    //var wpr2= WebPlotRequest.makeDSSRequest(wp,'poss2ukstu_red',.1 );
    wpr1.setPlotGroupId('2massGroup');
    wpr2.setPlotGroupId('2massGroup');
    wpr1.setInitialZoomLevel(parseFloat(request.zoom));
    wpr2.setInitialZoomLevel(parseFloat(request.zoom));
    wpr2.setInitialColorTable(4);
    ImagePlotCntlr.dispatchPlotImage('TestImage1', wpr1);
    ImagePlotCntlr.dispatchPlotImage('TestImage2', wpr2);
    //====temp
}

function resultsFail(request) {
    showResults(false,request);
}

function resultsSuccess(request) {
    showResults(true,request);
}


function selectArea() {
    //var s= AppDataCntlr.getCommandState('SelectAreaCmd');
    var plotView= PlotViewUtils.getActivePlotView(visRoot());
    if (!plotView) return;

    var dl= getDrawLayerByType(getDlAry(), SelectArea.TYPE_ID);
    if (!dl) {
        DrawLayerCntlr.dispatchCreateDrawLayer(SelectArea.TYPE_ID);
    }


    if (!PlotViewUtils.isDrawLayerAttached(dl,plotView.plotId)) {
        dispatchAttachLayerToPlot(SelectArea.TYPE_ID,plotView.plotId,true);
    }
    else {
        dispatchDetachLayerFromPlot(SelectArea.TYPE_ID,plotView.plotId,true);
    }

}

function layerPopup() {
    showDrawingLayerPopup();
}


function distanceTool() {
    //var s= AppDataCntlr.getCommandState('SelectAreaCmd');
    var plotView= PlotViewUtils.getActivePlotView(visRoot());
    if (!plotView) return;

    var dl= PlotViewUtils.getDrawLayerByType(getDlAry(), DistanceTool.TYPE_ID);
    if (!dl) {
        DrawLayerCntlr.dispatchCreateDrawLayer(DistanceTool.TYPE_ID);
    }

    if (!PlotViewUtils.isDrawLayerAttached(dl,plotView.plotId)) {
        dispatchAttachLayerToPlot(DistanceTool.TYPE_ID,plotView.plotId,true);
    }
    else {
        dispatchDetachLayerFromPlot(DistanceTool.TYPE_ID,plotView.plotId,true);
    }

}

function zoom(zType) {
    console.log(zType);
    switch (zType) {
        case 'up':
            ImagePlotCntlr.dispatchZoom('TestImage1',ZoomUtil.UserZoomTypes.UP);
            break;
        case 'down':
            ImagePlotCntlr.dispatchZoom('TestImage1',ZoomUtil.UserZoomTypes.DOWN);
            break;
        case 'fit':
            ImagePlotCntlr.dispatchZoom('TestImage1',ZoomUtil.UserZoomTypes.FIT);
            break;
        case 'fill':
            ImagePlotCntlr.dispatchZoom('TestImage1',ZoomUtil.UserZoomTypes.FILL);
            break;
        case '1x':
            ImagePlotCntlr.dispatchZoom('TestImage1',ZoomUtil.UserZoomTypes.ONE);
            break;

    }
}

function showExDialog() {
    console.log('showing example dialog');
    showExampleDialog();
}


function showFitsDialog() {
     console.log('showing Fits Download  dialog');
     showFitsDownloadDialog();
 }

function TestImagePanelView({selectOn,distOn}) {
    var s = AppDataCntlr.getCommandState('SelectAreaCmd');
    var selectText = (selectOn) ? 'Turn Select Off' : 'Turn Select On';
    var distText = (distOn) ? 'Turn Distance Tool Off' : 'Turn Distance Tool On';
    return (
        <div>
            <div style={{display:'inline-block', verticalAlign:'top'}}>
                <FieldGroup groupKey='TEST_IMAGE_PANEL' reducerFunc={ipReducer} keepState={true}>
                    <TargetPanel groupKey='TEST_IMAGE_PANEL'/>
                    <ValidationField fieldKey={'zoom'}
                                     groupKey='TEST_IMAGE_PANEL'/>
                    <div style={{height:10}}/>
                    <CompleteButton groupKey='TEST_IMAGE_PANEL'
                                    onSuccess={resultsSuccess}
                                    onFail={resultsFail}
                    />
                    <div style={{height:50}}/>
                    <button type='button' onClick={() => zoom('down')}>Zoom Down</button>
                    <button type='button' onClick={() => zoom('up')}>Zoom Up</button>
                    <button type='button' onClick={() => zoom('fit')}>Zoom Fit</button>
                    <button type='button' onClick={() => zoom('fill')}>Zoom Fill</button>
                    <button type='button' onClick={() => zoom('1x')}>Zoom 1x</button>
                    <button type='button' onClick={() => selectArea()}>{selectText}</button>
                    <button type='button' onClick={() => distanceTool()}>{distText}</button>
                    <button type='button' onClick={() => layerPopup()}>Layers</button>
                    <button type='button' onClick={showExDialog}>Example Dialog</button>
                    <button type='button' onClick={showFitsDialog}>Fits Download Dialog</button>
                    <button type='button' onClick={() => console.log('hello form my button')}>my button</button>
                </FieldGroup>
            </div>
            <div style={{display:'inline-block', width:400,height:400,marginLeft:10}}>
                <ImageViewer plotId='TestImage1'/>
            </div>
            <div style={{display:'inline-block', width:400,height:400,marginLeft:10}}>
                <ImageViewer plotId='TestImage2'/>
            </div>
        </div>
    );
}

var TestImagePanel= React.createClass({


    // code that connects to store
    // code that connects to store
    // code that connects to store

    componentWillUnmount() {
        if (this.unbinder) this.unbinder();
    },

    componentDidMount() {
       this.unbinder= flux.addListener( () => {
           var pv= PlotViewUtils.getActivePlotView(visRoot());
           var selectOn= false;
           var distOn= false;
           if (pv) {
               var dlAry= getDlAry();
               const selectLayer= PlotViewUtils.getDrawLayerByType(dlAry,SelectArea.TYPE_ID);
               selectOn=  PlotViewUtils.isDrawLayerAttached(selectLayer,pv.plotId);
               const distLayer= PlotViewUtils.getDrawLayerByType(dlAry,DistanceTool.TYPE_ID);
               distOn=  PlotViewUtils.isDrawLayerAttached(distLayer,pv.plotId);
           }
           this.setState({selectOn,distOn});
        });
    },

    render() {
        var selectOn= (this.state && this.state.selectOn) ? this.state.selectOn : false;
        var distOn= (this.state && this.state.distOn) ? this.state.distOn : false;
        return (<TestImagePanelView selectOn={selectOn} distOn={distOn}/>);
    }

    // end code that connects to store
});


export default TestImagePanel;