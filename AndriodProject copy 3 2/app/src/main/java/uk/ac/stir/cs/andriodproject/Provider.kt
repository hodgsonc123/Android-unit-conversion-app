package uk.ac.stir.cs.andriodproject

import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
/**
 * provider class that manages the access to the database.
 * **/
class Provider : ContentProvider() {
    private lateinit var contentResolver: ContentResolver  // init content resolver
    private lateinit var conversionsDatabase: SQLiteDatabase // init sqlite database
    private lateinit var uriMatcher: UriMatcher  //content provider name

    /**
     * function used to delete a table row
     *  @param uri the uri of the database(file location)
     *  @param selection which rows
     *  @param arguments where the values are equal to....
     * **/
    override fun delete(uri: Uri, selection: String?, arguments: Array<String>?): Int {
        var count = 0
        when (uriMatcher.match(uri)) {
            ALL_CONVERSIONS -> count = conversionsDatabase.delete(DATABASE_TABLE, selection, arguments) // delete whole table
            ONE_CONVERSION -> { // delete a row where the code matches
                var code = uri.pathSegments[1]
                code = "code = '$code'"
                if (!TextUtils.isEmpty(selection)) code += " AND ($selection)"
                count = conversionsDatabase.delete(DATABASE_TABLE, code, arguments)
            }
            else -> throw IllegalArgumentException("Unknown URI '$uri'")
        }
        contentResolver.notifyChange(uri, null) // notify the content resolver of the change
        return count
    }

    /**
     * get the type
     *  @param uri file location
     * **/
    override fun getType(uri: Uri): String? {
        val mimeType: String
        mimeType = when (uriMatcher.match(uri)) {
            ALL_CONVERSIONS -> "vnd.uk.ac.stir.cs.cursor.dir/modules"
            ONE_CONVERSION -> "vnd.uk.ac.stir.cs.cursor.item/modules"
            else -> throw IllegalArgumentException("Invalid URI '$uri'")
        }
        return mimeType
    }

    /**
     * function to insert a row into the database
     * @param uri database file location
     * @param values the content values storing a row of data
     * **/
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val newUri: Uri
        val rowIdentifier = conversionsDatabase.insert(DATABASE_TABLE, "", values)
        if (rowIdentifier > 0) {
            newUri = ContentUris.withAppendedId(CONTENT_URI, rowIdentifier)
            contentResolver.notifyChange(newUri, null)
        } else throw SQLException("Failed to insert row into '$uri'")
        return newUri
    }

    /**
     * creating the writeable database
     * **/
    override fun onCreate(): Boolean {
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        uriMatcher.addURI(PROVIDER_NAME, "modules", ALL_CONVERSIONS)
        uriMatcher.addURI(PROVIDER_NAME, "modules/*", ONE_CONVERSION)
        val context = context
        contentResolver = context!!.contentResolver
        val databaseHelper = DatabaseHelper(context)
        conversionsDatabase = databaseHelper.writableDatabase
        return conversionsDatabase != null
    }

    /**
     * method used to query the database to retrieve information that is required
     * @param uri file location
     * @param projection list of columns to return
     * @param selection which rows to return
     * @param arguments the values you want to check for
     * @param sortOrder how to order the rows
     * @return cursor storing the query results
     * **/
    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       arguments: Array<String>?, sortOrder: String?): Cursor? {
        var so = sortOrder
        val sqlBuilder = SQLiteQueryBuilder()
        sqlBuilder.tables = DATABASE_TABLE
        if (uriMatcher.match(uri) == ONE_CONVERSION) {
            val code = uri.pathSegments[1]
            sqlBuilder.appendWhere("code = '$code'")
        }

        if (so == null || so === "") {
            so = "code"
        }
        val cursor = sqlBuilder.query(conversionsDatabase, projection, selection,
            arguments, null, null, sortOrder)
        cursor.setNotificationUri(contentResolver, uri)
        return cursor
    }

    /**
     * function to update the database with given values at given location
     * **/
    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        arguments: Array<String>?): Int {
        var count = 0
        when (uriMatcher.match(uri)) {
            ALL_CONVERSIONS -> count = conversionsDatabase.update(DATABASE_TABLE, values, selection,
                arguments)
            ONE_CONVERSION -> {
                var code = uri.pathSegments[1]
                code = "code = '$code'"
                if (!TextUtils.isEmpty(selection)) code += " AND ($selection)"
                count = conversionsDatabase.update(DATABASE_TABLE, values, code,
                    arguments)
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        contentResolver.notifyChange(uri, null)
        return count
    }

    /**
     * companion object declaring some important information
     * **/
    companion object {
        const val DATABASE_TABLE = "modules" // modules as this code was adapted from the practical. I changed every single instance in the code that uses modules to conversions but i got an error saying the table didnt exist?
        const val PROVIDER_NAME = "uk.ac.stir.cs.andriodproject.Conversion"
        /** Content provider URI  */
        @JvmField
        val CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/" + DATABASE_TABLE)

        /** URI code for all conversions  */
        private const val ALL_CONVERSIONS = 1

        /** URI code for one conversion  */
        private const val ONE_CONVERSION = 2
    }
}

/**
 * internal class for the database helper used to define the structure of the database
 * **/
internal class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     * on create execute the sql to create the table in the given database
     * **/
    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS " + Provider.DATABASE_TABLE + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "code TEXT NOT NULL," +
                    "startUnit TEXT NOT NULL," +
                    "convertedUnit TEXT NOT NULL," +
                    "multiplier TEXT NOT NULL," +
                    "category TEXT NOT NULL" +
                    ");"
        )
    }

    /**
     *on upgrade drop the old table and create the new one
     * **/
    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int,
                           newVersion: Int) {
        database.execSQL("DROP TABLE IF EXISTS " + Provider.DATABASE_TABLE)
        onCreate(database)
    }

    /**
     * companion object storing the database name and version
     * **/
    companion object {
        val DATABASE_NAME = "conversionsDatabase"
        val DATABASE_VERSION = 1
    }
}
