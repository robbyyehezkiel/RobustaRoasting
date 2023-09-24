package com.robbyyehezkiel.robustaroasting.ui.menu.quiz.question

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.robbyyehezkiel.robustaroasting.R
import com.robbyyehezkiel.robustaroasting.data.model.Question
import com.robbyyehezkiel.robustaroasting.data.model.Quiz
import com.robbyyehezkiel.robustaroasting.databinding.ActivityQuestionBinding
import com.robbyyehezkiel.robustaroasting.ui.menu.quiz.result.ResultActivity
import com.robbyyehezkiel.robustaroasting.utils.snackBarShort
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private var quizzes: MutableList<Quiz> = mutableListOf()
    private var questions: MutableMap<String, Question> = mutableMapOf()
    private var index = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.toolbar_faq)

        setUpEventListener()
        setUpFireStore()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setUpEventListener() {
        binding.btnPrevious.setOnClickListener {
            index--
            bindViews()
        }

        binding.btnNext.setOnClickListener {
            index++
            bindViews()
        }

        binding.btnSubmit.setOnClickListener {
            val intent = Intent(this, ResultActivity::class.java)
            val json = Gson().toJson(quizzes[0])
            intent.putExtra("QUIZ", json)
            startActivity(intent)
        }
    }

    private fun setUpFireStore() {
        val quizTitle = intent.getStringExtra("Title")
        if (quizTitle != null) {
            val fireStore = FirebaseFirestore.getInstance()

            lifecycleScope.launch {
                try {
                    val result = fireStore.collection("questions")
                        .whereEqualTo("title", quizTitle)
                        .get()
                        .await()

                    if (!result.isEmpty) {
                        quizzes = result.toObjects(Quiz::class.java)
                        questions = quizzes[0].questions
                        bindViews()
                    }
                } catch (e: Exception) {
                    snackBarShort(binding.root, getString(R.string.fetching_failed_message, e))
                }
            }
        }
    }

    private fun bindViews() {
        binding.btnPrevious.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        binding.btnNext.visibility = View.GONE

        when (index) {
            1 -> { // First question
                binding.btnNext.visibility = View.VISIBLE
            }

            questions.size -> { // Last question
                binding.btnSubmit.visibility = View.VISIBLE
                binding.btnPrevious.visibility = View.VISIBLE
            }

            else -> { // Middle
                binding.btnPrevious.visibility = View.VISIBLE
                binding.btnNext.visibility = View.VISIBLE
            }
        }

        val question = questions["question$index"]
        question?.let {
            binding.questionDescription.text = it.description
            val optionAdapter = OptionAdapter(this, it)
            binding.answerRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.answerRecyclerView.adapter = optionAdapter
            binding.answerRecyclerView.setHasFixedSize(true)
        }
    }
}
