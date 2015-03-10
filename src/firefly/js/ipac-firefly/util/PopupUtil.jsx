/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */

/**
 * Created by roby on 3/7/15.
 */
/*jshint browserify:true*/
"use strict";

var React= require('react/addons');
//var ModalDialog= require('ipac-firefly/ui/ModalDialog.jsx');
var Modal = require('react-modal');

var modalDiv= null;

//var appElement = document.getElementById('modal-element');



var ModalInternal = React.createClass(
        {

            getInitialState : function() {
                return {  modalOpen : true};
            },


            onClick: function(ev) {
                this.setState({modalOpen : false});
                this.props.closing();
            },

            render: function() {
                /*jshint ignore:start */
                var retval= (
                        <Modal isOpen={this.state.modalOpen}
                                onRequestClose={this.okClick} >
                            <h2>{this.props.title}</h2>
                            {this.props.message}
                            <div>
                                <button onClick={this.onClick}>close</button>
                            </div>
                        </Modal>

                );
                return retval;
                /*jshint ignore:end */
            }


        });




var getModal = function(title,message,show,closing) {
    if (!modalDiv) {
        modalDiv = document.createElement('div');
        document.body.appendChild(modalDiv);
        Modal.setAppElement(modalDiv);
    }

    var retval= null;
    if (show) {
        /*jshint ignore:start */
        retval= (
                <ModalInternal title={title}
                        message={message}
                        closing={closing}
                />
        );
        /*jshint ignore:end */
    }
    return retval;

};


exports.getModal= getModal;
