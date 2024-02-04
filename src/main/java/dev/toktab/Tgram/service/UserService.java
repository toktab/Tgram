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
import java.util.*;
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

    private boolean credentialsUsed(User updatedUser, int id) {
        List<User> users = userRepo.findAll();

        // Use an iterator to safely remove elements
        users.removeIf(user -> user.getId() == id);
        for (User user : users) {
            if (Objects.equals(user.getUsername(), updatedUser.getUsername()) || Objects.equals(user.getEmail(), updatedUser.getEmail())) {
                return true;
            }
        }
        return false;
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

    public ResponseEntity<Object> disable(ResponseEntity<Object> activeUser) {
        if (activeUser.getStatusCode().is2xxSuccessful()) {
            Object responseBody = activeUser.getBody();

            if (responseBody instanceof Optional) {
                Optional<User> userOptional = (Optional<User>) responseBody;

                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    if (!user.isEnabled()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: User is already disabled.");
                    }

                    user.setEnabled(false); // Disabling
                    userRepo.save(user); // Saving

                    return ResponseEntity.ok("User successfully disabled.");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: User not found in the response body.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Response body is not an Optional<User>. Actual class: " + responseBody.getClass());
            }
        } else {
            return ResponseEntity.status(activeUser.getStatusCode()).body("Error: Response status is not successful. Status: " + activeUser.getStatusCode());
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

    public ResponseEntity<Object> update(User oldUser, User newUser) {
        //get oldUser id createdOn isEnabled role - not changeable
        //change username; email; - must be checked before changing, can be not changed
        //password; picture; - can be not changed
        //updatedOn - must be changed

        if (newUser.getUsername() == null) {newUser.setUsername(oldUser.getUsername());}
        if (newUser.getEmail() == null) {newUser.setEmail(oldUser.getEmail());}
        if (newUser.getPassword() == null) {newUser.setPassword(oldUser.getPassword());}
        if (newUser.getPicture() == null) {newUser.setPicture(oldUser.getPicture());}

        newUser.setId(oldUser.getId());
        newUser.setCreatedOn(oldUser.getCreatedOn());
        newUser.setEnabled(oldUser.isEnabled());
        newUser.setRoles(oldUser.getRoles());
        newUser.setUpdatedOn(oldUser.getUpdatedOn());

        if(!updateAvailability(newUser)){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Error: Username or Email already used by some other User");
        }if(oldUser.equals(newUser)){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Error: Nothing modified");
        }else{
            newUser.setUpdatedOn(LocalDateTime.now());
            userRepo.save(newUser);
            return ResponseEntity.ok("Successfully updated user:" + newUser.toString());
        }
    }
    private boolean updateAvailability(User newUser) {
        List<User> existingUsers = userRepo.findAll();
        for(int i = 0; i < existingUsers.size(); i++){
            if(existingUsers.get(i).getId()==newUser.getId()){
                existingUsers.remove(i);
                break;
            }
        }
        for(int i = 0; i < existingUsers.size(); i++){
            if(Objects.equals(existingUsers.get(i).getUsername(), newUser.getUsername()) ||
                    Objects.equals(existingUsers.get(i).getEmail(), newUser.getEmail()))
            {
                return false; //username or email is already used by some other user
            }
        }
        return true;
    }
}

