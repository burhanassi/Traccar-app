package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.CodChangeRequestBody

interface ChangeCodDialogListener {
    fun onCodChanged(codChangeRequestBody: CodChangeRequestBody?)
}