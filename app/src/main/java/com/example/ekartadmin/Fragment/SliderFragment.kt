package com.example.ekartadmin.Fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ekartadmin.R
import com.example.ekartadmin.databinding.FragmentSliderBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class SliderFragment : Fragment() {

    private lateinit var binding: FragmentSliderBinding

    private var imgUrl:Uri?=null

    private lateinit var dialog: Dialog

    private var launchGalleryActivity=registerForActivityResult(

        ActivityResultContracts.StartActivityForResult()){


        if (it.resultCode==Activity.RESULT_OK){
            imgUrl=it.data!!.data

            binding.imageView.setImageURI(imgUrl)


        }




    }






    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding= FragmentSliderBinding.inflate(layoutInflater)

        dialog= Dialog(requireContext())
        dialog.setContentView(R.layout.progress_bar)
        dialog.setCancelable(false)

        binding.apply {


            imageView.setOnClickListener(){

                val intent=Intent("android.intent.action.GET_CONTENT")
                intent.type="image/*"
                launchGalleryActivity.launch(intent)





            }





            button5.setOnClickListener(){

                if (imgUrl != null){

                    uploadImage(imgUrl!!)
                }
                else{
                    Toast.makeText(requireContext(),"please select the image ",Toast.LENGTH_LONG).show()
                }



            }

        }






        return binding.root
    }

    private fun uploadImage(uri: Uri) {


        dialog.show()

        val fileName=UUID.randomUUID().toString()+".jpg"

        val reftStorage=FirebaseStorage.getInstance().reference.child("slider/$fileName")

        reftStorage.putFile(uri)
            .addOnSuccessListener {

                it.storage.downloadUrl.addOnSuccessListener {
                        image ->


                    storeData(image.toString())
                }
            }
            .addOnFailureListener(){

                dialog.dismiss()

                Toast.makeText(requireContext(),"Something went wrong with slider",
                    Toast.LENGTH_SHORT).show()


            }




    }

    private fun storeData(image: String) {

        val db=Firebase.firestore

        val data= hashMapOf<String,Any>(

            "img" to image

        )


        db.collection("slider").document("item").set(data)
            .addOnSuccessListener {

                dialog.dismiss()
                Toast.makeText(requireContext(),"Slider Updated",Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(),"Something went wrong",Toast.LENGTH_LONG).show()




            }




    }


}
