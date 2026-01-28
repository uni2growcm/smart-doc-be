package it.oh.plugin.customer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @GetMapping("/{id}")
    public Map<String, Object> getCustomer(@PathVariable String id) {
        return Map.of(
            "id", id,
            "name", "Mario Rossi",
            "source", "customer-plugin"
                     );
    }
}
