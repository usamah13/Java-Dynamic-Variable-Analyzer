package com.restservice;

import ast.Main;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestController
class OutputController {
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/output", consumes = "application/json", produces = "application/json")
        public Object newRequest(@RequestBody Request newRequest) {


        try {
            String jsonString = Main.process(newRequest.program);
            System.out.println(jsonString);
            return new ResponseEntity<>(jsonString, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            return new ResponseEntity<>(exceptionAsString, HttpStatus.BAD_REQUEST);
        }

        //return jsonString;
    }
}