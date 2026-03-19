package com.example.gamegiaido;

import com.example.gamegiaido.dto.RegisterForm;
import com.example.gamegiaido.model.Role;
import com.example.gamegiaido.model.UserAccount;
import com.example.gamegiaido.repository.UserAccountRepository;
import com.example.gamegiaido.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class AdminSecurityTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void registerShouldAlwaysCreatePlayerRole() {
        String suffix = String.valueOf(System.nanoTime());
        RegisterForm form = new RegisterForm();
        form.setUsername("user" + suffix);
        form.setPassword("12345678");
        form.setDisplayName("User " + suffix);

        authService.register(form);

        UserAccount account = userAccountRepository.findByUsername(form.getUsername()).orElse(null);
        assertNotNull(account);
        assertEquals(Role.PLAYER, account.getRole());
    }
}
