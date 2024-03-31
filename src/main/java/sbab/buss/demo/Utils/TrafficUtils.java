package sbab.buss.demo.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import sbab.buss.demo.configuration.ConstantApi;
import sbab.buss.demo.configuration.TrafficProps;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import sbab.buss.demo.model.Line;
import sbab.buss.demo.model.Departure;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public  class TrafficUtils {

    private static final Logger logger = LoggerFactory.getLogger(TrafficUtils.class);


    // Building any simple uri

    public static String buildSimpleUri(String... values) {
        StringBuilder url = new StringBuilder();
        for (String val : values)
            url.append(val);
        return url.toString();
    }

    public static HttpEntity<Object> getHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        return new HttpEntity<>(headers);
    }

    public static HttpEntity<String> getResponse(HttpEntity<Object> entity, String urlTemplate) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(urlTemplate, HttpMethod.GET, entity, String.class);
    }

    public static Map<Integer, String> getStopPointDataFromJson(String response, TrafficProps props, ObjectMapper objectMapper) {
        var sites = new HashMap<Integer,String>();
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
                StreamSupport.stream(jsonNode.spliterator(), false)
                        .forEach(element ->  {
                            if (element.path(ConstantApi.TRANSPORT_AUTHORITY).path(ConstantApi.ID).asInt() ==1 &&
                               (element.path(ConstantApi.TYPE).asText()
                                       .equalsIgnoreCase(props.getType())))
                                sites.put(element.path(ConstantApi.STOP_AREA).path(ConstantApi.ID).asInt(),element.path(ConstantApi.NAME).asText());
                        });
        } catch (JsonProcessingException e) {
            logger.debug ("Couldn't read json data :",e.getMessage());
        }
        return sites;
    }

    public static void getDeparturesDataFromJson(String response, ObjectMapper objectMapper, Map<Integer, Set<Departure>> lineIdAndBusDeparture) {

        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode departureNode = jsonNode.path(ConstantApi.DEPARTURES);
            for (JsonNode element : departureNode) {
                String destination = element.path(ConstantApi.DESTINATION).asText();
                int directionCode = element.path(ConstantApi.DIRECTION_CODE).asInt();
                Departure.StopArea stopArea = null;
                int lineId = 0;

                Iterator<Map.Entry<String, JsonNode>> fields = element.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String fieldName = field.getKey();
                    JsonNode fieldValue = field.getValue();

                    if (fieldName.equals(ConstantApi.STOP_POINT)) {
                        int id = fieldValue.path(ConstantApi.ID).asInt();
                        String name = fieldValue.path(ConstantApi.NAME).asText();
                        String designation = fieldValue.path(ConstantApi.DESIGNATION).asText();
                        stopArea = new Departure.StopArea(id, name, designation);
                    } else if (fieldName.equals(ConstantApi.LINE)) {
                        lineId = fieldValue.path(ConstantApi.ID).asInt();
                    }
                }
                    Departure departure = new Departure(destination, directionCode, stopArea);
                    lineIdAndBusDeparture.computeIfAbsent(lineId, k -> new HashSet<>()).add(departure);
            }
        } catch (JsonProcessingException e) {
            logger.debug ("Couldn't read json data :",e.getMessage());
        }
    }

    //Get final uri template
    public static String uriTemplate(String url, MultiValueMap<String,String> params){
        return  UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParams(params)
                .encode().toUriString();
    }

    public static Set<Integer> getSitesFromJson(String response, ObjectMapper objectMapper, Map<Integer,String> idsWithNames) {
        var sites = new HashSet<Integer>();
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            StreamSupport.stream(jsonNode.spliterator(), false)
                    .forEach(element ->  {
                        for (Map.Entry<Integer, String> entry : idsWithNames.entrySet()) {
                            if (entry.getKey()!=element.path(ConstantApi.ID).asInt()
                                    && entry.getValue().equalsIgnoreCase(element.path(ConstantApi.NAME).asText()))
                                sites.add(element.path(ConstantApi.ID).asInt());
                        }
                    });
        } catch (JsonProcessingException e) {
            logger.debug ("Couldn't read json data :",e.getMessage());
        }
        return sites;

    }

    public static Map<Integer, Line> findMostStopBus(Map<Integer, Set<Departure>> lineIdAndBusDeparture) {
        return lineIdAndBusDeparture.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<String> stopNames = entry.getValue().stream()
                                    .filter(e -> e.getDirectionCode() == 1)
                                    .map(e -> e.getStopArea().getName())
                                    .collect(Collectors.toList());
                            entry.getValue().stream()
                                    .filter(e -> e.getDirectionCode() == 1)
                                    .map(Departure::getDestination)
                                    .findFirst()
                                    .ifPresent(stopNames::add);
                            return new Line(stopNames, stopNames.size());
                        }
                ));

    }

    public static List<Map.Entry<Integer, Line>> sort(Map<Integer, Line> mostStopbus) {
        ArrayList<Map.Entry<Integer, Line>> entries = new ArrayList<>(mostStopbus.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Integer, Line>>() {
            @Override
            public int compare(Map.Entry<Integer, Line> o1, Map.Entry<Integer, Line> o2) {
                return Integer.compare( o2.getValue().getStopAreaCounter(),o1.getValue().getStopAreaCounter());
            }
        });
        return entries.stream().limit(10).collect(Collectors.toList());
    }

}


