package com.sydney.vacbook.controller;

import com.sydney.vacbook.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Controller
public class IndexController {


    @RequestMapping("/index")
    public String viewHomePage() {
        return "index";
    }

    public String viewLoginPage(){
        return null;
    }





}
