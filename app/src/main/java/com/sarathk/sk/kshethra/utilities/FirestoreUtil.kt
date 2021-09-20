package com.sarathk.sk.kshethra.utilities

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.sarathk.sk.kshethra.model.User
import java.util.*
import kotlin.collections.HashMap

object FirestoreUtil {

//    val settings = FirebaseFirestoreSettings.Builder()
//        .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
//        .build()

    private val firestoreInstance: FirebaseFirestore by lazy {
//        FirebaseFirestore.getInstance().firestoreSettings = settings
        FirebaseFirestore.getInstance()
    }

//    var topDate: Date? = null
//    var bottomDate: Date? = null
//    var currentLoadedCount = 1
//    var isLoading = false

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document(
            "users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null")}"
        )

//    private val chatChannelCollectionRef = firestoreInstance.collection("chatchannels")

    fun initCurrentUserIfFirstTime(onComplete: (status: Boolean) -> Unit) {
        currentUserDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
                    val newUser = User(
                        "",
                        FirebaseAuth.getInstance().currentUser?.phoneNumber ?: "",
                        null,
                        mutableListOf()
                    )
                    currentUserDocRef.set(newUser).addOnSuccessListener {
                        onComplete(true)
                    }
                } else { }
            }.addOnFailureListener {
                onComplete(false)
            }
    }

    fun updateCurrentUser(fullName: String = "", mobileNumber: String = "", profilePicture: String? = null, onComplete: (status: Boolean) -> Unit) {
        val userFieldMap = mutableMapOf<String, Any>()

        if (fullName.isNotBlank()) userFieldMap["fullName"] = fullName
        if (mobileNumber.isNotBlank()) userFieldMap["mobileNumber"] = mobileNumber
        if (profilePicture != null) userFieldMap["profilePicture"] = profilePicture

        currentUserDocRef.update(userFieldMap)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun getCurrentUser(onComplete: (User?) -> Unit) {
        currentUserDocRef.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java))
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun getUserWithId(otherUserId: String, onComplete: (User?) -> Unit) {
        firestoreInstance.collection("users")
            .document(otherUserId)
            .get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java))
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun addUsersListener(context: Context, selected: ArrayList<String>, onListen: (HashMap<String, User>) -> Unit): ListenerRegistration {
        return firestoreInstance.collection("users")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("firestore listen error", "error", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = HashMap<String, User>()
                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                querySnapshot?.documents?.forEach {
                    if (it.id != currentUserId && selected.indexOf(it.id) < 0) {
                        items[it.id] = it.toObject(User::class.java)!!
                    }
                }

                onListen(items)
            }
    }

//    fun addUsersSearchListener(context: Context, searchText: String, selected: ArrayList<String>, onListen: (List<ClipData.Item>) -> Unit): ListenerRegistration {
//        return firestoreInstance.collection("users")
//            .orderBy("firstName", Query.Direction.ASCENDING)
//            .whereGreaterThanOrEqualTo("firstName", searchText)
//            //.orderBy("firstName").startAt(searchText).endAt(searchText + "uf8ff")
//            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                if (firebaseFirestoreException != null) {
//                    Log.e("firestore listen error", "error", firebaseFirestoreException)
//                    return@addSnapshotListener
//                }
//
//                val items = mutableListOf<Item>()
//                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
//
//                querySnapshot?.documents?.forEach {
//                    if (it.id != currentUserId && selected.indexOf(it.id) < 0) {
//                        items.add(PersonItem(it.toObject(User::class.java)!!, it.id, context))
//                    }
//                }
//
//                onListen(items)
//            }
//    }
//
//    fun addChatChannelsSearchListener(context: Context, searchText: String, onListen: (List<Item>) -> Unit): ListenerRegistration {
//
//        return currentUserDocRef.collection("engagedChatChannels")
//            .whereIn("msgType", listOf(MessageType.TEXT, MessageType.AUDIO, MessageType.IMAGE))
//            .orderBy("name", Query.Direction.ASCENDING)
//            .whereGreaterThanOrEqualTo("name", searchText)
//            .orderBy("msgTime", Query.Direction.DESCENDING)
//            //.orderBy("name").startAt(searchText).endAt(searchText + "uf8ff")
//            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                if (firebaseFirestoreException != null) {
//                    Log.e("firestore listen error", "error", firebaseFirestoreException)
//                    return@addSnapshotListener
//                }
//                val items = mutableListOf<Item>()
//                querySnapshot?.documents?.forEach {
//                    items.add(ChatChannelItem(it.toObject(UserChatChannel::class.java)!!, it.id, context, ""))
//                    Log.e("search", it.toString())
//                }
//                onListen(items)
//            }
//    }
//
//    fun addChatChannelsListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
//
//        return currentUserDocRef.collection("engagedChatChannels")
//            .whereIn("msgType", listOf(MessageType.TEXT, MessageType.AUDIO, MessageType.IMAGE))
//            .orderBy("msgTime", Query.Direction.DESCENDING)
//            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                if (firebaseFirestoreException != null) {
//                    Log.e("firestore listen error", "error", firebaseFirestoreException)
//                    return@addSnapshotListener
//                }
//
//                val items = mutableListOf<Item>()
//
//                querySnapshot?.documents?.forEach {
//                    items.add(ChatChannelItem(it.toObject(UserChatChannel::class.java)!!, it.id, context, ""))
//                }
//
//                onListen(items)
//            }
//    }
//
//    fun addStoriesListener(context: Context, onListen: (List<Item>, User) -> Unit): ListenerRegistration {
//        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
//        val cal = Calendar.getInstance()
//        cal.add(Calendar.DATE, -1)
//        val time = cal.time
//
//        var curUser = User()
//
//        return firestoreInstance.collection("users")
//            .orderBy("storyUpdated", Query.Direction.DESCENDING)
//            .endAt(time)
//            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                if (firebaseFirestoreException != null) {
//                    Log.e("firestore listen error", "error", firebaseFirestoreException)
//                    return@addSnapshotListener
//                }
//
//                val items = mutableListOf<Item>()
//
//                querySnapshot?.documents?.forEach {
//                    if (it.id == currentUserId) {
//                        curUser = it.toObject(User::class.java)!!
//                    } else {
//                        items.add(StatusItem(it.toObject(User::class.java)!!, it.id, context))
//                    }
//                }
//
//                onListen(items, curUser)
//            }
//    }
//
//    fun getStoriesForUserId(userId: String, onListen: (List<Story>) -> Unit) {
//        val cal = Calendar.getInstance()
//        cal.add(Calendar.DATE, -1)
//        val time = cal.time
//
//        val items = mutableListOf<Story>()
//
//        firestoreInstance.collection("users")
//            .document(userId).collection("stories")
//            .whereGreaterThanOrEqualTo("time", time)
//            .orderBy("time", Query.Direction.ASCENDING)
//            .get()
//            .addOnSuccessListener { querySnapshot ->
//                querySnapshot?.documents?.forEach {
//                    items.add(Story(it.id, it["file"].toString(), null))
//                }
//
//                onListen(items)
//            }
//    }
//
//    fun updateStoryForCurrentUser(image: String, onComplete: (status: Boolean) -> Unit) {
//        val time = Calendar.getInstance().time
//
//        val userFieldMap = mutableMapOf<String, Any>()
//
//        userFieldMap["file"] = image
//        userFieldMap["time"] = time
//
//        currentUserDocRef.collection("stories")
//            .add(userFieldMap)
//            .addOnSuccessListener {
//                currentUserDocRef.update("storyUpdated", time)
//                    .addOnSuccessListener {
//                        onComplete(true)
//                    }
//            }
//            .addOnFailureListener {
//                onComplete(false)
//            }
//    }
//
//    fun addChatChannelMemberListener(channelId: String, context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
//
//        return chatChannelCollectionRef.document(channelId)
//            .collection("userIds")
//            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                if (firebaseFirestoreException != null) {
//                    Log.e("firestore listen error", "error", firebaseFirestoreException)
//                    return@addSnapshotListener
//                }
//
//                val items = mutableListOf<Item>()
//                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
//
//                querySnapshot?.documents?.forEach {
//                    if (it?.id != currentUserId) {
//                        items.add(ChatMemberItem(it.id, it.toObject(ChatUser::class.java)!!,  context))
//                    } else {
//                        items.add(0, ChatMemberItem(it.id, it.toObject(ChatUser::class.java)!!,  context))
//                    }
//                }
//
//                onListen(items)
//            }
//    }
//
//    fun removeListener(registration: ListenerRegistration) = registration.remove()
//
//    fun getOrCreateChatChannel(otherUserId: String, otherUserName: String, onComplete: (newChannel: NewChannel) -> Unit) {
//
//        currentUserDocRef.collection("engagedChatChannels")
//            .document(otherUserId).get().addOnSuccessListener {
//
//                if (it.exists()) { // means we are already chatting with the user
//                    val time = it["joinTime"] as com.google.firebase.Timestamp
//                    onComplete(NewChannel(it["channelId"] as String, time.toDate()))
//                    return@addOnSuccessListener
//                }
//
//                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
//
//                val newChannel = chatChannelCollectionRef.document()
//                newChannel.set(ChatChannel("", "", false))
//
//                val date = Date()
//
//                chatChannelCollectionRef.document(newChannel.id)
//                    .collection("userIds")
//                    .document(currentUserId)
//                    .set(ChatUser(date))
//
//                chatChannelCollectionRef.document(newChannel.id)
//                    .collection("userIds")
//                    .document(otherUserId)
//                    .set(ChatUser(date))
//
//                currentUserDocRef
//                    .collection("engagedChatChannels")
//                    .document(otherUserId)
//                    .set(UserChatChannel(newChannel.id, false, otherUserName, "", Date(0), "", date))
//
//                firestoreInstance.collection("users").document(otherUserId)
//                    .collection("engagedChatChannels")
//                    .document(currentUserId)
//                    .set(UserChatChannel(newChannel.id, false, (if (Variables.name == null) "" else Variables.name!!), "", Date(0), "", date))
//
//                onComplete(NewChannel(newChannel.id, date))
//            }
//    }
//
//    fun createNewGroup(groupName: String, selectedList: ArrayList<SelUser>, profilePicture: String? = null, onComplete: (status: Boolean) -> Unit) {
//
//        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
//
//        val newChannel = chatChannelCollectionRef.document()
//
//        newChannel.set(ChatChannel(groupName, (if (profilePicture == null) "" else profilePicture), true))
//
//        val date = Date()
//
//        val userChatChannel = UserChatChannel(newChannel.id, true, groupName, "", Date(0), MessageType.TEXT, date)
//
//        currentUserDocRef
//            .collection("engagedChatChannels")
//            .document("GROUP_${newChannel.id}")
//            .set(userChatChannel)
//
//        chatChannelCollectionRef.document(newChannel.id)
//            .collection("userIds")
//            .document(currentUserId)
//            .set(ChatUser(date))
//
//        for (user in selectedList) {
//            chatChannelCollectionRef.document(newChannel.id)
//                .collection("userIds")
//                .document(user.ID)
//                .set(ChatUser(date))
//                .addOnSuccessListener {
//                    firestoreInstance.collection("users").document(user.ID)
//                        .collection("engagedChatChannels")
//                        .document("GROUP_${newChannel.id}")
//                        .set(userChatChannel)
//                }
//        }
//
//        onComplete(true)
//    }
//
//    fun updateGroup(channelId: String, groupName: String, profilePicture: String? = null, onComplete: (status: Boolean) -> Unit) {
//
//        chatChannelCollectionRef.document(channelId)
//            .update(
//                mapOf(
//                    "name" to groupName,
//                    "picture" to (if (profilePicture == null) "" else profilePicture)
//                )
//            )
//            .addOnSuccessListener {
//                onComplete(true)
//            }
//    }
//
//    fun removeUserFromGroup (userId: String, channelId: String, onComplete: (status: Boolean) -> Unit) {
//
//        chatChannelCollectionRef.document(channelId)
//            .collection("userIds")
//            .document(userId)
//            .delete()
//            .addOnSuccessListener {
//                chatChannelCollectionRef.document(channelId)
//                    .collection("removeUser")
//                    .document(userId)
//                    .set(ChatUser(Date()))
//                    .addOnSuccessListener {
//                        firestoreInstance.collection("users").document(userId)
//                            .collection("engagedChatChannels")
//                            .document("GROUP_${channelId}")
//                            .delete()
//
//                        onComplete(true)
//                    }
//            }
//    }
//
//    fun addUsersToGroup (selectedList: ArrayList<SelUser>, channelId: String, groupName: String, onComplete: (status: Boolean) -> Unit) {
//
//        val date = Date()
//
//        val userChatChannel = UserChatChannel(channelId, true, groupName, "", Date(0), MessageType.TEXT, date)
//
//        for (user in selectedList) {
//            chatChannelCollectionRef.document(channelId)
//                .collection("userIds")
//                .document(user.ID)
//                .set(ChatUser(date))
//                .addOnSuccessListener {
//                    firestoreInstance.collection("users").document(user.ID)
//                        .collection("engagedChatChannels")
//                        .document("GROUP_${channelId}")
//                        .set(userChatChannel)
//                }
//        }
//
//        onComplete(true)
//    }
//
//    fun updateGroupLastMessage(messageType: String, message: String, time: Date, channelId: String) {
//
//        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
//
//        chatChannelCollectionRef.document(channelId)
//            .collection("userIds")
//            .get()
//            .addOnSuccessListener { document ->
//                document?.forEach {
//                    if (it.id == currentUserId) {
//                        firestoreInstance.collection("users").document(it.id)
//                            .collection("engagedChatChannels")
//                            .document("GROUP_${channelId}")
//                            .update(
//                                mapOf(
//                                    "lastMsg" to "Me - $message",
//                                    "msgTime" to time,
//                                    "msgType" to  messageType
//                                )
//                            )
//                    } else {
//                        firestoreInstance.collection("users").document(it.id)
//                            .collection("engagedChatChannels")
//                            .document("GROUP_${channelId}")
//                            .update(
//                                mapOf(
//                                    "lastMsg" to (if (Variables.name == null) "" else Variables.name + " - ") + message,
//                                    "msgTime" to time,
//                                    "msgType" to messageType
//                                )
//                            )
//                    }
//                }
//            }
//    }
//
//    fun updateSingleLastMessage(messageType: String, message: String, time: Date, otherUserId: String, otherUserName: String) {
//        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
//
//        currentUserDocRef
//            .collection("engagedChatChannels")
//            .document(otherUserId)
//            .update(
//                mapOf(
//                    "lastMsg" to message,
//                    "msgTime" to time,
//                    "isGroup" to false,
//                    "name" to otherUserName,
//                    "msgType" to  messageType
//                )
//            )
//
//        firestoreInstance.collection("users").document(otherUserId)
//            .collection("engagedChatChannels")
//            .document(currentUserId)
//            .update(
//                mapOf(
//                    "lastMsg" to message,
//                    "msgTime" to time,
//                    "isGroup" to false,
//                    "name" to (if (Variables.name == null) "" else Variables.name),
//                    "msgType" to  messageType
//                )
//            )
//    }
//
////    fun getLastMessage(otherUserId: String, onComplete: (msg: String, time: Timestamp) -> Unit) {
////
////        currentUserDocRef.collection("engagedChatChannels")
////            .document(otherUserId).get().addOnSuccessListener {
////                if (it.exists()) {
////                    try {
////                        var message = ""
////                        if (it["msgType"] == MessageType.TEXT) {
////                            message = it["lastMsg"] as String
////                        } else if (it["msgType"] == MessageType.AUDIO) {
////                            message = "\uD83C\uDFA4 Audio"
////                        } else if (it["msgType"] == MessageType.IMAGE) {
////                            message = "\uD83D\uDCF7 Photo"
////                        }
////
////                        onComplete(message, it["msgTime"] as Timestamp)
////
////                    } catch (e: Exception) {
////                        e.printStackTrace()
////                    }
////                }
////            }
////    }
//
//    //: ListenerRegistration
//    fun loadMessages(channelId: String, context: Context, isGroup: Boolean, joinTime: Date?, onListen: (List<Item>) -> Unit) {
//        Log.e("loadMessages", topDate.toString())
//
//        isLoading = true
//
////        if (joinTime !=  null && joinTime > topDate && topDate != null) {
////            topDate = joinTime
////        }
//
//        chatChannelCollectionRef.document(channelId).collection("messages")
//            .whereGreaterThanOrEqualTo("time", joinTime!!)
//            .orderBy("time", Query.Direction.DESCENDING)
//            .limit(Constants.CHAT_LOAD_LIMIT)
//            .startAfter(topDate)
//            .get().addOnSuccessListener { document ->
//                isLoading = false
//
//                if (document != null) {
//                    val items = mutableListOf<Item>()
//                    document.forEach {
////                        if ((it["sendStatus"] == true && it["senderId"] != FirebaseAuth.getInstance().currentUser!!.uid) ||
////                            it["senderId"] == FirebaseAuth.getInstance().currentUser!!.uid) {
//                            if (it["type"] == MessageType.TEXT) {
//                                Log.d("topDate", topDate.toString())
//                                Log.d("item_3change", it.getDate("time").toString())
//                                Log.d("item_3change", it.getString("text"))
//                                items.add(
//                                    TextMessageItem(
//                                        it.toObject(TextMessage::class.java)!!,
//                                        context,
//                                        channelId,
//                                        it.id,
//                                        isGroup
//                                    )
//                                )
//                            } else if (it["type"] == MessageType.AUDIO) {
//                                Log.d("topDate", topDate.toString())
//                                Log.d("item_3change", it.getDate("time").toString())
//                                Log.d("item_3change", it.getString("imagePath"))
//                                items.add(
//                                    AudioMessageItem(
//                                        it.toObject(AudioMessage::class.java)!!,
//                                        context,
//                                        channelId,
//                                        it.id,
//                                        isGroup
//                                    )
//                                )
//                            } else {
//                                Log.d("topDate", topDate.toString())
//                                Log.d("item_3change", it.getDate("time").toString())
//                                Log.d("item_3change", it.getString("imagePath"))
//                                items.add(
//                                    ImageMessageItem(
//                                        it.toObject(ImageMessage::class.java)!!,
//                                        context,
//                                        channelId,
//                                        it.id,
//                                        isGroup
//                                    )
//                                )
//                            }
////                        }
//                    }
//                    onListen(items)
//                } else {
//                    onListen(mutableListOf())
//                }
//            }
//
////        return chatChannelCollectionRef.document(channelId).collection("messages")
////            .orderBy("time", Query.Direction.DESCENDING)
////            .limit(Constants.CHAT_LOAD_LIMIT)
////            .startAfter(topDate)
////            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
////                if (firebaseFirestoreException != null) {
////                    Log.e("exception occurred", "")
////                    return@addSnapshotListener
////                }
////
////                val items = mutableListOf<Item>()
////                querySnapshot!!.documents.forEach {
////                    if (it["type"] == MessageType.TEXT) {
////                        Log.d("item_1loaded", it.getTimestamp("time").toString())
////                        Log.d("item_1loaded", it.getString("text"))
////
////                        items.add(TextMessageItem(
////                                it.toObject(TextMessage::class.java)!!,
////                                context
////                            )
////                        )
////
////                    } else {
////                        items.add(
////                            ImageMessageItem(
////                                it.toObject(ImageMessage::class.java)!!,
////                                context
////                            )
////                        )
////                    }
////                }
////
////                onListen(items)
////
////            }
//
//    }
//
//    fun addChatMessageListener(channelId: String, context: Context, isGroup: Boolean, joinTime: Date?, onListen: (List<Item>) -> Unit): ListenerRegistration {
//        Log.e("addChatMessageListener", bottomDate.toString())
//        Log.e("addChatMessageListener", joinTime.toString())
//
//        isLoading = true
//
//        if (bottomDate != null) {
//            if (joinTime !=  null && joinTime > bottomDate) {
//                bottomDate = joinTime
//            }
//
//            Log.e("addChatMessageListener", joinTime.toString())
//
//            return chatChannelCollectionRef.document(channelId).collection("messages")
//                .orderBy("time", Query.Direction.ASCENDING)
//                .startAfter(bottomDate)
//                //.whereEqualTo("sendStatus", true)
//                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                    isLoading = false
//                    if (firebaseFirestoreException != null) {
//                        Log.e("exception occurred", "")
//                        return@addSnapshotListener
//                    }
//
//                    val items = mutableListOf<Item>()
//                    querySnapshot!!.documents.forEach {
////                        if ((it["sendStatus"] == true && it["senderId"] != FirebaseAuth.getInstance().currentUser!!.uid) ||
////                            it["senderId"] == FirebaseAuth.getInstance().currentUser!!.uid) {
//                        if (it["type"] == MessageType.TEXT) {
//                            Log.e("bottomDate", bottomDate.toString())
//                            Log.d("item_3loaded", it.getDate("time").toString())
//                            Log.d("item_3loaded", it.getString("text"))
//                            items.add(
//                                TextMessageItem(
//                                    it.toObject(TextMessage::class.java)!!,
//                                    context,
//                                    channelId,
//                                    it.id,
//                                    isGroup
//                                )
//                            )
//                        } else if (it["type"] == MessageType.AUDIO) {
//                            Log.e("bottomDate", bottomDate.toString())
//                            Log.d("item_3loaded", it.getDate("time").toString())
//                            Log.d("item_3loaded", it.getString("imagePath"))
//                            items.add(
//                                AudioMessageItem(
//                                    it.toObject(AudioMessage::class.java)!!,
//                                    context,
//                                    channelId,
//                                    it.id,
//                                    isGroup
//                                )
//                            )
//                        } else {
//                            Log.e("bottomDate", bottomDate.toString())
//                            Log.d("item_3loaded", it.getDate("time").toString())
//                            Log.d("item_3loaded", it.getString("imagePath"))
//                            items.add(
//                                ImageMessageItem(
//                                    it.toObject(ImageMessage::class.java)!!,
//                                    context,
//                                    channelId,
//                                    it.id,
//                                    isGroup
//                                )
//                            )
//                        }
////                        }
//                    }
//                    onListen(items)
//                }
//
//        } else {
//            Log.e("addChatMessageListener", "isLoading " + joinTime.toString())
//            isLoading = true
//
//            return chatChannelCollectionRef.document(channelId).collection("messages")
//                .orderBy("time", Query.Direction.DESCENDING)
//                .limit(Constants.CHAT_LOAD_LIMIT)
//                .endAt(joinTime)
//                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                    isLoading = false
//                    if (firebaseFirestoreException != null) {
//                        Log.e("exception occurred", "")
//                        return@addSnapshotListener
//                    }
//
//                    val items = mutableListOf<Item>()
//                    querySnapshot!!.documents.forEach {
////                        if ((it["sendStatus"] == true && it["senderId"] != FirebaseAuth.getInstance().currentUser!!.uid) ||
////                            it["senderId"] == FirebaseAuth.getInstance().currentUser!!.uid) {
//                            if (it["type"] == MessageType.TEXT) {
//                                Log.d("item_loaded", it.getDate("time").toString())
//                                Log.d("item_loaded", it.getString("text"))
//                                items.add(
//                                    TextMessageItem(
//                                        it.toObject(TextMessage::class.java)!!,
//                                        context,
//                                        channelId,
//                                        it.id,
//                                        isGroup
//                                    )
//                                )
//                            } else if (it["type"] == MessageType.AUDIO) {
//                                Log.d("item_loaded", it.getDate("time").toString())
//                                Log.d("item_loaded", it.getString("imagePath"))
//                                items.add(
//                                    AudioMessageItem(
//                                        it.toObject(AudioMessage::class.java)!!,
//                                        context,
//                                        channelId,
//                                        it.id,
//                                        isGroup
//                                    )
//                                )
//                            } else {
//                                Log.d("item_loaded", it.getDate("time").toString())
//                                Log.d("item_loaded", it.getString("imagePath"))
//                                items.add(
//                                    ImageMessageItem(
//                                        it.toObject(ImageMessage::class.java)!!,
//                                        context,
//                                        channelId,
//                                        it.id,
//                                        isGroup
//                                    )
//                                )
//                            }
////                        }
//                    }
//                    onListen(items)
//                }
//        }
//    }
//
//    fun addMessageChangeListener(channelId: String, context: Context, isGroup: Boolean, onListen: (List<Item>) -> Unit): ListenerRegistration {
//
//        isLoading = true
////        chatChannelCollectionRef.document(channelId).collection("messages")
////            .orderBy("time", Query.Direction.ASCENDING)
////            .limit(currentLoadedCount.toLong())
////            .startAfter(topDate)
////            .get().addOnSuccessListener { document ->
////
////                if (document != null) {
////                    val items = mutableListOf<Item>()
////                    document.forEach {
////                        if (it["type"] == MessageType.TEXT) {
////                            Log.d("item_3change", it.getTimestamp("time").toString())
////                            Log.d("item_3change", it.getString("text"))
////                            items.add(
////                                TextMessageItem(
////                                    it.toObject(TextMessage::class.java)!!,
////                                    context
////                                )
////                            )
////                        } else {
////                            items.add(
////                                ImageMessageItem(
////                                    it.toObject(ImageMessage::class.java)!!,
////                                    context
////                                )
////                            )
////                        }
////                    }
////                    onListen(items)
////                } else {
////                    onListen(mutableListOf())
////                }
////            }
//        Log.e("addMessageChangeList", topDate.toString() + " - " + bottomDate.toString())
//
//        return chatChannelCollectionRef.document(channelId).collection("messages")
//            .orderBy("time", Query.Direction.ASCENDING)
//            .startAt(topDate)
//            .endAt(bottomDate)
//            //.whereEqualTo("sendStatus", true)
//            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                isLoading = false
//                if (firebaseFirestoreException != null) {
//                    Log.e("exception occurred", "")
//                    return@addSnapshotListener
//                }
//
//                val items = mutableListOf<Item>()
//                querySnapshot!!.documents.forEach {
////                    if ((it["sendStatus"] == true && it["senderId"] != FirebaseAuth.getInstance().currentUser!!.uid) ||
////                        it["senderId"] == FirebaseAuth.getInstance().currentUser!!.uid) {
//                        if (it["type"] == MessageType.TEXT) {
//                            Log.e(
//                                "topDate-bottomDate",
//                                topDate.toString() + " - " + bottomDate.toString()
//                            )
//                            Log.d("item_3loadedchange", it.getDate("time").toString())
//                            Log.d("item_3loadedchange", it.getString("text"))
//                            items.add(
//                                TextMessageItem(
//                                    it.toObject(TextMessage::class.java)!!,
//                                    context,
//                                    channelId,
//                                    it.id,
//                                    isGroup
//                                )
//                            )
//                        } else if (it["type"] == MessageType.AUDIO) {
//                            Log.e(
//                                "topDate-bottomDate",
//                                topDate.toString() + " - " + bottomDate.toString()
//                            )
//                            Log.d("item_3loadedchange", it.getDate("time").toString())
//                            Log.d("item_3loadedchange", it.getString("imagePath"))
//                            items.add(
//                                AudioMessageItem(
//                                    it.toObject(AudioMessage::class.java)!!,
//                                    context,
//                                    channelId,
//                                    it.id,
//                                    isGroup
//                                )
//                            )
//                        } else {
//                            Log.e(
//                                "topDate-bottomDate",
//                                topDate.toString() + " - " + bottomDate.toString()
//                            )
//                            Log.d("item_3loadedchange", it.getDate("time").toString())
//                            Log.d("item_3loadedchange", it.getString("imagePath"))
//                            items.add(
//                                ImageMessageItem(
//                                    it.toObject(ImageMessage::class.java)!!,
//                                    context,
//                                    channelId,
//                                    it.id,
//                                    isGroup
//                                )
//                            )
//                        }
////                    }
//                }
//                onListen(items)
//            }
//    }
//
////    fun addDocumentReferenceListener() {}
//
//    fun sendMessage(message: Message, channelId: String, onComplete: (String?) -> Unit) {
//
//        Log.d("new_message", message.time.toString() + message.type)
//
//        chatChannelCollectionRef.document(channelId)
//            .collection("messages")
//            .add(message)
//            .addOnSuccessListener {
//                onComplete (it.id)
//            }
//    }
//
//    fun updateSend(refId: String, channelId: String, onComplete: (Boolean?) -> Unit) {
//
//        val time = Calendar.getInstance().time
//
//        chatChannelCollectionRef.document(channelId)
//            .collection("messages")
//            .document(refId)
//            .update(mapOf(
//                "time" to time,
//                "sendStatus" to true,
//                "sendTime" to time))
//            .addOnSuccessListener {
//                onComplete (true)
//            }
//    }
//
//    fun updateRead(refId: String, channelId: String, onComplete: (Boolean?) -> Unit) {
//
//        chatChannelCollectionRef.document(channelId)
//            .collection("messages")
//            .document(refId)
//            .update(mapOf(
//                "readStatus" to true,
//                "receivedTime" to Calendar.getInstance().time))
//            .addOnSuccessListener {
//                onComplete (true)
//            }
//    }
//
////    fun updateMessage(refId: String, channelId: String, imageUrl: String, onComplete: (status: Boolean) -> Unit) {
////        Log.d("update_message", refId)
////        chatChannelCollectionRef.document(channelId)
////            .collection("messages")
////            .document(refId)
////            .update("imagePath", imageUrl)
////            .addOnSuccessListener { onComplete(true) }
////            .addOnFailureListener { onComplete(false) }
////    }
//
//    //FCM
//    fun getFCMRegistrationTokens(onComplete: (tokens: MutableList<String>) -> Unit) {
//        currentUserDocRef.get().addOnSuccessListener {
//            val user = it.toObject(User::class.java)!!
//            onComplete(user.registrationTokens)
//        }
//    }
//
//    fun setFCMRegistrationTokens(registrationTokens: MutableList<String>) {
//        currentUserDocRef.update(mapOf("registrationTokens" to registrationTokens))
//    }
//    //end FCM
}