package com.flatmate.repository;

import com.flatmate.model.Household;
import com.flatmate.model.User;
import com.flatmate.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class HouseholdRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveHouseholdFromPostgres() {
        // Arrange
        Household household = new Household(0, "Test Household");

        // Act
        Household savedHousehold = householdRepository.save(household);
        Household retrievedHousehold = householdRepository
                .findById(savedHousehold.getId())
                .orElseThrow();

        // Assert
        assertThat(retrievedHousehold.getId()).isPositive();
        assertThat(retrievedHousehold.getName()).isEqualTo("Test Household");
    }

    @Test
    void     shouldRejectHouseholdWithNullName(){

        //Arrange
        Household household = new Household(0,null);

        // Act & Assert
        assertThatThrownBy(() -> householdRepository.saveAndFlush(household))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldSaveUserWithHousehold() {
        // Arrange
        Household household = householdRepository.save(
                new Household(0, "Test Household")
        );

        User user = new User(0, "Test User", "test@example.com");
        user.setHousehold(household);

        // Act
        User savedUser = userRepository.saveAndFlush(user);

        User retrievedUser = userRepository
                .findById(savedUser.getId())
                .orElseThrow();

        // Assert
        assertThat(retrievedUser.getHousehold()).isNotNull();
        assertThat(retrievedUser.getHousehold().getId())
                .isEqualTo(household.getId());
        assertThat(retrievedUser.getHousehold().getName())
                .isEqualTo("Test Household");
    }

    /**
     *
     */
    @Test
    void shouldRetrieveUsersFromHousehold() {
        // Arrange
        Household household = householdRepository.save(
                new Household(0, "Test Household")
        );

        User user = new User(0, "Test User", "test@example.com");
        user.setHousehold(household);
        userRepository.saveAndFlush(user);

        // Act
        householdRepository.flush();
        entityManager.clear();

        Household retrievedHousehold = householdRepository
                .findById(household.getId())
                .orElseThrow();

        // Assert
        assertThat(retrievedHousehold.getUsers()).hasSize(1);
        assertThat(retrievedHousehold.getUsers().get(0).getEmail())
                .isEqualTo("test@example.com");
    }
}