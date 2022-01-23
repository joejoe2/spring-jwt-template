package com.joejoe2.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class IndexController {
    public static final String indexUrl="/";
    public static final String helloUrl="/hello";

    //sub path and accept method
    @RequestMapping(path = indexUrl, method = RequestMethod.GET)
    public String index(){
        //specify temple html
        return "index";
    }

    @RequestMapping(path = helloUrl, method = RequestMethod.GET)
    public String hello(Model model){
        //specify template var
        model.addAttribute("msg", "hello world !");
        model.addAttribute("obj", new Integer(1));
        //specify temple html
        return "hello";
    }
}
