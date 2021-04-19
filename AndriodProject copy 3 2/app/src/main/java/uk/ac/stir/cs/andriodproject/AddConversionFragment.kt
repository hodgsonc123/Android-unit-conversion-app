package uk.ac.stir.cs.andriodproject

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.net.wifi.WifiConfiguration.AuthAlgorithm.strings
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * class handling the fragment used to add conversions. taking in the user data and adding it to the database
 * **/
class AddConversionFragment : Fragment() {

    // init text field variables
    lateinit var conversionName : EditText
    lateinit var startUnitName: EditText
    lateinit var convertedUnitName : EditText
    lateinit var multiplierValue : EditText

    // init database uri
    val PROVIDER_NAME = "uk.ac.stir.cs.andriodproject.Conversion"
    val DATABASE_TABLE = "modules"
    val CONTENT_URI: Uri = Uri.parse("content://$PROVIDER_NAME/$DATABASE_TABLE")
    //init content resolver
    private lateinit var cr: ContentResolver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_conversion, container, false) // set up add conversion layout
    }

    /**
     * Adds a conversion record to the database
     * @param code The unique code/name for referencing the row GramsToKilograms
     * @param startUnit The unit converting from eg metres grams
     * @param convertedUnit The unit converting to metres grams
     * @param multiplier The multiplier to give the converted value eg 0.001
     * @param category The category of the converion eg weight, distance
     * **/
    private fun addConversion(code: String, startUnit: String, convertedUnit: String, multiplier: String, category: String) {
        Log.v("adding conversion", "code $code startUnit $startUnit convertedUnit $convertedUnit multiplier $multiplier category $category")
        val values = ContentValues()
        var uri = Uri.withAppendedPath(Provider.CONTENT_URI, code)

        // store row of data in content values variable
        values.put("code", code)
        values.put("startUnit", startUnit)
        values.put("convertedUnit", convertedUnit)
        values.put("multiplier", multiplier)
        values.put("category", category)

        val rows = cr.update(uri, values, "", null)
        if (rows == 0) {// insert row into the database
            cr.insert(uri, values) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // declare edit text variables to reference the corresponding ones in the layout
        conversionName = view.findViewById(R.id.editTextAddConversionName)
        startUnitName = view.findViewById(R.id.editTextAddConversionStartUnit)
        convertedUnitName = view.findViewById(R.id.editTextAddConversionConvertedUnit)
        multiplierValue = view.findViewById(R.id.editTextAddConversionMultiplier)


        val addButton = view.findViewById<View>(R.id.addButton) as Button // declare button variable linked to the add button in the layout

        //when add button is clicked
        addButton.setOnClickListener{
            // declare a string version of the values in the text fields
            var conversionNameString : String = conversionName.text.toString()
            var startUnitNameString : String = startUnitName.text.toString()
            var convertedUnitNameString : String = convertedUnitName.text.toString()
            var multiplierValueString : String = multiplierValue.text.toString()


            // if all data entries are valid (validated by the valid() method)
            if(valid(conversionNameString, startUnitNameString, convertedUnitNameString, multiplierValueString)){

                cr = context!!.contentResolver // set content resolver to the content resolver of context
                addConversion(conversionNameString, startUnitNameString, convertedUnitNameString, multiplierValueString, conversionNameString) // add conversion to database

                Toast.makeText(
                    view?.context,
                    "Added",
                    Toast.LENGTH_SHORT
                ).show() // taost that the conversion has been added successfully
                conversionName.setText("")//clear text fields after
                startUnitName.setText("")
                convertedUnitName.setText("")
                multiplierValue.setText("")
            }
        }

        // if clear buttion is clicked clear all text fields
        val clearButton = view.findViewById<View>(R.id.clearAllButton) as Button // declare button variable linked to the add button in the layout
        clearButton.setOnClickListener{
            try {
                conversionName.setText("")
                startUnitName.setText("")
                convertedUnitName.setText("")
                multiplierValue.setText("")
            }catch (exception: Exception){ // if already cleared then toast its cleared
                Toast.makeText(view.context, "Cleared already", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     *method used to validate the user input. Checking there are no empty values and that the too and from values are not the same
     * @param name Given name of the conversion
     * @param startUnit The from unit of the conversino
     * @param convertedUnit the to unit of the conversion
     * @param multiplier the multiplier value
     * @return valid A boolean declaring if the entry is valid of not
     * **/
    private fun valid(name: String, startUnit: String, convertedUnit: String, multiplier: String) : Boolean{

        var valid = true

        // a text fields cant be empty as all data is required for the app to work
        if(name == "" || startUnit == "" || convertedUnit == "" || multiplier == ""){ // if any fields are empty
            valid = false // set validation to false
            Toast.makeText(
                view?.context,
                "Fill all text fields.",
                Toast.LENGTH_SHORT
            ).show() // toast that they must enter all fields
        }

        // units cant be the same as conversions such as grams to grams are not allowed
        if(startUnit == convertedUnit){ // if units are the same
            valid = false
            Toast.makeText(
                view?.context,
                "Units cannot be the same",
                Toast.LENGTH_SHORT
            ).show() // toast error mesage
        }

        if(isPresent()){ //  check if the conversion already exists. cannot have two of the same named conversions
            valid = false
        }
        return valid //  return if valid or not
    }

    /**
     * Function carries out a query of the database to discover if the suggested conversion name is already in the database
     * @return found, Boolean stating if the name was found or not
     * **/
    private fun isPresent() : Boolean{

        var found = false

        val where = "code=?"
        val args = arrayOf(conversionName.text.toString())
        try {
            val uri = Uri.parse(CONTENT_URI.toString())
            val cursor = context!!.contentResolver.query(uri, null, where, args, null)
            if (cursor!!.count > 0) { // if the cursor count is one aka one row do..
                Toast.makeText(
                    view?.context,
                    "Already Present, Rename.",
                    Toast.LENGTH_SHORT
                ).show()
                found = true
                cursor.close()
            }
        } catch (exception: Exception) {
            // report problem in pop-up window
            Toast.makeText(view?.context, "Invalid data - " + exception.message, Toast.LENGTH_SHORT).show()
        }
        return found
    }
}