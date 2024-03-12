package dev.toktab.Tgram.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping
public class HomeController {
    @GetMapping("/")
    public String goHome() {
        return "Publicly accessible url";
    }
}
