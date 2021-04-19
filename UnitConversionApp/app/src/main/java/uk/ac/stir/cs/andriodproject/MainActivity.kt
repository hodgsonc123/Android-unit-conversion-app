package uk.ac.stir.cs.andriodproject

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Switch
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

/**
 * Main activity class holds the code for fragments, the tabLayout and populating the database
 * **/
class MainActivity : AppCompatActivity() { // working hardcoded with ifs.

    // initialise content resolver used for inserting data to database
    private lateinit var cr: ContentResolver
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var nightModeSwitch: Switch
    /**
     * Adds a conversion record to the database
     * @param code The unique code/name for referencing the row GramsToKilograms
     * @param startUnit The unit converting from eg metres grams
     * @param convertedUnit The unit converting to metres grams
     * @param multiplier The multiplier to give the converted value eg 0.001
     * @param category The category of the converion eg weight, distance
     * **/
    private fun addConversion(code: String, startUnit: String, convertedUnit: String, multiplier: String, category: String) {
        Log.v("adding conversion", "code $code startUnit $startUnit convertedUnit $convertedUnit multiplier $multiplier category $category") // log the database entry
        val values = ContentValues() // set up variable to store the row for entry
        val uri = Uri.withAppendedPath(Provider.CONTENT_URI, code) // Uniform Resource Identifier stores location of database

        //adding values to the values set
        values.put("code", code)
        values.put("startUnit", startUnit)
        values.put("convertedUnit", convertedUnit)
        values.put("multiplier", multiplier)
        values.put("category", category)

        val rows = cr.update(uri, values, "", null)// get count
        if (rows == 0) {
            cr.insert(uri, values) } // insert the values to the content resolver/database
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP) // status bar required lollipop api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // load saved state on create used for restoring data after orientation
        setContentView(R.layout.activity_main)// set content view to activity main layout file

         //set up toolbar variable
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up tab layout finding it by id in layout
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        // set up night mode switch variable
        nightModeSwitch = findViewById<Switch>(R.id.nightSwitch)

        window.statusBarColor = ContextCompat.getColor(this, R.color.headerColour) // set status bar to match header colour

        // set up page adapter for tabs and fragments
        val viewPager = findViewById<ViewPager>(R.id.pager)
        val adapter = PagerAdapter(supportFragmentManager,
            tabLayout.tabCount)
        viewPager.adapter = adapter

        // listen for tab changes and change to fragment number corresponding with tab number
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // when night mode switch is checked, check the current state and toggle the night mode accordingly
        nightModeSwitch.setOnCheckedChangeListener{ compoundButton: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }

        //set up context resolver for given context
        cr = contentResolver

        // populate database with conversions using add method entering the values for each row
        //weight
        addConversion("GramsToKilograms", "Grams", "Kilograms", "0.001", "Weight")
        addConversion("GramsToPounds", "Grams", "Pounds", "0.00220462", "Weight")
        addConversion("GramsToStone", "Grams", "Stone", "0.000157473", "Weight")

        addConversion("KilogramsToGrams", "Kilograms", "Grams", "1000", "Weight")
        addConversion("KilogramsToPounds", "Kilograms", "Pounds", "2.20462", "Weight")
        addConversion("KilogramsToStone", "Kilograms", "Stone", "0.157473", "Weight")

        addConversion("PoundsToGrams", "Pounds", "Grams", "453.592", "Weight")
        addConversion("PoundsToKilograms", "Pounds", "Kilograms", "0.453592", "Weight")
        addConversion("PoundsToStone", "Pounds", "Stone", "0.0714286", "Weight")

        addConversion("StoneToGrams", "Stone", "Grams", "6350.29", "Weight")
        addConversion("StoneToKilograms", "Stone", "Kilograms", "6.35029", "Weight")
        addConversion("StoneToPounds", "Stone", "Pounds", "14", "Weight")

        //distance
        addConversion("MetresToKilometres", "Metres", "Kilometres", "0.001", "Distance")
        addConversion("MetresToYards", "Metres", "Yards", "1.09361", "Distance")
        addConversion("MetresToYMiles", "Metres", "Miles", "0.000621371", "Distance")

        addConversion("KilometresToMetres", "Kilometres", "Metres", "1000", "Distance")
        addConversion("KilometresToYards", "Kilometres", "Yards", "1093.61", "Distance")
        addConversion("KilometresToMiles", "Kilometres", "Miles", "0.621371", "Distance")

        addConversion("YardsToMetres", "Yards", "Metres", "0.9144", "Distance")
        addConversion("YardsToKilometres", "Yards", "Kilometres", "0.0009144", "Distance")
        addConversion("YardsToMiles", "Yards", "Miles", "0.000568182", "Distance")

        addConversion("MilesToMetres", "Miles", "Metres", "1609.34", "Distance")
        addConversion("MilesToKilometres", "Miles", "Kilometres", "1.60934", "Distance")
        addConversion("MilesToYards", "Miles", "Yards", "1760", "Distance")

        //speed
        addConversion("MPHTOKPH", "MPH", "KPH", "1.60934", "Speed")
        addConversion("MPHTOMS", "MPH", "MS", "0.44704", "Speed")

        addConversion("KPHTOMPH", "KPH", "MPH", "0.621371", "Speed")
        addConversion("KPHTOMS", "KPH", "MS", "0.277778", "Speed")

        addConversion("MSTOMPH", "MS", "MPH", "2.23694", "Speed")
        addConversion("MSTOKPH", "MS", "KPH", "3.6", "Speed")

        //time
        addConversion("SecondsToMinutes", "Seconds", "Minutes", "0.0166667", "Time")
        addConversion("SecondsToHours", "Seconds", "Hours", "0.000277778", "Time")

        addConversion("MinutesToSeconds", "Minutes", "Seconds", "60", "Time")
        addConversion("MinutesToHours", "Minutes", "Hours", "0.0166667", "Time")

        addConversion("HoursToSeconds", "Hours", "Seconds", "3600", "Time")
        addConversion("HoursToMinutes", "Hours", "Minutes", "60", "Time")
    }
}
