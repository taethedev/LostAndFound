package com.example.lostandfound

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.DatabaseError

class ScrollingActivity : AppCompatActivity() {

    // user id of the current user
    var UID: String = "";

    // Firebase Ref reference
    val fbref = FirebaseRef.create()

    private lateinit var listView: ListView
    private lateinit var searchView: SearchView


    //button for test purposes
    private lateinit var addBut: Button
    private lateinit var addButs: Button
    private lateinit var refreshButton: Button

    // create local array
    private lateinit var dataArray : ArrayList<LostItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)

        // gets the UID of the user after login if empty
        if(UID.equals(""))
            UID = intent.getStringExtra("uid").toString();

        /***
         *      get button reference
         ***/
        addBut = findViewById(R.id.addItemA)
        refreshButton = findViewById(R.id.refreshButton)

        /***
         *      get reference to listView and searchView
         ***/
        listView = findViewById(R.id.listView)
        searchView = findViewById(R.id.searchView)

        /***
         *      local array which stores the information from the global database
         ***/
        dataArray = ArrayList<LostItem>()

        /***
         *      get refernce to custom itemAdapter which is used for dynamically adding
         *      rows to the listview
         ***/
        val itemAdapter = ItemAdapter(this, dataArray)
        listView.adapter = itemAdapter

        /***
         *      add onClick listeners to the add and refresh button
         *
         *      The add button starts the EnterLostItemActivity. This activity allows user
         *      to post lost item. The current userID is sent as an external to be attached to the
         *      submission.
         *
         *      The refresh button calls the updateLocalList() function which basically
         *      restarts the activity. This helps fetching the data from the firebase to
         *      our local array.
         ***/
        addBut.setOnClickListener {
            val intent = Intent(this@ScrollingActivity, EnterLostItemActivity::class.java)
            intent.putExtra("UID", UID)
            startActivity(intent)

            itemAdapter.flag = true
            itemAdapter.notifyDataSetChanged()
        }
        refreshButton.setOnClickListener {
            Log.i("Click", "Refresh Clicked")
            updateLocalList()
            itemAdapter.notifyDataSetChanged()
        }

        /***
         *      Use to retrieve information from the firebase database when the activity
         *      first starts.
         *      It gets a snapshot of the data present, populates the global list, and then
         *      is deep copies into the local list.
         ***/
        var listener = object: OnGetDataListener {
            override fun onSuccess(snapshot: Object) {
                var list = snapshot as ArrayList<LostItemSubmission>
                fbref.lostItemsList = list

                // first lists the items that have not been claimed
                var itemsSize = fbref.lostItemsList.size
                for(i in itemsSize-1 downTo 0) {
                    if(list[i].status == false) {
                        dataArray.add(
                            LostItem(
                                list[i].userid,
                                list[i].id,
                                list[i].pictureURLs[0],
                                list[i].name,
                                list[i].location,
                                list[i].description,
                                list[i].dateFound,
                                list[i].dateSubmitted,
                                list[i].status,
                                list[i].tags
                            )
                        )
                    }
                }

                // list items that have been claimed in the past
                for(i in itemsSize-1 downTo 0) {
                    if(list[i].status == true) {
                        dataArray.add(
                            LostItem(
                                list[i].userid,
                                list[i].id,
                                list[i].pictureURLs[0],
                                list[i].name,
                                list[i].location,
                                list[i].description,
                                list[i].dateFound,
                                list[i].dateSubmitted,
                                list[i].status,
                                list[i].tags
                            )
                        )
                    }
                }
                itemAdapter.notifyDataSetChanged()

            }

            override fun onStart() {
            }

            override fun onFailure(error: Object) {
                var err = error as DatabaseError
                Log.i(TAG, err.message)
                Toast.makeText(
                    applicationContext,
                    "NETWORK ERROR - Please check your network connection",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        fbref.fetchSubmissionsList(listener)

        /***
         *      create click listener for each row.
         *      Once a row is clicked it is passed on to another activity, ClaimItem.kt.
         *      The activity is sent relevant information to be displayed, used for
         *      emailing.
         *
         *      We also send the current userID. we compare the userID of the item and that
         *      of the user. If they are similar that means the user posted that item, giving
         *      the user ability to set the posting as claimed.
         ***/
        listView.setOnItemClickListener { parent, view, position, id ->
            val it : LostItem = itemAdapter.getItem(position) as LostItem
            val name = it.name.toString()
            val uid = it.uid.toString()
            val id = it.id
            val imgURL = it.imgURL
            val location = it.locationFound.toString()
            val desc = it.desc.toString()
            val dateFound = it.dateFound
            val datePosted = it.datePosted
            val selfUID = UID
            val status = it.status.toString()

            val intent = Intent(this, ClaimItem::class.java)
            intent.putExtra("Name", name)
            intent.putExtra("UID", uid)
            intent.putExtra("ID", id)
            intent.putExtra("MyUID", selfUID)
            intent.putExtra("IMGUrl", imgURL)
            intent.putExtra("Location", location)
            intent.putExtra("Desc", desc)
            intent.putExtra("Found", dateFound)
            intent.putExtra("Posted", datePosted)
            intent.putExtra("Status", status)
            startActivity(intent)
        }

        /***
         *      SEARCH FUNCTIONALITY
         *      Adds a query change listener to the search field.
         *      If the field notices any changes then it calls the filter function,
         *      in the listAdapter and displays the those certain lists.
         ***/
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.i("Click", "Reached searchView listener")
                if (TextUtils.isEmpty(newText)) {
                    itemAdapter.filter("")
                    listView.clearTextFilter()
                } else newText?.let { itemAdapter.filter(it) }
                return false
            }

        })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /***
         *      Handle action bar item clicks here. The action bar will
         *      automatically handle clicks on the Home/Up button, so long
         *      as you specify a parent activity in AndroidManifest.xml.
         ***/
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    /***
     * displayArray and dispGlobalArray are used for debugging purposes.
     */
    fun displayArray(arrList: ArrayList<LostItem>): String {
        for (i in arrList){
            Log.i(locArr, i.name + i.id)
        }
        return "done"
    }
    fun dispGlobalArray(arrList: ArrayList<LostItemSubmission>): String {
        for (i in arrList){
            Log.i(globalArr, i.name + " " + i.id + " " + i.pictureURLs[0])
        }
        return "done"
    }

    /***
     * restart activity
     ***/
    fun updateLocalList(){
                finish();
                startActivity(getIntent());

    }

    companion object {
        fun create(): FirebaseRef = FirebaseRef();
        const val TAG = "Lost&Found";
        const val locArr = "local"
        const val globalArr = "global"
    }
}