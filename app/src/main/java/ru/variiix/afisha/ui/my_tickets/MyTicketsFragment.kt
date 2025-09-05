package ru.variiix.afisha.ui.my_tickets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.variiix.afisha.databinding.FragmentMyTicketsBinding

class MyTicketsFragment : Fragment() {

    private var _binding: FragmentMyTicketsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val myTicketsViewModel =
            ViewModelProvider(this).get(MyTicketsViewModel::class.java)

        _binding = FragmentMyTicketsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textMyTickets
        myTicketsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}