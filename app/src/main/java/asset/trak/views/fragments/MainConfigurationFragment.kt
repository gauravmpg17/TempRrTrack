package asset.trak.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asset.trak.views.baseclasses.BaseFragment
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.settings.SettingListFragment
import kotlinx.android.synthetic.main.fragment_main_confriguration.*


class MainConfigurationFragment : BaseFragment(R.layout.fragment_main_confriguration) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        rfdConfigure.setOnClickListener {
            replaceFragment(
                requireActivity().supportFragmentManager, SettingListFragment(),
                R.id.content_frame
            )
        }

        appConfigCard.setOnClickListener {
            replaceFragment(
                requireActivity().supportFragmentManager, AppConfigurationFragment(),
                R.id.content_frame
            )
        }
    }
}