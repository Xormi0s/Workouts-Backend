package com.xormios.workouts.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/workout")
public class WorkoutController {

    @GetMapping("test")
    public String test(){
        return "test";
    }
}
