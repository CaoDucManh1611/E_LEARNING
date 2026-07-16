package com.example.doan.Controller;

import com.example.doan.Model.EdaResponse;
import com.example.doan.Service.FlaskApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * File: Controller/EdaController.java
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EdaController {

    private static final Logger log = LoggerFactory.getLogger(EdaController.class);
    private final FlaskApiService flaskApiService;

    public EdaController(FlaskApiService flaskApiService) {
        this.flaskApiService = flaskApiService;
    }

    @GetMapping("/eda")
    public ResponseEntity<EdaResponse> eda(
            @RequestParam(defaultValue = "students") String dataset) {

        log.info("EDA request: dataset={}", dataset);
        EdaResponse response = flaskApiService.getEda(dataset);

        if (response != null && response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }
}
