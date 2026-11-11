package com.rizeabovedataworks.thegoodiebox

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

/**
 * Page 2 Fragment: Kitchen Tools & Guides Hub.
 *
 * Renders large interactive cards for quick access to:
 * - Recipe Search Input Bar
 * - Creator's Memoir Letter (`CreatorLetterActivity`)
 * - Turkey Master Guide (`TurkeyGuideActivity`)
 * - Ham Master Guide (`HamGuideActivity`)
 * - Mom's Kitchen Tips & Hacks (`TipsActivity`)
 * - Measurement Conversions Calculator (`ConversionsActivity`)
 * - How to Use App Guide (`HowToUseActivity`)
 * - Add Custom Family Recipe (`AddRecipeActivity`)
 */
class ToolsHubFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tools_hub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val etSearchRecipes = view.findViewById<EditText>(R.id.etSearchRecipes)
        etSearchRecipes?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        view.findViewById<View>(R.id.cardHubCreatorLetter)?.setOnClickListener {
            startActivity(Intent(requireContext(), CreatorLetterActivity::class.java))
        }

        view.findViewById<View>(R.id.cardHubTurkey)?.setOnClickListener {
            startActivity(Intent(requireContext(), TurkeyGuideActivity::class.java))
        }

        view.findViewById<View>(R.id.cardHubHam)?.setOnClickListener {
            startActivity(Intent(requireContext(), HamGuideActivity::class.java))
        }

        view.findViewById<View>(R.id.cardHubTips)?.setOnClickListener {
            startActivity(Intent(requireContext(), TipsActivity::class.java))
        }

        view.findViewById<View>(R.id.cardHubConversions)?.setOnClickListener {
            startActivity(Intent(requireContext(), ConversionsActivity::class.java))
        }

        view.findViewById<View>(R.id.cardHubHowToUse)?.setOnClickListener {
            startActivity(Intent(requireContext(), HowToUseActivity::class.java))
        }

        view.findViewById<View>(R.id.cardHubAddRecipe)?.setOnClickListener {
            startActivity(Intent(requireContext(), AddRecipeActivity::class.java))
        }
    }
}
