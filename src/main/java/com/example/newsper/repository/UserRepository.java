package com.example.newsper.repository;

import com.example.newsper.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    @Query(value = "SELECT * FROM userEntity WHERE email = :email", nativeQuery = true)
    UserEntity findByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM userEntity WHERE name = :name", nativeQuery = true)
    UserEntity findByName(@Param("name") String name);
}
