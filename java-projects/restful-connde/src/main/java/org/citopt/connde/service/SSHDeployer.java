package org.citopt.connde.service;

import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.type.Type;
import org.citopt.connde.domain.type.Code;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.TypeRepository;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component
public class SSHDeployer {

    private static final Logger LOGGER = Logger.getLogger(SSHDeployer.class.getName());

    private static final String SCRIPTDIR = "/home/pi/scripts";
    private static final String SERVICEDIR = "/etc/init";
    private static final String SERVICEPREFIX = "connde";

    private static final String AUTODEPLOY_FILE = "/etc/connde/autodeploy.conf";
    private static final String AUTODEPLOY_NAME_SUFIX = " AUTO@";

    public static final int SSH_PORT = 22;

    public static String DEFAULT_USER = "pi";

    public static String KEY
            = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEogIBAAKCAQEAyGALfW0RP//eXFfhKfVcQK8rCCxymWBduf0rmMmDApN50Kzv\n"
            + "ESS955Y8HWvTPGDwd0ny6rthWcbDRF2+2J2AsKa+UnrXamZ3PdOfIPmuCFSigiQd\n"
            + "fnjFk8Zg8sdtywBCBy2SHwq7QBsZME2Aztyx3L4k4lk2VK8w+2F9gCmAVxY+KLDN\n"
            + "Da5NsgVEe9xVvvzhwkmf86T6r4dhYmWPgzW30GkUh4vvBvozBbfa0YV/vj4f1DP0\n"
            + "U3l91wiUl96Ag0e7r2wsCuufW6Gs8Gy1IE/CpAbyrUxrH+yDoNFur0QP7qDiioRR\n"
            + "X7p+HpCdhl3qKKB6CeflpQOlKpx7Pj87QhL0LQIDAQABAoIBACzWWRva8RY6Ij7V\n"
            + "p1vlPJx41g9BKu+pQa/huAS7auaDq6mHWQOkDh6pXpBS1XTYWFbJJGNkRLd7I6zD\n"
            + "sXX1YJum5EW+mT+E6D/cf+o4FLpmferTPApV6hhUNtN8ztOzHhNPHjh2BUqmBa/q\n"
            + "V91yQxabMdO4lNDEVxiZSyUHpGFYAj4odQVJvGRG2502L0BKyYeMABmtZrKjaS5K\n"
            + "aahbL0Z2pkQ+gakEn+1cb/Rd2IDQhrA6EpacK9reoWydpUxP/MReQdeMU62rwqFe\n"
            + "TpEPc6ZS19XxWKyIhHHLiZl7qNcXkCOK64kEgvlark9miNj3JUf9P0OAmElRAtdM\n"
            + "PXP6Qf0CgYEA8mwnIyJ1atBsqgTySD6X+dTPtHUiSJ8euOtiqQTH8t2MTU06mZuA\n"
            + "8e7Fy45yxKSQ7w6uJA9UJs2Ru2vN6lC6zav0ri4LXhv2VAwJFkQKDv2fSR1lOAbk\n"
            + "/cKnwoWNSqda+lq+Bl7ZiLxSeviYbus+LcgIq3HyBVmcvKpIJRN/tYMCgYEA05kE\n"
            + "2fI5/dnyH1MvLCoSKkYp40uUwatnSDt07WSqa3SH5E/uz4lasFcgeJSmqOYpa3tw\n"
            + "/bqBXNlqWCWI6oNi/23Pv4mj69EFrfSf85IQcms8dGStdcin+9VmYSJpn+QPgia/\n"
            + "n4vm125CQrURmuE2r+oOcV3ShcpO1lS4AMs8MI8CgYBoFi3btRDrMuBlQ8hvYojI\n"
            + "WSpxVhXJTqDXTyHGZmofiiaSjkVJ7O25cwb0No5qhipAqnH0w6wjGQKokUoRgGYk\n"
            + "pt9g5h41YxYp0h0YtVAITbdVokxyeOtbVXfIWqVm12KFue57N8B5KDrV1+VDQrgo\n"
            + "2gl26266A1b73rUpTiz4VwKBgHgUtrQYyuBM9yLfyj1+AqELAGqFUf42j35mf4zZ\n"
            + "O/2PPC9NTXFpuZWpXDwR4CKpu4fLnevgE9nlaHxtkK3FskDSyLsiGWySSm7WDI/l\n"
            + "rH/Ca6SCHg5huTMpf9hP9zFN858g7k5UzsQjRmck6sDCXo6mfVvIqthSXzszCNkq\n"
            + "fRXxAoGARRp2fahKz31kUOVprVSK2UsH340fET43X3QlygyNI33J4V6tYUpTgCY7\n"
            + "dyBUmBHZKeZwJYYAtfkI4ACDCI0KEa6NdzAtwcwUgsR10fh6jGGBrKT88F4C5Xe1\n"
            + "8JinHG8VObUcB1S7+vmct88/ELxa+9CnJ/NbiYyDw0cuAxqWUWg=\n"
            + "-----END RSA PRIVATE KEY-----";

    public static String PUBKEY
            = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDIYAt9bRE//95cV+Ep9VxArysILH"
            + "KZYF25/SuYyYMCk3nQrO8RJL3nljwda9M8YPB3SfLqu2FZxsNEXb7YnYCwpr5Setdq"
            + "Znc9058g+a4IVKKCJB1+eMWTxmDyx23LAEIHLZIfCrtAGxkwTYDO3LHcviTiWTZUrz"
            + "D7YX2AKYBXFj4osM0Nrk2yBUR73FW+/OHCSZ/zpPqvh2FiZY+DNbfQaRSHi+8G+jMF"
            + "t9rRhX++Ph/UM/RTeX3XCJSX3oCDR7uvbCwK659boazwbLUgT8KkBvKtTGsf7IOg0W"
            + "6vRA/uoOKKhFFfun4ekJ2GXeoooHoJ5+WlA6UqnHs+PztCEvQt pi@raspberrypi";

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private TypeRepository typeRepository;

    public static String getScriptDir(String id) {
        return SCRIPTDIR + "/" + SERVICEPREFIX + id;
    }

    public static String parseService(String service, Map<String, String> values) {
        for (String k : values.keySet()) {
            if (values.get(k) != null && !values.get(k).isEmpty()) {
                service = service.replace(k, values.get(k));
            }
        }
        return service;
    }

    public void autodeploy(Device address, Integer port, String user, String key, String mqtt)
            throws UnknownHostException, IOException {
        String url = address.getIpAddress();
        Shell shell = new Shell.Safe(
                new SSH(
                        url, port,
                        user, key
                )
        );

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        // read autodeploy configuration
        System.out.println("read conf file");
        shell.exec(
                "sudo bash -c \"cat " + AUTODEPLOY_FILE + "\"",
                new ByteArrayInputStream("".getBytes()),
                stdout,
                stderr
        );
        System.out.println("read conf file successful");

        JsonObject root;
        //get JsonObject from JsonReader
        try (JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(stdout.toByteArray()))) {
            //get JsonObject from JsonReader
            root = jsonReader.readObject();
            //we can close IO resource and JsonReader now
            JsonArray sensors = root.getJsonArray("sensors");
            JsonArray actuators = root.getJsonArray("actuators");

            // iterate
            sensors.forEach((JsonValue jSensor) -> {
                // register (if not registered) and deploy sensor
                if (jSensor instanceof JsonObject) {
                    Sensor sensor = findOrRegisterSensor(sensorRepository, (JsonObject) jSensor, address);
                    if (sensor != null) {
                        try {
                            sensor = sensorRepository.insert(sensor);
                            
                            String jPinset = ((JsonObject) jSensor).getString("pinset");
                            
                            deploy(sensor.getId(), sensor.getDevice().getIpAddress(), port, user, key, mqtt, sensor.getType(), "SENSOR", jPinset);
                        } catch (IOException ex) {
                        }
                    }
                }
            });

            actuators.forEach((JsonValue jActuator) -> {
                // register (if not registered) and deploy sensor
                System.out.println(jActuator);
            });

        } catch (Exception e) {
            throw e;
        }

    }

    public void deploy(String id, String url, Integer port, String user,
            String key, String mqtt, Type type,
            String component, String pinset)
            throws UnknownHostException, IOException {
        LOGGER.log(Level.FINE, "service deploy called for: "
                + "{0} {1} {2} {3} {4} {5} {6} {7} {8}",
                new Object[]{
                    id, url, port, user, key, mqtt, type, component, pinset});
        System.out.println("service deploy called for: "
                + id + url + port + user + key + mqtt + type + component + pinset);

        String scriptDir = getScriptDir(id);
        String servicename = SERVICEPREFIX + id;
        String topicName = new String(component.toLowerCase()) + "/" + id;
        
        Shell shell = new Shell.Safe(new SSH (url, port, user, key));

        OutputStream stdout = new ByteArrayOutputStream();
        OutputStream stderr = new ByteArrayOutputStream();

        // creates routine dir
        System.out.println("starting remote mkdir, dir=" + scriptDir);
        shell.exec(
                "mkdir -p " + scriptDir,
                new ByteArrayInputStream("".getBytes()),
                stdout,
                stderr
        );
        System.out.println("remote mkdir successful");

        System.out.println("copying adapter scripts to device");
        for (Code routine : type.getRoutines()) {
            String content = routine.getContent();
            // copies routine        
            shell.exec(
                    "sudo bash -c  \"cat > " + scriptDir + "/" + routine.getName() + "\"",
                    new ByteArrayInputStream(content.getBytes()),
                    stdout,
                    stderr
            );
        }
        System.out.println("copying scripts was succesful");
        
        // executing install script
        shell.exec("sudo chmod +x " + scriptDir + "/install.sh | sudo bash " + scriptDir + "/install.sh " + topicName + " " + mqtt + " " + scriptDir, 
        		new ByteArrayInputStream("".getBytes()), stdout, stderr);
        
        // executing start script
        shell.exec("sudo chmod +x " + scriptDir + "/start.sh | sudo bash " + scriptDir + "/start.sh " + scriptDir, new ByteArrayInputStream("".getBytes()), stdout, stderr);
        System.out.println(stdout.toString());
        System.out.println(stderr.toString());

//        if (type.getService() != null) {
//	        String service = type.getService().getContent();
//	        Map<String, String> serviceParser = new HashMap<>();
//	        serviceParser.put("${dir}", scriptDir);
//	        serviceParser.put("${id}", id);
//	        serviceParser.put("${mqtturl}", mqtt);
//	        serviceParser.put("${component}", component);
//	        serviceParser.put("${pinset}", pinset);
//	        service = parseService(service, serviceParser);
//	        System.out.println("service file parsing done");
//	
//	        System.out.println("starting remote Service output");
//	        shell.exec(
//	                "sudo bash -c  \"cat > " + SERVICEDIR + "/"
//	                + servicename + ".conf\"",
//	                new ByteArrayInputStream(service.getBytes()),
//	                stdout,
//	                stderr
//	        );
//	        System.out.println("remote Service output succesful");
//	
//	        System.out.println("starting remote reload-configuration");
//	        shell.exec(
//	                "sudo initctl reload-configuration",
//	                new ByteArrayInputStream("".getBytes()),
//	                stdout,
//	                stderr
//	        );
//	        System.out.println("remote reload-configuration successful");
//	
//	        // stops old service (if it exists)
//	        try {
//	            System.out.println("trying to stop remote old service");
//	            shell.exec(
//	                    "sudo service " + servicename + " stop",
//	                    new ByteArrayInputStream("".getBytes()),
//	                    stdout,
//	                    stderr
//	            );
//	            System.out.println("stop remote old service successful");
//	        } catch (Exception e) {
//	            System.out.println("stop remote old service unsuccessful "
//	                    + stdout);
//	        }
//	
//	        // starts service
//	        System.out.println("start remote service");
//	        shell.exec(
//	                "sudo service " + servicename + " start",
//	                new ByteArrayInputStream("".getBytes()),
//	                stdout,
//	                stderr
//	        );
//	        System.out.println("start remote service succesful");
//        }
        
        LOGGER.log(Level.FINE, "adapter deployed successful for id {0}", id);
    }

    public boolean isRunning(String id, String url, Integer port,
            String user, String key)
            throws UnknownHostException, IOException {
        String sid = SERVICEPREFIX + id;

        Shell shell = new Shell.Safe(
                new SSH(
                        url, port,
                        user, key
                )
        );

        OutputStream stdout = new ByteArrayOutputStream();
        OutputStream stderr = new ByteArrayOutputStream();

        //FIXME: list services with native command
        // lists services
        shell.exec(
                "sudo initctl list",
                new ByteArrayInputStream("".getBytes()),
                stdout,
                stderr
        );

        // parse stdout list
        Pattern p = Pattern.compile(sid + "( start/running)(.*)");
        BufferedReader bufReader = new BufferedReader(
                new StringReader(stdout.toString()));
        String line;
        while ((line = bufReader.readLine()) != null) {
            Matcher m = p.matcher(line);
            if (m.matches()) {
                return true;
            }
        }

        return false;
    }

    public void undeploy(String id, String url, Integer port, String user,
            String key)
            throws IOException {
        String sid = SERVICEPREFIX + id;

        Shell shell = new Shell.Safe(
                new SSH(
                        url, port,
                        user, key
                )
        );

        OutputStream stdout = new ByteArrayOutputStream();
        OutputStream stderr = new ByteArrayOutputStream();

        // stop service
        shell.exec(
                "sudo service " + sid + " stop",
                new ByteArrayInputStream("".getBytes()),
                stdout,
                stderr
        );
    }

    private Sensor findOrRegisterSensor(SensorRepository sensorRepository, JsonObject jSensor, Device device) {
        String jName = jSensor.getString("name");
        String jType = jSensor.getString("type");
        String jPinset = jSensor.getString("pinset");

        // JSON Object must have name, type and pinset attribute
        if (jType == null || jType.isEmpty()) {
            return null;
        }
        if (jPinset == null || jPinset.isEmpty()) {
            return null;
        }
        if (jName == null || jName.isEmpty()) {
            return null;
        }

        // name on file + current device
        String name = getAutodeployName(jName, device.getMacAddress());
        Type type = typeRepository.findByName(jType);
        
        if (type == null) {
            return null;
        }

        // find registered sensor or create a new one
        Sensor sensor = sensorRepository.findByName(name);

        if (sensor == null) {
            sensor = new Sensor();
        }
        sensor.setName(name);
        sensor.setType(type);
        sensor.setDevice(device);

        return sensor;
    }

    private String getAutodeployName(String name, String macAddress) {
        return name + AUTODEPLOY_NAME_SUFIX + macAddress;
    }

}
