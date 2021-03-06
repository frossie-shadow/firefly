/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.server.rpc;

import com.google.gwt.user.server.rpc.RPCRequest;
import edu.caltech.ipac.firefly.core.EndUserException;
import edu.caltech.ipac.firefly.core.RPCException;
import edu.caltech.ipac.firefly.core.background.BackgroundStatus;
import edu.caltech.ipac.firefly.core.background.JobAttributes;
import edu.caltech.ipac.firefly.core.background.ScriptAttributes;
import edu.caltech.ipac.firefly.data.DownloadRequest;
import edu.caltech.ipac.firefly.data.FileStatus;
import edu.caltech.ipac.firefly.data.Request;
import edu.caltech.ipac.firefly.data.TableServerRequest;
import edu.caltech.ipac.firefly.data.table.RawDataSet;
import edu.caltech.ipac.firefly.rpc.SearchServices;
import edu.caltech.ipac.firefly.server.ServerContext;
import edu.caltech.ipac.firefly.server.query.BackgroundEnv;
import edu.caltech.ipac.firefly.server.query.SearchManager;

import java.io.File;
import java.util.List;

/**
 * @author tatianag
 * $Id: SearchServicesImpl.java,v 1.14 2012/10/03 22:18:11 loi Exp $
 */
public class SearchServicesImpl extends BaseRemoteService implements SearchServices {

    public RawDataSet getRawDataSet(TableServerRequest request) throws RPCException{
        try {
            return  new SearchManager().getRawDataSet(request);

        } catch (Throwable e) {
            throw createRPCException(e);
        }
    }

    public FileStatus getFileStatus(String filePath) throws RPCException {
        try {
            return  new SearchManager().getFileStatus(new File(filePath));
        } catch (Throwable e) {
            throw createRPCException(e);
        }
    }


    public BackgroundStatus submitBackgroundSearch(TableServerRequest request, Request clientRequest, int waitMillis) throws RPCException {
        try {
            return  new SearchManager().getRawDataSetBackground(request, clientRequest, waitMillis);
        } catch (Throwable e) {
            throw createRPCException(e);
        }
    }

    public RawDataSet getEnumValues(String filePath) throws RPCException {
        try {
            if (filePath != null) {
                return  new SearchManager().getEnumValues(new File(filePath));
            }
        } catch (Throwable e) {
            throw createRPCException(e);
        }
        return null;
    }

    public List<String> getDataFileValues(String filePath, List<Integer> rows, String colName) throws RPCException {
        try {
            return  new SearchManager().getDataFileValues(new File(filePath), rows, colName);
        } catch (Throwable e) {
            throw createRPCException(e);
        }
    }

    public BackgroundStatus packageRequest(DownloadRequest request) throws RPCException {
        try {
            return  new SearchManager().packageRequest(request);

        } catch (Throwable e) {
            throw createRPCException(e);
        }
    }


    public BackgroundStatus getStatus(String id, boolean polling) { return BackgroundEnv.getStatus(id, polling); }

    public boolean addIDToPushCriteria (String id) {
        BackgroundEnv.addIDToPushCriteria(id);
        return true;
    }

    public boolean cleanup(String id) { return BackgroundEnv.cleanup(id); }

    public boolean cancel(String id) { return BackgroundEnv.cancel(id); }


    public boolean setAttribute(String id, JobAttributes attribute) {
        BackgroundEnv.setAttribute(id,attribute);
        return true;
    }

    public boolean setAttribute(List<String> idList, JobAttributes attribute) {
        BackgroundEnv.setAttribute(idList,attribute);
        return true;
    }

    public boolean setEmail(String id, String email) {
        BackgroundEnv.setEmail(id,email);
        return true;
    }

    public boolean setEmail(List<String> idList, String email) {
        BackgroundEnv.setEmail(idList,email);
        return true;
    }

    public String getEmail(String id) {
        return BackgroundEnv.getEmail(id);
    }


    public String createDownloadScript(String id,
                                       String fname,
                                       String dataSource,
                                       List<ScriptAttributes> attributes) {

        BackgroundEnv.ScriptRet retval= BackgroundEnv.createDownloadScript(id, fname, dataSource, attributes);
        return retval!=null ? retval.getServlet() : null;
    }



    public boolean resendEmail(List<String> idList, String email) {
        BackgroundEnv.resendEmail(idList,email);
        return true;
    }

    public boolean clearPushEntry(String id, int idx) {
        BackgroundEnv.clearPushEntry(id,idx);
        return true;
    }

    public boolean reportUserAction(String channel, String desc, String data) {
        BackgroundEnv.reportUserAction(channel,desc,data);
        return true;
    }

    public SearchServices.DownloadProgress getDownloadProgress(String fileKey) {
        return BackgroundEnv.getDownloadProgress(fileKey);
    }



    protected RPCException createRPCException(Throwable e) {
         e.printStackTrace();
         RPCRequest req = (RPCRequest) ServerContext.getRequestOwner().getAttribute("rpcRequest");

        String userMsg = "";
        String msg = "";
        for (Throwable t= e.getCause(); (t!=null); t= t.getCause()) {
            if (t instanceof EndUserException) {
                userMsg= ((EndUserException)t).getEndUserMsg();
            }
            msg = t.getMessage();
        }

        RPCException rcpE= new RPCException (e, getClass().getSimpleName(),
                                         req.getMethod().getName(),
                                         "The call failed on the server",
                                         msg);
        rcpE.setEndUserMsg(userMsg);

         return rcpE;
     }

}
