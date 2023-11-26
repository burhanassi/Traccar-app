package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.ModifyProfileRequestBody

interface ChangeProfileDialogListener {
    fun onProfileChanged(profileChangeRequestBody: ModifyProfileRequestBody?, isPhone: Boolean)
}