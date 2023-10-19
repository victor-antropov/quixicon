package com.quixicon.presentation.fragments.test.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.quixicon.R
import com.quixicon.core.adapters.SpinnerAdapter
import com.quixicon.core.support.extensions.FragmentExtensions.setTitle
import com.quixicon.databinding.FragmentTestSettingsBinding
import com.quixicon.domain.entities.enums.TestDirection
import com.quixicon.presentation.activities.test.models.TestCollectionModel
import com.quixicon.presentation.fragments.test.listeners.TestSettingsListener
import com.quixicon.presentation.fragments.test.models.TestDirectionModel
import com.quixicon.presentation.fragments.test.models.TestSettingsModel
import timber.log.Timber

class TestSettingsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentTestSettingsBinding

    private lateinit var model: TestSettingsModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.e("Create Settings Fragment")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_settings, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            model = it.getSerializable(MODEL) as TestSettingsModel
            initComponents()
            Timber.e("Settings Fragment created: $model")
        }
    }

    override fun onDestroy() {
        Timber.e("Destroy Settings Fragment")
        super.onDestroy()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val item = parent.getItemAtPosition(position)

        // Collection Selected (TestCollectionModel)
        if (parent.id == R.id.spinner_collections) {
            Timber.e("Collection Selected")
            resetPartSize()
        }

        // Size selected (String)
        else if (parent.id == R.id.spinner_part_size) {
            val part = (item as? Int) ?: 50
            selectPart(part)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun initComponents() {
        setTitle(getString(R.string.test_setup))

        binding.settingsModel = model

        SpinnerAdapter(requireContext(), R.layout.simple_spinner_item, model.collections).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCollections.adapter = this
        }

        val orders = listOf(
            TestDirectionModel(getString(R.string.test_order_translation), TestDirection.DIRECT),
            TestDirectionModel(getString(R.string.test_order_original), TestDirection.INVERTED),
            TestDirectionModel(getString(R.string.test_order_mixed), TestDirection.MIXED)
        )

        SpinnerAdapter(requireContext(), R.layout.simple_spinner_item, orders).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerOrder.adapter = this
        }

        val parts = resources.getIntArray(R.array.test_parts).toList()

        SpinnerAdapter(requireContext(), R.layout.simple_spinner_item, parts).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerPartSize.adapter = this
        }

        try {
            val index = orders.indexOfFirst { it.value == model.testDirection }
            binding.spinnerOrder.setSelection(index)
        } catch (e: Exception) {}

        Timber.e("init collection ${model.selectedCollection?.name}")
        try {
            val index = model.collections.indexOfFirst { it.id == model.selectedCollection?.id }
            binding.spinnerCollections.setSelection(index)
        } catch (e: Exception) {}

        binding.btnStart.setOnClickListener {
            startTest()
        }

        binding.spinnerCollections.onItemSelectedListener = this
        binding.spinnerPartSize.onItemSelectedListener = this
    }

    private fun resetPartSize() {
        Timber.e("reset part size ${model.size}")
        try {
            val size = binding.spinnerPartSize.adapter.count
            var selected = false
            for (i in 0 until size) {
                val itemPart = binding.spinnerPartSize.getItemAtPosition(i)
                if ((itemPart as Int) == model.size) {
                    Timber.e("Equal to ${model.size}")
                    selected = true
                    binding.spinnerPartSize.setSelection(i)
                }
            }
            if (!selected) {
                val defaultPosition = 1
                val itemPart = binding.spinnerPartSize.getItemAtPosition(defaultPosition) as? Int
                Timber.e("Part Size By default: $itemPart")
                binding.spinnerPartSize.setSelection(1)
                itemPart?.let {
                    selectPart(it)
                }
            }
        } catch (e: Exception) {
            binding.spinnerPartSize.setSelection(1)
            selectPart(50)
        }
    }

    private fun startTest() {
        val model = binding.spinnerCollections.selectedItem as TestCollectionModel
        val partSize = (binding.spinnerPartSize.selectedItem as? String)?.toIntOrNull()
        val partNumber = (binding.spinnerPartNumber.selectedItem as? Int) ?: 1
        val direction = (binding.spinnerOrder.selectedItem as TestDirectionModel).value
        Timber.e("Collection: ${model.name}, size $partSize,  volume $partNumber, direction $direction")
        binding.settingsModel?.run {
            selectedCollection = model
            size = partSize
            volume = partNumber
            testDirection = direction
            (activity as? TestSettingsListener)?.onTestSettingsReady(this)
        }
    }

    private fun selectPart(part: Int) {
        Timber.e("Part Size Selected: $part")
        (binding.spinnerCollections.selectedItem as? TestCollectionModel)?.run {
            val maxPartNumber = kotlin.math.ceil(size.toDouble() / part).toInt()
            ArrayAdapter(
                requireContext(),
                R.layout.simple_spinner_item,
                IntArray(maxPartNumber) { it + 1 }.toList()
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerPartNumber.adapter = this
                Timber.e("init part number: ${model.volume} of $maxPartNumber")
                model.volume?.let {
                    binding.spinnerPartNumber.setSelection(if (it in 1..maxPartNumber) it - 1 else 0)
                } ?: run { binding.spinnerPartNumber.setSelection(0) }
            }
        }
    }

    companion object {
        const val MODEL = "MODEL"

        @JvmStatic
        fun createInstance(model: TestSettingsModel) = TestSettingsFragment().apply {
            arguments = bundleOf(MODEL to model)
        }
    }
}
