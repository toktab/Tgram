package dev.toktab.Tgram.controller;

import dev.toktab.Tgram.model.User;
import dev.toktab.Tgram.repository.UserRepo;
import dev.toktab.Tgram.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@RestController
@RequestMapping
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String goHome() {
        return "Publicly accessible url";
    }

    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody User user) {
        return userService.create(user);
    }

    @GetMapping("/admin/users/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> getAllUsers() {
        return userService.getAll();
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Object> getMyDetails() {
        if (!userService.isEnabled(userService.getActiveUserDetails())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User disabled. Please log out.\n'/logout");
        }
        return userService.getActiveUserDetails();
    }

    @DeleteMapping("/profile/delete")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Object> disableUser(@RequestParam(name = "confirm", defaultValue = "false") boolean confirm) {
        // /profile/delete?confirm=true
        if (!confirm) {
            // If confirmation is not provided, return a redirect response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.LOCATION, "/profile");
            return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
        }

        var activeUser = userService.getActiveUserDetails();
        return userService.disable(activeUser);
    }

    @PostMapping("/profile/edit")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Object> updateUser(@RequestBody User newUser) {
        var oldUserResponse = userService.getActiveUserDetails();
        User oldUser = extractUserFromResponseEntity(oldUserResponse);
        return userService.update(oldUser, newUser);

        //example:
        //POST
//        {
//            "username": "taba",
//              "email": "taba@gmail.com",
//              "password": "taba",
//              "picture": "null"
//        }

        //Basic Auth
        //toko toko
    }
    public User extractUserFromResponseEntity(ResponseEntity<Object> responseEntity) {
        if (responseEntity.getBody() instanceof Optional) {
            Optional<User> userOptional = (Optional<User>) responseEntity.getBody();
            return userOptional.orElse(null);
        } else {
            return null;
        }
    }



}


//    @GetMapping("/users/single")
//    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
//    public ResponseEntity<Object> getMyDetails(){
//        return ResponseEntity.ok(myUserRepo.findByEmail(getLoggedInUserDetails().getUsername()));
//    }
//    public UserDetails getLoggedInUserDetails(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(authentication != null && authentication.getPrincipal() instanceof UserDetails){
//            return (UserDetails) authentication.getPrincipal();
//        }
//        return null;
//    }

