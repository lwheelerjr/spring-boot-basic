//import com.resttest.domain.Runner;
//import com.resttest.domain.Run;
import groovyx.net.http.RESTClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import static groovyx.net.http.ContentType.JSON;

@Controller
class RunTrackerRESTService {

    def COUCH_URL = "http://localhost:5984/";
    def COUCH_DB = "runnerdb";
    def COUCH_VIEW_URL = "${COUCH_DB}/_design/runner/_view";

    @RequestMapping("/")
    @ResponseBody
    String home() {
        "Welcome to the Run Tracker REST Service v. 0.01";
    }


    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.0-RC2')
    def getRESTClient(){
        def rc = new RESTClient(COUCH_URL);
        rc.parser.'application/json' = rc.parser.'text/plain';  // set json parser to same as text so Jackson can intercept
        return rc;
    }

    Map restGet(String url, Map query = [:]) {
        def client = getRESTClient();
        def resp = client.get( path: url , query: query);
        def myMap = getMap( resp );
        return myMap;
    }

    Map restPut(String url, Map body) {
        def client = getRESTClient();
        def resp = client.put( 
                path : url,
                contentType: JSON,
                requestContentType:  JSON,
                body : body );
        def myMap = getMap( resp );
        return myMap;
    }       

    HashMap getMap(def resp) {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap myMap = objectMapper.readValue(resp.data, HashMap.class);
        println "Response Map is: ${myMap}";
        return myMap;
    }

    String collapseIntList(List<Integer> l) {        
        (l.collect {
            it < 10 ? "0${it}" : "${it}";
        }).join();
    }

    String removeDashes(String s) {
        s.replaceAll('-', '');
    }

    def getNewUUID() {
        def myMap = restGet('_uuids');

        return myMap.get("uuids")[0];
    }

    boolean runnerExists(String runnerId)
    {
        try {    
           // test to see if runner exists
           def rev = restGet("${COUCH_DB}/${runnerId}")._rev;
        }
        catch( ex ) {            
            return false; 
        }     

        return true;
    }


    boolean runExists(String runnerId, String date)
    {
        try {    
           // test to see if run exists
           def rev = restGet("${COUCH_DB}/${runnerId}_${removeDashes(date)}")._rev;
        }
        catch( ex ) {            
            return false; 
        }     

        return true;
    }    

    @RequestMapping(value = "register", method = RequestMethod.POST) 
    @ResponseBody
    public Runner registerRunner(@RequestBody final Runner r) {

        def uuid = getNewUUID();

        def myMap = restPut( "${COUCH_DB}/${uuid}", [ type: "runner", name: r.name, age: r.age] );

        return [runnerId: uuid, name: r.name, age: r.age] as Runner;
    }


    @RequestMapping(value = "getrunner/{runnerId}", method = RequestMethod.GET) 
    @ResponseBody
    public Runner getRunner(@PathVariable String runnerId) {

        def myMap = restGet("${COUCH_VIEW_URL}/allrunners", [ key: "\"${runnerId}\"" ] );

        def runners = myMap.rows.collect {
            [ runnerId: runnerId, 
              name: it.value[0], 
              age: it.value[1] ] as Runner;              
        }

        return runners[0];
    }



    @RequestMapping(value = "setrun", method = RequestMethod.POST) 
    @ResponseBody
    public Object setRunForRunner(@RequestBody final Run r) {

        def bodyMap = [ type: "run",
                        runnerId: r.runnerId,
                        date: r.date,
                        time: r.time,
                        distance: r.distance ];

        def dateString = removeDashes(r.date);  

        if (! runnerExists(r.runnerId)) {
            return [ error: "Runner not found" ];
        }    

        if (runExists(r.runnerId, r.date)) {
           bodyMap._rev = restGet("${COUCH_DB}/${r.runnerId}_${dateString}")._rev;
        }

        def myMap = restPut("${COUCH_DB}/${r.runnerId}_${dateString}", bodyMap);

        return r;
    }    


    @RequestMapping(value = "getallruns/{runnerId}", method = RequestMethod.GET) 
    @ResponseBody
    public Object getAllRunsForRunner(@PathVariable String runnerId) {

        if (! runnerExists(runnerId)) {
            return [ error: "Runner not found" ];
        }  

        def myMap = restGet("${COUCH_VIEW_URL}/runsByRunnerIdDate", [ key:"[\"${runnerId}\"]", endkey:"[\"${runnerId}\",{}]" ]);

        def runs = myMap.rows.collect {
            [ runnerId: it.value[0], 
              date: it.value[1], 
              time: it.value[2], 
              distance: it.value[3] ] as Run;              
        }

        return runs;
    }


    @RequestMapping(value = "getrun/{runnerId}/{date}", method = RequestMethod.GET) 
    @ResponseBody
    public Object getRunForRunner(@PathVariable String runnerId, @PathVariable String date) {

        if (! runExists(runnerId, date)) {
            if (! runnerExists(runnerId)) {
                return [ error: "Runner not found" ];
            }
            return [ error: "Run not found" ];
        }         

        def dateString = removeDashes(date);  

        def myMap = restGet("${COUCH_VIEW_URL}/allruns", [ key:"\"${runnerId}_${dateString}\"" ]);

        def runs = myMap.rows.collect {
            [ runnerId: it.value[0], 
              date: it.value[1], 
              time: it.value[2], 
              distance: it.value[3] ] as Run;              
        }

        return runs[0];
    }    

}
