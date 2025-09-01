package com.fridger.backend

import fridger.backend.db.RefreshTokensTable
import fridger.backend.db.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseTestHelper {

    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(UsersTable, RefreshTokensTable)
        }
    }

    fun teardown() {
        transaction {
            SchemaUtils.drop(UsersTable, RefreshTokensTable)
        }
    }
}
