package com.robbyyehezkiel.robustaroasting.ui.menu.quiz.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.robbyyehezkiel.robustaroasting.R
import com.robbyyehezkiel.robustaroasting.data.model.Quiz
import com.robbyyehezkiel.robustaroasting.ui.menu.quiz.question.QuestionActivity

class QuizAdapter(private val context: Context, private var quizzes: List<Quiz>) :
    RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_list_quiz, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(quizzes[position])
    }


    override fun getItemCount(): Int = quizzes.size

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView = itemView.findViewById(R.id.ed_quiz)

        fun bind(quiz: Quiz) {
            textViewTitle.text = quiz.title
            itemView.setOnClickListener {
                navigateToQuestionActivity(quiz.title)
            }
        }

        private fun navigateToQuestionActivity(quizTitle: String) {
            val intent = Intent(context, QuestionActivity::class.java)
            intent.putExtra("Title", quizTitle)
            context.startActivity(intent)
        }
    }// Function to update the data in the adapter

    fun updateData(newQuizzes: List<Quiz>) {
        // Calculate the differences between the old and new data.
        val diffResult = DiffUtil.calculateDiff(QuizDiffCallback(quizzes, newQuizzes))

        // Update the data and dispatch the specific change events.
        quizzes = newQuizzes
        diffResult.dispatchUpdatesTo(this)
    }
}
