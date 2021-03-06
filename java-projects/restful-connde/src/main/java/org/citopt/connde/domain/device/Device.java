package org.citopt.connde.domain.device;

import javax.persistence.GeneratedValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 *
 * @author rafaelkperes
 */
public class Device {

    @Id
    @GeneratedValue
    private String id;
    
    @Indexed(unique = true)
    private String name;
    
    @Indexed(unique = true)
    private String macAddress;

    private String ipAddress;

    private String iface;

    private String date;
    
    public static String formatMAC(String raw) {
        if (raw != null) {
            String formatted = raw.replaceAll("(.{2})", "$1" + "-").substring(0, 17);
            return formatted.toUpperCase();
        } else {
            return raw;
        }
    }

    public static String rawMAC(String formatted) {
        String raw = formatted.replace(":", "");
        raw = raw.replace("-", "");
        raw = raw.replace(" ", "");
        raw = raw.toLowerCase();
        return raw;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ip) {
        this.ipAddress = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIface() {
        return iface;
    }

    public void setIface(String iface) {
        this.iface = iface;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
