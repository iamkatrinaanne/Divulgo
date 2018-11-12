package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Katrina on 9/27/2018.
 */
public interface UserRepository  extends JpaRepository<User, Integer> {
    User findByUsernameAndPassword(String username,String password);
    User findByEmail(String email);

}
