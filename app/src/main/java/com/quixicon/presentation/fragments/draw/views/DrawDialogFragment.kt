package com.quixicon.presentation.fragments.draw.views

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quixicon.BuildConfig
import com.quixicon.R
import com.quixicon.core.support.extensions.changeAppearance
import com.quixicon.databinding.FragmentDrawDialogBinding
import com.quixicon.presentation.fragments.draw.models.DrawModel
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class DrawDialogFragment : BottomSheetDialogFragment(), View.OnClickListener, DrawCanvasView.OnTouchUp {

    private lateinit var binding: FragmentDrawDialogBinding

    private lateinit var drawCanvasView: DrawCanvasView

    var created = false

    var restore: Boolean = false
    lateinit var model: DrawModel

    private val fileName = BuildConfig.DRAW_FILE_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        Timber.e("Draw Dialog Created: restore $restore")
        created = true
    }

    override fun onDestroy() {
        super.onDestroy()
        created = false
    }

    override fun onDismiss(dialog: DialogInterface) {
        Timber.e("Draw Fragment Dialog is dismissed: $restore")
        (activity as? Listener)?.onDrawDialogDismissed(restore)
        super.onDismiss(dialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomDialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return bottomDialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putBoolean(IS_RESTORE, restore)
            putSerializable(MODEL, model)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_draw_dialog, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponents()

        if (dialog is BottomSheetDialog) {
            val behaviour = (dialog as BottomSheetDialog).behavior
            behaviour.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_delete) {
            drawCanvasView.clearCanvas()
            restore = false
        }
        if (view.id == R.id.btn_close) {
            dismiss()
        }
    }

    override fun onTouchUp() {
        restore = true
        saveCanvasToFile()
    }

    private fun initComponents() {
        binding.onClickListener = this
        // Считать цвет, соответствующий текущей теме:
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.colorSurface, typedValue, true)
        val backgroundColor = typedValue.data

        requireContext().theme.resolveAttribute(R.attr.colorOnSurface, typedValue, true)
        val drawColor = typedValue.data

        if (restore) {
            val file = File(requireContext().getExternalFilesDir(null)?.absolutePath, fileName)
            drawCanvasView = if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                DrawCanvasView(requireContext(), backgroundColor, drawColor, initBitmap = bitmap)
            } else {
                DrawCanvasView(requireContext(), backgroundColor, drawColor)
            }
        } else {
            drawCanvasView = DrawCanvasView(requireContext(), backgroundColor, drawColor)
        }

        val params = ViewGroup.LayoutParams(
            GridLayout.LayoutParams.MATCH_PARENT,
            resources.displayMetrics.widthPixels,
        )

        binding.canvasContainer.addView(drawCanvasView, params)
        binding.model = model
        // Set attributes in layout_draw_hint.xml
        binding.layoutOriginal.findViewById<AppCompatTextView>(R.id.tv_hint_original)?.apply {
            text = model.hintOriginal
            changeAppearance(model.hintOriginal.isNotEmpty())
        }
        //
        drawCanvasView.setOnTouchUpListener(this)
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            restore = it.getBoolean(IS_RESTORE, false)
            model = it.getSerializable(MODEL) as DrawModel
        } ?: run {
            arguments?.also {
                restore = it.getBoolean(IS_RESTORE, false)
                model = it.getSerializable(MODEL) as DrawModel
            }
        }
    }

    private fun saveCanvasToFile() {
        val file = File(requireContext().getExternalFilesDir(null)?.absolutePath, fileName)
        try {
            Timber.e("Save file")
            val output = FileOutputStream(file)
            drawCanvasView.extraBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
            output.flush()
            output.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val IS_RESTORE = "IS_RESTORE"
        const val MODEL = "MODEL"

        @JvmStatic
        fun createInstance(model: DrawModel, isImageRestore: Boolean = false) = DrawDialogFragment().apply {
            arguments = Bundle().apply {
                putBoolean(IS_RESTORE, isImageRestore)
                putSerializable(MODEL, model)
            }
        }
    }

    interface Listener {
        fun onDrawDialogDismissed(value: Boolean)
    }
}
