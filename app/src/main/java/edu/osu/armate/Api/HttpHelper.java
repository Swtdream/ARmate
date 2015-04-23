package edu.osu.armate.Api;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Created by 陈英硕 on 2015/4/19.
 */
public class HttpHelper {

    final String url = "http://codexu.com/armate/query.php";
    private HttpResponse httpResponse = null;
    private String result;

    public boolean findModelByName(String name) {
        String getUrl = url + "?target_name=" + name;
        HttpGet httpGet = new HttpGet(getUrl);
        try {
            httpResponse = new DefaultHttpClient().execute(httpGet);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if(httpResponse.getStatusLine().getStatusCode() == 200) {
            try {
                result = EntityUtils.toString(httpResponse.getEntity());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            result.replaceAll("\r","");
            return true;
        }
        return true;
    }

    public String getResult() {
        if(result!=null) {
            return result;
        } else{
            return "";
        }
    }
}
