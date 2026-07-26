package com.extension.codepilot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	 @GetMapping("/")
	    public String home() {
	        return "Backend Running...";
	    }

	    @GetMapping("/hello")
	    public String hello() {
	        return "Hello User";
	    }
}
