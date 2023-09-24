package com.robbyyehezkiel.robustaroasting.ui.menu.quiz.home

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.robbyyehezkiel.robustaroasting.R
import com.robbyyehezkiel.robustaroasting.data.model.Quiz
import com.robbyyehezkiel.robustaroasting.utils.snackBarShort

class QuizViewModel : ViewModel() {

    private val quizList = MutableLiveData<MutableList<Quiz>>()
    private lateinit var setFireStoreQuestions: FirebaseFirestore

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setFireStoreQuiz(context: Context, view: View) {
        _isLoading.value = true
        setFireStoreQuestions = FirebaseFirestore.getInstance()
        val collectionReference = setFireStoreQuestions.collection("questions")
        collectionReference.addSnapshotListener { value, error ->
            if (value == null || error != null) {
                snackBarShort(view, context.getString(R.string.fetching_failed_message))
                _isLoading.value = false // Set isLoading to false on error
                return@addSnapshotListener
            }
            quizList.value?.clear()
            quizList.postValue(value.toObjects(Quiz::class.java))
            _isLoading.value = false // Set isLoading to false after data fetch
        }
    }

    fun getFireStoreQuiz(): LiveData<MutableList<Quiz>> {
        return quizList
    }
}
