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
        User userId = userRepository.findByEmail(updatedUser.getEmail());
        if ( userId != null) {
            userId.setName(updatedUser.getName());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                userId.setPassword(updatedUser.getPassword());
            }
            return ResponseEntity.ok(userRepository.save(userId));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User tidak ditemukan");
    }

    // New endpoint for updating user photo (KTP or profile photo)
    @PutMapping("/photo")
    public ResponseEntity<?> updatePhoto(
            @RequestParam("email") String userEmail,
            @RequestPart("photo") MultipartFile photoFile) {
        User userId = userRepository.findByEmail(userEmail);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User tidak ditemukan");
        }
        try {
            userId.setFotoKTP(photoFile.getBytes());
            userRepository.save(userId);
            return ResponseEntity.ok("Foto berhasil diupdate");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Gagal menyimpan foto");
        }
    }

    // Endpoint to get user photo as image
    @GetMapping("/photo/{userEmail}")
    public ResponseEntity<?> getPhoto(@PathVariable String userEmail) {
        User userId = userRepository.findByEmail(userEmail);
        if (userId == null || userId.getFotoKTP() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Foto tidak ditemukan");
        }
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(userId.getFotoKTP());
    }

    @GetMapping("/email/{userEmail}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String userEmail) {
        User userId = userRepository.findByEmail(userEmail);
        if (userId != null) return ResponseEntity.ok(userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User tidak ditemukan");
    }

    @PutMapping("/update-name/{userEmail}")
    public ResponseEntity<?> updateName(@PathVariable String userEmail, @RequestBody Map<String, String> body) {
        User userId = userRepository.findByEmail(userEmail);
        if (userId != null) {
            userId.setName(body.get("name"));
            userRepository.save(userId);
            return ResponseEntity.ok(userId);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User tidak ditemukan");
    }

    @GetMapping("/profile/{userEmail}")
    public ResponseEntity<?> getProfile(@PathVariable String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User tidak ditemukan");
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id_user", user.getId_user());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("hasProfilePhoto", user.getFotoKTP() != null);
        profile.put("hasKTPPhoto", user.getFotoKTP() != null);
        profile.put("verified", user.isVerified());  // ✅ Add this line

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/verify/{email}")
    public ResponseEntity<?> verifyUser(@PathVariable String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User tidak ditemukan");
        }

        user.setVerified(true);
        userRepository.save(user);
        return ResponseEntity.ok("User berhasil diverifikasi");
    }
}