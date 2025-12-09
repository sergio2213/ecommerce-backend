package com.ecommerce.service.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void saveUser_Success() {
        String rawPassword = "password123";
        String encodedPassword = "encoded_password_simulated_for_test";
        
        User userToSave = new User(null, "testuser", "email@email.com", rawPassword, null);

        Role mockRole = new Role(1L, "ROLE_USER");

        User userSaved = new User(1L, "testuser", "email@email.com" , encodedPassword, Collections.singleton(mockRole));

        when(this.passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        when(this.roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(mockRole));

        when(this.userRepository.save(any(User.class))).thenReturn(userSaved);

        User result = this.userService.saveUser(userToSave);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(this.userRepository, times(1)).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();

        verify(this.passwordEncoder, times(1)).encode(rawPassword);

        assertTrue(capturedUser.getPassword().equals(encodedPassword));

        assertTrue(capturedUser.getRoles().contains(mockRole) && capturedUser.getRoles().size() == 1);

        assertTrue(result.getId().equals(1L));
    }

    @Test
    void saveUser_Failure_RoleNotFound() {
        String rawPassword = "password123";
        String encodedPassword = "encoded_password_simulated_for_test";

        User userToSave = new User(null, "testuser", "email@email.com", rawPassword, null);

        when(this.passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        when(this.roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.userService.saveUser(userToSave));

        assertTrue(exception.getMessage().contains("Role not found"));

        verify(this.userRepository, never()).save(any(User.class));
        verify(this.passwordEncoder, times(1)).encode(rawPassword);
        verify(this.roleRepository, times(1)).findByName("ROLE_USER");
        
    }

}
