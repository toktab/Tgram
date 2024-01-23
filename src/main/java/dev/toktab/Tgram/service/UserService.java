package dev.toktab.Tgram.service;

import dev.toktab.Tgram.config.UserInfoDetails;
import dev.toktab.Tgram.model.User;
import dev.toktab.Tgram.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    private boolean credentialsUsed(User user) {
        List<String> usernames = userRepo.findAllUsername();
        List<String> emails = userRepo.findAllEmail();
        return usernames.contains(user.getUsername()) || emails.contains(user.getEmail());
    }

    // CREATE
    public ResponseEntity<Object> create(User user) {
        if (credentialsUsed(user)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Username or email already used");
        }
        try {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);

            user.setCreatedOn(LocalDateTime.now());
            user.setUpdatedOn(LocalDateTime.now());
            user.setEnabled(true);

            userRepo.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User saved");
        } catch (Exception e) {
            // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Exception caught");
        }
    }

    // UPDATE
    public ResponseEntity<Object> update(User newUser) {
        try {
            Optional<User> optionalOldUser = userRepo.findById(newUser.getId());
            if (optionalOldUser.isPresent()) {
                User oldUser = optionalOldUser.get();

                // Check if nothing changed
                if (oldUser.equals(newUser)) {
                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Error: Nothing changed");
                }

                // Update fields
                oldUser.setUsername(newUser.getUsername());
                oldUser.setEmail(newUser.getEmail());
                oldUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
                oldUser.setUpdatedOn(LocalDateTime.now());
                oldUser.setEnabled(newUser.isEnabled());
                oldUser.setPicture(newUser.getPicture());
                oldUser.setRoles(newUser.getRoles());

                // Save the updated user
                userRepo.save(oldUser);

                return ResponseEntity.ok("User updated");
            }
            // User with the given ID not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: User not found");
        } catch (Exception e) {
            // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Exception caught");
        }
    }

    public ResponseEntity<Object> getAll() {
        List<User> users = userRepo.findAll();

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: No users found");
        } else {
            return ResponseEntity.ok(users);
        }
    }

    public ResponseEntity<Object> getActiveUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof UserDetails){
            return ResponseEntity.ok(userRepo
                    .findByUsername(
                            ((UserDetails) authentication
                                    .getPrincipal())
                                    .getUsername())
            );
        }
        return null;
    }
    public ResponseEntity<Object> get(String username) {
        Optional<User> user = userRepo.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Couldn't find the user, because username is invalid");
        } else {
            return ResponseEntity.ok(user);
        }
    }

//    public ResponseEntity<Object> disable() {
//        ResponseEntity<Object> activeUserDetails = getActiveUserDetails();
//
//        if(activeUserDetails.getStatusCode().is2xxSuccessful()){
//            Object responseBody = activeUserDetails.getBody();
//            if(responseBody instanceof User){
//                User user = (User) responseBody;
//                if(user.isEnabled()){
//                    user.setEnabled(false);
//                }else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: User is disabled");
//            }
//        }
//        //todo it stops at 136 and doesnt access 126
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Internal error, couldn't disable");
//    }

    public void disable(ResponseEntity<Object> activeUser) {
        if (activeUser.getStatusCode().is2xxSuccessful()) {
            Object responseBody = activeUser.getBody();

            if (responseBody instanceof Optional) {
                Optional<User> userOptional = (Optional<User>) responseBody;

                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    user.setEnabled(false);//Disabling
                    userRepo.save(user);//Saving

                } else {
                    System.out.println("Response body is an empty Optional");
                }
            } else {
                System.out.println("Response body is not an Optional<User>. Actual class: " + responseBody.getClass());
            }
        } else {
            System.out.println("Response status is not successful");
        }
    }

    public UserInfoDetails mapUserToUserInfoDetails(User user) {
        UserInfoDetails userInfoDetails = new UserInfoDetails();
        userInfoDetails.setUsername(user.getUsername());
        userInfoDetails.setEmail(user.getEmail());
        userInfoDetails.setPassword(user.getPassword());
        userInfoDetails.setCreatedOn(user.getCreatedOn());
        userInfoDetails.setUpdatedOn(user.getUpdatedOn());
        userInfoDetails.setEnabled(user.isEnabled());
        userInfoDetails.setPicture(user.getPicture());
        userInfoDetails.setRoles(Arrays.stream(user.getRoles().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));
        return userInfoDetails;
    }

    public User mapUserInfoDetailsToUser(UserInfoDetails userInfoDetails) {
        User user = new User();
        user.setUsername(userInfoDetails.getUsername());
        user.setEmail(userInfoDetails.getEmail());
        user.setPassword(userInfoDetails.getPassword());
        user.setCreatedOn(userInfoDetails.getCreatedOn());
        user.setUpdatedOn(userInfoDetails.getUpdatedOn());
        user.setEnabled(userInfoDetails.isEnabled());
        user.setPicture(userInfoDetails.getPicture());
        user.setRoles(userInfoDetails.getRoles().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        return user;
    }

    public boolean isEnabled(ResponseEntity<Object> activeUser) {
        if (activeUser.getStatusCode().is2xxSuccessful()) {
            Object responseBody = activeUser.getBody();

            if (responseBody instanceof Optional) {
                Optional<User> userOptional = (Optional<User>) responseBody;

                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    return user.isEnabled();

                } else {
                    System.out.println("Response body is an empty Optional");
                }
            } else {
                System.out.println("Response body is not an Optional<User>. Actual class: " + responseBody.getClass());
            }
        } else {
            System.out.println("Response status is not successful");
        }
        return false;
    }
}

