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
import com.example.ekartadmin.Adapter.CategoryAdapter
import com.example.ekartadmin.Model.CategoryModel
import com.example.ekartadmin.R
import com.example.ekartadmin.databinding.FragmentCategoryBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding

    private var imgUrl: Uri?=null

    private lateinit var dialog: Dialog

    private var launchGalleryActivity=registerForActivityResult(

        ActivityResultContracts.StartActivityForResult()){


        if (it.resultCode== Activity.RESULT_OK){
            imgUrl=it.data!!.data

            binding.imageView.setImageURI(imgUrl)


        }




    }





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding=FragmentCategoryBinding.inflate(layoutInflater)

        dialog= Dialog(requireContext())
        dialog.setContentView(R.layout.progress_bar)
        dialog.setCancelable(false)

        getData()

        binding.apply {


            imageView.setOnClickListener(){

                val intent= Intent("android.intent.action.GET_CONTENT")
                intent.type="image/*"
                launchGalleryActivity.launch(intent)



            }

            button6.setOnClickListener(){

                validateData(binding.categoryName.text.toString())


            }


        }

        return binding.root

    }

    private fun getData() {


        val list=ArrayList<CategoryModel>()
        Firebase.firestore.collection("category")
            .get().addOnSuccessListener {
                list.clear()

                for(doc in it.documents){

                    val data=doc.toObject(CategoryModel::
                    class.java)
                    list.add(data!!)


            }
                binding.categoryRecycler.adapter=CategoryAdapter(requireContext(),list)
            }


    }

    private fun validateData(categoryName: String) {

        if(categoryName.isEmpty()){
            Toast.makeText(requireContext(),"Please provide category Name",Toast.LENGTH_SHORT).show()

        }
        else if (imgUrl==null){
            Toast.makeText(requireContext(),"please select image",Toast.LENGTH_SHORT).show()

        }
        else{
            uploadImage(categoryName)
        }

    }

    private fun uploadImage(categoryName: String) {
        dialog.show()

        val fileName= UUID.randomUUID().toString()+".jpg"

        val reftStorage= FirebaseStorage.getInstance().reference.child("category/$fileName")

        reftStorage.putFile(imgUrl!!)
            .addOnSuccessListener {

                it.storage.downloadUrl.addOnSuccessListener {
                        image ->


                    storeData(categoryName,image.toString())
                }
            }
            .addOnFailureListener(){

                dialog.dismiss()

                Toast.makeText(requireContext(),"Something went wrong with slider",
                    Toast.LENGTH_SHORT).show()


            }

    }

    private fun storeData(categoryName: String, url: String) {

        val db= Firebase.firestore

        val data= hashMapOf<String,Any>(

            "cate" to categoryName,
            "img" to url

        )


        db.collection("category").add(data)
            .addOnSuccessListener {

                dialog.dismiss()
                binding.imageView.setImageDrawable(resources.getDrawable(R.drawable.preview_image))
                binding.categoryName.text=null
                getData()
                Toast.makeText(requireContext(),"category Updated",Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(),"Something went wrong",Toast.LENGTH_LONG).show()




            }




    }

    }


