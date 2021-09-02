package com.karrar.fluxapplication.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RequestCountRTO {
    private Long requestCounter;
//    private String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
}
