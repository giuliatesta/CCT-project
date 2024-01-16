package it.giuliatesta.cctproject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/counter")
    public int greeting(@RequestParam(value = "counter") int counter) {
        return ++counter;
    }
}
