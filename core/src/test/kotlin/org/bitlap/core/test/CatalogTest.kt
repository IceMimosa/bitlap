package org.bitlap.core.test

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bitlap.common.exception.BitlapException
import org.bitlap.core.BitlapContext
import org.bitlap.core.test.base.BaseLocalFsTest

/**
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2020/12/25
 */
class CatalogTest : BaseLocalFsTest() {
    init {

        "test database" {
            val testDatabase = "test_database"
            val catalog = BitlapContext.catalog
            catalog.createDatabase(testDatabase)
            catalog.dropDatabase(testDatabase)
            catalog.createDatabase(testDatabase, true)
            shouldThrow<BitlapException> { catalog.createDatabase(testDatabase) }
            catalog.renameDatabase(testDatabase, "test_database_to")
            catalog.renameDatabase("test_database_to", testDatabase)
            catalog.getDatabase(testDatabase).name shouldBe testDatabase
        }

        "test table create" {
            val testName = "test_table"
            val catalog = BitlapContext.catalog
            catalog.createTable(testName)
            catalog.createTable(testName, ifNotExists = true)
            // get table
            shouldThrow<BitlapException> { catalog.getTable("xxx") }
            val getTable = catalog.getTable(testName)
            getTable.name shouldBe testName
            getTable.createTime shouldNotBe null
        }
    }
}
