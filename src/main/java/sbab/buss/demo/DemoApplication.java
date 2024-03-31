package sbab.buss.demo;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import sbab.buss.demo.model.Line;
import sbab.buss.demo.model.StockholmTrafficApi;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableCaching
public class DemoApplication {
    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

    }

    @PostConstruct
    private void start() {
        System.out.println("Start fetching data, please wait.. ");
        StockholmTrafficApi trafficApi = applicationContext.getBean(StockholmTrafficApi.class);
        List<Map.Entry<Integer, Line>> data = trafficApi.fetchDataFromApi();
        for (Map.Entry<Integer, Line> entry : data) {
            System.out.println("Line :" + entry.getKey());
            System.out.println("Bus stops o onn their route " + entry.getValue().getStopAreaCounter());
            entry.getValue().getNames().forEach(System.out::println);
            System.out.println("**************************************");
        }
        System.out.println("End of fetching data, Thank you for your patient.. ");
    }
}
