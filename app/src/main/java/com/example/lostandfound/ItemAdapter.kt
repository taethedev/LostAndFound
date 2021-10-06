package com.example.lostandfound

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.StorageReference
import java.io.InputStream

import java.util.*
import kotlin.collections.ArrayList

/***
 * CUSTOM ADAPTER
 *
 * used for dynamically creating rows of items.
 */

/***
 * https://www.raywenderlich.com/155-android-listview-tutorial-with-kotlin
 *
 * used the link above to learn how custom listView adapter works.
 */

class ItemAdapter(
    private val context: Context,
    private val dataSource: ArrayList<LostItem>
) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var flag = true
    var tempList : ArrayList<LostItem> = ArrayList()


    override fun getCount(): Int {
//        TODO("Not yet implemented")s
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
//        TODO("Not yet implemented")
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
//        TODO("Not yet implemented")
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        TODO("Not yet implemented")
        val rowView = inflater.inflate(R.layout.list_row, parent, false)
        val imageView = rowView.findViewById(R.id.image) as ImageView
        val titleView = rowView.findViewById(R.id.txtName) as TextView
        val locationView = rowView.findViewById(R.id.location) as TextView
        val descView = rowView.findViewById(R.id.des) as TextView
        val getRef = getItem(position) as LostItem
        val getStatus = getRef.status
        val imgRef = FirebaseRef.storageRef.child(getRef.imgURL)
        imgRef.downloadUrl.addOnSuccessListener { Uri ->

            val imageURL = Uri.toString()
            try{
                Glide.with(context).load(imageURL).into(imageView)
            } catch (e: Exception){
                e.printStackTrace()
            }
        }

        titleView.text = getRef.name
        locationView.text = getRef.locationFound
        descView.text = getRef.desc
        if(getStatus){
            rowView.setBackgroundColor(Color.parseColor("#c76d76"))
            titleView.text = getRef.name + " (Claimed)";
        }
        else{
            rowView.setBackgroundColor(Color.parseColor("#6dcf87"))
        }
        return rowView
    }

    /***
     * Filter function used for searching.
     * Filters based on if any of the item properties contains the value present in
     * the search field.
     */

    /***
     * https://www.codegrepper.com/code-examples/kotlin/android+listview+search+filter+custom+adapter+kotlin
     * used as a reference for filter functionality
     */
    fun filter(charText: String){
        //create deep copy of
        if (flag){
            tempList.clear()
            for(i in dataSource){
            tempList.add(
                LostItem(
                    i.uid,
                    i.id,
                    i.imgURL,
                    i.name,
                    i.locationFound,
                    i.desc,
                    i.dateFound,
                    i.datePosted,
                    i.status,
                    i.tags
                )
            )
            flag = false
        }}
        val charText = charText.toLowerCase(Locale.getDefault())
        dataSource.clear()
        if(charText.isEmpty()){
            Log.i("Click", "looking for length == 0")
            dataSource.clear()
            dataSource.addAll(tempList)
        }
        else{
            Log.i("Click", "looking for length != 0")
            for(i in tempList){
                if(i.locationFound.contains(charText, ignoreCase = true) or
                    i.name.toLowerCase().contains(charText, ignoreCase = true) or
                    i.desc.contains(charText, ignoreCase = true) or
                        i.tags.contains(charText, ignoreCase = true))
                    dataSource.add(i)
            }
            Log.i("Click", "temp_list after else:" + tempList.size.toString())
        }
        notifyDataSetChanged()
    }

}

