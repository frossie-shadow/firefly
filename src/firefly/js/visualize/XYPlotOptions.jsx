/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
import React from 'react';

import {get} from 'lodash';

import ColValuesStatistics from './ColValuesStatistics.js';
import CompleteButton from '../ui/CompleteButton.jsx';
import FieldGroup from '../ui/FieldGroup.jsx';
import FieldGroupUtils from '../fieldGroup/FieldGroupUtils.js';
import InputGroup from '../ui/InputGroup.jsx';
import Validate from '../util/Validate.js';
import ValidationField from '../ui/ValidationField.jsx';
import CheckboxGroupInputField from '../ui/CheckboxGroupInputField.jsx';
import ListBoxInputField from '../ui/ListBoxInputField.jsx';



var XYPlotOptions = React.createClass({

    unbinder : null,

    propTypes: {
        groupKey: React.PropTypes.string.isRequired,
        colValStats: React.PropTypes.arrayOf(React.PropTypes.instanceOf(ColValuesStatistics)).isRequired,
        onOptionsSelected: React.PropTypes.func.isRequired
    },

    /*

      @param xyPlotParams key value pairs

      axisParamsShape = React.PropTypes.shape({
          columnOrExpr : React.PropTypes.string,
          label : React.PropTypes.string,
          unit : React.PropTypes.string,
          options : React.PropTypes.string, // ex. 'log,flip'
          nbins : React.PropTypes.number,
          min : React.PropTypes.number,
          max : React.PropTypes.number
      });

      React.PropTypes.shape({
         grid : React.PropTypes.boolean,
         xyRatio : React.PropTypes.number,
         stretch : React.PropTypes.oneOf(['fit','fill'])
         x : axisParamsShape,
         y : axisParamsShape
      })
     */

    getInitialState() {
        return {fields : FieldGroupUtils.getGroupFields(this.props.groupKey)};
    },

    componentWillUnmount() {
        if (this.unbinder) this.unbinder();
    },

    componentDidMount() {
        this.unbinder = FieldGroupUtils.bindToStore(this.props.groupKey, (fields) => this.setState({fields}));
    },

    getUnit(colname) {
        const statrow = this.props.colValStats.find((el) => { return el.name===colname; });
        if (statrow && statrow.unit && statrow.unit !== 'null') { return statrow.unit; }
        else {return '';}
    },

    resultsSuccess(flds) {
        const xName = get(flds, ['x.columnOrExpr']);
        const yName = get(flds, ['y.columnOrExpr']);
        const xyPlotParams = {
            x : { columnOrExpr : xName, label : xName, unit : this.getUnit(xName)},
            y : { columnOrExpr : yName, label : yName, unit : this.getUnit(yName)}
        };
        this.props.onOptionsSelected(xyPlotParams);
    },

    resultsFail() {
        // TODO: do I need to do anything here?
    },

    render() {
        const { colValStats, groupKey }= this.props;
        const {fields} = this.state;
        return (
            <div style={{padding:'5px'}}>
                <br/>
                <FieldGroup groupKey={groupKey} validatorFunc={null} keepState={true}>
                    <ListBoxInputField
                        initialState= {{
                                tooltip: 'Please select a column or expression for X axis',
                                label : 'X:',
                                labelWidth : 50
                            }}
                        options={
                                colValStats.map((colVal) => {
                                    return {
                                        label: colVal.name + ' ' + (colVal.unit && colVal.unit !== 'null' ? colVal.unit : ''),
                                        value: colVal.name
                                    };
                                })
                            }
                        multiple={false}
                        fieldKey='x.columnOrExpr'
                        groupKey={groupKey}
                    />
                    <br/>
                    <ListBoxInputField
                        initialState= {{
                                tooltip: 'Please select a column or expression for Y axis',
                                label : 'Y:',
                                labelWidth : 50
                            }}
                        options={
                                colValStats.map((colVal) => {
                                    return {
                                        label: colVal.name + ' ' + (colVal.unit && colVal.unit !== 'null' ? colVal.unit : ''),
                                        value: colVal.name
                                    };
                                })
                            }
                        multiple={false}
                        fieldKey='y.columnOrExpr'
                        groupKey={groupKey}
                    />
                    <br/>

                    <CompleteButton groupKey={groupKey}
                                    onSuccess={this.resultsSuccess}
                                    onFail={this.resultsFail}
                        />
                </FieldGroup>

            </div>
        );
    }
});

/*
                    <CheckboxGroupInputField
                        initialState= {{
                            value: '_none_',
                            tooltip: 'Check if you would like to plot grid',
                            label : 'Grid:'
                        }}
                        options={[
                            {label: 'grid', value: 'grid'}
                        ]}
                        fieldKey='grid'
                    />
                    <br/>
 */

export default XYPlotOptions;