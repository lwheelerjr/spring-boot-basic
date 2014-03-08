import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class Test {
    public static void main(String[] args) throws ClientProtocolException, IOException, JSONException {
        HttpClient client = new DefaultHttpClient();
        //HttpGet request = new HttpGet("http://192.210.137.230:8080/getrunner/2460c1f5f979fc8e769d3ceae0004698");
        HttpGet request = new HttpGet("http://192.210.137.230:8080/getallruns/2460c1f5f979fc8e769d3ceae0004698");
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
          System.out.println(line);
        }
	
	/*
        HttpPost post = new HttpPost("http://192.210.137.230:8080/register");
        StringEntity input = new StringEntity("{\"name\":\"Java\",\"age\":13}");
        input.setContentType("application/json");

	JSONObject json = new JSONObject();
	json.put("name", "Larry");
	json.put("age", 45);
	StringEntity se = new StringEntity( json.toString() );
        se.setContentType("application/json");
        
	post.setEntity( se );
        response = client.execute(post);
        rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        line = "";
        while ((line = rd.readLine()) != null) {
            System.out.println(line);
        }
	*/
    }
}
