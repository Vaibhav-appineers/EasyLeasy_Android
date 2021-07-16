package com.app.easyleasy.view.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.easyleasy.databinding.CvOnBoardingItemBinding
import com.app.easyleasy.dataclasses.OnBoarding

class OnBoardingAdapter(private val onBoardingItems : List<OnBoarding>)
    : RecyclerView.Adapter<OnBoardingAdapter.OnBoardingViewHolder>(){

    inner class OnBoardingViewHolder(val binding : CvOnBoardingItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(onBoarding: OnBoarding){
            with(binding){
                ivBoarding.setImageResource(onBoarding.image)
                tvBoardingTitle.text = onBoarding.title
                tvBoardingSubTitle.text = onBoarding.description
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CvOnBoardingItemBinding.inflate(inflater, parent, false)
        return OnBoardingViewHolder(binding)
    }

    override fun getItemCount() = onBoardingItems.size

    override fun onBindViewHolder(holder: OnBoardingViewHolder, position: Int)
            = holder.bind(onBoardingItems[position])
}