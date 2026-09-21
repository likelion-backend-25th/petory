package net.likelion.bebc25.projectpatory.running;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class RunningController {

    @GetMapping("/running")
    public ResponseEntity<Map<String, String>> runningCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Pet Club Backend Server is running!");
        return ResponseEntity.ok(response);
    }
}