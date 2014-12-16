package edu.caltech.ipac.astro.net;

import edu.caltech.ipac.astro.target.PTFAttribute;
import edu.caltech.ipac.astro.target.PositionJ2000;
import edu.caltech.ipac.util.download.FailedRequestException;
import edu.caltech.ipac.util.download.URLDownload;
import edu.caltech.ipac.util.Base64;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * @author Xiuqin Wu, based on Trey Roby's CoordConvert
 */
public class PTFNameResolver {

    private static final String CGI_CMD="http://ptf.caltech.edu/cgi-bin/ptf/transient/name_radec.cgi?name=";


    private final static String UNAME= "irsaquery";
    private final static String PWD= "iptf333";



   public static PTFAttribute lowlevelNameResolver(String objname) throws  FailedRequestException {
      PositionJ2000 positionOut = null;

       PTFAttribute pa;
       try {
           URL url= new URL(CGI_CMD+objname);

           HttpURLConnection conn = (HttpURLConnection) url.openConnection();

           String authStringEnc = Base64.encode(UNAME + ":" + PWD);
           conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
           String obj= URLDownload.getStringFromOpenURL(conn,null);
           if (obj.endsWith("\n")) {
               obj= obj.substring(0,obj.indexOf("\n"));
           }
           String sAry[]= obj.split(" +", 3);
           if (sAry.length!=3) {
               throw new FailedRequestException("Object not found", "server returned bad data: " + obj);
           }
           try {
               PositionJ2000 pos= new PositionJ2000(Double.parseDouble(sAry[1]),
                                                    Double.parseDouble(sAry[2]) );
               pa = new PTFAttribute(pos);

           } catch (NumberFormatException e) {
               throw new FailedRequestException("Object not found", "server returned bad data: " + obj);
           }

       } catch (IOException e) {
           throw new FailedRequestException("Object not found", "could not create URL", e);
       }
       return pa;
   }


}
/*
* THIS SOFTWARE AND ANY RELATED MATERIALS WERE CREATED BY THE CALIFORNIA
* INSTITUTE OF TECHNOLOGY (CALTECH) UNDER A U.S. GOVERNMENT CONTRACT WITH
* THE NATIONAL AERONAUTICS AND SPACE ADMINISTRATION (NASA). THE SOFTWARE
* IS TECHNOLOGY AND SOFTWARE PUBLICLY AVAILABLE UNDER U.S. EXPORT LAWS
* AND IS PROVIDED AS-IS TO THE RECIPIENT WITHOUT WARRANTY OF ANY KIND,
* INCLUDING ANY WARRANTIES OF PERFORMANCE OR MERCHANTABILITY OR FITNESS FOR
* A PARTICULAR USE OR PURPOSE (AS SET FORTH IN UNITED STATES UCC 2312-2313)
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