package jken.site.endpoint;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexEndpoint {
    @GetMapping({"", "/"})
    public String indexPage() {
        return "/index";
    }

    @GetMapping({"/dashboard"})
    public String dashboardPage() {
        return "/dashboard";
    }
}