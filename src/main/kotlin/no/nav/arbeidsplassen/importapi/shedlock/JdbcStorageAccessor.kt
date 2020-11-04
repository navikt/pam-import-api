/**
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.nav.arbeidsplassen.importapi.shedlock

import net.javacrumbs.shedlock.core.ClockProvider
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.support.AbstractStorageAccessor
import net.javacrumbs.shedlock.support.LockException
import no.nav.arbeidsplassen.importapi.Open
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.Timestamp
import javax.inject.Singleton
import javax.sql.DataSource
import javax.transaction.Transactional

@Singleton
@Open
class JdbcStorageAccessor(private val dataSource: DataSource) : AbstractStorageAccessor() {

    private val tableName = "shedlock"

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    override fun insertRecord( lockConfiguration: LockConfiguration): Boolean {
        // Try to insert if the record does not exists (not optimal, but the simplest platform agnostic way)
        val sql = "INSERT INTO $tableName(name, lock_until, locked_at, locked_by) VALUES(?, ?, ?, ?)"
        try {
            dataSource.connection.use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, lockConfiguration.name)
                    statement.setTimestamp(2, Timestamp.from(lockConfiguration.lockAtMostUntil))
                    statement.setTimestamp(3, Timestamp.from(ClockProvider.now()))
                    statement.setString(4, hostname)
                    val insertedRows = statement.executeUpdate()
                    if (insertedRows > 0) {
                        return true
                    }
                }
            }
        } catch (e: SQLException) {
            handleInsertionException(sql, e)
        }
        return false
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    override fun updateRecord( lockConfiguration: LockConfiguration): Boolean {
        val sql = "UPDATE $tableName SET lock_until = ?, locked_at = ?, locked_by = ? WHERE name = ? AND lock_until <= ?"
        try {
            dataSource.connection.use { connection ->
                connection.prepareStatement(sql).use { statement ->
                     val now = Timestamp.from(ClockProvider.now())
                    statement.setTimestamp(1, Timestamp.from(lockConfiguration.lockAtMostUntil))
                    statement.setTimestamp(2, now)
                    statement.setString(3, hostname)
                    statement.setString(4, lockConfiguration.name)
                    statement.setTimestamp(5, now)
                    val updatedRows = statement.executeUpdate()
                    return updatedRows > 0
                }
            }
        } catch (e: SQLException) {
            handleUpdateException(sql, e)
            return false
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    override fun extend( lockConfiguration: LockConfiguration): Boolean {
        val sql = "UPDATE $tableName SET lock_until = ? WHERE name = ? AND locked_by = ? AND lock_until > ? "
        logger.debug("Extending lock={} until={}", lockConfiguration.name, lockConfiguration.lockAtMostUntil)
        try {
            dataSource.connection.use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setTimestamp(1, Timestamp.from(lockConfiguration.lockAtMostUntil))
                    statement.setString(2, lockConfiguration.name)
                    statement.setString(3, hostname)
                    statement.setTimestamp(4, Timestamp.from(ClockProvider.now()))
                    return statement.executeUpdate() > 0
                }
            }
        } catch (e: SQLException) {
            handleUnlockException(sql, e)
            return false
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    override fun unlock( lockConfiguration: LockConfiguration) {
        val sql = "UPDATE $tableName SET lock_until = ? WHERE name = ?"
        try {
            dataSource.connection.use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setTimestamp(1, Timestamp.from(lockConfiguration.unlockTime))
                    statement.setString(2, lockConfiguration.name)
                    statement.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            handleUnlockException(sql, e)
        }
    }

    fun handleInsertionException(sql: String?, e: SQLException?) {
        if (e is SQLIntegrityConstraintViolationException) {
            // lock record already exists
        } else {
            // can not throw exception here, some drivers (Postgres) do not throw SQLIntegrityConstraintViolationException on duplicate key
            // we will try update in the next step, su if there is another problem, an exception will be thrown there
            logger.debug("Exception thrown when inserting record", e)
        }
    }

    fun handleUpdateException(sql: String?, e: SQLException?) {
        throw LockException("Unexpected exception when locking", e)
    }

    fun handleUnlockException(sql: String?, e: SQLException?) {
        throw LockException("Unexpected exception when unlocking", e)
    }

}
