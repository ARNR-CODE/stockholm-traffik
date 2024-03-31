package sbab.buss.demo.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Line {
    private List<String> names;
    private int stopAreaCounter;
}
