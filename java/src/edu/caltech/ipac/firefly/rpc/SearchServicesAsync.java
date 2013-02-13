package edu.caltech.ipac.firefly.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.caltech.ipac.firefly.core.RPCException;
import edu.caltech.ipac.firefly.core.background.BackgroundReport;
import edu.caltech.ipac.firefly.data.DownloadRequest;
import edu.caltech.ipac.firefly.data.FileStatus;
import edu.caltech.ipac.firefly.data.Request;
import edu.caltech.ipac.firefly.data.TableServerRequest;
import edu.caltech.ipac.firefly.data.table.RawDataSet;

import java.util.List;

/**
 * @author loi $Id: SearchServicesAsync.java,v 1.12 2011/06/29 17:03:48 roby Exp $
 */
public interface SearchServicesAsync {


    void getRawDataSet(TableServerRequest request, AsyncCallback<RawDataSet> async);

    void getFileStatus(String filePath, AsyncCallback<FileStatus> async);

    void packageRequest(DownloadRequest dataRequest, AsyncCallback<BackgroundReport> async);

    void submitBackgroundSearch(TableServerRequest request, Request clientRequest, int waitMillis, AsyncCallback<BackgroundReport> async);

    void getStatus(String id, AsyncCallback<BackgroundReport> async);

    void cancel(String id, AsyncCallback<Boolean> async);

    void cleanup(String id, AsyncCallback<Boolean> async);

    void getDownloadProgress(String fileKey, AsyncCallback<SearchServices.DownloadProgress> async);

    void setEmail(String id, String email, AsyncCallback<Boolean> async);
    void setEmail(List<String> idList, String email, AsyncCallback<Boolean> async);
    void setAttribute(String id, BackgroundReport.JobAttributes attribute, AsyncCallback<Boolean> async);
    void setAttribute(List<String> idList, BackgroundReport.JobAttributes attribute, AsyncCallback<Boolean> async);

    public void getEmail(String id, AsyncCallback<String> async);

    public void resendEmail(List<String> idList, String email, AsyncCallback<Boolean> async);


    public void createDownloadScript(String id,
                                     String fname,
                                     String dataSource,
                                     List<BackgroundReport.ScriptAttributes> attributes,
                                     AsyncCallback<String> async);

    void getEnumValues(String filePath, AsyncCallback<RawDataSet> async) throws RPCException;


    /**
     * returns a list of values from the given file path. for security reason, the path is validated before the file is
     * read.
     *
     * @param filePath path to the file
     * @param rows     rows of the data to collect
     * @param colName  only values from this column
     * @return
     * @throws edu.caltech.ipac.firefly.core.RPCException
     *
     */
    void getDataFileValues(String filePath, List<Integer> rows, String colName, AsyncCallback<List<String>> async);
}
