package uk.ac.stir.cs.andriodproject

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.conversion_fragment.*

/**
 * Fragment handling the removal of a database entry/converison
 * **/
class RemoveConversionFragment : Fragment() {

    // init text field variable
    private lateinit var conversionName : EditText

    // init content resolver
    private lateinit var cr: ContentResolver

    // set up database uri
    private val PROVIDER_NAME = "uk.ac.stir.cs.andriodproject.Conversion"
    private val DATABASE_TABLE = "modules"
    private val CONTENT_URI: Uri = Uri.parse("content://$PROVIDER_NAME/$DATABASE_TABLE")


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.remove_conversion, container, false) // set remove conversion layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        conversionName = view.findViewById(R.id.editTextRemoveConversion) // declare text field variable to corresponding edit text in the layout

        // set remove button to correspond to the one in the layout
        val removeButton = view.findViewById<View>(R.id.removeButton) as Button
        // if button is clicked
        removeButton.setOnClickListener {
            val conversionNameString : String = conversionName.text.toString() // string version of above
            // if the text field isnt empty try and remove the given name using the .delete method
            // in the database to delete the row with matching code
            if (conversionNameString.length != 0 && isPresent()) {
                try {
                    cr = context!!.contentResolver

                    var where = "code=?"
                    var args = arrayOf(conversionNameString)
                    cr.delete(CONTENT_URI, where, args)
                    conversionName.setText("")
                    Log.v("Removing conversion", "code $conversionNameString")
                    Toast.makeText(
                        view?.context,
                        "Removed",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        view?.context,
                        "Error removing" + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Function carries out a query of the database to discover if the suggested conversion name is already in the database
     * @return found, Boolean stating if the name was found or not
     * **/
    private fun isPresent() : Boolean{

        var found = true

        var where = "code=?"
        var args = arrayOf(conversionName.text.toString())
        try {
            val uri = Uri.parse(CONTENT_URI.toString())
            val cursor = context!!.contentResolver.query(uri, null, where, args, null)
            if (cursor!!.count == 0) { // if the cursor count is one aka one row do..
                found = false
                Toast.makeText(
                    view?.context,
                    "Not found",
                    Toast.LENGTH_SHORT
                ).show()
                cursor.close()
            } else {

            }
        } catch (exception: Exception) {
            // report problem in pop-up window
            Toast.makeText(view?.context, "Invalid data - " + exception.message, Toast.LENGTH_SHORT).show()
        }
        return found
    }
}
