package sbab.buss.demo.model;


import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import sbab.buss.demo.Utils.TrafficUtils;
import sbab.buss.demo.configuration.ConstantApi;
import sbab.buss.demo.configuration.TrafficProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class StockholmTrafficApi {

    private ObjectMapper objectMapper;
    private TrafficProps trafficProps;

    @Autowired
    public StockholmTrafficApi(ObjectMapper objectMapper , TrafficProps trafficProps) {
        this.objectMapper = objectMapper;
        this.trafficProps = trafficProps;
    }


    //This method will gather data from different traffiklab api endpoint

    @Cacheable("frequentStop")
    public List<Map.Entry<Integer, Line>> fetchDataFromApi() {
        var stopPointData = getStopPointData(trafficProps);
        var sites = getSites(stopPointData, trafficProps);
        var lineIdAndBusDeparture = new HashMap<Integer, Set<Departure>>();
        sites.forEach(site -> getDepartures(lineIdAndBusDeparture, trafficProps, site));
        Map<Integer, Line> mostFrequentStop = TrafficUtils.findMostStopBus(lineIdAndBusDeparture);
        return TrafficUtils.sort(mostFrequentStop);
    }



    // Fetch all siteId from
    // https://transport.integration.sl.se/v1/sites
    @Cacheable("sites")
    private Set<Integer> getSites(Map<Integer, String> idsWithNames, TrafficProps trafficProps) {
        var departuresUrl = TrafficUtils.buildSimpleUri(trafficProps.getBase(), trafficProps.getSites());
        var header = TrafficUtils.getHeader();
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put(ConstantApi.EXPAND, List.of(trafficProps.getExpand()));
        var uri = TrafficUtils.uriTemplate(departuresUrl, queryParams);
        var response = TrafficUtils.getResponse(header, uri).getBody();
        return TrafficUtils.getSitesFromJson(response, objectMapper, idsWithNames);
    }


    //Get stop point area name with ids from
    // https://transport.integration.sl.se/v1/stop-points
    @Cacheable("stopPoint")
    private Map<Integer, String> getStopPointData(TrafficProps trafficProps) {
        var stopPointUrl = TrafficUtils.buildSimpleUri(trafficProps.getBase(), trafficProps.getStopPoint());
        var header = TrafficUtils.getHeader();
        var uri = TrafficUtils.uriTemplate(stopPointUrl, new LinkedMultiValueMap<>());
        var response = TrafficUtils.getResponse(header, uri).getBody();
        return TrafficUtils.getStopPointDataFromJson(response, trafficProps, objectMapper);
    }


    // Get Bus information from
    // https://transport.integration.sl.se/v1/sites/{id}/departures

    @Cacheable("departures")
    private Object getDepartures(Map<Integer, Set<Departure>> lineIdAndBusDeparture, TrafficProps trafficProps, Integer siteId) {
        var departuresUrl = TrafficUtils.buildSimpleUri(trafficProps.getBase(), trafficProps.getSites(), "/", Integer.toString(siteId), trafficProps.getDepartures());
        var header = TrafficUtils.getHeader();
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put(ConstantApi.TRANSPORT, List.of(trafficProps.getTransport()));
        var response = TrafficUtils.getResponse(header, TrafficUtils.uriTemplate(departuresUrl, queryParams)).getBody();
        TrafficUtils.getDeparturesDataFromJson(response, objectMapper, lineIdAndBusDeparture);
        return lineIdAndBusDeparture;
    }

}
