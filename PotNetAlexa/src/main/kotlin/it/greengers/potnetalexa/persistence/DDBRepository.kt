package it.lm96.qbrtools.persistence

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.model.ReturnValue
import java.util.*
import kotlin.jvm.Throws

class DDBRepository : QBRRepository {

    private val client : AmazonDynamoDB
    private val ddb : DynamoDB
    private val table : Table
    private val tableInfo = TableInfo.TABLE_INFO

    init {
        val credentials = BasicAWSCredentials("AKIAYW2UX5MGVAFX4N56", "d2pADozXcuPHMvzZ6jiCXBmRbR0CqyJCUMutpwGH")
        client = AmazonDynamoDBClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.US_EAST_1)
            .build()
        ddb = DynamoDB(client)
        table = ddb.getTable(tableInfo.table)
    }

    @Throws(PersistenceException::class)
    override fun create(entry: QBREntry) {
        try {
            table.putItem(
                Item()
                    .withPrimaryKey(tableInfo.userMailColName, entry.USER_MAIL)
                    .withString(tableInfo.brAddressColName, entry.BASICROBOT_ADDRESS)
                    .withInt(tableInfo.brPortColName, entry.BASICROBOT_PORT)
            )
        } catch (e : Exception) {
            throw PersistenceException(e)
        }
    }

    @Throws(PersistenceException::class)
    override fun retrieve(userMail: String): Optional<QBREntry> {
        return try {
            val getItemSpec = GetItemSpec().withPrimaryKey(tableInfo.userMailColName, userMail)
            val item = table.getItem(getItemSpec)

            if(item == null)
                Optional.empty()
            else Optional.of(toQBREntry(item))

        } catch (e : Exception) {
            throw PersistenceException(e)
        }
    }

    @Throws(PersistenceException::class)
    override fun update(entry: QBREntry) {
        try {
            val updateItemSpec = UpdateItemSpec()
                .withPrimaryKey(tableInfo.userMailColName, entry.USER_MAIL)
                .withUpdateExpression("set ${tableInfo.brAddressColName} = :a, ${tableInfo.brPortColName} = :p")
                .withValueMap(ValueMap().withString(":a", entry.BASICROBOT_ADDRESS).withInt(":p", entry.BASICROBOT_PORT))
                .withReturnValues(ReturnValue.UPDATED_NEW)

            table.updateItem(updateItemSpec)
        } catch (e : Exception) {
            throw PersistenceException(e)
        }
    }

    @Throws(PersistenceException::class)
    override fun delete(userMail: String): Optional<QBREntry> {
        try {
            val deleteItemSpec = DeleteItemSpec()
                .withPrimaryKey(tableInfo.userMailColName, userMail)
                .withReturnValues(ReturnValue.ALL_OLD)

            val item = table.deleteItem(deleteItemSpec).item
            return if(item == null)
                Optional.empty()
            else Optional.of(toQBREntry(item))
        } catch (e : Exception) {
            throw PersistenceException(e)
        }
    }

    @Throws(PersistenceException::class)
    override fun createOrUpdate(entry: QBREntry) {
        val scanSpec = ScanSpec()
            .withFilterExpression("${tableInfo.userMailColName} = :m")
            .withValueMap(ValueMap().withString(":m", entry.USER_MAIL))

        when(table.scan(scanSpec).accumulatedItemCount) {
            0 -> create(entry)
            1 -> update(entry)
            else -> throw PersistenceException("Found more than one items (impossible)")
        }
    }

    override fun getAll(): List<QBREntry> {
        return table.scan().map { toQBREntry(it) }.toList()
    }

    private fun toQBREntry(item : Item) : QBREntry {
        return QBREntry(
            item.getString(tableInfo.userMailColName),
            item.getString(tableInfo.brAddressColName),
            item.getInt(tableInfo.brPortColName)
        )
    }
}