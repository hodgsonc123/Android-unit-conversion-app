package uk.ac.stir.cs.andriodproject

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.conversion_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.DecimalFormat

/**
 * Fragment handling the conversion of values of selected units
 * **/
class ConversionFragment : Fragment() {

    private val decimal = DecimalFormat("###.#######")

    // initialize unit label variables
    lateinit var startUnitLabel : TextView
    lateinit var convertedUnitLabel : TextView
    lateinit var categoryUnitLabel : TextView

    // initialise text field variables
    lateinit var startUnitValue : EditText
    lateinit var convertedUnitValue : EditText

    // initialize variables to hold selected category and units from selection fragment
    private var startSpinnerValue : String = ""
    private var convertedSpinnerValue : String = ""
    private var categorySpinnerValue : String = ""

    // set up database uri
    val PROVIDER_NAME = "uk.ac.stir.cs.andriodproject.Conversion"
    val DATABASE_TABLE = "modules"
    val CONTENT_URI: Uri = Uri.parse("content://$PROVIDER_NAME/$DATABASE_TABLE")

    // on start register from the event bus
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    // on stop unregister from the event bus
    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // on fragment creation load saved state
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.conversion_fragment, container, false) // load the conversion fragment layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // declare labels to corresponding text view in the layout
        startUnitLabel = view.findViewById(R.id.textViewStartUnitLabel)
        convertedUnitLabel  = view.findViewById(R.id.textViewConvertedUnitLabel)
        categoryUnitLabel = view.findViewById(R.id.categoryLabel)

        // declare the text fields to the corresponding edit texts in the layout
        startUnitValue = view.findViewById(R.id.editTextStartUnit)
        convertedUnitValue  = view.findViewById(R.id.editTextConvertedUnit)
        //convertedUnitValue.isEnabled = false // set the converted value to unclickable

        // declare clear button linked to the clear button in the layout
        val clearButton = view.findViewById<View>(R.id.clearButton) as Button
        // button listener \to check when clicked. sets the text values to empty strings, clearing them
        clearButton.setOnClickListener{

            try{
                startUnitValue.setText("") // clear text fields
                convertedUnitValue.setText("")
            }catch (exception: Exception){
                Toast.makeText(view.context, "Cleared", Toast.LENGTH_SHORT).show() // toast cleared
            }
        }


        // buttons to from the number pad to type in the values
        // the buttons add the corresponding number to the 'from' value.
        // calls the doConversion method which automatically calculates the converted value
        val button0 = view.findViewById<Button>(R.id.button0)
        button0.setOnClickListener {
            startUnitValue.append("0")
            doConversion()
        }

        val button1 = view.findViewById<Button>(R.id.button1)
        button1.setOnClickListener {
            startUnitValue.append("1")
            doConversion()
        }

        val button2 = view.findViewById<Button>(R.id.button2)
        button2.setOnClickListener {
            startUnitValue.append("2")
            doConversion()
        }

        val button3 = view.findViewById<Button>(R.id.button3)
        button3.setOnClickListener {
            startUnitValue.append("3")
            doConversion()
        }

        val button4 = view.findViewById<Button>(R.id.button4)
        button4.setOnClickListener {
            startUnitValue.append("4")
            doConversion()
        }

        val button5 = view.findViewById<Button>(R.id.button5)
        button5.setOnClickListener {
            startUnitValue.append("5")
            doConversion()
        }

        val button6 = view.findViewById<Button>(R.id.button6)
        button6.setOnClickListener {
            startUnitValue.append("6")
            doConversion()
        }

        val button7 = view.findViewById<Button>(R.id.button7)
        button7.setOnClickListener {
            startUnitValue.append("7")
            doConversion()
        }

        val button8 = view.findViewById<Button>(R.id.button8)
        button8.setOnClickListener {
            startUnitValue.append("8")
            doConversion()
        }

        val button9 = view.findViewById<Button>(R.id.button9)
        button9.setOnClickListener {
            startUnitValue.append("9")
            doConversion()
        }
        // button for decimal place. cannot be placed unless there is already a number typed
        val buttonDecimal = view.findViewById<Button>(R.id.buttonDot)
        buttonDecimal.setOnClickListener {
            if (startUnitValue.length() != 0) {
                startUnitValue.append(".")
            }
            doConversion()
        }

        // delete button, removes the end value in the 'from' unit text box(providing it is not already empty)
        val buttonDel = view.findViewById<Button>(R.id.buttonDel)
        buttonDel.setOnClickListener {

            if (startUnitValue.length() != 0) {
                startUnitValue.text.delete(startUnitValue.length() - 1, startUnitValue.length())
            }
            else{
                convertedUnitValue.setText("")
            }
            doConversion()
        }
    }

    /**
     * Method to carry out the conversion between the two units.
     * gets the multiplier from the database by searching the database to find the row where the start and converted units
     * match the ones that are in the selection spinner
     * **/
    private fun doConversion() {

        var where = "startUnit=? AND convertedUnit=?"
        var args = arrayOf(startSpinnerValue, convertedSpinnerValue)
        try {
            val uri = Uri.parse(CONTENT_URI.toString())
            val cursor = context!!.contentResolver.query(uri, null, where, args, null)
            if (cursor!!.count == 1) { // if the cursor count is one aka one row do..
                cursor.moveToFirst()
                val multiplier = cursor.getString(cursor.getColumnIndex("multiplier")) // store the muiltiplier

                // set the converted text field to contain the converted value(start value of type start unit * multiplier = converted value of type converted unit)
                editTextConvertedUnit.setText(decimal.format((editTextStartUnit.text.toString().toDouble()) * multiplier.toDouble()).toString())

                cursor.close()
            }
        } catch (exception: Exception) {
            // report problem in pop-up window
            Toast.makeText(view?.context, "Invalid data - " + exception.message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * subscriber to the category spinner event bus data class
     * @param unitSpinnerData Value selected in category spinner
     * **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun listenItemChange(unitSpinnerData: SelectionFragment.unitCategorySpinner){
        var itemSelected = unitSpinnerData.selectedItem // store the selected category
        categorySpinnerValue = itemSelected // store selected category
        categoryUnitLabel.text = itemSelected // set category label
    }

    /**
     * subscriber to the start unit spinner event bus data class
     * @param startSpinnerData Value selected in category spinner
     * **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun listenItemChange(startSpinnerData: SelectionFragment.startUnitSpinner) {
        var itemSelected = startSpinnerData.selectedItem // get selected unit
        startSpinnerValue = itemSelected // store selected unit for use in query
        startUnitLabel.text = itemSelected // set start unit label
    }

    /**
     * subscriber to the converted unit spinner event bus data class
     * @param convertedSpinnerData Value selected in category spinner
     * **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun listenItemChange(convertedSpinnerData: SelectionFragment.convertedUnitSpinner){
        var itemSelected = convertedSpinnerData.selectedItem // get selected to unit
        convertedSpinnerValue = itemSelected // store selected unit for use in query
        convertedUnitLabel.text = itemSelected // set start unit label
    }
}

