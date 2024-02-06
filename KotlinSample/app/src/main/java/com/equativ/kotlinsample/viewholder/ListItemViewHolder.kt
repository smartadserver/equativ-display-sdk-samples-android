package com.equativ.kotlinsample.viewholder

import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.equativ.kotlinsample.R
import com.equativ.kotlinsample.databinding.ListItemBinding

class ListItemViewHolder(private val binding: ListItemBinding) : ViewHolder(binding.root) {

    fun setIndex(index: Int) {
        binding.indexTextView.text = index.toString()

        // Update label if index is 0, to use this cell as header
        if (index == 0) {
            binding.indexTextView.visibility = View.GONE

            binding.titleTextView.text = binding.root.context.resources.getString(R.string.activity_banner_in_list_header_title)
            binding.titleTextView.gravity = Gravity.CENTER

            binding.contentTextView.text = binding.root.context.resources.getString(R.string.activity_banner_in_list_header_instructions)
        } else {
            binding.indexTextView.visibility = View.VISIBLE

            binding.titleTextView.text = binding.root.context.resources.getString(R.string.lorem_ipsum_title)
            binding.titleTextView.gravity = Gravity.START

            binding.contentTextView.text = binding.root.context.resources.getString(R.string.lorem_ipsum_content)
        }
    }
}