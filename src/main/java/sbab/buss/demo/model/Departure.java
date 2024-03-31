package sbab.buss.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/*Container class for json data*/
@Data
@AllArgsConstructor
public class Departure {
    private String destination;
    private Integer directionCode;
    private StopArea stopArea;

    @Data
    @AllArgsConstructor
    public static class StopArea {
        private Integer id;
        private String name;
        private String designation;
    }

    @Data
    @AllArgsConstructor
    public static class Sites {
        private Integer id;
        private String name;
}
}