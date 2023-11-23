package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.CodChangeRequestBody
import com.logestechs.driver.api.requests.ModifyProfileRequestBody
import com.logestechs.driver.databinding.DialogChangeProfileBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.interfaces.ChangeProfileDialogListener


class ChangeProfileDialog(
    var context: Context,
    var listener: ChangeProfileDialogListener?,
    var isPhone: Boolean = true
) {

    lateinit var binding: DialogChangeProfileBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogChangeProfileBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_change_profile, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding

        if(!isPhone) {
            binding.titleChange.text = getString(R.string.change_password)
            binding.iconDialog.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_password_filled
            ))
            binding.containerMobileChange.visibility = View.GONE
            binding.containerPasswordChange.visibility = View.VISIBLE
        }

        binding.buttonDone.setOnClickListener {
            if (isPhone) {
                if (binding.etNewMobile.editText.text.toString().isNotEmpty()) {
                    binding.etNewMobile.makeValid()
                    listener?.onProfileChanged(
                        ModifyProfileRequestBody(
                            binding.etNewMobile.editText.text.toString(),
                            null,
                            null
                        ),
                        isPhone
                    )
                    alertDialog.dismiss()
                } else {
                    binding.etNewMobile.makeInvalid()
                }
            } else {
                if (binding.etOldPassword.editText.text.toString().isNotEmpty()
                    && binding.etNewPassword.editText.text.toString().isNotEmpty()
                    && binding.etRepeatPassword.editText.text.toString().isNotEmpty()) {

                    if (binding.etNewPassword.editText.text.toString().length >= 6
                        && binding.etNewPassword.editText.text.toString().matches(Regex(".*[a-zA-Z].*\\d.*|.*\\d.*[a-zA-Z].*"))) {
                        if (binding.etNewPassword.editText.text.toString() == binding.etRepeatPassword.editText.text.toString()) {
                            binding.etNewPassword.makeValid()
                            binding.etRepeatPassword.makeValid()
                            listener?.onProfileChanged(
                                ModifyProfileRequestBody(
                                    null,
                                    binding.etNewPassword.editText.text.toString(),
                                    binding.etRepeatPassword.editText.text.toString()
                                ),
                                isPhone
                            )
                            alertDialog.dismiss()
                        } else {
                            binding.etNewPassword.makeInvalid();
                            binding.etRepeatPassword.makeInvalid();
                            Helper.showErrorMessage(
                                context,
                                getString(R.string.passwords_must_match)
                            )
                        }
                    } else {
                        binding.etNewPassword.makeInvalid();
                        Helper.showErrorMessage(
                            context,
                            getString(R.string.valid_password_rules)
                        )
                    }
                } else {
                    if (binding.etOldPassword.editText.text.toString().isEmpty()) {
                        binding.etOldPassword.makeInvalid();
                    }
                    if (binding.etNewPassword.editText.text.toString().isEmpty()) {
                        binding.etNewPassword.makeInvalid();
                    }
                    if (binding.etRepeatPassword.editText.text.toString().isEmpty()) {
                        binding.etRepeatPassword.makeInvalid();
                    }
                }
            }
            clearFocus()
        }

        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.root.setOnClickListener {
            clearFocus()
        }

        this.alertDialog = alertDialog
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.show()
    }

    private fun getString(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }

    private fun clearFocus() {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.etNewMobile.editText.clearFocus()
        binding.etOldPassword.editText.clearFocus()
        binding.etNewPassword.editText.clearFocus()
        binding.etRepeatPassword.editText.clearFocus()

    }

    fun showWaitDialog() {
        try {
            if ((context is LogesTechsActivity)) {
                (context as LogesTechsActivity).showWaitDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideWaitDialog() {
        try {
            if ((context is LogesTechsActivity)) {
                (context as LogesTechsActivity).hideWaitDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
