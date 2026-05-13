package com.example.sysinventory.Controladores;

import com.example.sysinventory.Servicios.TokenService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final OAuth2AuthorizedClientService clientService;
    private final TokenService tokenService;

    public AuthController(OAuth2AuthorizedClientService clientService,
                          TokenService tokenService) {
        this.clientService = clientService;
        this.tokenService = tokenService;
    }

    @GetMapping("/auth/login")
    public String login() {
        return "redirect:/oauth2/authorization/azure";
    }

    @GetMapping("/auth/success")
    public String success(HttpSession session, RedirectAttributes redirectAttrs) {
        System.out.println("=== /auth/success reached ===");

        // Obtener el token desde el SecurityContext en vez de inyección directa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication instanceof OAuth2AuthenticationToken)) {
            System.out.println("No OAuth2 authentication found in SecurityContext");
            redirectAttrs.addFlashAttribute("error", "No se pudo autenticar.");
            return "redirect:/auth/login?error=true";
        }

        OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken) authentication;
        System.out.println("Auth principal: " + auth.getName());

        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                auth.getAuthorizedClientRegistrationId(),
                auth.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            System.out.println("client or token is NULL");
            redirectAttrs.addFlashAttribute("error", "No se obtuvo token de acceso.");
            return "redirect:/auth/login?error=true";
        }

        String accessToken = client.getAccessToken().getTokenValue();
        System.out.println("Token obtained: " + accessToken);
        tokenService.setAccessToken(accessToken);
        session.setAttribute("accessToken", accessToken);
        System.out.println("Token saved in session and TokenService");

        return "redirect:/";
    }
}