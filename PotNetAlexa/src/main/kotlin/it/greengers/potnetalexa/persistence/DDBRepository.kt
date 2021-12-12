package it.greengers.potnetalexa.persistence

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
import it.lm96.qbrtools.persistence.PersistenceException
import it.lm96.qbrtools.persistence.PotNetEntry
import it.lm96.qbrtools.persistence.PotNetEntryRepository
import java.util.*
import kotlin.jvm.Throws

class DDBRepository : PotNetEntryRepository {

    private val client : AmazonDynamoDB
    private val ddb : DynamoDB
    private val table : Table
    private val tableInfo = TableInfo.TABLE_INFO

    init {
        val credentials = BasicAWSCredentials("", "")
        client = AmazonDynamoDBClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.US_EAST_1)
            .build()
        ddb = DynamoDB(client)
        table = ddb.getTable(tableInfo.table)
    }

    @Throws(PersistenceException::class)
    override fun create(entry: PotNetEntry) {
        try {
            table.putItem(
                Item()
                    .withPrimaryKey(tableInfo.userMailColName, entry.USER_MAIL)
                    .withString(tableInfo.potIdColName, entry.POT_ID)
            )
        } catch (e : Exception) {
            throw PersistenceException(e)
        }
    }

    @Throws(PersistenceException::class)
    override fun retrieve(userMail: String): Optional<PotNetEntry> {
        return try {
            val getItemSpec = GetItemSpec().withPrimaryKey(tableInfo.userMailColName, userMail)
            val item = table.getItem(getItemSpec)

            if(item == null)
                Optional.empty()
            else Optional.of(toPotNetEntry(item))

        } catch (e : Exception) {
            throw PersistenceException(e)
        }
    }

    @Throws(PersistenceException::class)
    override fun update(entry: PotNetEntry) {
        try {
            val updateItemSpec = UpdateItemSpec()
                .withPrimaryKey(tableInfo.userMailColName, entry.USER_MAIL)
                .withUpdateExpression("set ${tableInfo.potIdColName} = :a")
                .withValueMap(ValueMap().withString(":a", entry.POT_ID))
                .withReturnValues(ReturnValue.UPDATED_NEW)

            table.updateItem(updateItemSpec)
        } catch (e : Exception) {
            throw PersistenceException(e)
        }
    }

    @Throws(PersistenceException::class)
    override fun delete(userMail: String): Optional<PotNetEntry> {
        try {
            val deleteItemSpec = DeleteItemSpec()
                .withPrimaryKey(tableInfo.userMailColName, userMail)
                .withReturnValues(ReturnValue.ALL_OLD)

            val item = table.deleteItem(deleteItemSpec).item
            return if(item == null)
                Optional.empty()
            else Optional.of(toPotNetEntry(item))
        } catch (e : Exception) {
            throw PersistenceException(e)
        }
    }

    @Throws(PersistenceException::class)
    override fun createOrUpdate(entry: PotNetEntry) {
        val scanSpec = ScanSpec()
            .withFilterExpression("${tableInfo.userMailColName} = :m")
            .withValueMap(ValueMap().withString(":m", entry.USER_MAIL))

        when(table.scan(scanSpec).accumulatedItemCount) {
            0 -> create(entry)
            1 -> update(entry)
            else -> throw PersistenceException("Found more than one items (impossible)")
        }
    }

    override fun getAll(): List<PotNetEntry> {
        return table.scan().map { toPotNetEntry(it) }.toList()
    }

    private fun toPotNetEntry(item : Item) : PotNetEntry {
        return PotNetEntry(
            item.getString(tableInfo.userMailColName),
            item.getString(tableInfo.potIdColName)
        )
    }
}