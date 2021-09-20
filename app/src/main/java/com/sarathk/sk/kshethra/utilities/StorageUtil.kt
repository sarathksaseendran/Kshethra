package com.sarathk.sk.kshethra.utilities

import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import java.io.File


object StorageUtil {

    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val currentUserRef: StorageReference
        get() = storageInstance.reference
            //.child(FirebaseAuth.getInstance().currentUser?.uid ?: throw NullPointerException("UID is null."))

    fun uploadFileBytes(fileBytes: ByteArray, filename: String, onSuccess: (filePath: String) -> Unit) {
        val metadata = storageMetadata {
            contentType = "image/jpeg"
        }
        //val ref = currentUserRef.child("profilePictures/${Variables.mobile}_${UUID.nameUUIDFromBytes(imageBytes)}")
        val ref = currentUserRef.child(filename)
        ref.putBytes(fileBytes, metadata)
            .addOnSuccessListener {
                onSuccess(ref.path)
            }
    }

    fun uploadFile(file: File, filename: String, onSuccess: (filePath: String) -> Unit) {
        val metadata = storageMetadata {
            contentType = "image/jpeg"
        }
        //val ref = currentUserRef.child("profilePictures/${Variables.mobile}_${SimpleDateFormat("yyyyMMddHHmmssSZ", Locale.ENGLISH).format(Date())}")
        val ref = currentUserRef.child(filename)
        ref.putFile(file.toUri(), metadata)
            .addOnSuccessListener {
                onSuccess(ref.path)
            }
    }

    fun uploadFileUri(fileUri: Uri, filename: String, onSuccess: (filePath: String) -> Unit) {
        val metadata = storageMetadata {
            contentType = "image/jpeg"
        }
        //val ref = currentUserRef.child("profilePictures/${Variables.mobile}_${SimpleDateFormat("yyyyMMddHHmmssSZ", Locale.ENGLISH).format(Date())}")
        val ref = currentUserRef.child(filename)
        ref.putFile(fileUri, metadata)
            .addOnSuccessListener {
                onSuccess(ref.path)
            }
    }

    fun downloadFile(filePath: String, localFile: File, onSuccess: (result: Boolean) -> Unit) {
        val ref = currentUserRef.child(filePath)

        ref.getFile(localFile).addOnSuccessListener {
            onSuccess(true)
        }.addOnFailureListener {
            onSuccess(false)
        }
    }

    fun pathToReference(path: String, onSuccess: (imageUrl: String) -> Unit) = storageInstance.reference.child(path).downloadUrl.addOnSuccessListener {
            uri ->  onSuccess(uri.toString())
    }

    fun checkFileExist(path: String, onComplete: (result: Boolean) -> Unit) {
        storageInstance.reference.child(path).downloadUrl
            .addOnSuccessListener { uri ->
                onComplete(true)
            }.addOnFailureListener {
                if (it is StorageException && it.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    onComplete(false)
                } else {
                    onComplete(true)
                }
            }
    }
}