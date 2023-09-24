package com.robbyyehezkiel.robustaroasting.ui.menu.quiz.result

import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.robbyyehezkiel.robustaroasting.R
import com.robbyyehezkiel.robustaroasting.data.model.Quiz
import com.robbyyehezkiel.robustaroasting.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    lateinit var quiz: Quiz
    private lateinit var binding: ActivityResultBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.toolbar_quiz_result)
        }
        setUpViews()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setUpViews() {
        val quizData = intent.getStringExtra("QUIZ")
        quiz = Gson().fromJson<Quiz>(quizData, Quiz::class.java)
        calculateScore()
        setAnswerView()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setAnswerView() {
        val builder = StringBuilder("")

        for (entry in quiz.questions.entries) {
            val question = entry.value
            builder.append("<font color'#18206F'><b>Question: ${question.description}</b></font><br/><br/>")
            builder.append("<font color='#009688'>Answer: ${question.userAnswer}</font><br/><br/>")
        }
        binding.txtAnswer.text = Html.fromHtml(builder.toString(), Html.FROM_HTML_MODE_COMPACT)
    }

    private fun calculateScore() {
        var score = 0
        for (entry in quiz.questions.entries) {
            val question = entry.value
            if (question.answer == question.userAnswer) {
                score += 10
            }
        }
        binding.txtScore.text = "Your Score : $score"
    }
}