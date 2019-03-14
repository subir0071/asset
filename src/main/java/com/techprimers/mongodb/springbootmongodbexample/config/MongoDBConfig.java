package com.techprimers.mongodb.springbootmongodbexample.config;

import com.techprimers.mongodb.springbootmongodbexample.document.Users;
import com.techprimers.mongodb.springbootmongodbexample.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackageClasses = UserRepository.class)
@Configuration
public class MongoDBConfig {


    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository) {
        return strings -> {
            //userRepository.save(new Users(1, "Peter", "Development", 3000L));
            //userRepository.save(new Users(2, "Sam", "Operations", 2000L));
            userRepository.save(new Users(3, "Sourabh", "DevOps", 2000L, "Male"));
            userRepository.save(new Users(4, "Lohit", "DevOps", 2000L, "Male"));
            //userRepository.save(new Users(5, "Subir", "DevOps", 2000L, "Male"));
        };
    }

}
