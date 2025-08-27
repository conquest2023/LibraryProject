package project.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MapController
{


    @GetMapping("/")
    public String home(){

        return  "home.html";

    }


    @GetMapping("/map")
    public String getMap()
    {
        return "map.html";
    }
}
