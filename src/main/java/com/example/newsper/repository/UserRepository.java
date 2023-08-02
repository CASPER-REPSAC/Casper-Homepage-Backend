package com.example.newsper.repository;

import com.example.newsper.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<UserEntity, String> {

}
