package com.example.sysinventory.Controladores;

import com.example.sysinventory.Servicios.TokenService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

@Controller
public class AuthController {

    private final TokenService tokenService;

    private final String clientId = "8a2c149d-c019-4287-b370-f545aaa60f2a";
    private final String redirectUri = "https://spmardique.onrender.com/auth/callback";
    private final String tenantId = "f33bed9f-22bd-4350-b7da-66bd88fc6458";
    private final String scope = "Files.Read Files.Read.All Sites.Read.All offline_access";
    private final String state = UUID.randomUUID().toString();

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/auth/login")
    public RedirectView login() {
        String authUrl = "https://login.microsoftonline.com/" + tenantId
                + "/oauth2/v2.0/authorize"
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + redirectUri
                + "&scope=" + scope.replace(" ", "%20")
                + "&state=" + state;
        return new RedirectView(authUrl);
    }

    @GetMapping("/auth/callback")
    public String callback(@RequestParam("code") String code, @RequestParam("state") String state) {
        tokenService.fetchTokens(code, redirectUri);
        return "redirect:/";
    }
}