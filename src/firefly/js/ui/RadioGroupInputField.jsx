import React from 'react/addons';
import FormStoreLinkMixin from '../ui/model/FormStoreLinkMixin.js';

import InputFieldLabel from './InputFieldLabel.jsx';

var RadioGroupInputField= React.createClass(
    {
        mixins: [React.addons.PureRenderMixin, FormStoreLinkMixin],

        propTypes: {
            options: React.PropTypes.array.isRequired
        },


        componentWillMount() {
            // if no default value is specified, select the first option
            if (typeof this.state.fieldState.value === 'undefined' || this.state.fieldState.value==='') {
                this.state.fieldState.value = this.props.options[0].value;
            }
        },

        onChange(ev) {
            // the value of the group is the value of the selected option
            var val = ev.target.value;
            var checked = ev.target.checked;

            if (checked) {
                this.fireValueChange({
                    fieldKey: this.props.fieldKey,
                    newValue: val,
                    fieldState: this.state.fieldState
                });
            }
        },

        render() {
            return (
                <div style={{whiteSpace:'nowrap'}}>
                    <InputFieldLabel label={this.getLabel()}
                        tooltip={this.getTip()}
                        labelWidth={this.props.labelWidth}
                    />
                    {this.props.options.map(option => {
                        return <input type="radio"
                                key={option.value}
                                name={this.props.fieldKey}
                                value={option.value}
                                defaultChecked={this.getValue()===option.value}
                                onChange={this.onChange}
                            >&nbsp;{option.label}&nbsp;&nbsp;</input>;
                        })}
                </div>
            );
        }


    });

export default RadioGroupInputField;
