package sbab.buss.demo.controller;
import sbab.buss.demo.model.StockholmTrafficApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/bus")
public class StockholmTrafficControl {

    private StockholmTrafficApi stockholmTrafficApi;


    @Autowired
    public StockholmTrafficControl(StockholmTrafficApi stockholmTrafficApi) {
        this.stockholmTrafficApi = stockholmTrafficApi;

    }

    @GetMapping(path="/get/all")
    public  Object getMostStopBusses() {
            return stockholmTrafficApi.fetchDataFromApi();
    }



}
