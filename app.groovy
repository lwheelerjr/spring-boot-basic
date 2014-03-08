import com.resttest.domain.Thing;
import groovyx.net.http.RESTClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import static groovyx.net.http.ContentType.JSON;

@Controller
class SpringBootApp {

    @RequestMapping("/")
    @ResponseBody
    String home() {
        "Hello World!"
    }


    @RequestMapping("test")
    @ResponseBody
    public Thing test() {
        new Thing();
    }


    @RequestMapping("num/{n}")
    @ResponseBody
    public Thing num(@PathVariable Integer n) {
         Thing t = new Thing();
         t.number = n;
         return t;
    }

    @RequestMapping("calc/{n}/{n2}")
    @ResponseBody
    public Thing calc(@PathVariable Integer n, @PathVariable Integer n2) {
         Thing t = new Thing();
         t.number = n + n2;
         return t;
    }

    @RequestMapping("num/{n}/{s}/{n2}")
    @ResponseBody
    public Thing num(@PathVariable Integer n, @PathVariable String s, @PathVariable Integer n2) {
         Thing t = new Thing();
         t.number = n+n2;
         t.name = s;
         return t;
    }


    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.0-RC2')
    def getRESTClient(){
        def rc = new RESTClient("http://localhost:5984/");
        rc.parser.'application/json' = rc.parser.'test/plain';
        return rc;
    }

    HashMap getMap(def resp) {
         ObjectMapper objectMapper = new ObjectMapper();
         HashMap myMap = objectMapper.readValue(resp.data, HashMap.class);
         return myMap;
    }


    @RequestMapping(value = "square", method = RequestMethod.POST) 
    @ResponseBody
    public String calc2(@RequestBody final Thing r) {

        /*
        response = getRESTClient().put(path: "1234334325", contentType: JSON,
        requestContentType:  JSON,
        body: [officer: "Kristen Ree",
                location: "199 Baldwin Dr",
                vehicle_plate: "Maryland 77777",
                offense: "Parked in no parking zone",
                date: "2009/01/31"])
         */

         def couch = getRESTClient();
         def resp = couch.get( path: '_uuids');
         def myMap = getMap(resp);
         println "UUID Map is: ${myMap}";

         def uuid = myMap.get("uuids")[0];

         resp = couch.put( 
                path : "mydb/${uuid}",
                contentType: JSON,
                requestContentType:  JSON,
                    body : [type: "company",
                        name: r.name,
                        employees: r.number]);
         myMap = getMap(resp);
         println "Response Map is: ${myMap}";

         return "New Entry UUID = ${uuid} \n";
    }

}
