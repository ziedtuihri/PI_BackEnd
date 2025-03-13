package esprit.example.pi.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }
}
