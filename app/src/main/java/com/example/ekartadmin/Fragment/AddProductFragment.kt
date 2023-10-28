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
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ekartadmin.Adapter.AddProductImageAdapter
import com.example.ekartadmin.Model.AddProductModel
import com.example.ekartadmin.Model.CategoryModel
import com.example.ekartadmin.R
import com.example.ekartadmin.databinding.FragmentAddProductBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class AddProductFragment : Fragment() {


    private lateinit var binding: FragmentAddProductBinding
    private lateinit var list: ArrayList<Uri>
    private lateinit var listImages: ArrayList<String>
    private lateinit var adapter: AddProductImageAdapter
    private var coverImage: Uri? = null
    private lateinit var dialog: Dialog
    private var coverImageUrl: String? = ""
    private lateinit var categoryList: ArrayList<String>

    private var launchGalleryActivity = registerForActivityResult(


        ActivityResultContracts.StartActivityForResult()
    ) {


        if (it.resultCode == Activity.RESULT_OK) {

            coverImage = it.data!!.data

            binding.productCoverImg.setImageURI(coverImage)

            binding.productCoverImg.visibility=View.VISIBLE


        }


    }


    private var launchProductActivity = registerForActivityResult(

        ActivityResultContracts.StartActivityForResult()
    ) {


        if (it.resultCode == Activity.RESULT_OK) {
            val imageUrl = it.data!!.data

            list.add(imageUrl!!)
            adapter.notifyDataSetChanged()


        }


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        binding = FragmentAddProductBinding.inflate(layoutInflater)


        list= ArrayList()
        listImages= ArrayList()

        dialog= Dialog(requireContext())
        dialog.setContentView(R.layout.progress_bar)


        dialog.setCancelable(false)

        binding.selectCoverImg.setOnClickListener {



            val intent= Intent("android.intent.action.GET_CONTENT")
            intent.type="image/*"
            launchGalleryActivity.launch(intent)


        }


        binding.productImgBtn.setOnClickListener {



            val intent= Intent("android.intent.action.GET_CONTENT")
            intent.type="image/*"
            launchProductActivity.launch(intent)


        }

        setProductCategory()


        adapter= AddProductImageAdapter(list)

        binding.productImgRecyclerView.adapter=adapter


        binding.submitProductBtn.setOnClickListener {
            validateData()
        }





        return binding.root
    }

    private fun validateData() {

        if (binding.productNameEdt.text.toString().isEmpty()){
            binding.productNameEdt.requestFocus()
            binding.productNameEdt.error="Empty"
        }

        else if (binding.productSpEdt.text.toString().isEmpty())
        {
            binding.productSpEdt.requestFocus()
            binding.productSpEdt.error="empty"

        }

        else if (coverImage==null){

            Toast.makeText(requireContext(),"please select cover image", Toast.LENGTH_SHORT).show()






        }
        else if (list.size<1){

            Toast.makeText(requireContext(),"Please select product images", Toast.LENGTH_SHORT).show()

        }


        else{

            uploadImage()
        }



    }

    private fun uploadImage() {


        dialog.show()

        val fileName= UUID.randomUUID().toString()+".jpg"

        val refStorage= FirebaseStorage.getInstance().reference.child("products/$fileName")

        refStorage.putFile(coverImage!!)
            .addOnSuccessListener {

                it.storage.downloadUrl.addOnSuccessListener {
                        image ->

                    coverImageUrl=image.toString()


                    uploadProductImage()

                }
            }
            .addOnFailureListener(){

                dialog.dismiss()

                Toast.makeText(requireContext(),"Something went wrong with slider",
                    Toast.LENGTH_SHORT).show()


            }






    }

    private var i=0
    private fun uploadProductImage() {

        dialog.show()

        val fileName= UUID.randomUUID().toString()+".jpg"

        val reftStorage= FirebaseStorage.getInstance().reference.child("products/$fileName")

        reftStorage.putFile(list[i]!!)
            .addOnSuccessListener {

                it.storage.downloadUrl.addOnSuccessListener {
                        image ->


                    listImages.add(image!!.toString())


                    if (list.size==listImages.size){
                        storeData()
                    }
                    else{

                        i+=1
                        uploadProductImage()



                    }



                }
            }
            .addOnFailureListener(){

                dialog.dismiss()

                Toast.makeText(requireContext(),"Something went wrong with slider",
                    Toast.LENGTH_SHORT).show()


            }






    }

    private fun storeData() {

        val db= Firebase.firestore.collection("products")
        val key=db.document().id


        val data=AddProductModel(


            binding.productNameEdt.text.toString(),
            binding.productDiscriptionEdt.text.toString(),
            coverImageUrl.toString(),
            categoryList[binding.productCategoryDropDown.selectedItemPosition],
            key,
            binding.productMrpEdt.text.toString(),
            binding.productSpEdt.text.toString(),
            listImages




        )
        db.document(key).set(data)
            .addOnSuccessListener {
                dialog.dismiss()

                Toast.makeText(requireContext(),"Product Added ", Toast.LENGTH_SHORT).show()

                binding.productNameEdt.text=null

            }


            .addOnFailureListener {
                dialog.dismiss()

                Toast.makeText(requireContext(),"Somethong went wrong ", Toast.LENGTH_SHORT).show()





            }






    }


    private fun setProductCategory(){

        categoryList = ArrayList()

        Firebase.firestore.collection("category").get()
            .addOnSuccessListener {

                categoryList.clear()

                for(doc in it.documents){

                    val data=doc.toObject(CategoryModel::class.java)
                    categoryList.add(data!!.cate!!)





                }

                categoryList.add(0,"select Category")


                val arrayList=
                    ArrayAdapter(requireContext(),R.layout.dropdown_item_layout,categoryList)

                binding.productCategoryDropDown.adapter=arrayList

            }


    }



}