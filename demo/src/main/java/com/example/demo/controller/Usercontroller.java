package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.User;
import com.example.demo.repo.UserRepository;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class Usercontroller {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email sudah digunakan.");
        }

        // Simpan user baru
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        User user = userRepository.findByEmail(email);
        if (user != null) {
            if (user.getPassword().equals(password)) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Password salah");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email tidak ditemukan");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser) {
        User user = userRepository.findByEmail(updatedUser.getEmail());
        if (user != null) {
            user.setName(updatedUser.getName());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(updatedUser.getPassword());
            }
            return ResponseEntity.ok(userRepository.save(user));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User tidak ditemukan");
    }

    // New endpoint for updating user photo (KTP or profile photo)
    @PutMapping("/photo")
    public ResponseEntity<?> updatePhoto(
            @RequestParam("email") String email,
            @RequestPart("photo") MultipartFile photoFile) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User tidak ditemukan");
        }
        try {
            user.setFotoKTP(photoFile.getBytes());
            userRepository.save(user);
            return ResponseEntity.ok("Foto berhasil diupdate");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Gagal menyimpan foto");
        }
    }

    // Endpoint to get user photo as image
    @GetMapping("/photo/{email}")
    public ResponseEntity<?> getPhoto(@PathVariable String email) {
        User user = userRepository.findByEmail(email);
        if (user == null || user.getFotoKTP() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Foto tidak ditemukan");
        }
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(user.getFotoKTP());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) return ResponseEntity.ok(user);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User tidak ditemukan");
    }

    @PutMapping("/update-name/{email}")
    public ResponseEntity<?> updateName(@PathVariable String email, @RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setName(body.get("name"));
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User tidak ditemukan");
    }


}