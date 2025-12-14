package com.sp.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KafkaController {

    private final Producer kafkaProducer;

    @GetMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String message){
        kafkaProducer.sendMessage(message);
        return ResponseEntity.ok("Message sent successfully");
    }


    @GetMapping("/sendAsync")
    public ResponseEntity<String> sendMessageAsync(@RequestParam String message){
        kafkaProducer.sendMessageWithCallback(message);
        return ResponseEntity.ok("Message sent successfully");
    }


    @GetMapping("/sendAsyncMultiple")
    public ResponseEntity<String> sendMessageAsyncMultiple(@RequestParam String message, @RequestParam boolean isBatch){
        kafkaProducer.sendMessageWithCallbackMultiple(message, isBatch);
        return ResponseEntity.ok("Message sent successfully");
    }

    @GetMapping("/sendKeyValue")
    public ResponseEntity<String> sendMessageKeyValue(@RequestParam String key, @RequestParam String value){
        kafkaProducer.sendMessageWithKey(key, value);
        return ResponseEntity.ok("Message sent successfully");
    }


}
