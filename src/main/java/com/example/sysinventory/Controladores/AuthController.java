package com.example.sysinventory.Controladores;

import com.example.sysinventory.Servicios.TokenService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public String success(@AuthenticationPrincipal OAuth2AuthenticationToken auth,
                          HttpSession session,
                          RedirectAttributes redirectAttrs) {
        if (auth == null) {
            redirectAttrs.addFlashAttribute("error", "No se pudo autenticar. Intente de nuevo.");
            return "redirect:/auth/login";
        }

        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                auth.getAuthorizedClientRegistrationId(),
                auth.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            redirectAttrs.addFlashAttribute("error", "No se obtuvo token de acceso.");
            return "redirect:/auth/login";
        }

        String accessToken = client.getAccessToken().getTokenValue();
        tokenService.setAccessToken(accessToken);
        session.setAttribute("accessToken", accessToken);

        return "redirect:/";
    }
}