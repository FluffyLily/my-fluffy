package com.renee328.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {

    @RequestMapping(value = {
            "/",
            "/main",
            "/notice",
            "/admin",
            "/user",
            "/board",
            "/post/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
