package com.auth.demo.repository;

import com.auth.demo.entity.RefreshTokenEntity;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.data.cassandra.core.cql.IncorrectResultSetColumnCountException;
import org.springframework.data.cassandra.core.cql.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class RefreshTokenRepository {
    private final CqlTemplate cqlTemplate;

    @Autowired
    public RefreshTokenRepository(CassandraOperations cassandraOperations) {
        cqlTemplate = (CqlTemplate) cassandraOperations.getCqlOperations();
    }

    public Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken) {
        Optional<RefreshTokenEntity> refreshTokenEntity;

        try {
            refreshTokenEntity = Optional.ofNullable(cqlTemplate.queryForObject(
                    "SELECT * FROM refresh_tokens WHERE refresh_token = ?",
                    new RowMapper<RefreshTokenEntity>() {
                        @Override
                        public RefreshTokenEntity mapRow(Row row, int rowNum) throws DriverException {
                            RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();

                            refreshTokenEntity.setUsername(row.getString("username"));
                            refreshTokenEntity.setRefreshToken(row.getString("refresh_token"));
                            refreshTokenEntity.setRevoked(row.getBoolean("revoked"));
                            refreshTokenEntity.setCreatedAt(row.getInstant("created_at"));
                            refreshTokenEntity.setUpdatedAt(row.getInstant("updated_at"));

                            return refreshTokenEntity;
                        }
                    },
                    refreshToken));
        } catch (IncorrectResultSetColumnCountException exception) {
            log.error("[RefreshTokenRepository:findByRefreshToken] Db returns 0 or more than 1 result.");

            throw new RuntimeException(exception.getMessage());
        } catch (DataAccessException exception) {
            log.error("[RefreshTokenRepository:findByRefreshToken] Data access error.");

            throw new RuntimeException(exception.getMessage());
        }

        return refreshTokenEntity;
    }

    public List<RefreshTokenEntity> findAllRefreshTokenByUsername(String username) {
        List<RefreshTokenEntity> refreshTokenEntities = new ArrayList<>();
        try {
            refreshTokenEntities = cqlTemplate.query(
                    "SELECT * FROM refresh_tokens WHERE refresh_token = ?",
                    new RowMapper<RefreshTokenEntity>() {
                        @Override
                        public RefreshTokenEntity mapRow(Row row, int rowNum) throws DriverException {
                            RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();

                            refreshTokenEntity.setUsername(row.getString("username"));
                            refreshTokenEntity.setRefreshToken(row.getString("refresh_token"));
                            refreshTokenEntity.setRevoked(row.getBoolean("revoked"));
                            refreshTokenEntity.setCreatedAt(row.getInstant("created_at"));
                            refreshTokenEntity.setUpdatedAt(row.getInstant("updated_at"));

                            return refreshTokenEntity;
                        }
                    },
                    username);
        } catch (DataAccessException exception) {
            log.error("[RefreshTokenRepository:findAllRefreshTokenByUsername] Data access error.");

            throw new RuntimeException(exception.getMessage());
        }

        return refreshTokenEntities;
    }

    public Optional<RefreshTokenEntity> save(RefreshTokenEntity refreshTokenEntity) {
        boolean queryExecutionResponse = insert(refreshTokenEntity);

        if (!queryExecutionResponse) {
            return Optional.ofNullable(null);
        }

        return Optional.of(refreshTokenEntity);
    }

    public boolean insert(RefreshTokenEntity refreshTokenEntity) {
        boolean isRefreshTokenEntity = false;

        try {
            isRefreshTokenEntity = cqlTemplate.execute(
                    "INSERT INTO refresh_tokens_by_user (username, refresh_token, revoked, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    refreshTokenEntity.getUsername(),
                    refreshTokenEntity.getRefreshToken(),
                    refreshTokenEntity.isRevoked(),
                    refreshTokenEntity.getCreatedAt(),
                    refreshTokenEntity.getUpdatedAt());
        } catch (DataAccessException exception) {
            log.error("[RefreshTokenRepository:insert] Data access error.");

            throw new RuntimeException(exception.getMessage());
        }

        return isRefreshTokenEntity;
    }

    public RefreshTokenEntity update(RefreshTokenEntity refreshToken) {
        RefreshTokenEntity refreshTokenEntity = cqlTemplate.queryForObject(
                "UPDATE refresh_tokens_by_user revoked = true WHERE refresh_token = ?",
                new RowMapper<RefreshTokenEntity>() {
                    @Override
                    public RefreshTokenEntity mapRow(Row row, int rowNum) throws DriverException {
                        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();

                        refreshTokenEntity.setUsername(row.getString("username"));
                        refreshTokenEntity.setRefreshToken(row.getString("refresh_token"));
                        refreshTokenEntity.setRevoked(row.getBoolean("revoked"));
                        refreshTokenEntity.setCreatedAt(row.getInstant("created_at"));
                        refreshTokenEntity.setUpdatedAt(row.getInstant("updated_at"));

                        return refreshTokenEntity;
                    }
                }, refreshToken.getRefreshToken());

        return refreshTokenEntity;
    }
}
