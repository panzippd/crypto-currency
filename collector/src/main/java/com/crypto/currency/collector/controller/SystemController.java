package com.crypto.currency.collector.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Panzi
 * @Description Health check and shut down the system
 * @date 2022/4/26 16:49
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    private final ApplicationContext context;

    private static final String OK = "I am OK!";
    private static final String NOT_OK = "Not health!";
    private static final String SHUT_DOWN = "Shutting Down in 15 seconds!";

    private static final AtomicBoolean HEALTH = new AtomicBoolean(true);

    public SystemController(ApplicationContext context) {
        this.context = context;
    }

    @Autowired
    private SystemShutDownController shutDownController;

    @GetMapping("/shutdown")
    public ResponseEntity<String> shutdownApp() {
        HEALTH.set(false);

        new Thread(() -> {
            System.out.println("Shutdown Hook is running !");
            shutDownController.shutDownAll();
            try {
                Thread.sleep(1000 * 15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(SpringApplication.exit(context, () -> 0));
        }).start();

        System.out.println("Application Terminating ...");
        return new ResponseEntity<>(SHUT_DOWN, HttpStatus.OK);
    }

    @RequestMapping(value = "/health", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.HEAD})
    public ResponseEntity<String> health() {
        if (HEALTH.get()) {
            return new ResponseEntity<>(OK, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(NOT_OK, HttpStatus.BAD_GATEWAY);
        }
    }
}
