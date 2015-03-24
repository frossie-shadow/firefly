/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
/*jshint browserify:true*/
/*jshint esnext:true*/
/*jshint curly:false*/

import toBoolean from "underscore.string/toBoolean";
import join from "underscore.string/join";
import replaceAll from "underscore.string/replaceAll";
import words from "underscore.string/words";
import _ from "underscore";
import validator from "validator";
import Point from "ipac-firefly/visualize/Point.js";


const REQUEST_CLASS= "RequestClass";
const SERVER_REQUEST_CLASS = "ServerRequest";
const PARAM_SEP = "&";
const URL_SUB = "URL_PARAM_SEP";
const KW_DESC_SEP = "/";
const KW_VAL_SEP = "=";
const BACKGROUNDABLE = "bgable";

const ID_NOT_DEFINED = "ID_NOT_DEFINED";



class ServerRequest {
    constructor(id, copyFromReq) {
        this.params= {};
        this.ID_KEY = "id";
        if (copyFromReq) {
            Object.assign(this.params, copyFromReq.params ? copyFromReq.params : copyFromReq);
        }
        if (id)  this.setRequestId(id);
        if (!this.params[this.ID_KEY]) this.params[this.ID_KEY]= ID_NOT_DEFINED;
        this.setRequestClass(SERVER_REQUEST_CLASS);
    }

//====================================================================
//
//====================================================================

    /**
     * return true if this parameter is a user input parameter.
     * @param paramName
     * @return
     */
    isInputParam(paramName) { return true; }


    getRequestId() { return this.getParam(this.ID_KEY); }

    setRequestId(id) { this.params[this.ID_KEY]= id; }

    isBackgroundable() {
        return this.getBooleanParam(BACKGROUNDABLE, false);
    }

    setIsBackgroundable(isBackgroundable) {
        this.setParam(BACKGROUNDABLE, isBackgroundable+"");
    }

    getRequestClass() {
        return this.containsParam(REQUEST_CLASS) ? this.getParam(REQUEST_CLASS) : SERVER_REQUEST_CLASS;
    }

    setRequestClass(reqType) { this.setParam(REQUEST_CLASS,reqType); }

//====================================================================

    containsParam(paramKey) {
        return this.params[paramKey]?true:false;
    }

    getParam(paramKey) {
        return this.params[paramKey]|| null;
    }

    getParams() { return this.params; }


    setParam() {
        if (arguments.length===1 && typeof arguments[0] === 'object')  {
            var v= arguments[0];
            if (v.name && v.value) this.params[v.name]= v.value;
        }
        else if (arguments.length===2) {
            this.params[arguments[0]]= arguments[1]+"";
        }
        else if (arguments.length>2) {
            var values= [];
            for(var i=2; i<arguments.length; i++) {
               values.push(arguments[i]);
            }
            this.setParamNameMultiValue(arguments[0],values);
        }
    }

    setParamNameMultiValue(key,valAry) {
        this.params[arguments[0]]= join(valAry);
    }

    setParams(params) {
         Object.assign(this.params, params);
    }

    //public void setParams(Map<String,String> paramMap) {
    //    for(Map.Entry<String,String> entry : paramMap.entrySet()) {
    //        setParam(new Param(entry.getKey(),entry.getValue()));
    //    }
    //}

    setWorldPtParam(name, wpt) {
        this.params[name]=wpt? null : wpt.toString();
    }


    setSafeParam(name,val) {
        this.params[name]= val ? replaceAll(val,PARAM_SEP,URL_SUB) : null;
    }

    getSafeParam(name) {
        var val= this.params[name];
        return val ? replaceAll(val,URL_SUB,PARAM_SEP) : null;
    }

    isValid() { return this.params[this.ID_KEY] ? true : false; }

    removeParam(name) { delete this.params[name]; }

    /**
     * Add a predefined attribute
     * @param param the param to add
     * @return true if this was a predefined attribute and was set, false it this is an unknow attribute
     */
    addPredefinedAttrib(param) { return false; }

//====================================================================


    copyFrom(req) {
        Object.assign(this.params,req.params?req.params:req);
    }

    /**
     * Parses the string argument into a ServerRequest object.
     * This method is reciprocal to toString().
     * @param str
     * @param req
     * @return the passed request
     */
    static parse(str,req) {
        if (!str) return null;
        words(str,PARAM_SEP).forEach(p => {
            var outParam= words(p,PARAM_SEP);
            if (outParam.length===2) {
                var newParam= {name : outParam[0], value:outParam[1]};
                if (!req.addPredefinedAttrib(newParam)) {
                   req.setParam(outParam);
                }
            }
        });
        return req;
    }



    /**
     * Serialize this object into its string representation.
     * This class uses the url convention as its format.
     * Parameters are separated by '&'.  Keyword and value are separated
     * by '='.  If the keyword contains a '/' char, then the left side is
     * the keyword, and the right side is its description.
     * @return
     */
    toString() {
        var idStr= (this.ID_KEY+KW_VAL_SEP+this.params[this.ID_KEY]);
        var retStr= _.keys(this.params).reduce((str,key) => {
            if (key!==this.ID_KEY) str+= key+KW_VAL_SEP+this.params[key];
            return str;
        },idStr);
        return retStr;
    }


    cloneRequest() {
        var sr = this.newInstance();
        sr.copyFrom(this);
        return sr;
    }

    newInstance() {
        return new ServerRequest();
    }



//====================================================================
//  overriding equals
//====================================================================
//    @Override
//    public int hashCode() {
//        return toString().hashCode();
//    }

    equals(obj) {
        if (obj instanceof ServerRequest) {
            return this.toString()===obj.toString();
        }
        return false;
    }

//====================================================================
//  convenience data converting routines
//====================================================================
    getBooleanParam(key, def=false) {
        return this.params[key] ? toBoolean(this.params[key]) : def;
    }

    getIntParam(key, def=0) {
        var retval= validator.toInt(this.getParam(key));
        return !isNaN(retval) ? retval : def;
    }

    getFloatParam(key, def=0) {
        var retval= validator.toFloat(this.getParam(key));
        return !isNaN(retval) ? retval : def;
    }

    getDateParam(key) {
        var dateValue= validator.toInt(this.getParam(key));
        return !isNaN(dateValue) ? new Date(dateValue) : null;
    }

    getWorldPtParam(key) {
        var wpStr= this.getParam(key);
        return wpStr ? Point.parseWorldPt(wpStr) : null;
    }

//====================================================================
//  convenience data converting routines
//====================================================================


    addParam(str, key, value) {
        if (str && key && value) {
            str+= (PARAM_SEP+key+KW_VAL_SEP+value);
        }
        return str;
    }


//====================================================================
//  inner classes
//====================================================================

}

exports.ServerRequest= ServerRequest;
exports.ID_NOT_DEFINED= ID_NOT_DEFINED;
