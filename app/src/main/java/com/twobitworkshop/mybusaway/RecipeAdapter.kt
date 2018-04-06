package com.twobitworkshop.mybusaway

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import android.graphics.Typeface
import android.support.v4.content.ContextCompat





/**
 * Created by jtlabak on 4/6/18.
 */
class RecipeAdapter : BaseAdapter
{
    private var context : Context
    private var inflater : LayoutInflater
    private var dataSource : ArrayList<Recipe>

    private val LABEL_COLORS = object : HashMap<String, Int>() {
        init {
            put("Low-Carb", R.color.colorLowCarb)
            put("Low-Fat", R.color.colorLowFat)
            put("Low-Sodium", R.color.colorLowSodium)
            put("Medium-Carb", R.color.colorMediumCarb)
            put("Vegetarian", R.color.colorVegetarian)
            put("Balanced", R.color.colorBalanced)
        }
    }

    constructor(ctx: Context, items: ArrayList<Recipe>) : super()
    {
        context = ctx
        dataSource = items
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
    {
        val rowView : View = inflater.inflate(R.layout.list_item_recipe, parent, false)

        val titleTextView : TextView = rowView.findViewById(R.id.recipe_list_title)
        val subtitleTextView : TextView = rowView.findViewById(R.id.recipe_list_subtitle)
        val detailTextView : TextView = rowView.findViewById(R.id.recipe_list_detail)
        val thumbnailImageView : ImageView = rowView.findViewById(R.id.recipe_list_thumbnail)

        val recipe : Recipe = getItem(position) as Recipe

        titleTextView.text = recipe.title
        subtitleTextView.text = recipe.description
        detailTextView.text = recipe.label
        Picasso.with(context).load(recipe.imageUrl).placeholder(R.mipmap.ic_launcher).into(thumbnailImageView)

        val titleTypeFace = Typeface.createFromAsset(context.assets, "fonts/JosefinSans-Bold.ttf")
        titleTextView.typeface = titleTypeFace

        val subtitleTypeFace = Typeface.createFromAsset(context.assets, "fonts/JosefinSans-SemiBoldItalic.ttf")
        subtitleTextView.typeface = subtitleTypeFace

        val detailTypeFace = Typeface.createFromAsset(context.assets, "fonts/Quicksand-Bold.otf")
        detailTextView.typeface = detailTypeFace

        val color = LABEL_COLORS[recipe.label]
        if (color != null) {
            detailTextView.setTextColor(ContextCompat.getColor(context, color))
        } else {
            detailTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        }

        return rowView
    }

    override fun getItem(position: Int): Any
    {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long
    {
        return position.toLong()
    }

    override fun getCount(): Int
    {
        return dataSource.size
    }
}