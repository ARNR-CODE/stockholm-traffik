package sbab.buss.demo.configuration;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("traffic")
public class TrafficProps {
    private String base;
    private String lines;
    private String sites;
    private String transportAuthorityId;
    private String departures;
    private String transport;
    private String expand;
    private String stopPoint;
    private String type;
}

