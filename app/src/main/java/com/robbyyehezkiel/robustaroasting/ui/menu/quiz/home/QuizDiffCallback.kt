package com.robbyyehezkiel.robustaroasting.ui.menu.quiz.home

import androidx.recyclerview.widget.DiffUtil
import com.robbyyehezkiel.robustaroasting.data.model.Quiz

class QuizDiffCallback(private val oldList: List<Quiz>, private val newList: List<Quiz>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id // Replace 'id' with your actual identifier.
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}