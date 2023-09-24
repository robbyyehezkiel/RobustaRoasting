package com.robbyyehezkiel.robustaroasting.ui.menu.quiz.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.robbyyehezkiel.robustaroasting.R
import com.robbyyehezkiel.robustaroasting.databinding.ActivityQuizBinding
import com.robbyyehezkiel.robustaroasting.utils.snackBarShort

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var adapter: QuizAdapter
    private lateinit var quizViewModel: QuizViewModel
    private lateinit var rvQuiz: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        initViewModel()
        setupRecyclerView()
        observeQuizList()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.tools_quiz)
        }
    }

    private fun initViewModel() {
        quizViewModel = ViewModelProvider(this)[QuizViewModel::class.java]
    }

    private fun setupRecyclerView() {
        rvQuiz = binding.quizRecyclerView
        rvQuiz.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        rvQuiz.setHasFixedSize(true)
    }

    private fun observeQuizList() {
        quizViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        quizViewModel.getFireStoreQuiz().observe(this) { quizzes ->
            if (quizzes.isNullOrEmpty()) {
                snackBarShort(binding.root, getString(R.string.tools_null_data)) // Show a toast message if the quiz list is empty
            }

            if (!::adapter.isInitialized) {
                adapter = QuizAdapter(this, quizzes)
                rvQuiz.adapter = adapter
            } else {
                adapter.updateData(quizzes)
            }
        }
        quizViewModel.setFireStoreQuiz(this, binding.root)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun showLoading(state: Boolean) {
        binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE
    }
}
