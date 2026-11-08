package com.rizeabovedataworks.thegoodiebox

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ToolsHubFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tools_hub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.cardHubCreatorLetter)?.setOnClickListener {
            startActivity(Intent(requireContext(), CreatorLetterActivity::class.java))
        }

        view.findViewById<View>(R.id.cardHubTurkey)?.setOnClickListener {
            startActivity(Intent(requireContext(), TurkeyGuideActivity::class.java))
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
