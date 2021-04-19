package uk.ac.stir.cs.andriodproject

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Fragment containing the selection of category and units
 * **/
class SelectionFragment : Fragment() {

    // set up database uri
    val PROVIDER_NAME = "uk.ac.stir.cs.andriodproject.Conversion"
    val DATABASE_TABLE = "modules"
    val CONTENT_URI: Uri = Uri.parse("content://$PROVIDER_NAME/$DATABASE_TABLE")

    // setup spinner data classes for event buses
    data class unitCategorySpinner(val selectedItem: String)
    data class startUnitSpinner(val selectedItem: String)
    data class convertedUnitSpinner(val selectedItem: String)

    //initialise variables to store data on spinner positions and values
    var startSpinnerVal = ""
    var convertedSpinnerVal = ""

    // start category will always be weight so it is initialised for filling the units spinners
    var cat : String = "Weight"

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)// on fragment creation load saved instance state.
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.selection_fragment, container, false) //set up selection fragment layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //  load saved state

        // set up spinner variable to reference the corresponding spinner in the layout using its id
        val unitCategorySpinner : Spinner = view.findViewById(R.id.spinnerCategory)
        val startUnitSpinner : Spinner = view.findViewById(R.id.spinnerStartUnit)
        val convertedUnitSpinner : Spinner = view.findViewById(R.id.spinnerConvertedUnit)

        //listener to handle category spinner item selection
        unitCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                val value : String = parent.getItemAtPosition(position).toString() // get value at current spinner position
                cat = value // store value in global variable
                populateUnitSpinner(startUnitSpinner, convertedUnitSpinner) // adjust the unit selection spinners as when category change, so does unit selection
                EventBus.getDefault().post(unitCategorySpinner(value)) // post selected category to data class using event bus
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // listener to handle start unit spinner item selection
        startUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                startSpinnerVal = parent.getItemAtPosition(position).toString() // store spinner value

                // if value selected is the same as the converted unit spinner then change the value
                // of this spinner to the value next to it checking that it is not out of bounds
                if(startSpinnerVal == convertedSpinnerVal && position <=1){
                    convertedUnitSpinner.setSelection(position + 1)
                }
                else if (startSpinnerVal == convertedSpinnerVal && position >1){
                    convertedUnitSpinner.setSelection(position - 1)
                }

                val value : String = parent.getItemAtPosition(position).toString()
                EventBus.getDefault().post(startUnitSpinner(value)) // post selected unit to event bus for unit labels in conversion fragment
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // listener to handle converted unit selection. Same as above but for converted unit spinner
        convertedUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                convertedSpinnerVal = parent.getItemAtPosition(position).toString()

                if(startSpinnerVal == convertedSpinnerVal && position <=1){
                    startUnitSpinner.setSelection(position + 1)
                }
                else if (startSpinnerVal == convertedSpinnerVal && position >1){
                    startUnitSpinner.setSelection(position - 1)
                }
                val value : String = parent.getItemAtPosition(position).toString()
                EventBus.getDefault().post(convertedUnitSpinner(value))

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        populateSpinner(unitCategorySpinner)// populate category spinner
        populateUnitSpinner(startUnitSpinner, convertedUnitSpinner) // populate unit spinners
    }

    /**
     * method to populate the category spinner. retreiving the category values from the database
     * @param spinner The category spinner
     *
     * **/
    private fun populateSpinner(spinner: Spinner) {
        try { // try to retrieve category data
            val uri = Uri.parse(CONTENT_URI.toString()) // declare uri of database table
            val cursor = context!!.contentResolver.query(uri, null, null, null, null) // declare cursor to store the result of the database query

            val result = ArrayList<String>() // array to store results/categories
            while(cursor?.moveToNext()!!) { // while cursor has another value
                val item = cursor.getString(cursor.getColumnIndex("category")) // get values in category column
                if(!result.contains(item)) // If the category is not in the list(DISTINCT didn't seem to work)
                    result.add(item) // add the item to the results array
            }
            cursor.close()
            // array adapter storing the results for input into the category spinner
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, result)
            spinner.adapter = adapter

        } catch (exception: Exception) { // if the above cause exception then toast error message
            // report problem in pop-up window
            Toast.makeText(view?.context, "Invalid data - " + exception.message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * method to populate the units spinners. retrieving the unit values from the database
     * @param startSpinner THE start unit spinner
     * @param categorySpinner THE converted unit spinner
     *
     * **/
    private fun populateUnitSpinner(startSpinner: Spinner, convertedSpinner: Spinner) {
        // where category equals selected category (selection and arguments for query)
        val where = "category=?"
        val args = arrayOf(cat)

        try {//try the following
            val uri = Uri.parse(CONTENT_URI.toString()) // database uri
            val cursor = context!!.contentResolver.query(uri, null, where, args, null) // cursor storing query results with selection and args

            // following section finds units where the start and converted units are of the selected category and adds them to the spinners
            // as above for category
            val startResult = ArrayList<String>()
            val convertedResult = ArrayList<String>()
                while(cursor?.moveToNext()!!) {
                    val startUnit = cursor.getString(cursor.getColumnIndex("startUnit"))
                    val convertedUnit = cursor.getString(cursor.getColumnIndex("convertedUnit"))
                    if(!startResult.contains(startUnit)) {
                        startResult.add(startUnit)
                    }
                    if(!convertedResult.contains(convertedUnit)) {
                        convertedResult.add(convertedUnit)
                    }
                }
                cursor.close()

                val startAdapter: ArrayAdapter<String> = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, startResult)
                val convertedAdapter: ArrayAdapter<String> = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, convertedResult)
                startSpinner.adapter = startAdapter
                convertedSpinner.adapter = convertedAdapter

        } catch (exception: Exception) { // if this cause and exception toast and error message
            // report problem in pop-up
            Toast.makeText(
                view?.context,
                "Invalid data - " + exception.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}