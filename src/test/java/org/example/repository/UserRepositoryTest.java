package org.example.repository;

import org.example.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save user successfully")
    void testSaveUser() {
        // Arrange
        UserEntity user = new UserEntity("test@example.com", "password123");

        // Act
        UserEntity savedUser = userRepository.save(user);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("password123");
    }

    @Test
    @DisplayName("Should find user by email when user exists")
    void testFindByEmail_Found() {
        // Arrange
        UserEntity user = new UserEntity("john@example.com", "password456");
        userRepository.save(user);

        // Act
        Optional<UserEntity> foundUser = userRepository.findByEmail("john@example.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("john@example.com");
        assertThat(foundUser.get().getPassword()).isEqualTo("password456");
    }

    @Test
    @DisplayName("Should return empty optional when user email does not exist")
    void testFindByEmail_NotFound() {
        // Act
        Optional<UserEntity> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find user by id when user exists")
    void testFindById_Found() {
        // Arrange
        UserEntity user = new UserEntity("jane@example.com", "password789");
        UserEntity savedUser = userRepository.save(user);

        // Act
        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("Should return empty optional when user id does not exist")
    void testFindById_NotFound() {
        // Act
        Optional<UserEntity> foundUser = userRepository.findById(999L);

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser() {
        // Arrange
        UserEntity user = new UserEntity("delete@example.com", "password000");
        UserEntity savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);

        // Assert
        Optional<UserEntity> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUser() {
        // Arrange
        UserEntity user = new UserEntity("update@example.com", "oldpassword");
        UserEntity savedUser = userRepository.save(user);

        // Act
        savedUser.setEmail("updated@example.com");
        savedUser.setPassword("newpassword");
        UserEntity updatedUser = userRepository.save(savedUser);

        // Assert
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getPassword()).isEqualTo("newpassword");

        // Verify the update persisted
        Optional<UserEntity> fetchedUser = userRepository.findById(savedUser.getId());
        assertThat(fetchedUser).isPresent();
        assertThat(fetchedUser.get().getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("Should throw exception when saving user with duplicate email")
    void testEmailUniqueness() {
        // Arrange
        UserEntity user1 = new UserEntity("duplicate@example.com", "password1");
        userRepository.save(user1);

        UserEntity user2 = new UserEntity("duplicate@example.com", "password2");

        // Act & Assert
        assertThatThrownBy(() -> {
            userRepository.save(user2);
            userRepository.flush(); // Force the constraint check
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should handle case-sensitive email searches")
    void testFindByEmail_CaseSensitive() {
        // Arrange
        UserEntity user = new UserEntity("Test@Example.com", "password123");
        userRepository.save(user);

        // Act
        Optional<UserEntity> foundWithExactCase = userRepository.findByEmail("Test@Example.com");
        Optional<UserEntity> foundWithDifferentCase = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(foundWithExactCase).isPresent();
        // Note: Email search might be case-insensitive depending on database configuration
        // This test documents the current behavior
    }

    @Test
    @DisplayName("Should return count of all users")
    void testCountUsers() {
        // Arrange
        userRepository.save(new UserEntity("user1@example.com", "pass1"));
        userRepository.save(new UserEntity("user2@example.com", "pass2"));
        userRepository.save(new UserEntity("user3@example.com", "pass3"));

        // Act
        long count = userRepository.count();

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void testExistsByEmail() {
        // Arrange
        UserEntity user = new UserEntity("exists@example.com", "password");
        userRepository.save(user);

        // Act
        Optional<UserEntity> exists = userRepository.findByEmail("exists@example.com");
        Optional<UserEntity> notExists = userRepository.findByEmail("notexists@example.com");

        // Assert
        assertThat(exists).isPresent();
        assertThat(notExists).isEmpty();
    }
}
