package com.alertaenchentes.controller;

import com.alertaenchentes.model.StatusResponse;
import com.alertaenchentes.model.WeatherData;
import com.alertaenchentes.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final StatusService statusService;

    @GetMapping("/")
    public String home(Model model) {
        StatusResponse status = statusService.getFullStatus();
        WeatherData weather = statusService.getWeather();

        model.addAttribute("pontos", status.getPontos());
        model.addAttribute("alertas", status.getAlertas());
        model.addAttribute("relatos", status.getRelatos());
        model.addAttribute("weather", weather);

        return "index";
    }
}