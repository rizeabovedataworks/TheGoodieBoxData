package com.rizeabovedataworks.thegoodiebox

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val onClick: (Recipe) -> Unit,
    private val onFavoriteClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvRecipeTitle)
        val description: TextView = view.findViewById(R.id.tvRecipeDesc)
        val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)

        init {
            title.typeface = Typeface.create("cursive", Typeface.BOLD)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.title.text = recipe.title
        holder.description.text = recipe.description

        if (recipe.isFavorite) {
            holder.ivFavorite.setImageResource(R.drawable.ic_star_filled)
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_star_outline)
        }

        holder.ivFavorite.setOnClickListener {
            onFavoriteClick(recipe)
        }

        holder.itemView.setOnClickListener { onClick(recipe) }
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}
