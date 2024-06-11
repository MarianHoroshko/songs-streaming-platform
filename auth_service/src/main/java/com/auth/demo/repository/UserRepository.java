package com.auth.demo.repository;

import com.auth.demo.entity.UserEntity;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.Row;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.data.cassandra.core.cql.IncorrectResultSetColumnCountException;
import org.springframework.data.cassandra.core.cql.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Slf4j
public class UserRepository {
    private final CqlTemplate cqlTemplate;

    @Autowired
    public UserRepository(CassandraOperations cassandraOperations) {
        cqlTemplate = (CqlTemplate) cassandraOperations.getCqlOperations();
    }

    public Optional<UserEntity> findByUsername(String username) {
        Optional<UserEntity> user;

        try {
            user = Optional.ofNullable(
                    cqlTemplate.queryForObject("SELECT * FROM users WHERE username = ? ALLOW FILTERING",
                            new RowMapper<UserEntity>() {
                                @Override
                                public UserEntity mapRow(Row row, int rowNum) throws DriverException {
                                    UserEntity userEntity = new UserEntity();

                                    userEntity.setUsername(row.getString("username"));
                                    userEntity.setEmail(row.getString("email"));
                                    userEntity.setPassword(row.getString("pass"));
                                    userEntity.setCreatedAt(row.getInstant("created_at"));
                                    userEntity.setUpdatedAt(row.getInstant("updated_at"));

                                    return userEntity;
                                }
                            },
                            username));
        } catch (IncorrectResultSetColumnCountException exception) {
            log.error("[UserRepository:findByUsername] Db returns 0 or more than 1 result.");

            return Optional.ofNullable(null);
        } catch (DataAccessException exception) {
            log.error("[UserRepository:findByUsername] Data access error: " + exception.getMessage());

            return Optional.ofNullable(null);
        }

        return user;
    }

    public Optional<UserEntity> save(UserEntity user) {
        boolean queryExecutionResponse = insert(user);

        if (!queryExecutionResponse) {
            return Optional.ofNullable(null);
        }

        return Optional.of(user);
    }

    public boolean insert(UserEntity user) {
        boolean queryExecutionResponse = false;

        try {
            queryExecutionResponse = cqlTemplate.execute(
                    "INSERT INTO users (username, email, pass, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getCreatedAt(),
                    user.getUpdatedAt());
        } catch (DataAccessException exception) {
            log.error("[UserRepository:insert] Data access error: " + exception.getMessage());

            throw new RuntimeException(exception.getMessage());
        }

        return queryExecutionResponse;
    }
}
