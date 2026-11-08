package com.rizeabovedataworks.thegoodiebox

import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CreatorLetterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator_letter)

        val toolbar = findViewById<Toolbar>(R.id.toolbarCreatorLetter)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "A Note from the Creator"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val scrollView = findViewById<ScrollView>(R.id.scrollViewCreatorLetter)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, 0, v.paddingRight, systemBars.bottom + 48)
            insets
        }

        val letterText = """
My name is Greg, and I wrote this app to preserve my mom's recipes. But it has turned into something so much bigger than I ever imagined. As I've worked on it, it has brought back so many fond memories from throughout my life.

At the time I'm writing this, I'm 63 years old. My mom is 91 and has dementia. I am her caregiver. It's a hard life, watching your parent go from making all those wonderful meals to being confined to a wheelchair and eating soup with her fingers because she no longer remembers how to use a spoon.

As Christmas fast approaches, I'm reminded of all the little moments I had growing up with two amazing parents who loved me and taught me how to be a better person. Christmas was always a special time for us.

For years, I grew up with an aluminum Christmas tree and a spinning color wheel that shined on the tree and spread brilliant colors around the room like a disco ball. Mom always spent two or three days in the kitchen right after Thanksgiving making treats. Dad had quite a sweet tooth, and Mom really knew how to satisfy it.

Over and over for those two or three days, it was make a treat, clean up, make a treat, clean up, again and again. She had her little 3x5 index cards listing everything she wanted to make that year, and she prepared for it like she was going to war! Grocery lists hung from the refrigerator, and then we'd help her haul everything in from the car. She seemed to have it down to a science because I don't remember her ever having to go back for much.

She gave a lot away, too. After making all those treats, friends and family would receive their share, and we'd often get wonderful goodies back in return.

My aunts and uncles, Hazel, Vernon, and Uncle Steven, often stopped by during my younger years. Uncle Steven was a tall man, but always gentle, at least as I remember him. He smoked a pipe and always seemed to smell of Cavendish tobacco. Aunt Hazel was very much like my mom, a great cook working her magic in a small kitchen. She had a kind spirit and a beautiful smile that always seemed to make everything all right. We spent many Thanksgivings there, playing G.I. Joe with my cousin Cary on the porch and teasing his sister, my beautiful cousin Cindy.

Uncle Vernon was a rotund man who intimidated me a little as a child, but he had a brilliant sense of humor and often did little things to make us laugh, when he wasn't sleeping off a food coma in his chair.

There was little television, no phones, no iPads. Just family, food, company, and an amazing spirit. People say life was simpler back then, and for me, it really was. As a child, I never had to worry about mortgages, utility bills, or any of that. It was a time to play and let my imagination take over. One minute I was G.I. Joe fighting a war, and the next I was racing Hot Wheels down a track and crashing them into five other cars at the bottom.

Even if I was sitting at the kids' table, the smells from those feasts made my stomach growl like a panther. The turkey, ham, mashed potatoes, scalloped corn, pumpkin pies, and stuffing always filled me with excitement. I loved seeing that plate come my way, piled high with food and usually sporting a little drip of gravy running off the edge. The anticipation of taking that first bite was almost as good as the meal itself.

Those are the times I remember, the people who are still here and those who are gone now. These are the memories and the spirit of the season that I carry forward.

As much as I would love to be lying on the floor on my stomach, feet kicking in the air while flipping through the JCPenney, Sears, and other toy catalogs, circling what I wanted with a crayon, I also love having watched my own children experience that same joy. Somewhere along the way, I think I passed a little bit of that spirit on to them, too.

Now here I sit, with a few tears in my eyes and a heart full of amazing memories, writing a simple little app to preserve a small piece of my life, my mom's life, and my dad's life, and sharing it with all of you.

I hope this recipe app brings you as much joy and love as is found in every recipe within it. I hope Christmas finds its way into every day of the year. I hope the blessing of the food in this app helps bring you a little closer to the people you love. And may your kitchen always be filled with warmth, laughter, and good food.

Thank you for taking a moment to read this far. Now go fire up that stove and make a memory of your own.
""".trimIndent()

        findViewById<TextView>(R.id.tvCreatorLetterBody).text = letterText
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
