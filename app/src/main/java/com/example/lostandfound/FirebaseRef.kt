package com.example.lostandfound

import android.Manifest
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.*
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.sql.RowSetListener
import kotlin.collections.ArrayList

/**
 * References used:
 * - DB Write/Read: https://firebase.google.com/docs/database/android/read-and-write
 * - Upload Images: https://firebase.google.com/docs/storage/android/upload-files
 * - Interface: https://stackoverflow.com/questions/30659569/wait-until-firebase-retrieves-data
 */

public interface OnGetDataListener {
    fun onSuccess(snapshot: Object);
    fun onStart();
    fun onFailure(error: Object);
}

data class LostItemSubmission(
    val id: String = "",
    val userid: String="",
    val name: String ="",
    val location: String ="",
    val description: String ="",
    val pictureURLs: ArrayList<String>,
    val dateFound: String,
    val dateSubmitted: String,
    val tags: String,
    val status: Boolean);

data class User(
    val email: String="",
    val name: String="",
    val phone_number: String="",
    val uid: String =""
)


class FirebaseRef: AppCompatActivity() {

    var lostItemsList = ArrayList<LostItemSubmission>()

    /**
     * Function: uploadImage
     * Desc: Takes in an image file and the path of that file and uploads it to Firebase storage
     * Listener
     *  - onSuccess(String path of the image in Firebase storage)
     *  - onFailure(Exception Object)
     * Returns
     *  - String path of the image in Firebase storage
     */
    public fun uploadImage(fName: String, fpath: String, submission_id: String, in_listener: OnGetDataListener) {
        in_listener.onStart()

        var uniqueID = UUID.randomUUID().toString()

        // creates unique id for the file on storage
        val path = IMAGE_PATH + submission_id +"/"+ fName + uniqueID

        // setReferences to database
        val filePathRef = storageRef.child(IMAGE_PATH).child(submission_id).child(fName + uniqueID);

        // upload image via stream
        val imgFile = File(fpath) as File;
        if(!imgFile.exists()) {
            Log.i(TAG, "ERROR: File Does not Exist");
            return;
        }

        val stream = FileInputStream(imgFile);
        val uploadTask = filePathRef.putStream(stream);
        uploadTask.addOnFailureListener() {
            Log.i(TAG, "ERROR: Failed to Upload");
            in_listener.onFailure(it as Object)
        }.addOnSuccessListener {  taskSnapshot ->
            Log.i(TAG, "Image Upload Success!");
            in_listener.onSuccess(path as Object)
        }
    }

    /**
     * Function: newSubmission
     * Adds a new Lost Item submission onto Firebase database
     */
    public fun newSubmission(uid: String, name: String, description: String, location: String, pictureURLs: ArrayList<String>, dateFound: LocalDateTime, tags: String) {
        val database = Firebase.database
        val myRef = database.getReference(SUBMISSIONS_PATH);
        val id = database.getReference(SUBMISSIONS_PATH).push().key;

        val pattern = "yyyy-MM-dd HH:mm:ss"
        val current_time = LocalDateTime.now();
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val toAdd = LostItemSubmission(id!!, uid, name, location, description, pictureURLs, dateFound.format(formatter), current_time.format(formatter), tags, false);
        myRef.child(id).setValue(toAdd);

    }

    /**
     *  Function: fetchSubmissionsList
     *  Fetches all submissions and puts them in the global List, LostItemsList
     *  Listener
     *  - onSuccess(ArrayList<LostItemSubmission>)
     *  - onFailure(DatabaseError)
     * Returns
     *  Nothing
     */
    public fun fetchSubmissionsList(in_listener : OnGetDataListener) {
        in_listener.onStart()

        val database = Firebase.database
        val myRef = database.getReference(SUBMISSIONS_PATH);

        var listener = object: OnGetDataListener {

            override fun onSuccess(snapshot: Object) {
                var dataSnapshot = snapshot as DataSnapshot
                val list = ArrayList<LostItemSubmission>()
                val children = dataSnapshot!!.children
                for (child in children) {

                    Log.i(TAG, "Fetching Data: " + child.toString())

                    var id = child.child("id").getValue() as String
                    var userid = child.child("userid").getValue() as String
                    var name = child.child("name").getValue() as String
                    var location = child.child("location").getValue() as String
                    var description = child.child("description").getValue() as String
                    var pictureURLs = child.child("pictureURLs").getValue() as ArrayList<String>
                    var dateFound  = child.child("dateFound").getValue() as String
                    var dateSubmitted = child.child("dateSubmitted").getValue() as String
                    var tags = child.child("tags").getValue() as String
                    var status = child.child("status").getValue() as Boolean

                    list.add(LostItemSubmission(id, userid.toString(), name, location, description, pictureURLs, dateFound, dateSubmitted, tags, status))
                }

                lostItemsList = list
                in_listener.onSuccess(list as Object)
            }

            override fun onStart() {
                Log.i(TAG, "Getting all Submissions...")
            }

            override fun onFailure(error: Object) {
                var databaseError = error as DatabaseError
                Log.i(TAG, "Firebase Fetch Failed: " + databaseError.message);
                in_listener.onFailure(error);
            }

        }
        fetchSingleValue(myRef, listener);
    }

    /**
     * Function: changeStatus
     * Changes the status of a given submission ID
     */
    public fun changeStatus(id: String, newStatus: Boolean) {
        val database = Firebase.database
        val myRef = database.getReference(SUBMISSIONS_PATH).child(id);

        myRef.child("status").setValue(newStatus);
    }

    /**
     * Function: FindUserByID
     * Fetches user data with their provided UID
     * * Listener
     *  - onSuccess(User)
     *  - onFailure(DatabaseError)
     * Returns
     *  - Nothing
     */
    public fun findUserByID(uid : String, in_listener: OnGetDataListener) {
        in_listener.onStart()
        val database = Firebase.database
        val myRef = database.getReference(USER_PATH);

        var listener = object: OnGetDataListener {
            override fun onSuccess(snapshot: Object) {
                var dataSnapshot = snapshot as DataSnapshot
                val children = dataSnapshot!!.children
                for (child in children) {

                    var curID = child.child("uid").getValue() as String
                    if(curID.equals(uid)) {
                        val email = child.child("email").getValue() as String
                        val name  = child.child("name").getValue() as String
                        val phone_number = child.child("phone_number").getValue() as String

                        in_listener.onSuccess(User(email, name, phone_number,uid) as Object)
                    }
                }
            }
            override fun onStart() {
                Log.i(TAG, "Getting Users...")
            }

            override fun onFailure(error: Object) {
                in_listener.onFailure(error)
                var databaseError = error as DatabaseError
                Log.i(TAG, "Firebase Fetch Failed: " + databaseError.message);
            }
        }

        fetchSingleValue(myRef, listener)
    }

    /**
     * Private helper function that fetches single data entries from the database
     */
    private fun fetchSingleValue(ref: DatabaseReference, listener: OnGetDataListener){
        listener.onStart();
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listener.onSuccess(snapshot as Object);
            }
            override fun onCancelled(error: DatabaseError) {
                listener.onFailure(error as Object);
            }
        });
    }


    companion object {
        fun create(): FirebaseRef = FirebaseRef();
        const val TAG = "Lost&Found-FirebaseRef"
        const val IMAGE_PATH = "images/"
        const val SUBMISSIONS_PATH = "submissions/"
        const val USER_PATH = "users/"

        val storage = Firebase.storage;
        val storageRef = storage.getReference();

    }
}

