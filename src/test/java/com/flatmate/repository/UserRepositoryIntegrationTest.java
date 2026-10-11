package com.flatmate.repository;

import com.flatmate.model.Household;
import com.flatmate.model.User;
import com.flatmate.repository.HouseholdRepository;
import com.flatmate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("flatmate_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Test
    void shouldSaveAndRetrieveUserFromPostgres() {
        Household household = new Household();
        household.setName("Integration Test Household");
        household = householdRepository.saveAndFlush(household);

        User user = new User();
        user.setName("Integration Test User");
        user.setEmail("integration-test@example.com");
        user.setHousehold(household);

        User savedUser = userRepository.saveAndFlush(user);
        User retrievedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(retrievedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(retrievedUser.getName()).isEqualTo("Integration Test User");
        assertThat(retrievedUser.getEmail()).isEqualTo("integration-test@example.com");
        assertThat(retrievedUser.getHousehold().getId()).isEqualTo(household.getId());
    }

    @Test
    void shouldFindUserByEmail() {
        Household household = new Household();
        household.setName("Email Test Household");
        household = householdRepository.saveAndFlush(household);

        User user = new User();
        user.setName("Email Test User");
        user.setEmail("email-test@example.com");
        user.setHousehold(household);

        userRepository.saveAndFlush(user);

        Optional<User> foundUser = userRepository.findByEmail("email-test@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Email Test User");
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<User> foundUser =
                userRepository.findByEmail("nonexistent@example.com");

        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        Household household = new Household();
        household.setName("Exists Test Household");
        household = householdRepository.saveAndFlush(household);

        User user = new User();
        user.setName("Exists Test User");
        user.setEmail("exists-test@example.com");
        user.setHousehold(household);

        userRepository.saveAndFlush(user);

        boolean exists = userRepository.existsByEmail("exists-test@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("missing@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void shouldAllowMultipleUsersInSameHousehold() {
        Household household = new Household();
        household.setName("Shared Household");
        household = householdRepository.saveAndFlush(household);

        User firstUser = new User();
        firstUser.setName("Alice");
        firstUser.setEmail("alice@example.com");
        firstUser.setHousehold(household);
        userRepository.saveAndFlush(firstUser);

        User secondUser = new User();
        secondUser.setName("Bob");
        secondUser.setEmail("bob@example.com");
        secondUser.setHousehold(household);
        userRepository.saveAndFlush(secondUser);

        User retrievedAlice = userRepository.findByEmail("alice@example.com")
                .orElseThrow();
        User retrievedBob = userRepository.findByEmail("bob@example.com")
                .orElseThrow();

        assertThat(retrievedAlice.getHousehold().getId())
                .isEqualTo(retrievedBob.getHousehold().getId());
    }

    @Test
    void shouldRejectDuplicateEmailAddresses() {
        Household household = new Household();
        household.setName("Duplicate Email Test Household");
        household = householdRepository.saveAndFlush(household);

        User firstUser = new User();
        firstUser.setName("First User");
        firstUser.setEmail("duplicate@example.com");
        firstUser.setHousehold(household);

        userRepository.saveAndFlush(firstUser);

        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail("duplicate@example.com");
        secondUser.setHousehold(household);

        assertThatThrownBy(() -> userRepository.saveAndFlush(secondUser))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
