package com.logestechs.driver.data.model

data class GroupedMassCodReports(
    var customerName: String?,
    var pkgs: ArrayList<MassCodReport?>?,
    @Transient
    var isExpanded: Boolean = false
)
