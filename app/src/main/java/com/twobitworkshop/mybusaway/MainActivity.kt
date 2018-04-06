package com.twobitworkshop.mybusaway

import android.app.Activity
import android.os.Bundle
import android.widget.ListView

class MainActivity : Activity()
{
    private lateinit var listView : ListView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.recipe_list_view)

        val recipeList : ArrayList<Recipe> = Recipe.getRecipesFromFile("recipes.json", this)

        val adapter = RecipeAdapter(this, recipeList)
        listView.adapter = adapter
    }
}
