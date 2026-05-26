package se.iths.lw.microprojectproductservice.controller;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test(@AuthenticationPrincipal Jwt jwt){
        return "✅ JWT verification succeeded!! User: " + jwt.getSubject();
    }
}
